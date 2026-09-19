package pharmacyims.ui.admin;

import com.formdev.flatlaf.FlatClientProperties;
import pharmacyims.dao.MedicineDAO;
import pharmacyims.dao.SupplierDAO;
import pharmacyims.model.Medicine;
import pharmacyims.model.Supplier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

// Form dialog for adding and editing medicines.
public class MedicineFormDialog extends JDialog {

    private final MedicineDAO medicineDAO;
    private final SupplierDAO supplierDAO;
    private final Medicine existingMedicine;
    private boolean saved = false;

    private JTextField txtName;
    private JTextField txtCompany;
    private JComboBox<String> cmbType;
    private JTextField txtPrice;
    private JSpinner spinQuantity;
    private JSpinner spinReorderLevel;
    private JTextField txtExpiryDate;
    private JComboBox<Supplier> cmbSupplier;
    private JLabel lblError;

    public MedicineFormDialog(Frame parent, Medicine medicineToEdit, MedicineDAO medDAO, SupplierDAO supDAO) {
        super(parent, medicineToEdit == null ? "Add New Medicine" : "Edit Medicine: " + medicineToEdit.getName(), true);
        this.existingMedicine = medicineToEdit;
        this.medicineDAO = medDAO;
        this.supplierDAO = supDAO;

        initUI();
    }

    private void initUI() {
        setSize(520, 620);
        setResizable(false);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(existingMedicine == null ? "Register New Medicine" : "Update Medicine Details");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSubtitle = new JLabel("Fill in the pharmaceutical details and stock specifications.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Input fields grid
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        formGrid.setBorder(new EmptyBorder(16, 0, 16, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        int row = 0;

        txtName = new JTextField();
        txtName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. Amoxicillin 500mg");
        txtName.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        addField(formGrid, gbc, row++, "Medicine / Brand Name *", txtName);

        txtCompany = new JTextField();
        txtCompany.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. MedPharma Logistics");
        txtCompany.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        addField(formGrid, gbc, row++, "Pharmaceutical Manufacturer *", txtCompany);

        String[] types = {"Tablet", "Capsule", "Syrup", "Injection", "Cream", "Inhaler", "Drops", "Ointment", "Other"};
        cmbType = new JComboBox<>(types);
        cmbType.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        addField(formGrid, gbc, row++, "Dosage Form / Type *", cmbType);

        txtPrice = new JTextField();
        txtPrice.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. 12.50");
        txtPrice.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        addField(formGrid, gbc, row++, "Unit Dispense Price ($) *", txtPrice);

        spinQuantity = new JSpinner(new SpinnerNumberModel(50, 0, 999999, 1));
        spinQuantity.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        addField(formGrid, gbc, row++, "Initial Stock Quantity *", spinQuantity);

        spinReorderLevel = new JSpinner(new SpinnerNumberModel(15, 1, 9999, 1));
        spinReorderLevel.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        addField(formGrid, gbc, row++, "Reorder Warning Level *", spinReorderLevel);

        txtExpiryDate = new JTextField();
        LocalDate defaultExp = LocalDate.now().plusMonths(12);
        txtExpiryDate.setText(defaultExp.toString());
        txtExpiryDate.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "YYYY-MM-DD");
        txtExpiryDate.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        addField(formGrid, gbc, row++, "Expiration Date (YYYY-MM-DD) *", txtExpiryDate);

        cmbSupplier = new JComboBox<>();
        cmbSupplier.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        populateSuppliers();
        addField(formGrid, gbc, row++, "Assigned Supplier *", cmbSupplier);

        mainPanel.add(formGrid, BorderLayout.CENTER);

        // Actions panel
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);

        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblError.setForeground(new Color(220, 38, 38));
        bottomPanel.add(lblError, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        btnCancel.addActionListener(e -> dispose());
        btnRow.add(btnCancel);

        JButton btnSave = new JButton(existingMedicine == null ? "Save Medicine" : "Update Changes");
        btnSave.setBackground(new Color(13, 148, 136));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 8; hoverBackground: #0F766E;");
        btnSave.addActionListener(e -> handleSave());
        btnRow.add(btnSave);

        bottomPanel.add(btnRow, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Pre-fill fields when editing
        if (existingMedicine != null) {
            txtName.setText(existingMedicine.getName());
            txtCompany.setText(existingMedicine.getCompany());
            cmbType.setSelectedItem(existingMedicine.getMedicineType());
            txtPrice.setText(existingMedicine.getPrice() != null ? existingMedicine.getPrice().toPlainString() : "0.00");
            spinQuantity.setValue(existingMedicine.getQuantityInStock());
            spinReorderLevel.setValue(existingMedicine.getReorderLevel());
            if (existingMedicine.getExpiryDate() != null) {
                txtExpiryDate.setText(existingMedicine.getExpiryDate().toString());
            }
            selectCurrentSupplier(existingMedicine.getSupplierId());
        }

        setContentPane(mainPanel);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(51, 65, 85));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    private void populateSuppliers() {
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        cmbSupplier.removeAllItems();
        for (Supplier s : suppliers) {
            cmbSupplier.addItem(s);
        }
    }

    private void selectCurrentSupplier(Integer supplierId) {
        if (supplierId == null) return;
        for (int i = 0; i < cmbSupplier.getItemCount(); i++) {
            Supplier s = cmbSupplier.getItemAt(i);
            if (s != null && s.getSupplierId() == supplierId) {
                cmbSupplier.setSelectedIndex(i);
                break;
            }
        }
    }

    private void handleSave() {
        lblError.setText(" ");
        String name = txtName.getText().trim();
        String company = txtCompany.getText().trim();
        String type = (String) cmbType.getSelectedItem();
        String priceText = txtPrice.getText().trim();
        String expiryText = txtExpiryDate.getText().trim();
        Supplier selectedSupplier = (Supplier) cmbSupplier.getSelectedItem();

        if (name.isEmpty()) {
            showError("Medicine Name is required.");
            txtName.requestFocus();
            return;
        }

        if (company.isEmpty()) {
            showError("Manufacturer Company is required.");
            txtCompany.requestFocus();
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceText);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Unit Price must be greater than zero.");
                txtPrice.requestFocus();
                return;
            }
        } catch (Exception e) {
            showError("Please enter a valid numeric price (e.g. 12.50).");
            txtPrice.requestFocus();
            return;
        }

        Date expiryDate;
        try {
            LocalDate parsedDate = LocalDate.parse(expiryText);
            expiryDate = Date.valueOf(parsedDate);
        } catch (DateTimeParseException e) {
            showError("Invalid Expiry Date format. Use YYYY-MM-DD.");
            txtExpiryDate.requestFocus();
            return;
        }

        int qty = (int) spinQuantity.getValue();
        int reorder = (int) spinReorderLevel.getValue();
        Integer supplierId = selectedSupplier != null ? selectedSupplier.getSupplierId() : null;

        if (existingMedicine == null) {
            Medicine newMed = new Medicine(0, name, company, type, price, qty, reorder, expiryDate, supplierId);
            if (selectedSupplier != null) newMed.setSupplierName(selectedSupplier.getName());
            boolean ok = medicineDAO.addMedicine(newMed);
            if (ok) {
                saved = true;
                dispose();
            } else {
                showError("Failed to add medicine record.");
            }
        } else {
            existingMedicine.setName(name);
            existingMedicine.setCompany(company);
            existingMedicine.setMedicineType(type);
            existingMedicine.setPrice(price);
            existingMedicine.setQuantityInStock(qty);
            existingMedicine.setReorderLevel(reorder);
            existingMedicine.setExpiryDate(expiryDate);
            existingMedicine.setSupplierId(supplierId);
            if (selectedSupplier != null) existingMedicine.setSupplierName(selectedSupplier.getName());

            boolean ok = medicineDAO.updateMedicine(existingMedicine);
            if (ok) {
                saved = true;
                dispose();
            } else {
                showError("Failed to update medicine record.");
            }
        }
    }

    private void showError(String msg) {
        lblError.setText("⚠ " + msg);
    }

    public boolean isSaved() {
        return saved;
    }
}
