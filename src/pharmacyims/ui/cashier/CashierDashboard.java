package pharmacyims.ui.cashier;

import com.formdev.flatlaf.FlatClientProperties;
import pharmacyims.dao.MedicineDAO;
import pharmacyims.dao.SaleDAO;
import pharmacyims.model.Medicine;
import pharmacyims.model.Sale;
import pharmacyims.model.SaleItem;
import pharmacyims.session.UserSession;
import pharmacyims.ui.common.CurrencyTableCellRenderer;
import pharmacyims.ui.common.HeaderBar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Cashier point-of-sale dashboard for dispensing medicines and processing checkout.
public class CashierDashboard extends JFrame {

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SaleDAO saleDAO = new SaleDAO();

    private final List<SaleItem> cartItems = new ArrayList<>();
    private final List<Medicine> cartMedicines = new ArrayList<>();

    private StockLookupPanel stockLookupPanel;

    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JButton btnRemoveItem;
    private JButton btnClearCart;

    private JLabel lblGrandTotal;
    private JTextField txtAmountPaid;
    private JLabel lblChangeDue;
    private JButton btnCompleteSale;

    private BigDecimal currentGrandTotal = BigDecimal.ZERO;
    private final NumberFormat currencyFmt = NumberFormat.getCurrencyInstance(Locale.US);

    public CashierDashboard() {
        setTitle("Fyto PIMS — Cashier Point-of-Sale (POS) Dispensing Portal");
        setSize(1240, 780);
        setMinimumSize(new Dimension(1024, 660));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleWindowClose();
            }
        });

        initUI();
        setupShortcuts();
    }

    private void handleWindowClose() {
        if (!cartItems.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "You have " + cartItems.size() + " uncompleted item(s) in your dispensing cart.\n"
                            + "Are you sure you want to exit and abandon this transaction?",
                    "Unsaved Cart Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to exit Fyto PIMS POS?",
                    "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        dispose();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(248, 250, 252));

        HeaderBar headerBar = new HeaderBar(this, "Point-of-Sale (POS) Dispensing Portal");
        rootPanel.add(headerBar, BorderLayout.NORTH);

        // Split view: stock lookup on left, dispensing cart on right
        JPanel workspace = new JPanel(new GridLayout(1, 2, 16, 0));
        workspace.setOpaque(false);
        workspace.setBorder(new EmptyBorder(16, 18, 16, 18));

        stockLookupPanel = new StockLookupPanel(medicineDAO);
        stockLookupPanel.setOnAddToCartListener(this::addItemToCart);
        workspace.add(stockLookupPanel);

        JPanel rightPanel = createCartAndCheckoutPanel();
        workspace.add(rightPanel);

        rootPanel.add(workspace, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    private JPanel createCartAndCheckoutPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        // Header: Active Cart Title + Cart Action Buttons
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JLabel lblHeading = new JLabel("ACTIVE DISPENSING CART");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHeading.setForeground(new Color(100, 116, 139));
        topHeader.add(lblHeading, BorderLayout.WEST);

        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        cartActions.setOpaque(false);

        btnRemoveItem = new JButton("Remove Item (Del)");
        btnRemoveItem.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnRemoveItem.setEnabled(false);
        btnRemoveItem.addActionListener(e -> removeSelectedCartItem());
        cartActions.add(btnRemoveItem);

        btnClearCart = new JButton("Clear Cart");
        btnClearCart.setForeground(new Color(220, 38, 38));
        btnClearCart.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnClearCart.setEnabled(false);
        btnClearCart.addActionListener(e -> clearCart());
        cartActions.add(btnClearCart);

        topHeader.add(cartActions, BorderLayout.EAST);
        panel.add(topHeader, BorderLayout.NORTH);

        // Center: Cart JTable
        String[] cols = {"#", "Medicine Name", "Dosage Form", "Unit Price", "Qty", "Subtotal"};
        cartTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                if (col == 0 || col == 4) return Integer.class;
                if (col == 3 || col == 5) return BigDecimal.class;
                return String.class;
            }
        };

        cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(32);
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartTable.getTableHeader().setBackground(new Color(241, 245, 249));

        cartTable.getColumnModel().getColumn(3).setCellRenderer(new CurrencyTableCellRenderer());
        cartTable.getColumnModel().getColumn(5).setCellRenderer(new CurrencyTableCellRenderer());

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        cartTable.getColumnModel().getColumn(0).setCellRenderer(rightAlign);
        cartTable.getColumnModel().getColumn(4).setCellRenderer(rightAlign);

        cartTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(55);
        cartTable.getColumnModel().getColumn(5).setPreferredWidth(95);

        cartTable.getSelectionModel().addListSelectionListener(e -> {
            btnRemoveItem.setEnabled(cartTable.getSelectedRow() != -1);
        });

        JScrollPane scrollCart = new JScrollPane(cartTable);
        scrollCart.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        panel.add(scrollCart, BorderLayout.CENTER);

        // Bottom: Bill Summary & Checkout
        JPanel summaryPanel = createPaymentSummaryPanel();
        panel.add(summaryPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPaymentSummaryPanel() {
        JPanel summary = new JPanel(new BorderLayout(0, 12));
        summary.setBackground(new Color(248, 250, 252));
        summary.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        summary.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");

        // Top Row: Grand Total Display
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);

        JLabel lblTotalLabel = new JLabel("GRAND TOTAL");
        lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalLabel.setForeground(new Color(100, 116, 139));
        totalRow.add(lblTotalLabel, BorderLayout.WEST);

        lblGrandTotal = new JLabel("$0.00");
        lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblGrandTotal.setForeground(new Color(15, 23, 42)); // Slate dark
        totalRow.add(lblGrandTotal, BorderLayout.EAST);

        summary.add(totalRow, BorderLayout.NORTH);

        // Middle Row: Amount Paid input & Change Due display
        JPanel paymentGrid = new JPanel(new GridLayout(2, 2, 12, 8));
        paymentGrid.setOpaque(false);

        JLabel lblPaidPrompt = new JLabel("Amount Tendered ($):");
        lblPaidPrompt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPaidPrompt.setForeground(new Color(51, 65, 85));

        txtAmountPaid = new JTextField();
        txtAmountPaid.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtAmountPaid.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "0.00");
        txtAmountPaid.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        txtAmountPaid.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { calculateChange(); }
            @Override public void removeUpdate(DocumentEvent e) { calculateChange(); }
            @Override public void changedUpdate(DocumentEvent e) { calculateChange(); }
        });

        JLabel lblChangePrompt = new JLabel("Change Due:");
        lblChangePrompt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChangePrompt.setForeground(new Color(51, 65, 85));

        lblChangeDue = new JLabel("$0.00");
        lblChangeDue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblChangeDue.setForeground(new Color(16, 185, 129)); // Emerald

        paymentGrid.add(lblPaidPrompt);
        paymentGrid.add(txtAmountPaid);
        paymentGrid.add(lblChangePrompt);
        paymentGrid.add(lblChangeDue);

        summary.add(paymentGrid, BorderLayout.CENTER);

        // Bottom Action: Complete Sale & Print Bill
        btnCompleteSale = new JButton("✔  Complete Sale & Print Bill (F5)");
        btnCompleteSale.setBackground(new Color(13, 148, 136)); // Medical Teal
        btnCompleteSale.setForeground(Color.WHITE);
        btnCompleteSale.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCompleteSale.setPreferredSize(new Dimension(0, 42));
        btnCompleteSale.putClientProperty(FlatClientProperties.STYLE, "arc: 10; hoverBackground: #0F766E;");
        btnCompleteSale.setEnabled(false);
        btnCompleteSale.addActionListener(e -> handleCheckout());

        summary.add(btnCompleteSale, BorderLayout.SOUTH);
        return summary;
    }

    // Cart management operations
    public void addItemToCart(Medicine med, int quantityToAdd) {
        // Check if medicine is already in cart
        int existingIndex = -1;
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getMedicineId() == med.getMedicineId()) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex != -1) {
            SaleItem existing = cartItems.get(existingIndex);
            int newTotalQty = existing.getQuantitySold() + quantityToAdd;

            if (newTotalQty > med.getQuantityInStock()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cannot add " + quantityToAdd + " more units of '" + med.getName() + "'.\n"
                                + "Cart already contains: " + existing.getQuantitySold() + " units.\n"
                                + "Total available in stock: " + med.getQuantityInStock() + " units.",
                        "Stock Limit Reached",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            existing.setQuantitySold(newTotalQty);
        } else {
            SaleItem item = new SaleItem(med.getMedicineId(), med.getName(), quantityToAdd, med.getPrice());
            cartItems.add(item);
            cartMedicines.add(med);
        }

        refreshCartTable();
    }

    private void removeSelectedCartItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) return;

        cartItems.remove(selectedRow);
        cartMedicines.remove(selectedRow);
        refreshCartTable();
    }

    public void clearCart() {
        if (cartItems.isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear all items in the dispensing cart?",
                "Clear Cart Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            cartItems.clear();
            cartMedicines.clear();
            txtAmountPaid.setText("");
            refreshCartTable();
        }
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        currentGrandTotal = BigDecimal.ZERO;

        for (int i = 0; i < cartItems.size(); i++) {
            SaleItem item = cartItems.get(i);
            Medicine med = cartMedicines.get(i);

            cartTableModel.addRow(new Object[]{
                    i + 1,
                    item.getMedicineName(),
                    med.getMedicineType(),
                    item.getPriceAtSale(),
                    item.getQuantitySold(),
                    item.getSubtotal()
            });

            currentGrandTotal = currentGrandTotal.add(item.getSubtotal());
        }

        lblGrandTotal.setText(currencyFmt.format(currentGrandTotal));
        btnClearCart.setEnabled(!cartItems.isEmpty());
        btnRemoveItem.setEnabled(false);

        calculateChange();
    }

    private void calculateChange() {
        String paidText = txtAmountPaid.getText().trim();
        if (paidText.startsWith("$")) {
            paidText = paidText.substring(1).trim();
        }

        if (cartItems.isEmpty() || currentGrandTotal.compareTo(BigDecimal.ZERO) <= 0) {
            lblChangeDue.setText("$0.00");
            lblChangeDue.setForeground(new Color(100, 116, 139));
            btnCompleteSale.setEnabled(false);
            return;
        }

        if (paidText.isEmpty()) {
            lblChangeDue.setText("Enter payment");
            lblChangeDue.setForeground(new Color(100, 116, 139));
            btnCompleteSale.setEnabled(false);
            return;
        }

        try {
            BigDecimal paid = new BigDecimal(paidText).setScale(2, RoundingMode.HALF_UP);
            BigDecimal change = paid.subtract(currentGrandTotal);

            if (change.compareTo(BigDecimal.ZERO) >= 0) {
                lblChangeDue.setText(currencyFmt.format(change));
                lblChangeDue.setForeground(new Color(16, 185, 129)); // Emerald Green
                txtAmountPaid.putClientProperty(FlatClientProperties.OUTLINE, null);
                btnCompleteSale.setEnabled(true);
            } else {
                BigDecimal deficit = change.abs();
                lblChangeDue.setText("Short by " + currencyFmt.format(deficit));
                lblChangeDue.setForeground(new Color(220, 38, 38)); // Red
                txtAmountPaid.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_WARNING);
                btnCompleteSale.setEnabled(false);
            }
        } catch (NumberFormatException e) {
            lblChangeDue.setText("Invalid amount");
            lblChangeDue.setForeground(new Color(220, 38, 38));
            txtAmountPaid.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            btnCompleteSale.setEnabled(false);
        }
    }

    // Processes checkout transaction and displays receipt
    public void handleCheckout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty. Add medicines before checkout.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String paidText = txtAmountPaid.getText().trim().replace("$", "").trim();
        BigDecimal amountPaid;
        try {
            amountPaid = new BigDecimal(paidText).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid payment amount.", "Invalid Payment", JOptionPane.WARNING_MESSAGE);
            txtAmountPaid.requestFocusInWindow();
            return;
        }

        if (amountPaid.compareTo(currentGrandTotal) < 0) {
            JOptionPane.showMessageDialog(this, "Amount paid cannot be less than Grand Total.", "Insufficient Payment", JOptionPane.WARNING_MESSAGE);
            txtAmountPaid.requestFocusInWindow();
            return;
        }

        BigDecimal changeGiven = amountPaid.subtract(currentGrandTotal);
        Integer userId = UserSession.getInstance() != null ? UserSession.getInstance().getUserId() : 2;

        Sale sale = new Sale(currentGrandTotal, amountPaid, changeGiven, userId);
        sale.setCashierName(UserSession.getInstance() != null ? UserSession.getInstance().getFullName() : "Jane Doe");

        try {
            // Save sale and line items atomically
            int saleId = saleDAO.processSale(sale, new ArrayList<>(cartItems));
            sale.setSaleId(saleId);

            // Display printable thermal receipt
            BillReceiptDialog receiptDialog = new BillReceiptDialog(this, sale, new ArrayList<>(cartItems));
            receiptDialog.setVisible(true);

            // Reset cart for next customer
            cartItems.clear();
            cartMedicines.clear();
            txtAmountPaid.setText("");
            refreshCartTable();

            stockLookupPanel.reloadMedicines();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Transaction Failed: " + ex.getMessage() + "\nAll changes have been safely rolled back.",
                    "Database Transaction Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Keyboard shortcuts for fast terminal operation (F1 search, F2 add, F5 checkout, Delete remove)
    private void setupShortcuts() {
        JRootPane root = getRootPane();

        root.registerKeyboardAction(e -> stockLookupPanel.focusSearch(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        root.registerKeyboardAction(e -> stockLookupPanel.handleAddToCartAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        root.registerKeyboardAction(e -> {
            if (btnCompleteSale.isEnabled()) {
                handleCheckout();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        root.registerKeyboardAction(e -> {
            if (btnRemoveItem.isEnabled()) {
                removeSelectedCartItem();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public List<SaleItem> getCartItems() {
        return cartItems;
    }

    public BigDecimal getCurrentGrandTotal() {
        return currentGrandTotal;
    }
}
