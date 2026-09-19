package pharmacyims.ui.admin;

import com.formdev.flatlaf.FlatClientProperties;
import pharmacyims.dao.SupplierDAO;
import pharmacyims.model.Supplier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.regex.Pattern;

// Form dialog for adding and editing suppliers.
public class SupplierFormDialog extends JDialog {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final SupplierDAO supplierDAO;
    private final Supplier existingSupplier;
    private boolean saved = false;

    private JTextField txtName;
    private JTextField txtContactPerson;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextArea txtAddress;
    private JLabel lblError;

    public SupplierFormDialog(Frame parent, Supplier supplierToEdit, SupplierDAO supDAO) {
        super(parent, supplierToEdit == null ? "Register New Supplier" : "Edit Supplier: " + supplierToEdit.getName(), true);
        this.existingSupplier = supplierToEdit;
        this.supplierDAO = supDAO;

        initUI();
    }

    private void initUI() {
        setSize(480, 520);
        setResizable(false);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(existingSupplier == null ? "Register Pharmaceutical Vendor" : "Update Supplier Details");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSubtitle = new JLabel("Enter verified contact credentials and distribution warehouse address.");
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
        gbc.insets = new Insets(7, 4, 7, 4);

        int row = 0;

        txtName = new JTextField();
        txtName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. Apex Bioscience");
        txtName.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        addField(formGrid, gbc, row++, "Supplier / Company Name *", txtName);

        txtContactPerson = new JTextField();
        txtContactPerson.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. Sarah Connor");
        txtContactPerson.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        addField(formGrid, gbc, row++, "Contact Person *", txtContactPerson);

        txtPhone = new JTextField();
        txtPhone.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. +1 (555) 024-8891");
        txtPhone.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        addField(formGrid, gbc, row++, "Phone Number *", txtPhone);

        txtEmail = new JTextField();
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. supply@apexbio.com");
        txtEmail.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        addField(formGrid, gbc, row++, "Email Address *", txtEmail);

        txtAddress = new JTextArea(3, 20);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        txtAddress.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        JScrollPane scrollAddress = new JScrollPane(txtAddress);
        scrollAddress.setPreferredSize(new Dimension(240, 70));
        addField(formGrid, gbc, row++, "Distribution Address *", scrollAddress);

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

        JButton btnSave = new JButton(existingSupplier == null ? "Save Supplier" : "Update Changes");
        btnSave.setBackground(new Color(13, 148, 136));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 8; hoverBackground: #0F766E;");
        btnSave.addActionListener(e -> handleSave());
        btnRow.add(btnSave);

        bottomPanel.add(btnRow, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Pre-fill fields when editing
        if (existingSupplier != null) {
            txtName.setText(existingSupplier.getName());
            txtContactPerson.setText(existingSupplier.getContactPerson());
            txtPhone.setText(existingSupplier.getPhone());
            txtEmail.setText(existingSupplier.getEmail());
            txtAddress.setText(existingSupplier.getAddress());
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

    private void handleSave() {
        lblError.setText(" ");
        clearFieldOutlines();

        String name = txtName.getText().trim();
        String contact = txtContactPerson.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        // 1. Validate Name
        if (name.isEmpty()) {
            showError(txtName, "Supplier/Company name is required.");
            return;
        }

        // 2. Validate Contact Person
        if (contact.isEmpty()) {
            showError(txtContactPerson, "Contact person name is required.");
            return;
        }

        // 3. Validate Phone
        if (phone.isEmpty()) {
            showError(txtPhone, "Phone number is required.");
            return;
        }

        // 4. Validate Email
        if (email.isEmpty()) {
            showError(txtEmail, "Email address is required.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError(txtEmail, "Please enter a valid email address (e.g. name@domain.com).");
            return;
        }

        // 5. Validate Address
        if (address.isEmpty()) {
            showError(txtAddress, "Physical distribution address is required.");
            return;
        }

        // Save or update
        boolean success;
        if (existingSupplier == null) {
            Supplier newSupplier = new Supplier(name, contact, phone, email, address);
            success = supplierDAO.addSupplier(newSupplier);
        } else {
            existingSupplier.setName(name);
            existingSupplier.setContactPerson(contact);
            existingSupplier.setPhone(phone);
            existingSupplier.setEmail(email);
            existingSupplier.setAddress(address);
            success = supplierDAO.updateSupplier(existingSupplier);
        }

        if (success) {
            saved = true;
            dispose();
        } else {
            lblError.setText("Failed to save supplier to database. Please check connection.");
        }
    }

    private void showError(JComponent comp, String message) {
        comp.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
        comp.requestFocusInWindow();
        lblError.setText(message);
    }

    private void clearFieldOutlines() {
        txtName.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtContactPerson.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtPhone.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtEmail.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtAddress.putClientProperty(FlatClientProperties.OUTLINE, null);
    }

    public boolean isSaved() {
        return saved;
    }
}
