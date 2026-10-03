package hometrack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private Main() { }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "--self-test".equals(args[0])) {
            selfTest();
            return;
        }
        Database database = new Database("jdbc:sqlite:hometrack.db");
        database.initialize();
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            new HomeTrackApp(database).setVisible(true);
        });
    }

    private static void selfTest() throws Exception {
        Path file = Files.createTempFile("hometrack-test-", ".db");
        try {
            Database database = new Database("jdbc:sqlite:" + file.toAbsolutePath());
            database.initialize();
            char[] password = "test-password".toCharArray();
            int userId = database.register("smoke-user", password);
            if (database.authenticate("smoke-user", password) != userId) throw new IllegalStateException("Login check failed");
            VehicleAsset vehicle = new VehicleAsset(0, userId, "Test vehicle", LocalDate.now(), 12500,
                    "Garage", LocalDate.now().plusDays(10), "Oil and filter service");
            vehicle.setWarrantyProvider("Example Cover");
            vehicle.setWarrantyEndDate(LocalDate.now().plusDays(90));
            database.saveAsset(vehicle);
            List<Asset> assets = database.listAssets(userId, "Test");
            if (assets.size() != 1 || assets.get(0).getId() != vehicle.getId()) throw new IllegalStateException("Asset create/search check failed");
            database.addService(userId, vehicle.getId(), LocalDate.now(), "Oil change", 75, "Smoke test record");
            if (database.listServices(userId, vehicle.getId()).size() != 1) throw new IllegalStateException("Service history check failed");
            Database.DashboardStats stats = database.dashboard(userId);
            if (stats.total() != 1 || stats.dueSoon() != 1) throw new IllegalStateException("Dashboard calculation check failed");
            vehicle.setName("Updated vehicle");
            database.saveAsset(vehicle);
            if (!"Updated vehicle".equals(database.listAssets(userId, "Updated").get(0).getName())) throw new IllegalStateException("Asset update check failed");
            database.deleteAsset(userId, vehicle.getId());
            if (!database.listAssets(userId, "").isEmpty()) throw new IllegalStateException("Asset delete check failed");
            System.out.println("HomeTrack self-test passed: authentication, asset CRUD/search, tracking, dashboard, and service history.");
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
