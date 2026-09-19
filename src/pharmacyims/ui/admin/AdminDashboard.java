package pharmacyims.ui.admin;

import com.formdev.flatlaf.FlatClientProperties;
import pharmacyims.dao.MedicineDAO;
import pharmacyims.dao.SaleDAO;
import pharmacyims.dao.SupplierDAO;
import pharmacyims.dao.UserDAO;
import pharmacyims.model.Medicine;
import pharmacyims.model.Supplier;
import pharmacyims.model.User;
import pharmacyims.session.UserSession;
import pharmacyims.ui.cashier.CashierDashboard;
import pharmacyims.ui.common.CurrencyTableCellRenderer;
import pharmacyims.ui.common.HeaderBar;
import pharmacyims.ui.common.MetricCard;
import pharmacyims.ui.common.StatusBadgeRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Fyto PIMS — Master Administrator Dashboard.
 * Complete management of Inventory, Suppliers, Cashier Accounts, and Financial Insights.
 */
public class AdminDashboard extends JFrame {

    private static final String CARD_MEDICINES = "MEDICINES";
    private static final String CARD_SUPPLIERS = "SUPPLIERS";
    private static final String CARD_USERS = "USERS";
    private static final String CARD_REPORTS = "REPORTS";

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final UserDAO userDAO = new UserDAO();
    private final SaleDAO saleDAO = new SaleDAO();

    // Top Metric Cards
    private MetricCard cardTotalMedicines;
    private MetricCard cardLowStock;
    private MetricCard cardExpiring;
    private MetricCard cardTotalSuppliers;

    // Navigation & Layout
    private CardLayout cardLayout;
    private JPanel contentCardsPanel;
    private final List<JButton> navButtons = new ArrayList<>();

    // Medicine Tab Components
    private JTextField txtMedSearch;
    private JComboBox<String> cmbMedTypeFilter;
    private DefaultTableModel medTableModel;
    private JTable medTable;
    private TableRowSorter<DefaultTableModel> medSorter;

    // Supplier Tab Components
    private JTextField txtSupSearch;
    private DefaultTableModel supTableModel;
    private JTable supTable;
    private TableRowSorter<DefaultTableModel> supSorter;

    // User Tab Components
    private DefaultTableModel userTableModel;
    private JTable userTable;

    // Insights Tab Components
    private JLabel lblTotalValuation;
    private JLabel lblTotalUnits;
    private JLabel lblItemsToRestock;
    private DefaultTableModel restockTableModel;
    private JTable restockTable;

    public AdminDashboard() {
        setTitle("Fyto PIMS — Administrator Management Portal");
        setSize(1240, 820);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        refreshAll();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(248, 250, 252)); // Light slate bg (#F8FAFC)

        // 1. Top Header Bar
        HeaderBar headerBar = new HeaderBar(this, "Administrator Management Portal");
        rootPanel.add(headerBar, BorderLayout.NORTH);

        // 2. Main Body: Left Sidebar + Center Workspace
        JPanel bodyPanel = new JPanel(new BorderLayout(16, 16));
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Left Navigation Sidebar
        JPanel sidebarPanel = createSidebar();
        bodyPanel.add(sidebarPanel, BorderLayout.WEST);

        // Center Content Workspace (Metrics Row + Card Panels)
        JPanel workspacePanel = new JPanel(new BorderLayout(0, 16));
        workspacePanel.setOpaque(false);

        // Metrics Row
        JPanel metricsPanel = createMetricsRow();
        workspacePanel.add(metricsPanel, BorderLayout.NORTH);

        // Card Container for Tabs
        cardLayout = new CardLayout();
        contentCardsPanel = new JPanel(cardLayout);
        contentCardsPanel.setOpaque(false);

        contentCardsPanel.add(createMedicinesPanel(), CARD_MEDICINES);
        contentCardsPanel.add(createSuppliersPanel(), CARD_SUPPLIERS);
        contentCardsPanel.add(createUsersPanel(), CARD_USERS);
        contentCardsPanel.add(createReportsPanel(), CARD_REPORTS);

        workspacePanel.add(contentCardsPanel, BorderLayout.CENTER);
        bodyPanel.add(workspacePanel, BorderLayout.CENTER);

        rootPanel.add(bodyPanel, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    /**
     * Top 4 KPI metric cards displaying live system health.
     */
    private JPanel createMetricsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);

        cardTotalMedicines = new MetricCard("TOTAL PRODUCTS", "0", "Catalog medicines", MetricCard.CardTheme.PRIMARY);
        cardLowStock = new MetricCard("LOW STOCK ALERTS", "0", "At or below reorder level", MetricCard.CardTheme.WARNING);
        cardExpiring = new MetricCard("EXPIRING SOON", "0", "Within 30 days threshold", MetricCard.CardTheme.DANGER);
        cardTotalSuppliers = new MetricCard("ACTIVE VENDORS", "0", "Verified supply partners", MetricCard.CardTheme.SUCCESS);

        row.add(cardTotalMedicines);
        row.add(cardLowStock);
        row.add(cardExpiring);
        row.add(cardTotalSuppliers);

        return row;
    }

    /**
     * Left modern sidebar navigation panel.
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 12, 16, 12)
        ));
        sidebar.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        JLabel lblNavHeading = new JLabel("NAVIGATION");
        lblNavHeading.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNavHeading.setForeground(new Color(148, 163, 184));
        lblNavHeading.setBorder(new EmptyBorder(4, 10, 10, 0));
        lblNavHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblNavHeading);

        JButton btnNavMedicines = createNavButton("📦  Medicine Inventory", CARD_MEDICINES, true);
        JButton btnNavSuppliers = createNavButton("🏢  Supplier Directory", CARD_SUPPLIERS, false);
        JButton btnNavUsers = createNavButton("👥  Cashier Accounts", CARD_USERS, false);
        JButton btnNavReports = createNavButton("📊  Financial Insights", CARD_REPORTS, false);

        sidebar.add(btnNavMedicines);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnNavSuppliers);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnNavUsers);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnNavReports);

        sidebar.add(Box.createVerticalStrut(14));
        JSeparator navSep = new JSeparator();
        navSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        navSep.setForeground(new Color(226, 232, 240));
        sidebar.add(navSep);
        sidebar.add(Box.createVerticalStrut(12));

        JButton btnOpenPOS = new JButton("🛒  Launch POS Portal");
        btnOpenPOS.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOpenPOS.setHorizontalAlignment(SwingConstants.LEFT);
        btnOpenPOS.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnOpenPOS.setPreferredSize(new Dimension(196, 38));
        btnOpenPOS.setBackground(new Color(240, 253, 250));
        btnOpenPOS.setForeground(new Color(13, 148, 136));
        btnOpenPOS.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnOpenPOS.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOpenPOS.addActionListener(e -> {
            CashierDashboard pos = new CashierDashboard();
            pos.setVisible(true);
        });
        sidebar.add(btnOpenPOS);

        sidebar.add(Box.createVerticalGlue());

        // Quick System Info at bottom of sidebar
        JPanel infoBox = new JPanel(new GridLayout(2, 1, 0, 2));
        infoBox.setBackground(new Color(248, 250, 252));
        infoBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        infoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        infoBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblVer = new JLabel("Fyto PIMS v1.0 Enterprise");
        lblVer.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblVer.setForeground(new Color(71, 85, 105));

        JLabel lblRole = new JLabel("Role: Administrator");
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRole.setForeground(new Color(100, 116, 139));

        infoBox.add(lblVer);
        infoBox.add(lblRole);
        sidebar.add(infoBox);

        return sidebar;
    }

    private JButton createNavButton(String title, String cardName, boolean active) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(196, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        updateNavButtonStyle(btn, active);

        btn.addActionListener(e -> {
            for (JButton b : navButtons) {
                updateNavButtonStyle(b, false);
            }
            updateNavButtonStyle(btn, true);
            cardLayout.show(contentCardsPanel, cardName);
        });

        navButtons.add(btn);
        return btn;
    }

    private void updateNavButtonStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(new Color(236, 253, 245)); // Emerald tint (#ECFDF5)
            btn.setForeground(new Color(13, 148, 136));  // Medical Teal (#0D9488)
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(167, 243, 208), 1, true),
                    new EmptyBorder(6, 12, 6, 12)
            ));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(71, 85, 105));
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        }
    }

    // =========================================================================
    // TAB 1: MEDICINE INVENTORY PANEL
    // =========================================================================

    private JPanel createMedicinesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        // Action Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        // Left Filters: Search input + Dosage Type dropdown
        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        txtMedSearch = new JTextField(18);
        txtMedSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search drug or company...");
        txtMedSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        txtMedSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateMedicineFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { updateMedicineFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { updateMedicineFilter(); }
        });
        leftFilters.add(txtMedSearch);

        String[] types = {"All Types", "Tablet", "Capsule", "Syrup", "Injection", "Cream", "Inhaler", "Drops", "Ointment", "Other"};
        cmbMedTypeFilter = new JComboBox<>(types);
        cmbMedTypeFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        cmbMedTypeFilter.addActionListener(e -> updateMedicineFilter());
        leftFilters.add(cmbMedTypeFilter);

        toolbar.add(leftFilters, BorderLayout.WEST);

        // Right Actions: Add, Edit, Delete, Refresh
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnAddMed = new JButton("+ Add Medicine");
        btnAddMed.setBackground(new Color(13, 148, 136));
        btnAddMed.setForeground(Color.WHITE);
        btnAddMed.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddMed.putClientProperty(FlatClientProperties.STYLE, "arc: 8; hoverBackground: #0F766E;");
        btnAddMed.addActionListener(e -> handleAddMedicine());
        rightActions.add(btnAddMed);

        JButton btnEditMed = new JButton("Edit Selected");
        btnEditMed.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnEditMed.addActionListener(e -> handleEditMedicine());
        rightActions.add(btnEditMed);

        JButton btnDeleteMed = new JButton("Delete");
        btnDeleteMed.setForeground(new Color(220, 38, 38));
        btnDeleteMed.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnDeleteMed.addActionListener(e -> handleDeleteMedicine());
        rightActions.add(btnDeleteMed);

        JButton btnRefreshMed = new JButton("Refresh");
        btnRefreshMed.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnRefreshMed.addActionListener(e -> {
            reloadMedicines();
            refreshMetrics();
        });
        rightActions.add(btnRefreshMed);

        toolbar.add(rightActions, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        // Medicines JTable
        String[] cols = {"ID", "Medicine Name", "Manufacturer", "Type", "Dispense Price", "Stock", "Reorder", "Expiry Date", "Supplier", "Stock Status", "Expiry Status"};
        medTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 5 || columnIndex == 6) return Integer.class;
                if (columnIndex == 4) return BigDecimal.class;
                return String.class;
            }
        };

        medTable = new JTable(medTableModel);
        medTable.setRowHeight(32);
        medTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        medTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        medTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        medTable.getTableHeader().setBackground(new Color(241, 245, 249));
        medTable.getTableHeader().setForeground(new Color(51, 65, 85));

        medSorter = new TableRowSorter<>(medTableModel);
        medTable.setRowSorter(medSorter);

        // Column Renderers
        medTable.getColumnModel().getColumn(4).setCellRenderer(new CurrencyTableCellRenderer());
        medTable.getColumnModel().getColumn(9).setCellRenderer(new StatusBadgeRenderer());
        medTable.getColumnModel().getColumn(10).setCellRenderer(new StatusBadgeRenderer());

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        medTable.getColumnModel().getColumn(0).setCellRenderer(rightAlign);
        medTable.getColumnModel().getColumn(5).setCellRenderer(rightAlign);
        medTable.getColumnModel().getColumn(6).setCellRenderer(rightAlign);

        // Width tuning
        medTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        medTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        medTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        medTable.getColumnModel().getColumn(3).setPreferredWidth(75);
        medTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        medTable.getColumnModel().getColumn(5).setPreferredWidth(65);
        medTable.getColumnModel().getColumn(6).setPreferredWidth(65);
        medTable.getColumnModel().getColumn(7).setPreferredWidth(95);
        medTable.getColumnModel().getColumn(8).setPreferredWidth(140);
        medTable.getColumnModel().getColumn(9).setPreferredWidth(95);
        medTable.getColumnModel().getColumn(10).setPreferredWidth(95);

        // Double-click to Edit
        medTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && medTable.getSelectedRow() != -1) {
                    handleEditMedicine();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(medTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void updateMedicineFilter() {
        String query = txtMedSearch.getText().trim();
        String selectedType = (String) cmbMedTypeFilter.getSelectedItem();

        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();
        if (!query.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(query), 1, 2, 8));
        }
        if (selectedType != null && !selectedType.equalsIgnoreCase("All Types") && !selectedType.equalsIgnoreCase("All")) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(selectedType) + "$", 3));
        }

        if (filters.isEmpty()) {
            medSorter.setRowFilter(null);
        } else {
            medSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void handleAddMedicine() {
        MedicineFormDialog dialog = new MedicineFormDialog(this, null, medicineDAO, supplierDAO);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reloadMedicines();
            refreshMetrics();
            JOptionPane.showMessageDialog(this, "Medicine registered successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleEditMedicine() {
        int selectedViewRow = medTable.getSelectedRow();
        if (selectedViewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medicine to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = medTable.convertRowIndexToModel(selectedViewRow);
        int medId = (int) medTableModel.getValueAt(modelRow, 0);
        Medicine med = medicineDAO.getMedicineById(medId);
        if (med == null) {
            JOptionPane.showMessageDialog(this, "Medicine record not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        MedicineFormDialog dialog = new MedicineFormDialog(this, med, medicineDAO, supplierDAO);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reloadMedicines();
            refreshMetrics();
            JOptionPane.showMessageDialog(this, "Medicine details updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleDeleteMedicine() {
        int selectedViewRow = medTable.getSelectedRow();
        if (selectedViewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medicine to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = medTable.convertRowIndexToModel(selectedViewRow);
        int medId = (int) medTableModel.getValueAt(modelRow, 0);
        String name = (String) medTableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete:\n" + name + " (ID: " + medId + ")?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = medicineDAO.deleteMedicine(medId);
            if (ok) {
                reloadMedicines();
                refreshMetrics();
                JOptionPane.showMessageDialog(this, "Medicine deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete medicine record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void reloadMedicines() {
        medTableModel.setRowCount(0);
        List<Medicine> medicines = medicineDAO.getAllMedicines();
        LocalDate today = LocalDate.now();

        for (Medicine m : medicines) {
            // Stock Status
            String stockStatus;
            if (m.getQuantityInStock() <= 0) {
                stockStatus = "Out of Stock";
            } else if (m.getQuantityInStock() <= m.getReorderLevel()) {
                stockStatus = "Low Stock";
            } else {
                stockStatus = "In Stock";
            }

            // Expiry Status
            String expiryStatus = "Good";
            if (m.getExpiryDate() != null) {
                LocalDate exp = m.getExpiryDate().toLocalDate();
                if (!exp.isAfter(today)) {
                    expiryStatus = "Expired";
                } else if (!exp.isAfter(today.plusDays(30))) {
                    expiryStatus = "Warning";
                }
            }

            medTableModel.addRow(new Object[]{
                    m.getMedicineId(),
                    m.getName(),
                    m.getCompany(),
                    m.getMedicineType(),
                    m.getPrice(),
                    m.getQuantityInStock(),
                    m.getReorderLevel(),
                    m.getExpiryDate() != null ? m.getExpiryDate().toString() : "N/A",
                    m.getSupplierName() != null ? m.getSupplierName() : "None",
                    stockStatus,
                    expiryStatus
            });
        }
    }

    // =========================================================================
    // TAB 2: SUPPLIER DIRECTORY PANEL
    // =========================================================================

    private JPanel createSuppliersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        // Search
        JPanel leftSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftSearch.setOpaque(false);

        txtSupSearch = new JTextField(20);
        txtSupSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search vendor or contact person...");
        txtSupSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        txtSupSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateSupplierFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { updateSupplierFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { updateSupplierFilter(); }
        });
        leftSearch.add(txtSupSearch);
        toolbar.add(leftSearch, BorderLayout.WEST);

        // Buttons: Add, Edit, Delete, Refresh
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnAddSup = new JButton("+ Add Supplier");
        btnAddSup.setBackground(new Color(13, 148, 136));
        btnAddSup.setForeground(Color.WHITE);
        btnAddSup.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddSup.putClientProperty(FlatClientProperties.STYLE, "arc: 8; hoverBackground: #0F766E;");
        btnAddSup.addActionListener(e -> handleAddSupplier());
        rightActions.add(btnAddSup);

        JButton btnEditSup = new JButton("Edit Selected");
        btnEditSup.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnEditSup.addActionListener(e -> handleEditSupplier());
        rightActions.add(btnEditSup);

        JButton btnDeleteSup = new JButton("Delete");
        btnDeleteSup.setForeground(new Color(220, 38, 38));
        btnDeleteSup.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnDeleteSup.addActionListener(e -> handleDeleteSupplier());
        rightActions.add(btnDeleteSup);

        JButton btnRefreshSup = new JButton("Refresh");
        btnRefreshSup.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnRefreshSup.addActionListener(e -> {
            reloadSuppliers();
            refreshMetrics();
        });
        rightActions.add(btnRefreshSup);

        toolbar.add(rightActions, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        // Suppliers JTable
        String[] cols = {"ID", "Supplier / Company Name", "Contact Person", "Phone", "Email Address", "Distribution Address"};
        supTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                return String.class;
            }
        };

        supTable = new JTable(supTableModel);
        supTable.setRowHeight(32);
        supTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        supTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        supTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        supTable.getTableHeader().setBackground(new Color(241, 245, 249));
        supTable.getTableHeader().setForeground(new Color(51, 65, 85));

        supSorter = new TableRowSorter<>(supTableModel);
        supTable.setRowSorter(supSorter);

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        supTable.getColumnModel().getColumn(0).setCellRenderer(rightAlign);
        supTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        supTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        supTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        supTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        supTable.getColumnModel().getColumn(4).setPreferredWidth(170);
        supTable.getColumnModel().getColumn(5).setPreferredWidth(250);

        supTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && supTable.getSelectedRow() != -1) {
                    handleEditSupplier();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(supTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void updateSupplierFilter() {
        String query = txtSupSearch.getText().trim();
        if (query.isEmpty()) {
            supSorter.setRowFilter(null);
        } else {
            supSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(query), 1, 2, 4));
        }
    }

    private void handleAddSupplier() {
        SupplierFormDialog dialog = new SupplierFormDialog(this, null, supplierDAO);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reloadSuppliers();
            refreshMetrics();
            JOptionPane.showMessageDialog(this, "Supplier registered successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleEditSupplier() {
        int selectedViewRow = supTable.getSelectedRow();
        if (selectedViewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = supTable.convertRowIndexToModel(selectedViewRow);
        int supId = (int) supTableModel.getValueAt(modelRow, 0);
        Supplier sup = supplierDAO.getSupplierById(supId);
        if (sup == null) {
            JOptionPane.showMessageDialog(this, "Supplier record not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SupplierFormDialog dialog = new SupplierFormDialog(this, sup, supplierDAO);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reloadSuppliers();
            refreshMetrics();
            JOptionPane.showMessageDialog(this, "Supplier details updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleDeleteSupplier() {
        int selectedViewRow = supTable.getSelectedRow();
        if (selectedViewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = supTable.convertRowIndexToModel(selectedViewRow);
        int supId = (int) supTableModel.getValueAt(modelRow, 0);
        String name = (String) supTableModel.getValueAt(modelRow, 1);

        // Check if supplier has associated medicines
        List<Medicine> allMeds = medicineDAO.getAllMedicines();
        long linkedCount = allMeds.stream().filter(m -> m.getSupplierId() != null && m.getSupplierId() == supId).count();
        if (linkedCount > 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Cannot delete supplier '" + name + "'!\nIt is currently linked to " + linkedCount + " medicines in inventory.\n"
                            + "Please reassign or remove those medicine records before deleting.",
                    "Integrity Restriction",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete supplier:\n" + name + " (ID: " + supId + ")?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = supplierDAO.deleteSupplier(supId);
            if (ok) {
                reloadSuppliers();
                refreshMetrics();
                JOptionPane.showMessageDialog(this, "Supplier deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete supplier record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void reloadSuppliers() {
        supTableModel.setRowCount(0);
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        for (Supplier s : suppliers) {
            supTableModel.addRow(new Object[]{
                    s.getSupplierId(),
                    s.getName(),
                    s.getContactPerson(),
                    s.getPhone(),
                    s.getEmail(),
                    s.getAddress()
            });
        }
    }

    // =========================================================================
    // TAB 3: CASHIER ACCOUNTS & ACCESS CONTROL PANEL
    // =========================================================================

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        // Action Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel lblHeading = new JLabel("System Personnel & Cashier Access Control");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeading.setForeground(new Color(15, 23, 42));
        toolbar.add(lblHeading, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnAddCashier = new JButton("+ Register Cashier");
        btnAddCashier.setBackground(new Color(13, 148, 136));
        btnAddCashier.setForeground(Color.WHITE);
        btnAddCashier.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddCashier.putClientProperty(FlatClientProperties.STYLE, "arc: 8; hoverBackground: #0F766E;");
        btnAddCashier.addActionListener(e -> handleAddCashier());
        rightActions.add(btnAddCashier);

        JButton btnResetPass = new JButton("Reset Password");
        btnResetPass.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnResetPass.addActionListener(e -> handleResetPassword());
        rightActions.add(btnResetPass);

        JButton btnDeleteUser = new JButton("Remove Account");
        btnDeleteUser.setForeground(new Color(220, 38, 38));
        btnDeleteUser.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnDeleteUser.addActionListener(e -> handleDeleteUser());
        rightActions.add(btnDeleteUser);

        JButton btnRefreshUsers = new JButton("Refresh");
        btnRefreshUsers.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnRefreshUsers.addActionListener(e -> reloadUsers());
        rightActions.add(btnRefreshUsers);

        toolbar.add(rightActions, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        // Users JTable
        String[] cols = {"User ID", "System Username", "Staff Full Name", "Assigned Role", "Registration Timestamp"};
        userTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                return String.class;
            }
        };

        userTable = new JTable(userTableModel);
        userTable.setRowHeight(32);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        userTable.getTableHeader().setBackground(new Color(241, 245, 249));
        userTable.getTableHeader().setForeground(new Color(51, 65, 85));

        userTable.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        userTable.getColumnModel().getColumn(0).setCellRenderer(rightAlign);
        userTable.getColumnModel().getColumn(0).setPreferredWidth(60);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void handleAddCashier() {
        UserFormDialog dialog = new UserFormDialog(this, userDAO);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reloadUsers();
            JOptionPane.showMessageDialog(this, "New Cashier registered successfully. They can now log in at POS.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleResetPassword() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account to reset password.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) userTableModel.getValueAt(selectedRow, 0);
        String username = (String) userTableModel.getValueAt(selectedRow, 1);

        String newPass = JOptionPane.showInputDialog(
                this,
                "Enter new password for user '" + username + "':",
                "Reset Password",
                JOptionPane.PLAIN_MESSAGE
        );

        if (newPass != null && !newPass.trim().isEmpty()) {
            if (newPass.trim().length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean ok = userDAO.updatePassword(userId, newPass.trim());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Password for '" + username + "' updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) userTableModel.getValueAt(selectedRow, 0);
        String username = (String) userTableModel.getValueAt(selectedRow, 1);
        String role = (String) userTableModel.getValueAt(selectedRow, 3);

        if ("Admin".equalsIgnoreCase(role)) {
            JOptionPane.showMessageDialog(this, "System Administrator accounts cannot be deleted.", "Security Guard", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to deactivate and remove cashier account:\n" + username + " (ID: " + userId + ")?",
                "Confirm Account Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = userDAO.deleteUser(userId);
            if (ok) {
                reloadUsers();
                JOptionPane.showMessageDialog(this, "Cashier account removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove account.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void reloadUsers() {
        userTableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            userTableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getRole(),
                    u.getCreatedAt() != null ? u.getCreatedAt().toString() : "System Default"
            });
        }
    }

    // =========================================================================
    // TAB 4: FINANCIAL & INVENTORY INSIGHTS / REPORTS SUMMARY PANEL
    // =========================================================================

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        // Top Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Pharmacy Operational Summary & Restock Advisory");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(15, 23, 42));
        topHeader.add(lblTitle, BorderLayout.WEST);

        JButton btnRefreshInsights = new JButton("Recalculate Insights");
        btnRefreshInsights.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnRefreshInsights.addActionListener(e -> reloadInsights());
        topHeader.add(btnRefreshInsights, BorderLayout.EAST);

        panel.add(topHeader, BorderLayout.NORTH);

        // Center Content: Summary Boxes + Restock Advisory Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setOpaque(false);

        // 3 mini summary insight blocks
        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 14, 0));
        cardsRow.setOpaque(false);

        cardsRow.add(createInsightCard("TOTAL INVENTORY VALUE", "$0.00", "Total retail stock valuation", new Color(13, 148, 136), val -> lblTotalValuation = val));
        cardsRow.add(createInsightCard("TOTAL STOCK UNITS", "0", "Aggregated units on shelves", new Color(2, 132, 199), val -> lblTotalUnits = val));
        cardsRow.add(createInsightCard("CRITICAL RESTOCK ITEMS", "0", "Medicines at/below reorder mark", new Color(239, 68, 68), val -> lblItemsToRestock = val));

        centerPanel.add(cardsRow, BorderLayout.NORTH);

        // Restock Needs Table
        JPanel tableSection = new JPanel(new BorderLayout(0, 8));
        tableSection.setOpaque(false);

        JLabel lblSub = new JLabel("⚠ Urgent Restock Attention List (Stock ≤ Reorder Level)");
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSub.setForeground(new Color(180, 83, 9)); // Amber
        tableSection.add(lblSub, BorderLayout.NORTH);

        String[] cols = {"Medicine Name", "Dosage Type", "Current Stock", "Reorder Level", "Deficit", "Preferred Supplier", "Supplier Contact Phone"};
        restockTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        restockTable = new JTable(restockTableModel);
        restockTable.setRowHeight(30);
        restockTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        restockTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        restockTable.getTableHeader().setBackground(new Color(241, 245, 249));

        JScrollPane scroll = new JScrollPane(restockTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        tableSection.add(scroll, BorderLayout.CENTER);

        centerPanel.add(tableSection, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInsightCard(String title, String initialVal, String sub, Color accent, java.util.function.Consumer<JLabel> labelConsumer) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");

        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblT.setForeground(new Color(100, 116, 139));
        card.add(lblT, BorderLayout.NORTH);

        JLabel lblV = new JLabel(initialVal);
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblV.setForeground(accent);
        labelConsumer.accept(lblV);
        card.add(lblV, BorderLayout.CENTER);

        JLabel lblS = new JLabel(sub);
        lblS.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblS.setForeground(new Color(148, 163, 184));
        card.add(lblS, BorderLayout.SOUTH);

        return card;
    }

    public void reloadInsights() {
        List<Medicine> medicines = medicineDAO.getAllMedicines();
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();

        BigDecimal totalValuation = BigDecimal.ZERO;
        int totalUnits = 0;
        int restockCount = 0;

        restockTableModel.setRowCount(0);

        for (Medicine m : medicines) {
            if (m.getPrice() != null) {
                totalValuation = totalValuation.add(m.getPrice().multiply(BigDecimal.valueOf(m.getQuantityInStock())));
            }
            totalUnits += m.getQuantityInStock();

            if (m.getQuantityInStock() <= m.getReorderLevel()) {
                restockCount++;
                int deficit = Math.max(0, m.getReorderLevel() - m.getQuantityInStock());

                String supPhone = "N/A";
                if (m.getSupplierId() != null) {
                    for (Supplier s : suppliers) {
                        if (s.getSupplierId() == m.getSupplierId()) {
                            supPhone = s.getPhone();
                            break;
                        }
                    }
                }

                restockTableModel.addRow(new Object[]{
                        m.getName(),
                        m.getMedicineType(),
                        m.getQuantityInStock(),
                        m.getReorderLevel(),
                        deficit > 0 ? "+" + deficit + " needed" : "At threshold",
                        m.getSupplierName() != null ? m.getSupplierName() : "Unassigned",
                        supPhone
                });
            }
        }

        NumberFormat currFmt = NumberFormat.getCurrencyInstance(Locale.US);
        if (lblTotalValuation != null) lblTotalValuation.setText(currFmt.format(totalValuation));
        if (lblTotalUnits != null) lblTotalUnits.setText(String.format("%,d", totalUnits));
        if (lblItemsToRestock != null) lblItemsToRestock.setText(String.valueOf(restockCount));
    }

    // =========================================================================
    // GLOBAL REFRESH & METRICS
    // =========================================================================

    public void refreshAll() {
        reloadMedicines();
        reloadSuppliers();
        reloadUsers();
        reloadInsights();
        refreshMetrics();
    }

    public void refreshMetrics() {
        int totalMeds = medicineDAO.getTotalCount();
        int lowStock = medicineDAO.getLowStockCount();
        int expiring = medicineDAO.getExpiringCount(30);
        int totalSuppliers = supplierDAO.getAllSuppliers().size();

        if (cardTotalMedicines != null) cardTotalMedicines.setValue(totalMeds);
        if (cardLowStock != null) cardLowStock.setValue(lowStock);
        if (cardExpiring != null) cardExpiring.setValue(expiring);
        if (cardTotalSuppliers != null) cardTotalSuppliers.setValue(totalSuppliers);
    }
}
