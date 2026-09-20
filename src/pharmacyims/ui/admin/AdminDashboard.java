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
import pharmacyims.ui.reports.ReportsPanel;
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

// Administrator dashboard for managing inventory, suppliers, users, and reports.
public class AdminDashboard extends JFrame {

    private static final String CARD_MEDICINES = "MEDICINES";
    private static final String CARD_SUPPLIERS = "SUPPLIERS";
    private static final String CARD_USERS = "USERS";
    private static final String CARD_REPORTS = "REPORTS";

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final UserDAO userDAO = new UserDAO();
    private final SaleDAO saleDAO = new SaleDAO();

    private MetricCard cardTotalMedicines;
    private MetricCard cardLowStock;
    private MetricCard cardExpiring;
    private MetricCard cardTotalSuppliers;

    private CardLayout cardLayout;
    private JPanel contentCardsPanel;
    private final List<JButton> navButtons = new ArrayList<>();

    private JTextField txtMedSearch;
    private JComboBox<String> cmbMedTypeFilter;
    private DefaultTableModel medTableModel;
    private JTable medTable;
    private TableRowSorter<DefaultTableModel> medSorter;

    private JTextField txtSupSearch;
    private DefaultTableModel supTableModel;
    private JTable supTable;
    private TableRowSorter<DefaultTableModel> supSorter;

    private DefaultTableModel userTableModel;
    private JTable userTable;

    private ReportsPanel reportsPanel;

    public AdminDashboard() {
        setTitle("Fyto PIMS — Administrator Management Portal");
        setSize(1240, 820);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        AdminDashboard.this,
                        "Are you sure you want to exit Fyto PIMS?",
                        "Exit Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });

        initUI();
        refreshAll();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(248, 250, 252));

        HeaderBar headerBar = new HeaderBar(this, "Administrator Management Portal");
        rootPanel.add(headerBar, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel(new BorderLayout(16, 16));
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(16, 18, 16, 18));

        bodyPanel.add(createSidebar(), BorderLayout.WEST);

        // Center workspace with KPI metrics and tab content
        JPanel workspacePanel = new JPanel(new BorderLayout(0, 16));
        workspacePanel.setOpaque(false);
        workspacePanel.add(createMetricsRow(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentCardsPanel = new JPanel(cardLayout);
        contentCardsPanel.setOpaque(false);

        contentCardsPanel.add(createMedicinesPanel(), CARD_MEDICINES);
        contentCardsPanel.add(createSuppliersPanel(), CARD_SUPPLIERS);
        contentCardsPanel.add(createUsersPanel(), CARD_USERS);
        reportsPanel = new ReportsPanel();
        contentCardsPanel.add(reportsPanel, CARD_REPORTS);

        workspacePanel.add(contentCardsPanel, BorderLayout.CENTER);
        bodyPanel.add(workspacePanel, BorderLayout.CENTER);

        rootPanel.add(bodyPanel, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    // Top KPI metric cards showing live inventory and vendor stats
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

    // Navigation sidebar panel
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
        btnOpenPOS.setBackground(new Color(240, 249, 255)); // Serene ice blue
        btnOpenPOS.setForeground(new Color(2, 132, 199));   // Serene cerulean
        btnOpenPOS.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(186, 230, 253), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btnOpenPOS.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #E0F2FE;");
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

        JLabel lblVer = new JLabel("● Workstation Active");
        lblVer.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblVer.setForeground(new Color(5, 150, 105)); // Healing green

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
            btn.setBackground(new Color(236, 253, 245)); // Healing mint wash (#ECFDF5)
            btn.setForeground(new Color(4, 120, 87));     // Botanical healing green (#047857)
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(5, 150, 105)),
                    new EmptyBorder(6, 11, 6, 12)
            ));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(71, 85, 105));
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            btn.setBorder(new EmptyBorder(6, 14, 6, 12));
        }
    }

    // Medicine inventory management tab
    private JPanel createMedicinesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

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

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnAddMed = new JButton("+ Add Medicine");
        btnAddMed.setBackground(new Color(5, 150, 105)); // Botanical healing green
        btnAddMed.setForeground(Color.WHITE);
        btnAddMed.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddMed.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #047857;");
        btnAddMed.addActionListener(e -> handleAddMedicine());
        rightActions.add(btnAddMed);

        JButton btnEditMed = new JButton("Edit Selected");
        btnEditMed.setForeground(new Color(2, 132, 199)); // Serene sky blue
        btnEditMed.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #F0F9FF;");
        btnEditMed.addActionListener(e -> handleEditMedicine());
        rightActions.add(btnEditMed);

        JButton btnDeleteMed = new JButton("Delete");
        btnDeleteMed.setForeground(new Color(225, 29, 72)); // Muted rose
        btnDeleteMed.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #FFF1F2;");
        btnDeleteMed.addActionListener(e -> handleDeleteMedicine());
        rightActions.add(btnDeleteMed);

        JButton btnRefreshMed = new JButton("Refresh");
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

    // Supplier directory management tab
    private JPanel createSuppliersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

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

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnAddSup = new JButton("+ Add Supplier");
        btnAddSup.setBackground(new Color(5, 150, 105)); // Botanical healing green
        btnAddSup.setForeground(Color.WHITE);
        btnAddSup.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddSup.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #047857;");
        btnAddSup.addActionListener(e -> handleAddSupplier());
        rightActions.add(btnAddSup);

        JButton btnEditSup = new JButton("Edit Selected");
        btnEditSup.setForeground(new Color(2, 132, 199)); // Serene sky blue
        btnEditSup.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #F0F9FF;");
        btnEditSup.addActionListener(e -> handleEditSupplier());
        rightActions.add(btnEditSup);

        JButton btnDeleteSup = new JButton("Delete");
        btnDeleteSup.setForeground(new Color(225, 29, 72)); // Muted rose
        btnDeleteSup.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #FFF1F2;");
        btnDeleteSup.addActionListener(e -> handleDeleteSupplier());
        rightActions.add(btnDeleteSup);

        JButton btnRefreshSup = new JButton("Refresh");
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

    // Cashier accounts and access control tab
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel lblHeading = new JLabel("System Personnel & Cashier Access Control");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeading.setForeground(new Color(15, 23, 42));
        toolbar.add(lblHeading, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnAddCashier = new JButton("+ Register Cashier");
        btnAddCashier.setBackground(new Color(5, 150, 105)); // Botanical healing green
        btnAddCashier.setForeground(Color.WHITE);
        btnAddCashier.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddCashier.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #047857;");
        btnAddCashier.addActionListener(e -> handleAddCashier());
        rightActions.add(btnAddCashier);

        JButton btnResetPass = new JButton("Reset Password");
        btnResetPass.setForeground(new Color(2, 132, 199)); // Serene sky blue
        btnResetPass.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #F0F9FF;");
        btnResetPass.addActionListener(e -> handleResetPassword());
        rightActions.add(btnResetPass);

        JButton btnDeleteUser = new JButton("Remove Account");
        btnDeleteUser.setForeground(new Color(225, 29, 72)); // Muted rose
        btnDeleteUser.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #FFF1F2;");
        btnDeleteUser.addActionListener(e -> handleDeleteUser());
        rightActions.add(btnDeleteUser);

        JButton btnRefreshUsers = new JButton("Refresh");
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

    // Refreshes all tabs and metric counters
    public void refreshAll() {
        reloadMedicines();
        reloadSuppliers();
        reloadUsers();
        if (reportsPanel != null) {
            reportsPanel.loadCurrentStream();
        }
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
