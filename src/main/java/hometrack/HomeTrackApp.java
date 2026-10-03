package hometrack;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public final class HomeTrackApp extends JFrame {
    private static final Color INK = new Color(25, 39, 57);
    private static final Color GREEN = new Color(15, 105, 115);
    private static final Color BUTTON_BACKGROUND = new Color(232, 238, 242);
    private static final Color BUTTON_HOVER = new Color(215, 227, 234);
    private static final Color PAPER = new Color(244, 247, 249);
    private static final Color SURFACE = Color.WHITE;
    private static final Color FIELD = Color.WHITE;
    private static final Color MUTED = new Color(82, 98, 113);
    private static final Color BORDER = new Color(190, 201, 210);
    private static final Color DISABLED_BACKGROUND = new Color(239, 242, 244);
    private static final Color DISABLED_TEXT = new Color(88, 101, 113);
    private final Database database;
    private int userId;
    private String username;
    private JTable assetTable;
    private JTable insuranceTable;
    private JTable emiTable;
    private JTable warrantyTable;
    private DefaultTableModel assetModel;
    private DefaultTableModel insuranceModel;
    private DefaultTableModel emiModel;
    private DefaultTableModel warrantyModel;
    private JTextField searchField;
    private final List<JComboBox<Asset>> serviceAssetCombos = new ArrayList<>();
    private final List<DefaultTableModel> serviceModels = new ArrayList<>();
    private final List<String> serviceCategories = new ArrayList<>();
    private JLabel[] dashboardValues;
    private List<Asset> currentAssets = List.of();

    public HomeTrackApp(Database database) {
        super("HomeTrack | Household Asset Manager");
        installLightTheme();
        this.database = database;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 580));
        setSize(1020, 690);
        setLocationRelativeTo(null);
        showLogin();
    }

    private static void installLightTheme() {
        UIManager.put("control", PAPER);
        UIManager.put("info", SURFACE);
        UIManager.put("nimbusBase", new Color(211, 221, 229));
        UIManager.put("nimbusLightBackground", SURFACE);
        UIManager.put("text", INK);
        UIManager.put("Panel.background", PAPER);
        UIManager.put("Panel.foreground", INK);
        UIManager.put("Label.foreground", INK);
        UIManager.put("Button.background", BUTTON_BACKGROUND);
        UIManager.put("Button.foreground", INK);
        UIManager.put("Button.disabledText", DISABLED_TEXT);
        UIManager.put("Button.disabled", DISABLED_BACKGROUND);
        UIManager.put("Button.select", BUTTON_HOVER);
        UIManager.put("Button.rollover", Boolean.TRUE);
        UIManager.put("Button.rolloverBackground", BUTTON_HOVER);
        UIManager.put("Button.focus", BORDER);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        UIManager.put("TextField.background", FIELD);
        UIManager.put("TextField.foreground", INK);
        UIManager.put("TextField.caretForeground", INK);
        UIManager.put("PasswordField.border", UIManager.get("TextField.border"));
        UIManager.put("PasswordField.background", FIELD);
        UIManager.put("PasswordField.foreground", INK);
        UIManager.put("PasswordField.caretForeground", INK);
        UIManager.put("ComboBox.border", BorderFactory.createLineBorder(BORDER));
        UIManager.put("ComboBox.background", FIELD);
        UIManager.put("ComboBox.foreground", INK);
        UIManager.put("List.background", SURFACE);
        UIManager.put("List.foreground", INK);
        UIManager.put("TabbedPane.background", PAPER);
        UIManager.put("TabbedPane.foreground", INK);
        UIManager.put("TabbedPane.selected", SURFACE);
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.foreground", INK);
        UIManager.put("Table.selectionBackground", new Color(205, 231, 233));
        UIManager.put("Table.selectionForeground", INK);
        UIManager.put("TableHeader.background", new Color(226, 233, 238));
        UIManager.put("TableHeader.foreground", INK);
        UIManager.put("ScrollPane.background", PAPER);
        UIManager.put("Viewport.background", SURFACE);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", INK);
        UIManager.put("OptionPane.foreground", INK);
        UIManager.put("OptionPane.buttonBackground", BUTTON_BACKGROUND);
        UIManager.put("OptionPane.buttonForeground", INK);
        UIManager.put("Separator.foreground", BORDER);
        UIManager.put("controlText", INK);
        UIManager.put("textText", INK);
    }

    private void showLogin() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(PAPER);
        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setBackground(SURFACE);
        form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(28, 32, 28, 32)));
        JLabel title = new JLabel("HomeTrack", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setForeground(INK);
        JLabel subtitle = new JLabel("Household Asset Manager", SwingConstants.CENTER);
        JTextField userField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        form.add(title);
        form.add(subtitle);
        form.add(new JLabel("Username"));
        form.add(userField);
        form.add(new JLabel("Password"));
        form.add(passwordField);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);
        JButton login = new JButton("Login");
        JButton register = new JButton("Register");
        Dimension loginButtonSize = new Dimension(102, 32);
        login.setPreferredSize(loginButtonSize);
        register.setPreferredSize(loginButtonSize);
        buttons.add(login);
        buttons.add(register);
        form.add(buttons);
        JPanel centered = new JPanel(new BorderLayout());
        centered.setOpaque(false);
        centered.setBorder(BorderFactory.createEmptyBorder(60, 180, 60, 180));
        centered.add(form, BorderLayout.CENTER);
        page.add(centered, BorderLayout.CENTER);
        login.addActionListener(event -> authenticate(userField.getText(), passwordField.getPassword(), false));
        register.addActionListener(event -> authenticate(userField.getText(), passwordField.getPassword(), true));
        passwordField.addActionListener(event -> authenticate(userField.getText(), passwordField.getPassword(), false));
        styleButtons(page);
        setContentPane(page);
        revalidate();
        repaint();
    }

    private void authenticate(String enteredUsername, char[] password, boolean register) {
        try {
            if (enteredUsername.isBlank() || password.length == 0) throw new IllegalArgumentException("Enter a username and password.");
            if (register && password.length < 6) throw new IllegalArgumentException("Choose a password with at least 6 characters.");
            int authenticatedId = register ? database.register(enteredUsername, password) : database.authenticate(enteredUsername, password);
            if (authenticatedId < 0) throw new IllegalArgumentException("Incorrect username or password.");
            userId = authenticatedId;
            username = enteredUsername.trim();
            showWorkspace();
        } catch (Exception exception) {
            showError(register && exception.getMessage() != null && exception.getMessage().contains("UNIQUE")
                    ? "That username is already registered." : exception.getMessage());
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void showWorkspace() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(PAPER);
        root.setBorder(BorderFactory.createEmptyBorder(14, 18, 18, 18));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel brand = new JLabel("HomeTrack");
        brand.setForeground(INK);
        brand.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        JLabel identity = new JLabel("Signed in as " + username);
        JButton logout = new JButton("Log out");
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(identity);
        right.add(logout);
        header.add(brand, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", buildDashboard());
        tabs.addTab("Assets", buildAssets());
        tabs.addTab("Warranty", buildTracking("Warranty"));
        tabs.addTab("Insurance", buildTracking("Insurance"));
        tabs.addTab("EMI", buildTracking("EMI"));
        tabs.addTab("Vehicle Service", buildServices("Vehicle"));
        tabs.addTab("Home Maintenance", buildServices("Home"));
        tabs.addTab("Service History", buildServices(null));
        root.add(tabs, BorderLayout.CENTER);
        logout.addActionListener(event -> showLogin());
        styleButtons(root);
        setContentPane(root);
        refreshAll();
        revalidate();
        repaint();
    }

    private JPanel buildDashboard() {
        JPanel page = new JPanel(new BorderLayout(10, 14));
        page.setBackground(PAPER);
        page.setBorder(BorderFactory.createEmptyBorder(20, 8, 8, 8));
        JLabel heading = new JLabel("Household overview");
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 21));
        heading.setForeground(INK);
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        String[] labels = {"Total assets", "Overdue", "Due within 30 days", "Safe"};
        Color[] colors = {INK, new Color(166, 54, 54), new Color(151, 99, 21), GREEN};
        dashboardValues = new JLabel[labels.length];
        for (int index = 0; index < labels.length; index++) {
            JPanel item = new JPanel(new BorderLayout(0, 8));
            item.setBackground(SURFACE);
            item.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(16, 16, 16, 16)));
            dashboardValues[index] = new JLabel("0");
            dashboardValues[index].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
            dashboardValues[index].setForeground(colors[index]);
            JLabel caption = new JLabel(labels[index]);
            caption.setForeground(INK);
            item.add(dashboardValues[index], BorderLayout.CENTER);
            item.add(caption, BorderLayout.SOUTH);
            cards.add(item);
        }
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(cards, BorderLayout.CENTER);
        page.add(top, BorderLayout.NORTH);
        JLabel note = new JLabel("Reminder dates include service, warranty, insurance and next EMI dates.");
        note.setForeground(MUTED);
        page.add(note, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildAssets() {
        JPanel page = new JPanel(new BorderLayout(8, 10));
        page.setBackground(PAPER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        searchField = new JTextField(24);
        searchField.setToolTipText("Search by asset name, location, or category");
        JButton add = new JButton("Add asset");
        JButton edit = new JButton("Edit");
        JButton delete = new JButton("Delete");
        actions.add(new JLabel("Search"));
        actions.add(searchField);
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        assetModel = tableModel("ID", "Name", "Category", "Purchase date", "Price", "Location", "Next reminder");
        assetTable = new JTable(assetModel);
        assetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assetTable.setRowHeight(25);
        assetTable.setAutoCreateRowSorter(true);
        styleTable(assetTable);
        page.add(actions, BorderLayout.NORTH);
        page.add(new JScrollPane(assetTable), BorderLayout.CENTER);
        add.addActionListener(event -> openAssetDialog(null));
        edit.addActionListener(event -> openAssetDialog(selectedAsset()));
        delete.addActionListener(event -> deleteSelectedAsset());
        assetTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) openAssetDialog(selectedAsset());
            }
        });
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) { refreshAssets(); }
            public void removeUpdate(DocumentEvent event) { refreshAssets(); }
            public void changedUpdate(DocumentEvent event) { refreshAssets(); }
        });
        return page;
    }

    private JPanel buildServices(String category) {
        JPanel page = new JPanel(new BorderLayout(8, 10));
        page.setBackground(PAPER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JComboBox<Asset> assetCombo = new JComboBox<>();
        assetCombo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Asset asset) setText(asset.getName() + " (" + asset.getCategory() + ")");
                return this;
            }
        });
        JButton add = new JButton(category == null ? "Add service record" : "Add record");
        actions.add(new JLabel(category == null ? "Asset" : category + " asset"));
        actions.add(assetCombo);
        actions.add(add);
        DefaultTableModel model = tableModel("Date", "Asset", "Service", "Cost", "Notes");
        JTable table = new JTable(model);
        table.setRowHeight(25);
        styleTable(table);
        serviceAssetCombos.add(assetCombo);
        serviceModels.add(model);
        serviceCategories.add(category);
        page.add(actions, BorderLayout.NORTH);
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        assetCombo.addActionListener(event -> refreshServices(assetCombo, model));
        add.addActionListener(event -> addServiceRecord(assetCombo, model));
        return page;
    }

    private JPanel buildTracking(String type) {
        JPanel page = new JPanel(new BorderLayout(8, 10));
        page.setBackground(PAPER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton add = new JButton("Add " + type.toLowerCase());
        JButton edit = new JButton("Edit selected");
        actions.add(add);
        actions.add(edit);
        DefaultTableModel model = tableModel("Asset ID", "Asset", "Provider / lender", "End / due date", "Amount");
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setAutoCreateRowSorter(true);
        styleTable(table);
        if ("Insurance".equals(type)) {
            insuranceModel = model;
            insuranceTable = table;
        } else if ("EMI".equals(type)) {
            emiModel = model;
            emiTable = table;
        } else {
            warrantyModel = model;
            warrantyTable = table;
        }
        page.add(actions, BorderLayout.NORTH);
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        add.addActionListener(event -> openTrackingDialog(type, table, false));
        edit.addActionListener(event -> openTrackingDialog(type, table, true));
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) openTrackingDialog(type, table, true);
            }
        });
        return page;
    }

    private void styleTable(JTable table) {
        table.setBackground(SURFACE);
        table.setForeground(INK);
        table.setSelectionBackground(new Color(205, 231, 233));
        table.setSelectionForeground(INK);
        table.setGridColor(BORDER);
        table.getTableHeader().setBackground(new Color(226, 233, 238));
        table.getTableHeader().setForeground(INK);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private void styleButtons(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof AbstractButton button) {
                button.setOpaque(true);
                button.setBackground(BUTTON_BACKGROUND);
                button.setForeground(INK);
                button.setRolloverEnabled(true);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(6, 12, 6, 12)));
                button.setFocusPainted(false);
                button.getModel().addChangeListener(event -> {
                    if (!button.isEnabled()) {
                        button.setBackground(DISABLED_BACKGROUND);
                        button.setForeground(DISABLED_TEXT);
                    } else if (button.getModel().isPressed() || button.getModel().isRollover()) {
                        button.setBackground(BUTTON_HOVER);
                        button.setForeground(INK);
                    } else {
                        button.setBackground(BUTTON_BACKGROUND);
                        button.setForeground(INK);
                    }
                });
            }
            if (component instanceof Container child) styleButtons(child);
        }
    }

    private DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private void refreshAll() {
        refreshAssets();
        refreshDashboard();
        refreshServiceAssets();
        refreshTracking();
    }

    private void refreshAssets() {
        if (assetModel == null) return;
        try {
            currentAssets = database.listAssets(userId, searchField == null ? "" : searchField.getText());
            assetModel.setRowCount(0);
            for (Asset asset : currentAssets) {
                assetModel.addRow(new Object[]{asset.getId(), asset.getName(), asset.getCategory(), display(asset.getPurchaseDate()),
                        String.format("%.2f", asset.getPurchasePrice()), asset.getLocation(), display(asset.getNextReminderDate())});
            }
            refreshDashboard();
            refreshServiceAssets();
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    private void refreshDashboard() {
        if (dashboardValues == null) return;
        try {
            Database.DashboardStats stats = database.dashboard(userId);
            dashboardValues[0].setText(Integer.toString(stats.total()));
            dashboardValues[1].setText(Integer.toString(stats.overdue()));
            dashboardValues[2].setText(Integer.toString(stats.dueSoon()));
            dashboardValues[3].setText(Integer.toString(stats.safe()));
        } catch (Exception exception) { showError(exception.getMessage()); }
    }

    private void refreshServiceAssets() {
        for (int index = 0; index < serviceAssetCombos.size(); index++) {
            JComboBox<Asset> combo = serviceAssetCombos.get(index);
            int oldId = combo.getSelectedItem() instanceof Asset previous ? previous.getId() : -1;
            String category = serviceCategories.get(index);
            DefaultComboBoxModel<Asset> model = new DefaultComboBoxModel<>();
            for (Asset asset : currentAssets) {
                if (category == null || category.equals(asset.getCategory())) model.addElement(asset);
            }
            combo.setModel(model);
            for (int item = 0; item < model.getSize(); item++) {
                if (model.getElementAt(item).getId() == oldId) combo.setSelectedIndex(item);
            }
            refreshServices(combo, serviceModels.get(index));
        }
    }

    private void refreshServices(JComboBox<Asset> combo, DefaultTableModel model) {
        model.setRowCount(0);
        if (!(combo.getSelectedItem() instanceof Asset asset)) return;
        try {
            for (Database.ServiceEntry entry : database.listServices(userId, asset.getId())) {
                model.addRow(new Object[]{entry.date(), entry.assetName(), entry.type(), String.format("%.2f", entry.cost()), entry.notes()});
            }
        } catch (Exception exception) { showError(exception.getMessage()); }
    }

    private void refreshTracking() {
        if (insuranceModel == null || emiModel == null || warrantyModel == null) return;
        try {
            List<Asset> assets = database.listAssets(userId, "");
            insuranceModel.setRowCount(0);
            emiModel.setRowCount(0);
            warrantyModel.setRowCount(0);
            for (Asset asset : assets) {
                if (hasValue(asset.getInsuranceProvider()) || asset.getInsuranceEndDate() != null || asset.getInsurancePremium() > 0) {
                    insuranceModel.addRow(new Object[]{asset.getId(), asset.getName(), value(asset.getInsuranceProvider()),
                            display(asset.getInsuranceEndDate()), String.format("%.2f", asset.getInsurancePremium())});
                }
                if (hasValue(asset.getEmiLender()) || asset.getEmiNextDueDate() != null || asset.getEmiMonthlyAmount() > 0) {
                    emiModel.addRow(new Object[]{asset.getId(), asset.getName(), value(asset.getEmiLender()),
                            display(asset.getEmiNextDueDate()), String.format("%.2f", asset.getEmiMonthlyAmount())});
                }
                if (hasValue(asset.getWarrantyProvider()) || asset.getWarrantyEndDate() != null) {
                    warrantyModel.addRow(new Object[]{asset.getId(), asset.getName(), value(asset.getWarrantyProvider()),
                            display(asset.getWarrantyEndDate()), ""});
                }
            }
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    private void openTrackingDialog(String type, JTable table, boolean editing) {
        if (editing && table.getSelectedRow() < 0) {
            showError("Select a " + type.toLowerCase() + " record first.");
            return;
        }
        try {
            List<Asset> assets = database.listAssets(userId, "");
            if (assets.isEmpty()) {
                showError("Add an asset before recording " + type.toLowerCase() + " details.");
                return;
            }
            int selectedAssetId = -1;
            if (editing) {
                int row = table.convertRowIndexToModel(table.getSelectedRow());
                selectedAssetId = ((Number) table.getModel().getValueAt(row, 0)).intValue();
            }
            JComboBox<Asset> assetCombo = new JComboBox<>(assets.toArray(Asset[]::new));
            assetCombo.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, selected, focus);
                    if (value instanceof Asset asset) setText(asset.getName() + " (" + asset.getCategory() + ")");
                    return this;
                }
            });
            JTextField provider = new JTextField();
            JTextField date = new JTextField();
            JTextField amount = new JTextField("0");
            Asset selected = null;
            for (Asset asset : assets) {
                if (asset.getId() == selectedAssetId) {
                    selected = asset;
                    break;
                }
            }
            if (selected != null) {
                assetCombo.setSelectedItem(selected);
                if ("Insurance".equals(type)) {
                    provider.setText(value(selected.getInsuranceProvider()));
                    date.setText(display(selected.getInsuranceEndDate()));
                    amount.setText(Double.toString(selected.getInsurancePremium()));
                } else if ("EMI".equals(type)) {
                    provider.setText(value(selected.getEmiLender()));
                    date.setText(display(selected.getEmiNextDueDate()));
                    amount.setText(Double.toString(selected.getEmiMonthlyAmount()));
                } else {
                    provider.setText(value(selected.getWarrantyProvider()));
                    date.setText(display(selected.getWarrantyEndDate()));
                }
            }
            String amountLabel = "Insurance".equals(type) ? "Premium" : "Monthly amount";
            Component[] fields = "Warranty".equals(type)
                    ? new Component[]{assetCombo, provider, date}
                    : new Component[]{assetCombo, provider, date, amount};
            String[] labels = "Warranty".equals(type)
                    ? new String[]{"Asset", "Provider", "End date (YYYY-MM-DD)"}
                    : new String[]{"Asset", "Provider / lender", "End / due date (YYYY-MM-DD)", amountLabel};
            JPanel form = formPanel(labels, fields);
            String title = (editing ? "Edit " : "Add ") + type.toLowerCase();
            if (JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            Asset asset = (Asset) assetCombo.getSelectedItem();
            if (asset == null) return;
            LocalDate parsedDate = parseOptional(date.getText());
            if ("Insurance".equals(type)) {
                double premium = nonNegative(amount.getText(), "Premium");
                if (provider.getText().isBlank() && parsedDate == null && premium == 0) throw new IllegalArgumentException("Enter insurance details.");
                asset.setInsuranceProvider(provider.getText().trim());
                asset.setInsuranceEndDate(parsedDate);
                asset.setInsurancePremium(premium);
            } else if ("EMI".equals(type)) {
                double monthly = nonNegative(amount.getText(), "Monthly amount");
                if (provider.getText().isBlank() && parsedDate == null && monthly == 0) throw new IllegalArgumentException("Enter EMI details.");
                asset.setEmiLender(provider.getText().trim());
                asset.setEmiNextDueDate(parsedDate);
                asset.setEmiMonthlyAmount(monthly);
            } else {
                if (provider.getText().isBlank() && parsedDate == null) throw new IllegalArgumentException("Enter warranty details.");
                asset.setWarrantyProvider(provider.getText().trim());
                asset.setWarrantyEndDate(parsedDate);
            }
            database.saveAsset(asset);
            refreshAll();
        } catch (Exception exception) {
            showError("Check the details. Dates must use YYYY-MM-DD. " + exception.getMessage());
        }
    }

    private double nonNegative(String text, String label) {
        double number = Double.parseDouble(text.trim());
        if (!Double.isFinite(number) || number < 0) throw new IllegalArgumentException(label + " must be a non-negative number.");
        return number;
    }

    private LocalDate parseOptional(String text) {
        return text == null || text.isBlank() ? null : LocalDate.parse(text.trim());
    }

    private boolean hasValue(String text) {
        return text != null && !text.isBlank();
    }

    private String value(String text) { return text == null ? "" : text; }

    private Asset selectedAsset() {
        int row = assetTable == null ? -1 : assetTable.getSelectedRow();
        if (row < 0) return null;
        int modelRow = assetTable.convertRowIndexToModel(row);
        int id = ((Number) assetModel.getValueAt(modelRow, 0)).intValue();
        return currentAssets.stream().filter(asset -> asset.getId() == id).findFirst().orElse(null);
    }

    private void openAssetDialog(Asset existing) {
        AssetEditor editor = new AssetEditor(this, existing);
        editor.setVisible(true);
        if (editor.result != null) {
            try {
                database.saveAsset(editor.result);
                refreshAll();
            } catch (Exception exception) { showError(exception.getMessage()); }
        }
    }

    private void deleteSelectedAsset() {
        Asset asset = selectedAsset();
        if (asset == null) { showError("Select an asset first."); return; }
        int answer = JOptionPane.showConfirmDialog(this, "Delete " + asset.getName() + " and its tracking records?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            try { database.deleteAsset(userId, asset.getId()); refreshAll(); }
            catch (Exception exception) { showError(exception.getMessage()); }
        }
    }

    private void addServiceRecord(JComboBox<Asset> combo, DefaultTableModel model) {
        if (!(combo.getSelectedItem() instanceof Asset asset)) { showError("Add an asset before recording a service."); return; }
        JTextField date = new JTextField(LocalDate.now().toString());
        JTextField type = new JTextField();
        JTextField cost = new JTextField("0");
        JTextField notes = new JTextField();
        JPanel form = formPanel(new String[]{"Service date (YYYY-MM-DD)", "Service type", "Cost", "Notes"}, new JTextField[]{date, type, cost, notes});
        if (JOptionPane.showConfirmDialog(this, form, "New service record", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            if (type.getText().isBlank()) throw new IllegalArgumentException("Enter a service type.");
            double amount = Double.parseDouble(cost.getText().trim());
            if (amount < 0) throw new IllegalArgumentException("Cost cannot be negative.");
            database.addService(userId, asset.getId(), LocalDate.parse(date.getText().trim()), type.getText(), amount, notes.getText());
            refreshServices(combo, model);
        } catch (Exception exception) { showError("Check the service details. " + exception.getMessage()); }
    }

    private static JPanel formPanel(String[] labels, Component[] fields) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 8, 8));
        panel.setBackground(SURFACE);
        for (int index = 0; index < labels.length; index++) {
            panel.add(new JLabel(labels[index]));
            panel.add(fields[index]);
        }
        return panel;
    }

    private static String display(LocalDate date) { return date == null ? "" : date.toString(); }
    private void showError(String message) { JOptionPane.showMessageDialog(this, message == null ? "Unexpected error." : message, "HomeTrack", JOptionPane.ERROR_MESSAGE); }

    private final class AssetEditor extends JDialog {
        private final int existingId;
        private final JComboBox<String> category = new JComboBox<>(new String[]{"Home", "Vehicle"});
        private final JTextField name = new JTextField();
        private final JTextField purchaseDate = new JTextField();
        private final JTextField purchasePrice = new JTextField("0");
        private final JTextField location = new JTextField();
        private final JTextField reminderDate = new JTextField();
        private final JTextField reminderNote = new JTextField();
        private final JTextField warrantyProvider = new JTextField();
        private final JTextField warrantyEnd = new JTextField();
        private final JTextField insuranceProvider = new JTextField();
        private final JTextField insuranceEnd = new JTextField();
        private final JTextField insurancePremium = new JTextField("0");
        private final JTextField emiLender = new JTextField();
        private final JTextField emiAmount = new JTextField("0");
        private final JTextField emiDue = new JTextField();
        private Asset result;

        AssetEditor(JFrame owner, Asset existing) {
            super(owner, existing == null ? "Add asset" : "Update asset", true);
            existingId = existing == null ? 0 : existing.getId();
            JTabbedPane fields = new JTabbedPane();
            fields.addTab("Asset", formPanel(
                    new String[]{"Category", "Name", "Purchase date (YYYY-MM-DD)", "Purchase price", "Location", "Reminder date (YYYY-MM-DD)", "Reminder note"},
                new Component[]{category, name, purchaseDate, purchasePrice, location, reminderDate, reminderNote}));
            fields.addTab("Warranty, insurance, EMI", formPanel(
                    new String[]{"Warranty provider", "Warranty end date", "Insurance provider", "Insurance end date", "Insurance premium", "EMI lender", "Monthly EMI", "Next EMI date"},
                    new JTextField[]{warrantyProvider, warrantyEnd, insuranceProvider, insuranceEnd, insurancePremium, emiLender, emiAmount, emiDue}));
            JButton save = new JButton("Save");
            JButton cancel = new JButton("Cancel");
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.add(cancel);
            buttons.add(save);
            setLayout(new BorderLayout(8, 8));
            add(fields, BorderLayout.CENTER);
            add(buttons, BorderLayout.SOUTH);
            styleButtons(getContentPane());
            setSize(610, 390);
            setLocationRelativeTo(owner);
            cancel.addActionListener(event -> dispose());
            save.addActionListener(this::save);
            if (existing != null) populate(existing);
        }

        private void populate(Asset asset) {
            category.setSelectedItem(asset.getCategory());
            name.setText(asset.getName());
            purchaseDate.setText(display(asset.getPurchaseDate()));
            purchasePrice.setText(Double.toString(asset.getPurchasePrice()));
            location.setText(asset.getLocation() == null ? "" : asset.getLocation());
            reminderDate.setText(display(asset.getReminderDate()));
            reminderNote.setText(asset.getReminderNote() == null ? "" : asset.getReminderNote());
            warrantyProvider.setText(value(asset.getWarrantyProvider()));
            warrantyEnd.setText(display(asset.getWarrantyEndDate()));
            insuranceProvider.setText(value(asset.getInsuranceProvider()));
            insuranceEnd.setText(display(asset.getInsuranceEndDate()));
            insurancePremium.setText(Double.toString(asset.getInsurancePremium()));
            emiLender.setText(value(asset.getEmiLender()));
            emiAmount.setText(Double.toString(asset.getEmiMonthlyAmount()));
            emiDue.setText(display(asset.getEmiNextDueDate()));
        }

        private void save(ActionEvent event) {
            try {
                if (name.getText().isBlank()) throw new IllegalArgumentException("Asset name is required.");
                LocalDate purchased = parseOptional(purchaseDate.getText());
                LocalDate reminder = parseOptional(reminderDate.getText());
                LocalDate warranty = parseOptional(warrantyEnd.getText());
                LocalDate insurance = parseOptional(insuranceEnd.getText());
                LocalDate emiDate = parseOptional(emiDue.getText());
                double price = nonNegative(purchasePrice.getText(), "Purchase price");
                double premium = nonNegative(insurancePremium.getText(), "Insurance premium");
                double monthly = nonNegative(emiAmount.getText(), "Monthly EMI");
                int id = selectedExistingId();
                result = "Vehicle".equals(category.getSelectedItem())
                        ? new VehicleAsset(id, userId, name.getText().trim(), purchased, price, location.getText().trim(), reminder, reminderNote.getText().trim())
                        : new HouseholdAsset(id, userId, name.getText().trim(), purchased, price, location.getText().trim(), reminder, reminderNote.getText().trim());
                result.setWarrantyProvider(warrantyProvider.getText().trim());
                result.setWarrantyEndDate(warranty);
                result.setInsuranceProvider(insuranceProvider.getText().trim());
                result.setInsuranceEndDate(insurance);
                result.setInsurancePremium(premium);
                result.setEmiLender(emiLender.getText().trim());
                result.setEmiMonthlyAmount(monthly);
                result.setEmiNextDueDate(emiDate);
                dispose();
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(this, "Check the fields. Dates must use YYYY-MM-DD. " + exception.getMessage(), "Invalid asset", JOptionPane.ERROR_MESSAGE);
            }
        }

        private int selectedExistingId() {
            return existingId;
        }

    }
}
