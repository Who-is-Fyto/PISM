package pharmacyims.ui.reports;

import com.formdev.flatlaf.FlatClientProperties;
import pharmacyims.dao.MedicineDAO;
import pharmacyims.dao.SaleDAO;
import pharmacyims.dao.SupplierDAO;
import pharmacyims.model.Medicine;
import pharmacyims.model.Sale;
import pharmacyims.model.SaleItem;
import pharmacyims.model.Supplier;
import pharmacyims.ui.common.CurrencyTableCellRenderer;
import pharmacyims.ui.common.MetricCard;
import pharmacyims.ui.common.StatusBadgeRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fyto PIMS — Master Business Reporting & Analytics Dashboard.
 * Supports 3 Analytical Streams:
 * 1. Sales Performance & Revenue Analytics
 * 2. Inventory Low Stock & Reorder Surveillance
 * 3. Expiration Date & Risk Analysis
 * Includes instant CSV export and printable audit summaries.
 */
public class ReportsPanel extends JPanel {

    private enum ReportStream {
        SALES,
        LOW_STOCK,
        EXPIRY
    }

    private final SaleDAO saleDAO = new SaleDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    private ReportStream activeStream = ReportStream.SALES;

    // Stream Selector Buttons
    private JButton btnStreamSales;
    private JButton btnStreamLowStock;
    private JButton btnStreamExpiry;

    // Dynamic Filter Controls
    private JPanel filterContainer;
    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JComboBox<String> cmbStockFilter;
    private JComboBox<String> cmbExpiryFilter;

    // 4 Dynamic Metric KPI Cards
    private MetricCard cardMetric1;
    private MetricCard cardMetric2;
    private MetricCard cardMetric3;
    private MetricCard cardMetric4;

    // Main Report Data Table
    private DefaultTableModel reportTableModel;
    private JTable reportTable;
    private TableRowSorter<DefaultTableModel> tableSorter;

    // Footer summary
    private JLabel lblRecordCount;
    private JLabel lblGrandSummary;

    private final NumberFormat currFmt = NumberFormat.getCurrencyInstance(Locale.US);

    public ReportsPanel() {
        initUI();
        loadCurrentStream();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        // 1. Top Section: Stream Selector Pill Bar + Filter Bar
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);

        // Stream Selector Bar
        JPanel streamBar = createStreamSelectorBar();
        topContainer.add(streamBar, BorderLayout.NORTH);

        // Filter Bar (Dynamically populated based on active stream)
        filterContainer = new JPanel(new BorderLayout(12, 0));
        filterContainer.setOpaque(false);
        topContainer.add(filterContainer, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // 2. Center Section: 4 KPI Cards + Report JTable
        JPanel centerContainer = new JPanel(new BorderLayout(0, 14));
        centerContainer.setOpaque(false);

        // 4 KPI Summary Cards Row
        JPanel cardsRow = createMetricsRow();
        centerContainer.add(cardsRow, BorderLayout.NORTH);

        // Data Table
        JPanel tablePanel = createTablePanel();
        centerContainer.add(tablePanel, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);

        // 3. Bottom Footer Summary Row
        JPanel footerRow = new JPanel(new BorderLayout());
        footerRow.setOpaque(false);
        footerRow.setBorder(new EmptyBorder(6, 4, 2, 4));

        lblRecordCount = new JLabel("Total: 0 records");
        lblRecordCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRecordCount.setForeground(new Color(100, 116, 139));
        footerRow.add(lblRecordCount, BorderLayout.WEST);

        lblGrandSummary = new JLabel("Grand Summary: $0.00");
        lblGrandSummary.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGrandSummary.setForeground(new Color(15, 23, 42));
        footerRow.add(lblGrandSummary, BorderLayout.EAST);

        add(footerRow, BorderLayout.SOUTH);
    }

    private JPanel createStreamSelectorBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);

        btnStreamSales = createStreamButton("📊  Sales Performance & Revenue", ReportStream.SALES);
        btnStreamLowStock = createStreamButton("⚠  Low Stock & Reorder Surveillance", ReportStream.LOW_STOCK);
        btnStreamExpiry = createStreamButton("⏳  Expiration Risk Analysis", ReportStream.EXPIRY);

        bar.add(btnStreamSales);
        bar.add(btnStreamLowStock);
        bar.add(btnStreamExpiry);

        updateStreamButtonStyles();
        return bar;
    }

    private JButton createStreamButton(String title, ReportStream stream) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setPreferredSize(new Dimension(245, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btn.addActionListener(e -> {
            activeStream = stream;
            updateStreamButtonStyles();
            rebuildFilterBar();
            loadCurrentStream();
        });
        return btn;
    }

    private void updateStreamButtonStyles() {
        applyButtonStyle(btnStreamSales, activeStream == ReportStream.SALES);
        applyButtonStyle(btnStreamLowStock, activeStream == ReportStream.LOW_STOCK);
        applyButtonStyle(btnStreamExpiry, activeStream == ReportStream.EXPIRY);
    }

    private void applyButtonStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(new Color(13, 148, 136)); // Medical Teal
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.putClientProperty(FlatClientProperties.STYLE, "arc: 8; hoverBackground: #0F766E;");
        } else {
            btn.setBackground(new Color(241, 245, 249)); // Soft slate
            btn.setForeground(new Color(51, 65, 85));
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            btn.putClientProperty(FlatClientProperties.STYLE, "arc: 8; hoverBackground: #E2E8F0;");
        }
    }

    private JPanel createMetricsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);

        cardMetric1 = new MetricCard("TOTAL REVENUE", "$0.00", "Gross dispensing volume", MetricCard.CardTheme.SUCCESS);
        cardMetric2 = new MetricCard("TRANSACTIONS", "0", "Completed bills", MetricCard.CardTheme.PRIMARY);
        cardMetric3 = new MetricCard("AVERAGE BASKET", "$0.00", "Revenue per sale", MetricCard.CardTheme.WARNING);
        cardMetric4 = new MetricCard("DISPENSED UNITS", "0", "Total medicines sold", MetricCard.CardTheme.PRIMARY);

        row.add(cardMetric1);
        row.add(cardMetric2);
        row.add(cardMetric3);
        row.add(cardMetric4);

        return row;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        reportTableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        reportTable = new JTable(reportTableModel);
        reportTable.setRowHeight(32);
        reportTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        reportTable.getTableHeader().setBackground(new Color(241, 245, 249));

        tableSorter = new TableRowSorter<>(reportTableModel);
        reportTable.setRowSorter(tableSorter);

        // Double-click handler for Sales stream: opens line item details
        reportTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && activeStream == ReportStream.SALES && reportTable.getSelectedRow() != -1) {
                    showSaleLineItemsDialog();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(reportTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // DYNAMIC FILTER BAR
    // =========================================================================

    private void rebuildFilterBar() {
        filterContainer.removeAll();

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftFilters.setOpaque(false);

        if (activeStream == ReportStream.SALES) {
            // Preset Buttons: Today, 7 Days, This Month, All Time
            JButton btnToday = createPresetButton("Today", () -> setSalesDateRange(LocalDate.now(), LocalDate.now()));
            JButton btn7Days = createPresetButton("Past 7 Days", () -> setSalesDateRange(LocalDate.now().minusDays(7), LocalDate.now()));
            JButton btnMonth = createPresetButton("This Month", () -> setSalesDateRange(LocalDate.now().withDayOfMonth(1), LocalDate.now()));
            JButton btnAllTime = createPresetButton("All Time", () -> setSalesDateRange(LocalDate.of(2020, 1, 1), LocalDate.now().plusYears(1)));

            leftFilters.add(btnToday);
            leftFilters.add(btn7Days);
            leftFilters.add(btnMonth);
            leftFilters.add(btnAllTime);

            JLabel lblFrom = new JLabel("From:");
            lblFrom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            leftFilters.add(lblFrom);

            txtStartDate = new JTextField(LocalDate.now().minusDays(7).toString(), 8);
            txtStartDate.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
            leftFilters.add(txtStartDate);

            JLabel lblTo = new JLabel("To:");
            lblTo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            leftFilters.add(lblTo);

            txtEndDate = new JTextField(LocalDate.now().toString(), 8);
            txtEndDate.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
            leftFilters.add(txtEndDate);

            JButton btnApply = new JButton("Apply Range");
            btnApply.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
            btnApply.addActionListener(e -> loadSalesDataFromCustomRange());
            leftFilters.add(btnApply);

        } else if (activeStream == ReportStream.LOW_STOCK) {
            JLabel lblFilter = new JLabel("Surveillance Filter:");
            lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
            leftFilters.add(lblFilter);

            String[] options = {"All Deficit Items (Stock ≤ Reorder)", "Out of Stock Only (0 units)", "Critically Low (Stock ≤ 5)"};
            cmbStockFilter = new JComboBox<>(options);
            cmbStockFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
            cmbStockFilter.addActionListener(e -> loadLowStockData());
            leftFilters.add(cmbStockFilter);

        } else if (activeStream == ReportStream.EXPIRY) {
            JLabel lblFilter = new JLabel("Expiration Window:");
            lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
            leftFilters.add(lblFilter);

            String[] options = {"Critical & Expired (≤ 30 Days)", "Medium Risk Window (≤ 90 Days)", "Expired Products Only", "All Catalog Expiries"};
            cmbExpiryFilter = new JComboBox<>(options);
            cmbExpiryFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
            cmbExpiryFilter.addActionListener(e -> loadExpiryData());
            leftFilters.add(cmbExpiryFilter);
        }

        filterContainer.add(leftFilters, BorderLayout.WEST);

        // Right Actions: Export CSV, Print, Refresh
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnExportCSV = new JButton("📥  Export to CSV");
        btnExportCSV.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExportCSV.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnExportCSV.addActionListener(e -> handleExportCSV());
        rightActions.add(btnExportCSV);

        JButton btnPrint = new JButton("🖨  Print Report");
        btnPrint.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnPrint.addActionListener(e -> handlePrintReport());
        rightActions.add(btnPrint);

        JButton btnRefresh = new JButton("🔄  Refresh");
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnRefresh.addActionListener(e -> loadCurrentStream());
        rightActions.add(btnRefresh);

        filterContainer.add(rightActions, BorderLayout.EAST);

        filterContainer.revalidate();
        filterContainer.repaint();
    }

    private JButton createPresetButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 6;");
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void setSalesDateRange(LocalDate start, LocalDate end) {
        if (txtStartDate != null) txtStartDate.setText(start.toString());
        if (txtEndDate != null) txtEndDate.setText(end.toString());
        loadSalesData(start, end);
    }

    private void loadSalesDataFromCustomRange() {
        try {
            LocalDate start = LocalDate.parse(txtStartDate.getText().trim());
            LocalDate end = LocalDate.parse(txtEndDate.getText().trim());
            if (start.isAfter(end)) {
                JOptionPane.showMessageDialog(this, "Start Date cannot be after End Date.", "Date Order Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            loadSalesData(start, end);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid dates in YYYY-MM-DD format.", "Date Format Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    // =========================================================================
    // STREAM DATA LOADERS
    // =========================================================================

    public void loadCurrentStream() {
        rebuildFilterBar();
        if (activeStream == ReportStream.SALES) {
            LocalDate start = LocalDate.now().minusDays(7);
            LocalDate end = LocalDate.now();
            if (txtStartDate != null && txtEndDate != null) {
                try {
                    start = LocalDate.parse(txtStartDate.getText().trim());
                    end = LocalDate.parse(txtEndDate.getText().trim());
                } catch (Exception ignored) {}
            }
            loadSalesData(start, end);
        } else if (activeStream == ReportStream.LOW_STOCK) {
            loadLowStockData();
        } else if (activeStream == ReportStream.EXPIRY) {
            loadExpiryData();
        }
    }

    /**
     * STREAM 1: Sales Performance & Revenue Analytics
     */
    private void loadSalesData(LocalDate start, LocalDate end) {
        List<Sale> sales = saleDAO.getSalesByDateRange(start, end);

        String[] columns = {"Sale ID", "Date & Time", "Cashier Name", "Line Items", "Amount Tendered", "Change Given", "Total Revenue"};
        reportTableModel.setDataVector(new Object[0][0], columns);
        tableSorter.setModel(reportTableModel);

        // Styling renderers
        reportTable.getColumnModel().getColumn(4).setCellRenderer(new CurrencyTableCellRenderer());
        reportTable.getColumnModel().getColumn(5).setCellRenderer(new CurrencyTableCellRenderer());
        reportTable.getColumnModel().getColumn(6).setCellRenderer(new CurrencyTableCellRenderer());

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        reportTable.getColumnModel().getColumn(0).setCellRenderer(rightAlign);
        reportTable.getColumnModel().getColumn(3).setCellRenderer(rightAlign);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalItemsSold = 0;
        Set<String> cashiers = new HashSet<>();

        for (Sale s : sales) {
            List<SaleItem> items = saleDAO.getSaleItems(s.getSaleId());
            int itemCount = 0;
            for (SaleItem it : items) {
                itemCount += it.getQuantitySold();
            }
            totalItemsSold += itemCount;

            if (s.getTotalAmount() != null) {
                totalRevenue = totalRevenue.add(s.getTotalAmount());
            }
            if (s.getCashierName() != null) {
                cashiers.add(s.getCashierName());
            }

            reportTableModel.addRow(new Object[]{
                    s.getSaleId(),
                    s.getSaleDate() != null ? s.getSaleDate().toString() : "N/A",
                    s.getCashierName() != null ? s.getCashierName() : "Cashier Staff",
                    itemCount + " units",
                    s.getAmountPaid(),
                    s.getChangeGiven(),
                    s.getTotalAmount()
            });
        }

        // Compute Averages
        BigDecimal avgBasket = BigDecimal.ZERO;
        if (!sales.isEmpty()) {
            avgBasket = totalRevenue.divide(BigDecimal.valueOf(sales.size()), 2, RoundingMode.HALF_UP);
        }

        // Update KPI Cards
        cardMetric1.setValue(currFmt.format(totalRevenue));
        cardMetric1.setSubtitle("Total sales volume in window");

        cardMetric2.setValue(String.valueOf(sales.size()));
        cardMetric2.setSubtitle("Completed customer transactions");

        cardMetric3.setValue(currFmt.format(avgBasket));
        cardMetric3.setSubtitle("Average revenue per ticket");

        cardMetric4.setValue(String.valueOf(totalItemsSold));
        cardMetric4.setSubtitle(cashiers.size() + " active cashiers logged");

        lblRecordCount.setText("Total: " + sales.size() + " sales transactions (Double-click a row to inspect line items)");
        lblGrandSummary.setText("Total Revenue: " + currFmt.format(totalRevenue));
    }

    /**
     * STREAM 2: Low Stock & Reorder Surveillance
     */
    private void loadLowStockData() {
        List<Medicine> allMeds = medicineDAO.getAllMedicines();
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();

        String filterChoice = cmbStockFilter != null ? (String) cmbStockFilter.getSelectedItem() : "All Deficit Items";

        String[] columns = {"ID", "Medicine Name", "Dosage Form", "Current Stock", "Reorder Level", "Deficit", "Est. Restock Cost", "Preferred Supplier", "Supplier Phone", "Status"};
        reportTableModel.setDataVector(new Object[0][0], columns);
        tableSorter.setModel(reportTableModel);

        reportTable.getColumnModel().getColumn(6).setCellRenderer(new CurrencyTableCellRenderer());
        reportTable.getColumnModel().getColumn(9).setCellRenderer(new StatusBadgeRenderer());

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        reportTable.getColumnModel().getColumn(0).setCellRenderer(rightAlign);
        reportTable.getColumnModel().getColumn(3).setCellRenderer(rightAlign);
        reportTable.getColumnModel().getColumn(4).setCellRenderer(rightAlign);
        reportTable.getColumnModel().getColumn(5).setCellRenderer(rightAlign);

        int criticalCount = 0;
        int outOfStockCount = 0;
        BigDecimal totalEstCost = BigDecimal.ZERO;
        Set<Integer> supplierIds = new HashSet<>();

        for (Medicine m : allMeds) {
            boolean include = false;
            if ("Out of Stock Only (0 units)".equals(filterChoice)) {
                include = (m.getQuantityInStock() == 0);
            } else if ("Critically Low (Stock ≤ 5)".equals(filterChoice)) {
                include = (m.getQuantityInStock() <= 5);
            } else {
                include = (m.getQuantityInStock() <= m.getReorderLevel());
            }

            if (!include) continue;

            criticalCount++;
            if (m.getQuantityInStock() == 0) outOfStockCount++;
            if (m.getSupplierId() != null) supplierIds.add(m.getSupplierId());

            int deficit = Math.max(0, m.getReorderLevel() - m.getQuantityInStock());
            int reorderQuantity = Math.max(deficit, m.getReorderLevel() * 2 - m.getQuantityInStock());
            BigDecimal itemCost = m.getPrice() != null ? m.getPrice().multiply(BigDecimal.valueOf(reorderQuantity)) : BigDecimal.ZERO;
            totalEstCost = totalEstCost.add(itemCost);

            String supName = "Unassigned";
            String supPhone = "N/A";
            if (m.getSupplierId() != null) {
                for (Supplier s : suppliers) {
                    if (s.getSupplierId() == m.getSupplierId()) {
                        supName = s.getName();
                        supPhone = s.getPhone();
                        break;
                    }
                }
            }

            String status = m.getQuantityInStock() == 0 ? "Out of Stock" : "Low Stock";

            reportTableModel.addRow(new Object[]{
                    m.getMedicineId(),
                    m.getName(),
                    m.getMedicineType(),
                    m.getQuantityInStock(),
                    m.getReorderLevel(),
                    deficit > 0 ? "+" + deficit + " needed" : "At threshold",
                    itemCost,
                    supName,
                    supPhone,
                    status
            });
        }

        // Update KPI Cards
        cardMetric1.setValue(String.valueOf(criticalCount));
        cardMetric1.setSubtitle("Medicines below reorder mark");

        cardMetric2.setValue(String.valueOf(outOfStockCount));
        cardMetric2.setSubtitle("Completely depleted drugs");

        cardMetric3.setValue(currFmt.format(totalEstCost));
        cardMetric3.setSubtitle("Est. procurement capital required");

        cardMetric4.setValue(String.valueOf(supplierIds.size()));
        cardMetric4.setSubtitle("Vendors requiring purchase orders");

        lblRecordCount.setText("Total: " + criticalCount + " medicines require procurement attention");
        lblGrandSummary.setText("Total Estimated Restock Capital: " + currFmt.format(totalEstCost));
    }

    /**
     * STREAM 3: Expiration Date & Risk Analysis
     */
    private void loadExpiryData() {
        List<Medicine> allMeds = medicineDAO.getAllMedicines();
        LocalDate today = LocalDate.now();

        String filterChoice = cmbExpiryFilter != null ? (String) cmbExpiryFilter.getSelectedItem() : "Critical & Expired";

        String[] columns = {"ID", "Medicine Name", "Manufacturer", "Stock Units", "Unit Value", "Total Value at Risk", "Expiry Date", "Days Remaining", "Risk Level"};
        reportTableModel.setDataVector(new Object[0][0], columns);
        tableSorter.setModel(reportTableModel);

        reportTable.getColumnModel().getColumn(4).setCellRenderer(new CurrencyTableCellRenderer());
        reportTable.getColumnModel().getColumn(5).setCellRenderer(new CurrencyTableCellRenderer());
        reportTable.getColumnModel().getColumn(8).setCellRenderer(new StatusBadgeRenderer());

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        reportTable.getColumnModel().getColumn(0).setCellRenderer(rightAlign);
        reportTable.getColumnModel().getColumn(3).setCellRenderer(rightAlign);
        reportTable.getColumnModel().getColumn(7).setCellRenderer(rightAlign);

        int atRiskCount = 0;
        int expiredCount = 0;
        int totalUnitsAtRisk = 0;
        BigDecimal totalValueAtRisk = BigDecimal.ZERO;

        for (Medicine m : allMeds) {
            if (m.getExpiryDate() == null) continue;
            LocalDate exp = m.getExpiryDate().toLocalDate();
            long daysLeft = ChronoUnit.DAYS.between(today, exp);

            boolean include = false;
            String riskLevel;

            if (daysLeft < 0) {
                riskLevel = "Expired";
                expiredCount++;
            } else if (daysLeft <= 30) {
                riskLevel = "Critical";
            } else if (daysLeft <= 90) {
                riskLevel = "Warning";
            } else {
                riskLevel = "Good";
            }

            if ("Expired Products Only".equals(filterChoice)) {
                include = (daysLeft < 0);
            } else if ("Critical & Expired (≤ 30 Days)".equals(filterChoice)) {
                include = (daysLeft <= 30);
            } else if ("Medium Risk Window (≤ 90 Days)".equals(filterChoice)) {
                include = (daysLeft <= 90);
            } else {
                include = true; // All
            }

            if (!include) continue;

            atRiskCount++;
            totalUnitsAtRisk += m.getQuantityInStock();
            BigDecimal productValAtRisk = m.getPrice() != null ? m.getPrice().multiply(BigDecimal.valueOf(m.getQuantityInStock())) : BigDecimal.ZERO;
            totalValueAtRisk = totalValueAtRisk.add(productValAtRisk);

            reportTableModel.addRow(new Object[]{
                    m.getMedicineId(),
                    m.getName(),
                    m.getCompany(),
                    m.getQuantityInStock(),
                    m.getPrice(),
                    productValAtRisk,
                    exp.toString(),
                    daysLeft < 0 ? Math.abs(daysLeft) + " days ago" : daysLeft + " days",
                    riskLevel
            });
        }

        // Update KPI Cards
        cardMetric1.setValue(String.valueOf(atRiskCount));
        cardMetric1.setSubtitle("Catalog items matching criteria");

        cardMetric2.setValue(String.valueOf(expiredCount));
        cardMetric2.setSubtitle("Already past expiration threshold");

        cardMetric3.setValue(currFmt.format(totalValueAtRisk));
        cardMetric3.setSubtitle("Total inventory value at risk");

        cardMetric4.setValue(String.valueOf(totalUnitsAtRisk));
        cardMetric4.setSubtitle("Physical units expiring in window");

        lblRecordCount.setText("Total: " + atRiskCount + " pharmaceutical products monitored");
        lblGrandSummary.setText("Inventory Capital at Risk: " + currFmt.format(totalValueAtRisk));
    }

    // =========================================================================
    // MODALS & EXPORTS
    // =========================================================================

    private void showSaleLineItemsDialog() {
        int selectedViewRow = reportTable.getSelectedRow();
        if (selectedViewRow == -1) return;

        int modelRow = reportTable.convertRowIndexToModel(selectedViewRow);
        int saleId = (int) reportTableModel.getValueAt(modelRow, 0);
        String saleDate = (String) reportTableModel.getValueAt(modelRow, 1);
        String cashier = (String) reportTableModel.getValueAt(modelRow, 2);

        List<SaleItem> items = saleDAO.getSaleItems(saleId);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Audit Breakdown — Sale #" + saleId, true);
        dialog.setSize(540, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        JLabel title = new JLabel("Transaction Details: Sale #" + saleId);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel sub = new JLabel("Processed on: " + saleDate + " • Cashier: " + cashier);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(100, 116, 139));
        header.add(title);
        header.add(sub);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Item #", "Medicine Name", "Quantity Sold", "Unit Price", "Line Subtotal"};
        DefaultTableModel itemsModel = new DefaultTableModel(cols, 0);
        JTable itemsTable = new JTable(itemsModel);
        itemsTable.setRowHeight(28);
        itemsTable.getColumnModel().getColumn(3).setCellRenderer(new CurrencyTableCellRenderer());
        itemsTable.getColumnModel().getColumn(4).setCellRenderer(new CurrencyTableCellRenderer());

        for (int i = 0; i < items.size(); i++) {
            SaleItem it = items.get(i);
            itemsModel.addRow(new Object[]{
                    i + 1,
                    it.getMedicineName() != null ? it.getMedicineName() : "Medicine ID " + it.getMedicineId(),
                    it.getQuantitySold(),
                    it.getPriceAtSale(),
                    it.getSubtotal()
            });
        }

        JScrollPane scroll = new JScrollPane(itemsTable);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnClose = new JButton("Close");
        btnClose.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        btnRow.add(btnClose);
        panel.add(btnRow, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void handleExportCSV() {
        String baseName = "Fyto_Report_" + activeStream.name().toLowerCase();
        CSVExporter.exportTableToCSV(this, reportTable, baseName);
    }

    private void handlePrintReport() {
        if (reportTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data available in the current report to print.", "Empty Report", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = "Fyto Pharmacy — " + (activeStream == ReportStream.SALES ? "Sales Performance Report" :
                activeStream == ReportStream.LOW_STOCK ? "Inventory Reorder Surveillance Report" : "Expiration Risk Report");

        try {
            boolean complete = reportTable.print(
                    JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat(title),
                    new MessageFormat("Page {0} • Generated on " + LocalDate.now()),
                    true,
                    null,
                    true
            );
            if (complete) {
                JOptionPane.showMessageDialog(this, "Report printed successfully.", "Print Finished", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public DefaultTableModel getReportTableModel() {
        return reportTableModel;
    }

    public int getRecordCount() {
        return reportTableModel.getRowCount();
    }
}
