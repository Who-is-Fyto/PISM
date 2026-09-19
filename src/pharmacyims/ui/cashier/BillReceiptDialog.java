package pharmacyims.ui.cashier;

import com.formdev.flatlaf.FlatClientProperties;
import pharmacyims.model.Sale;
import pharmacyims.model.SaleItem;
import pharmacyims.session.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Modern modal dialog rendering a formatted pharmacy thermal receipt.
 * Supports on-screen preview and one-click printing via Java Print API.
 */
public class BillReceiptDialog extends JDialog {

    private final Sale sale;
    private final List<SaleItem> items;
    private JTextArea txtReceipt;

    public BillReceiptDialog(Frame parent, Sale sale, List<SaleItem> items) {
        super(parent, "Transaction Receipt — Sale #" + (sale != null ? sale.getSaleId() : ""), true);
        this.sale = sale;
        this.items = items;

        initUI();
    }

    private void initUI() {
        setSize(460, 640);
        setResizable(false);
        setLocationRelativeTo(getParent());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(248, 250, 252));
        root.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header Title
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Transaction Completed");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSubtitle = new JLabel("Receipt ready for printing and customer records.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));

        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        root.add(titlePanel, BorderLayout.NORTH);

        // Receipt Paper Area (Monospace Text Area styled like thermal receipt paper)
        txtReceipt = new JTextArea();
        txtReceipt.setEditable(false);
        txtReceipt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtReceipt.setBackground(Color.WHITE);
        txtReceipt.setForeground(new Color(15, 23, 42));
        txtReceipt.setBorder(new EmptyBorder(14, 16, 14, 16));

        generateReceiptText();

        JScrollPane scrollPane = new JScrollPane(txtReceipt);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(12, 0, 12, 0),
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true)
        ));
        root.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Buttons: Print & Done
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClose.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnClose.addActionListener(e -> dispose());
        btnRow.add(btnClose);

        JButton btnPrint = new JButton("🖨  Print Receipt");
        btnPrint.setBackground(new Color(13, 148, 136)); // Medical Teal
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPrint.putClientProperty(FlatClientProperties.STYLE, "arc: 8; hoverBackground: #0F766E;");
        btnPrint.addActionListener(e -> handlePrint());
        btnRow.add(btnPrint);

        root.add(btnRow, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void generateReceiptText() {
        StringBuilder sb = new StringBuilder();
        NumberFormat cur = NumberFormat.getCurrencyInstance(Locale.US);
        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sale.getSaleDate() != null ? dtf.format(sale.getSaleDate()) : dtf.format(new Date());

        String cashier = sale.getCashierName();
        if (cashier == null || cashier.isEmpty()) {
            cashier = UserSession.getInstance() != null ? UserSession.getInstance().getFullName() : "Cashier Staff";
        }

        String receiptNum = String.format("REC-%s-%04d", new SimpleDateFormat("yyyyMMdd").format(new Date()), sale.getSaleId());

        sb.append("================================================\n");
        sb.append("               FYTO PHARMACY LTD                \n");
        sb.append("          124 Healthcare Parkway, Suite 10      \n");
        sb.append("             Tel: +1 (555) 0199-FYTO            \n");
        sb.append("================================================\n");
        sb.append(String.format("Receipt #: %s\n", receiptNum));
        sb.append(String.format("Date/Time: %s\n", dateStr));
        sb.append(String.format("Cashier:   %s\n", cashier));
        sb.append("------------------------------------------------\n");
        sb.append(String.format("%-22s %4s %9s %10s\n", "Item Description", "Qty", "Price", "Subtotal"));
        sb.append("------------------------------------------------\n");

        for (SaleItem item : items) {
            String name = item.getMedicineName();
            if (name == null || name.isEmpty()) name = "Medicine #" + item.getMedicineId();
            if (name.length() > 22) {
                name = name.substring(0, 19) + "...";
            }

            BigDecimal price = item.getPriceAtSale() != null ? item.getPriceAtSale() : BigDecimal.ZERO;
            BigDecimal subtotal = item.getSubtotal() != null ? item.getSubtotal() : price.multiply(BigDecimal.valueOf(item.getQuantitySold()));

            sb.append(String.format("%-22s %4d %9s %10s\n",
                    name,
                    item.getQuantitySold(),
                    cur.format(price),
                    cur.format(subtotal)
            ));
        }

        sb.append("------------------------------------------------\n");
        sb.append(String.format("TOTAL AMOUNT:                    %15s\n", cur.format(sale.getTotalAmount())));
        sb.append(String.format("AMOUNT TENDERED:                 %15s\n", cur.format(sale.getAmountPaid())));
        sb.append(String.format("CHANGE RETURNED:                 %15s\n", cur.format(sale.getChangeGiven())));
        sb.append("================================================\n");
        sb.append("        Thank you for choosing Fyto PIMS!       \n");
        sb.append("          Please store medicines safely.        \n");
        sb.append("        Questions? Call: +1 (555) 0199-FYTO      \n");
        sb.append("================================================\n");

        txtReceipt.setText(sb.toString());
        txtReceipt.setCaretPosition(0);
    }

    private void handlePrint() {
        try {
            boolean complete = txtReceipt.print(
                    new java.text.MessageFormat("Fyto Pharmacy — Official Customer Receipt"),
                    new java.text.MessageFormat("Page - {0}"),
                    true,
                    null,
                    null,
                    true
            );
            if (complete) {
                JOptionPane.showMessageDialog(this, "Receipt sent to printer successfully.", "Print Status", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage(), "Printing Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
