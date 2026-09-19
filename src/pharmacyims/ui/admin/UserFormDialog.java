package pharmacyims.ui.admin;

import com.formdev.flatlaf.FlatClientProperties;
import pharmacyims.dao.UserDAO;
import pharmacyims.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Form dialog for registering cashier accounts.
public class UserFormDialog extends JDialog {

    private final UserDAO userDAO;
    private boolean saved = false;

    private JTextField txtFullName;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JLabel lblError;

    public UserFormDialog(Frame parent, UserDAO uDAO) {
        super(parent, "Register New Cashier Account", true);
        this.userDAO = uDAO;

        initUI();
    }

    private void initUI() {
        setSize(460, 480);
        setResizable(false);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("New Cashier Account");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSubtitle = new JLabel("Provision POS dispensing access credentials for pharmacy personnel.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Input fields grid
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        formGrid.setBorder(new EmptyBorder(20, 0, 16, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);

        int row = 0;

        txtFullName = new JTextField();
        txtFullName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. Jane Doe");
        txtFullName.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        addField(formGrid, gbc, row++, "Full Staff Name *", txtFullName);

        txtUsername = new JTextField();
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. cashier3");
        txtUsername.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        addField(formGrid, gbc, row++, "System Username *", txtUsername);

        JTextField txtRole = new JTextField("Cashier (Point of Sale)");
        txtRole.setEditable(false);
        txtRole.setBackground(new Color(241, 245, 249));
        txtRole.setForeground(new Color(71, 85, 105));
        txtRole.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        addField(formGrid, gbc, row++, "Assigned Role", txtRole);

        txtPassword = new JPasswordField();
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Minimum 6 characters");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "arc: 8; showRevealButton: true;");
        addField(formGrid, gbc, row++, "Initial Password *", txtPassword);

        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Re-type password");
        txtConfirmPassword.putClientProperty(FlatClientProperties.STYLE, "arc: 8; showRevealButton: true;");
        addField(formGrid, gbc, row++, "Confirm Password *", txtConfirmPassword);

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

        JButton btnSave = new JButton("Create Cashier Account");
        btnSave.setBackground(new Color(13, 148, 136));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 8; hoverBackground: #0F766E;");
        btnSave.addActionListener(e -> handleSave());
        btnRow.add(btnSave);

        bottomPanel.add(btnRow, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

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

        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();
        String confirm = new String(txtConfirmPassword.getPassword()).trim();

        if (fullName.isEmpty()) {
            showError(txtFullName, "Staff full name is required.");
            return;
        }

        if (username.isEmpty()) {
            showError(txtUsername, "System username is required.");
            return;
        }

        if (username.length() < 3) {
            showError(txtUsername, "Username must be at least 3 characters.");
            return;
        }

        if (pass.isEmpty()) {
            showError(txtPassword, "Password is required.");
            return;
        }

        if (pass.length() < 6) {
            showError(txtPassword, "Password must be at least 6 characters.");
            return;
        }

        if (!pass.equals(confirm)) {
            showError(txtConfirmPassword, "Passwords do not match. Please re-enter.");
            return;
        }

        User newUser = new User(username, pass, "Cashier", fullName);
        boolean ok = userDAO.createCashier(newUser);
        if (ok) {
            saved = true;
            dispose();
        } else {
            lblError.setText("Failed to register cashier. Username may already exist.");
        }
    }

    private void showError(JComponent comp, String message) {
        comp.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
        comp.requestFocusInWindow();
        lblError.setText(message);
    }

    private void clearFieldOutlines() {
        txtFullName.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtUsername.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtPassword.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtConfirmPassword.putClientProperty(FlatClientProperties.OUTLINE, null);
    }

    public boolean isSaved() {
        return saved;
    }
}
