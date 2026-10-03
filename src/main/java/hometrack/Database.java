package hometrack;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Database {
    private static final int PASSWORD_ITERATIONS = 120_000;
    private final String jdbcUrl;

    public Database(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    private Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public void initialize() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, password_hash TEXT NOT NULL)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS assets (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, name TEXT NOT NULL, category TEXT NOT NULL, purchase_date TEXT, purchase_price REAL NOT NULL DEFAULT 0, location TEXT, reminder_date TEXT, reminder_note TEXT)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS warranties (id INTEGER PRIMARY KEY AUTOINCREMENT, asset_id INTEGER NOT NULL UNIQUE REFERENCES assets(id) ON DELETE CASCADE, provider TEXT, end_date TEXT)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS insurance (id INTEGER PRIMARY KEY AUTOINCREMENT, asset_id INTEGER NOT NULL UNIQUE REFERENCES assets(id) ON DELETE CASCADE, provider TEXT, end_date TEXT, premium REAL NOT NULL DEFAULT 0)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS emi (id INTEGER PRIMARY KEY AUTOINCREMENT, asset_id INTEGER NOT NULL UNIQUE REFERENCES assets(id) ON DELETE CASCADE, lender TEXT, monthly_amount REAL NOT NULL DEFAULT 0, next_due_date TEXT)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS service_history (id INTEGER PRIMARY KEY AUTOINCREMENT, asset_id INTEGER NOT NULL REFERENCES assets(id) ON DELETE CASCADE, service_date TEXT NOT NULL, service_type TEXT NOT NULL, cost REAL NOT NULL DEFAULT 0, notes TEXT)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_assets_user_name ON assets(user_id, name)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_service_asset_date ON service_history(asset_id, service_date)");
        }
    }

    public int register(String username, char[] password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = hashPassword(password, salt);
        String encoded = Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO users(username, password_hash) VALUES(?, ?)")) {
            statement.setString(1, username.trim());
            statement.setString(2, encoded);
            statement.executeUpdate();
        }
        return authenticate(username, password);
    }

    public int authenticate(String username, char[] password) throws Exception {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(
                "SELECT id, password_hash FROM users WHERE username = ?")) {
            statement.setString(1, username.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return -1;
                String[] parts = result.getString("password_hash").split(":", 2);
                byte[] salt = Base64.getDecoder().decode(parts[0]);
                byte[] expected = Base64.getDecoder().decode(parts[1]);
                byte[] actual = hashPassword(password, salt);
                if (!java.security.MessageDigest.isEqual(expected, actual)) return -1;
                return result.getInt("id");
            }
        }
    }

    private byte[] hashPassword(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PASSWORD_ITERATIONS, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    public void saveAsset(Asset asset) throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                if (asset.getId() == 0) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO assets(user_id,name,category,purchase_date,purchase_price,location,reminder_date,reminder_note) VALUES(?,?,?,?,?,?,?,?)")) {
                        bindAsset(statement, asset, false);
                        statement.executeUpdate();
                    }
                    try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SELECT last_insert_rowid()")) {
                        if (result.next()) asset.setId(result.getInt(1));
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE assets SET name=?,category=?,purchase_date=?,purchase_price=?,location=?,reminder_date=?,reminder_note=? WHERE id=? AND user_id=?")) {
                        bindAsset(statement, asset, true);
                        if (statement.executeUpdate() == 0) throw new SQLException("Asset not found or access denied.");
                    }
                }
                saveTrackingRows(connection, asset);
                connection.commit();
                asset.refreshTrackedDates();
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) throw sqlException;
                throw new SQLException("Could not save asset.", exception);
            }
        }
    }

    private void bindAsset(PreparedStatement statement, Asset asset, boolean update) throws SQLException {
        int index = 1;
        if (!update) statement.setInt(index++, asset.getUserId());
        statement.setString(index++, asset.getName().trim());
        statement.setString(index++, asset.getCategory());
        statement.setString(index++, dateString(asset.getPurchaseDate()));
        statement.setDouble(index++, asset.getPurchasePrice());
        statement.setString(index++, blankToNull(asset.getLocation()));
        statement.setString(index++, dateString(asset.getReminderDate()));
        statement.setString(index++, blankToNull(asset.getReminderNote()));
        if (update) {
            statement.setInt(index++, asset.getId());
            statement.setInt(index, asset.getUserId());
        }
    }

    private void saveTrackingRows(Connection connection, Asset asset) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO warranties(asset_id,provider,end_date) VALUES(?,?,?) ON CONFLICT(asset_id) DO UPDATE SET provider=excluded.provider,end_date=excluded.end_date")) {
            statement.setInt(1, asset.getId());
            statement.setString(2, blankToNull(asset.getWarrantyProvider()));
            statement.setString(3, dateString(asset.getWarrantyEndDate()));
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO insurance(asset_id,provider,end_date,premium) VALUES(?,?,?,?) ON CONFLICT(asset_id) DO UPDATE SET provider=excluded.provider,end_date=excluded.end_date,premium=excluded.premium")) {
            statement.setInt(1, asset.getId());
            statement.setString(2, blankToNull(asset.getInsuranceProvider()));
            statement.setString(3, dateString(asset.getInsuranceEndDate()));
            statement.setDouble(4, asset.getInsurancePremium());
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO emi(asset_id,lender,monthly_amount,next_due_date) VALUES(?,?,?,?) ON CONFLICT(asset_id) DO UPDATE SET lender=excluded.lender,monthly_amount=excluded.monthly_amount,next_due_date=excluded.next_due_date")) {
            statement.setInt(1, asset.getId());
            statement.setString(2, blankToNull(asset.getEmiLender()));
            statement.setDouble(3, asset.getEmiMonthlyAmount());
            statement.setString(4, dateString(asset.getEmiNextDueDate()));
            statement.executeUpdate();
        }
    }

    public List<Asset> listAssets(int userId, String search) throws SQLException {
        String sql = "SELECT a.*,w.provider warranty_provider,w.end_date warranty_end,i.provider insurance_provider,i.end_date insurance_end,i.premium insurance_premium,e.lender emi_lender,e.monthly_amount emi_amount,e.next_due_date emi_due FROM assets a LEFT JOIN warranties w ON w.asset_id=a.id LEFT JOIN insurance i ON i.asset_id=a.id LEFT JOIN emi e ON e.asset_id=a.id WHERE a.user_id=? AND (lower(a.name) LIKE lower(?) OR lower(coalesce(a.location,'')) LIKE lower(?) OR lower(a.category) LIKE lower(?)) ORDER BY a.name COLLATE NOCASE";
        List<Asset> assets = new ArrayList<>();
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            String pattern = "%" + (search == null ? "" : search.trim()) + "%";
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    LocalDate purchaseDate = parseDate(result.getString("purchase_date"));
                    LocalDate reminderDate = parseDate(result.getString("reminder_date"));
                    Asset asset = "Vehicle".equals(result.getString("category"))
                            ? new VehicleAsset(result.getInt("id"), userId, result.getString("name"), purchaseDate,
                                    result.getDouble("purchase_price"), result.getString("location"), reminderDate, result.getString("reminder_note"))
                            : new HouseholdAsset(result.getInt("id"), userId, result.getString("name"), purchaseDate,
                                    result.getDouble("purchase_price"), result.getString("location"), reminderDate, result.getString("reminder_note"));
                    asset.setWarrantyProvider(result.getString("warranty_provider"));
                    asset.setWarrantyEndDate(parseDate(result.getString("warranty_end")));
                    asset.setInsuranceProvider(result.getString("insurance_provider"));
                    asset.setInsuranceEndDate(parseDate(result.getString("insurance_end")));
                    asset.setInsurancePremium(result.getDouble("insurance_premium"));
                    asset.setEmiLender(result.getString("emi_lender"));
                    asset.setEmiMonthlyAmount(result.getDouble("emi_amount"));
                    asset.setEmiNextDueDate(parseDate(result.getString("emi_due")));
                    asset.refreshTrackedDates();
                    assets.add(asset);
                }
            }
        }
        return assets;
    }

    public void deleteAsset(int userId, int assetId) throws SQLException {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("DELETE FROM assets WHERE id=? AND user_id=?")) {
            statement.setInt(1, assetId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    public void addService(int userId, int assetId, LocalDate date, String type, double cost, String notes) throws SQLException {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO service_history(asset_id,service_date,service_type,cost,notes) SELECT id,?,?,?,? FROM assets WHERE id=? AND user_id=?")) {
            statement.setString(1, dateString(date));
            statement.setString(2, type.trim());
            statement.setDouble(3, cost);
            statement.setString(4, blankToNull(notes));
            statement.setInt(5, assetId);
            statement.setInt(6, userId);
            if (statement.executeUpdate() == 0) throw new SQLException("Asset not found or access denied.");
        }
    }

    public List<ServiceEntry> listServices(int userId, int assetId) throws SQLException {
        List<ServiceEntry> entries = new ArrayList<>();
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(
                "SELECT h.service_date,h.service_type,h.cost,h.notes,a.name FROM service_history h JOIN assets a ON a.id=h.asset_id WHERE a.user_id=? AND h.asset_id=? ORDER BY h.service_date DESC,h.id DESC")) {
            statement.setInt(1, userId);
            statement.setInt(2, assetId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) entries.add(new ServiceEntry(result.getString("name"), parseDate(result.getString("service_date")),
                        result.getString("service_type"), result.getDouble("cost"), result.getString("notes")));
            }
        }
        return entries;
    }

    public DashboardStats dashboard(int userId) throws SQLException {
        int total = 0, overdue = 0, dueSoon = 0, safe = 0;
        LocalDate today = LocalDate.now();
        for (Asset asset : listAssets(userId, "")) {
            total++;
            List<LocalDate> dates = new ArrayList<>();
            dates.add(asset.getReminderDate());
            dates.add(asset.getWarrantyEndDate());
            dates.add(asset.getInsuranceEndDate());
            dates.add(asset.getEmiNextDueDate());
            boolean isOverdue = dates.stream().anyMatch(date -> date != null && date.isBefore(today));
            boolean isDueSoon = dates.stream().anyMatch(date -> date != null && !date.isBefore(today) && !date.isAfter(today.plusDays(30)));
            if (isOverdue) overdue++;
            else if (isDueSoon) dueSoon++;
            else safe++;
        }
        return new DashboardStats(total, overdue, dueSoon, safe);
    }

    private static String dateString(LocalDate date) { return date == null ? null : date.toString(); }
    private static LocalDate parseDate(String value) { return value == null || value.isBlank() ? null : LocalDate.parse(value); }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record DashboardStats(int total, int overdue, int dueSoon, int safe) { }
    public record ServiceEntry(String assetName, LocalDate date, String type, double cost, String notes) { }
}
