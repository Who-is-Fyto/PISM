package pharmacyims.ui.cashier;

import com.formdev.flatlaf.FlatClientProperties;
import pharmacyims.dao.MedicineDAO;
import pharmacyims.model.Medicine;
import pharmacyims.ui.common.CurrencyTableCellRenderer;
import pharmacyims.ui.common.StatusBadgeRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

// Product search and inventory lookup panel for dispensing workflow.
public class StockLookupPanel extends JPanel {

    private final MedicineDAO medicineDAO;
    private BiConsumer<Medicine, Integer> onAddToCart;

    private JTextField txtSearch;
    private DefaultTableModel catalogModel;
    private JTable catalogTable;
    private TableRowSorter<DefaultTableModel> catalogSorter;

    // Selected product details view
    private Medicine selectedMedicine;
    private JLabel lblSelectedName;
    private JLabel lblSelectedCompany;
    private JLabel lblSelectedType;
    private JLabel lblSelectedPrice;
    private JLabel lblSelectedStock;
    private JLabel lblSelectedExpiry;
    private JLabel lblStockBadge;

    private JSpinner spinQuantity;
    private SpinnerNumberModel spinModel;
    private JButton btnAddToCart;

    public StockLookupPanel(MedicineDAO dao) {
        this.medicineDAO = dao;
        initUI();
        reloadMedicines();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        // Top search bar
        JPanel topSection = new JPanel(new BorderLayout(8, 6));
        topSection.setOpaque(false);

        JLabel lblSearchHeading = new JLabel("PRODUCT SEARCH & DISPENSING LOOKUP");
        lblSearchHeading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSearchHeading.setForeground(new Color(100, 116, 139));
        topSection.add(lblSearchHeading, BorderLayout.NORTH);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search medicine by name or manufacturer (F1)...");
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applySearchFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applySearchFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applySearchFilter(); }
        });
        topSection.add(txtSearch, BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);

        // Catalog table showing available products and stock
        String[] cols = {"ID", "Medicine Name", "Type", "Unit Price", "Stock", "Status"};
        catalogModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                if (col == 0 || col == 4) return Integer.class;
                if (col == 3) return BigDecimal.class;
                return String.class;
            }
        };

        catalogTable = new JTable(catalogModel);
        catalogTable.setRowHeight(30);
        catalogTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        catalogTable.getTableHeader().setBackground(new Color(241, 245, 249));

        catalogSorter = new TableRowSorter<>(catalogModel);
        catalogTable.setRowSorter(catalogSorter);

        catalogTable.getColumnModel().getColumn(3).setCellRenderer(new CurrencyTableCellRenderer());
        catalogTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        catalogTable.getColumnModel().getColumn(0).setCellRenderer(rightAlign);
        catalogTable.getColumnModel().getColumn(4).setCellRenderer(rightAlign);

        catalogTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        catalogTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        catalogTable.getColumnModel().getColumn(2).setPreferredWidth(75);
        catalogTable.getColumnModel().getColumn(3).setPreferredWidth(75);
        catalogTable.getColumnModel().getColumn(4).setPreferredWidth(55);
        catalogTable.getColumnModel().getColumn(5).setPreferredWidth(85);

        catalogTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection();
            }
        });

        JScrollPane scrollTable = new JScrollPane(catalogTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        add(scrollTable, BorderLayout.CENTER);

        // Bottom details preview and quantity controls
        JPanel bottomSection = new JPanel(new BorderLayout(0, 10));
        bottomSection.setOpaque(false);

        JPanel detailsCard = createDetailsCard();
        bottomSection.add(detailsCard, BorderLayout.CENTER);

        JPanel actionRow = createActionRow();
        bottomSection.add(actionRow, BorderLayout.SOUTH);

        add(bottomSection, BorderLayout.SOUTH);
    }

    private JPanel createDetailsCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");

        // Top Row: Drug Name + Status Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        lblSelectedName = new JLabel("Select a medicine above");
        lblSelectedName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSelectedName.setForeground(new Color(15, 23, 42));
        topRow.add(lblSelectedName, BorderLayout.WEST);

        lblStockBadge = new JLabel(" ");
        lblStockBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStockBadge.setOpaque(true);
        lblStockBadge.setBorder(new EmptyBorder(2, 8, 2, 8));
        lblStockBadge.putClientProperty(FlatClientProperties.STYLE, "arc: 999;");
        lblStockBadge.setVisible(false);
        topRow.add(lblStockBadge, BorderLayout.EAST);

        card.add(topRow, BorderLayout.NORTH);

        // Grid of 4 attributes: Manufacturer, Type, Price, Expiry
        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 6));
        grid.setOpaque(false);

        lblSelectedCompany = new JLabel("Company: —");
        lblSelectedCompany.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSelectedCompany.setForeground(new Color(71, 85, 105));

        lblSelectedType = new JLabel("Form: —");
        lblSelectedType.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSelectedType.setForeground(new Color(71, 85, 105));

        lblSelectedPrice = new JLabel("Price: $0.00");
        lblSelectedPrice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSelectedPrice.setForeground(new Color(15, 23, 42));

        lblSelectedExpiry = new JLabel("Expiry: —");
        lblSelectedExpiry.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSelectedExpiry.setForeground(new Color(71, 85, 105));

        grid.add(lblSelectedCompany);
        grid.add(lblSelectedPrice);
        grid.add(lblSelectedType);
        grid.add(lblSelectedExpiry);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        // Quantity controls
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        qtyPanel.setOpaque(false);

        JLabel lblQty = new JLabel("Qty:");
        lblQty.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblQty.setForeground(new Color(51, 65, 85));
        qtyPanel.add(lblQty);

        JButton btnMinus = new JButton("−");
        btnMinus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnMinus.setPreferredSize(new Dimension(34, 34));
        btnMinus.addActionListener(e -> {
            int cur = (int) spinQuantity.getValue();
            if (cur > 1) spinQuantity.setValue(cur - 1);
        });
        qtyPanel.add(btnMinus);

        spinModel = new SpinnerNumberModel(1, 1, 9999, 1);
        spinQuantity = new JSpinner(spinModel);
        spinQuantity.setPreferredSize(new Dimension(65, 34));
        spinQuantity.setFont(new Font("Segoe UI", Font.BOLD, 13));
        spinQuantity.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        qtyPanel.add(spinQuantity);

        JButton btnPlus = new JButton("+");
        btnPlus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPlus.setPreferredSize(new Dimension(34, 34));
        btnPlus.addActionListener(e -> {
            int cur = (int) spinQuantity.getValue();
            if (selectedMedicine != null && cur < selectedMedicine.getQuantityInStock()) {
                spinQuantity.setValue(cur + 1);
            }
        });
        qtyPanel.add(btnPlus);

        row.add(qtyPanel, BorderLayout.WEST);

        // Add to Cart Button
        btnAddToCart = new JButton("+ Add to Cart (F2)");
        btnAddToCart.setBackground(new Color(5, 150, 105)); // Botanical healing green
        btnAddToCart.setForeground(Color.WHITE);
        btnAddToCart.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAddToCart.setPreferredSize(new Dimension(170, 36));
        btnAddToCart.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #047857;");
        btnAddToCart.setEnabled(false);
        btnAddToCart.addActionListener(e -> handleAddToCartAction());
        row.add(btnAddToCart, BorderLayout.EAST);

        return row;
    }

    private void applySearchFilter() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            catalogSorter.setRowFilter(null);
        } else {
            catalogSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(query), 1, 2));
        }
    }

    private void handleTableSelection() {
        int selectedViewRow = catalogTable.getSelectedRow();
        if (selectedViewRow == -1) {
            clearSelectionDetails();
            return;
        }

        int modelRow = catalogTable.convertRowIndexToModel(selectedViewRow);
        int medId = (int) catalogModel.getValueAt(modelRow, 0);
        selectedMedicine = medicineDAO.getMedicineById(medId);

        if (selectedMedicine == null) {
            clearSelectionDetails();
            return;
        }

        NumberFormat cur = NumberFormat.getCurrencyInstance(Locale.US);
        lblSelectedName.setText(selectedMedicine.getName());
        lblSelectedCompany.setText("Company: " + selectedMedicine.getCompany());
        lblSelectedType.setText("Form: " + selectedMedicine.getMedicineType());
        lblSelectedPrice.setText("Price: " + cur.format(selectedMedicine.getPrice()));
        lblSelectedExpiry.setText("Expiry: " + (selectedMedicine.getExpiryDate() != null ? selectedMedicine.getExpiryDate().toString() : "N/A"));

        int stock = selectedMedicine.getQuantityInStock();
        boolean expired = selectedMedicine.isExpired();

        lblStockBadge.setVisible(true);
        if (stock <= 0) {
            lblStockBadge.setText("Out of Stock");
            lblStockBadge.setBackground(new Color(255, 241, 242)); // Soft rose wash
            lblStockBadge.setForeground(new Color(225, 29, 72));
            lblStockBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(254, 205, 211), 1, true),
                    new EmptyBorder(2, 8, 2, 8)
            ));
            btnAddToCart.setEnabled(false);
        } else if (expired) {
            lblStockBadge.setText("Expired");
            lblStockBadge.setBackground(new Color(255, 241, 242)); // Soft rose wash
            lblStockBadge.setForeground(new Color(225, 29, 72));
            lblStockBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(254, 205, 211), 1, true),
                    new EmptyBorder(2, 8, 2, 8)
            ));
            btnAddToCart.setEnabled(false);
        } else if (stock <= selectedMedicine.getReorderLevel()) {
            lblStockBadge.setText("Low Stock (" + stock + ")");
            lblStockBadge.setBackground(new Color(254, 243, 199)); // Calm honey wash
            lblStockBadge.setForeground(new Color(180, 83, 9));
            lblStockBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(253, 230, 138), 1, true),
                    new EmptyBorder(2, 8, 2, 8)
            ));
            btnAddToCart.setEnabled(true);
            spinModel.setMaximum(stock);
            spinQuantity.setValue(1);
        } else {
            lblStockBadge.setText("In Stock (" + stock + ")");
            lblStockBadge.setBackground(new Color(236, 253, 245)); // Healing mint wash
            lblStockBadge.setForeground(new Color(4, 120, 87));
            lblStockBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(167, 243, 208), 1, true),
                    new EmptyBorder(2, 8, 2, 8)
            ));
            btnAddToCart.setEnabled(true);
            spinModel.setMaximum(stock);
            spinQuantity.setValue(1);
        }
    }

    private void clearSelectionDetails() {
        selectedMedicine = null;
        lblSelectedName.setText("Select a medicine above");
        lblSelectedCompany.setText("Company: —");
        lblSelectedType.setText("Form: —");
        lblSelectedPrice.setText("Price: $0.00");
        lblSelectedExpiry.setText("Expiry: —");
        lblStockBadge.setVisible(false);
        btnAddToCart.setEnabled(false);
        spinQuantity.setValue(1);
    }

    public void handleAddToCartAction() {
        if (selectedMedicine == null) {
            JOptionPane.showMessageDialog(this, "Please select a medicine from the list first.", "No Product Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedMedicine.getQuantityInStock() <= 0) {
            JOptionPane.showMessageDialog(this, "Cannot dispense: This medicine is Out of Stock.", "Out of Stock", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedMedicine.isExpired()) {
            JOptionPane.showMessageDialog(this, "Cannot dispense: This medicine has expired.", "Dispense Blocked", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int requestedQty = (int) spinQuantity.getValue();
        if (requestedQty <= 0) {
            JOptionPane.showMessageDialog(this, "Dispense quantity must be at least 1.", "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (requestedQty > selectedMedicine.getQuantityInStock()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Requested quantity (" + requestedQty + ") exceeds available stock (" + selectedMedicine.getQuantityInStock() + ")!",
                    "Stock Overflow",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (onAddToCart != null) {
            onAddToCart.accept(selectedMedicine, requestedQty);
        }
    }

    public void reloadMedicines() {
        int previousSelectedId = selectedMedicine != null ? selectedMedicine.getMedicineId() : -1;

        catalogModel.setRowCount(0);
        List<Medicine> list = medicineDAO.getAllMedicines();
        for (Medicine m : list) {
            String status;
            if (m.getQuantityInStock() <= 0) status = "Out of Stock";
            else if (m.isExpired()) status = "Expired";
            else if (m.isLowStock()) status = "Low Stock";
            else status = "In Stock";

            catalogModel.addRow(new Object[]{
                    m.getMedicineId(),
                    m.getName(),
                    m.getMedicineType(),
                    m.getPrice(),
                    m.getQuantityInStock(),
                    status
            });
        }

        // Re-select previous item if still present
        if (previousSelectedId != -1) {
            for (int i = 0; i < catalogTable.getRowCount(); i++) {
                int modelIdx = catalogTable.convertRowIndexToModel(i);
                if ((int) catalogModel.getValueAt(modelIdx, 0) == previousSelectedId) {
                    catalogTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    public void setOnAddToCartListener(BiConsumer<Medicine, Integer> listener) {
        this.onAddToCart = listener;
    }

    public void focusSearch() {
        txtSearch.requestFocusInWindow();
        txtSearch.selectAll();
    }

    public Medicine getSelectedMedicine() {
        return selectedMedicine;
    }
}
