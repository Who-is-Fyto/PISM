package pharmacyims;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import pharmacyims.dao.UserDAO;
import pharmacyims.model.User;
import pharmacyims.session.UserSession;
import pharmacyims.ui.admin.AdminDashboard;
import pharmacyims.ui.cashier.CashierDashboard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;

// Authentication frame for Fyto PIMS with login form and branding.
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JButton btnSignIn;
    private JButton btnExit;
    private JPanel alertBanner;
    private JLabel lblAlertText;
    private JLabel lblStatusBadge;

    private BiConsumer<String, String> loginCallback;

    public LoginFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Fyto PIMS — Pharmacy Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(960, 580);
        setLocationRelativeTo(null);

        // Main 2-column layout: branding hero on left, login form on right
        JPanel mainContainer = new JPanel(new GridLayout(1, 2));
        mainContainer.add(createHeroPanel());
        mainContainer.add(createFormPanel());
        setContentPane(mainContainer);

        getRootPane().setDefaultButton(btnSignIn);
    }

    // Creates the left branding hero panel with serene blues, healing greens, and clinical branding
    private JPanel createHeroPanel() {
        JPanel heroPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Serene deep ocean-slate to botanical spruce gradient
                Color c1 = new Color(15, 29, 48);   // Serene midnight slate
                Color c2 = new Color(13, 64, 69);   // Deep healing spruce
                Color c3 = new Color(16, 88, 77);   // Botanical eucalyptus

                LinearGradientPaint gradient = new LinearGradientPaint(
                        0, 0, getWidth(), getHeight(),
                        new float[]{0.0f, 0.55f, 1.0f},
                        new Color[]{c1, c2, c3}
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle serene ambient glow
                g2.setPaint(new RadialGradientPaint(
                        getWidth() * 0.85f, getHeight() * 0.2f, 260,
                        new float[]{0.0f, 1.0f},
                        new Color[]{new Color(56, 189, 248, 25), new Color(56, 189, 248, 0)}
                ));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };

        heroPanel.setLayout(new BorderLayout());
        heroPanel.setBorder(new EmptyBorder(42, 42, 38, 42));

        // Top / Center content in hero
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Refined Botanical & Clinical Emblem
        JComponent brandEmblem = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Healing green rounded badge
                g2.setColor(new Color(5, 150, 105));
                g2.fillRoundRect(0, 0, 54, 54, 14, 14);

                // Subtle inner border
                g2.setColor(new Color(110, 231, 183, 100));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, 52, 52, 13, 13);

                // Crisp clinical cross in white
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(22, 13, 10, 28, 3, 3);
                g2.fillRoundRect(13, 22, 28, 10, 3, 3);

                // Serene sky blue accent dot
                g2.setColor(new Color(56, 189, 248));
                g2.fillOval(35, 11, 7, 7);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(54, 54);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(54, 54);
            }
        };
        brandEmblem.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(brandEmblem);
        contentPanel.add(Box.createVerticalStrut(20));

        JLabel lblTitle = new JLabel("Fyto PIMS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblTitle);

        contentPanel.add(Box.createVerticalStrut(4));

        JLabel lblSubtitle = new JLabel("Botanical Care & Clinical Pharmacy Suite");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(186, 230, 253)); // Serene sky ice
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblSubtitle);

        contentPanel.add(Box.createVerticalStrut(24));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(320, 2));
        sep.setForeground(new Color(255, 255, 255, 45));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(sep);

        contentPanel.add(Box.createVerticalStrut(20));

        // Bespoke feature cards
        contentPanel.add(createFeatureCard("🌿", "Smart Inventory Control", "Real-time reorder thresholds & batch tracking"));
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createFeatureCard("⚡", "Rapid Dispensing POS", "Barcode stock lookup, cart & thermal billing"));
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createFeatureCard("🛡", "Clinical Safeguards", "30-day expiration alerts & role permissions"));

        heroPanel.add(contentPanel, BorderLayout.CENTER);

        // Status indicator at bottom of hero panel
        JPanel bottomHero = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bottomHero.setOpaque(false);

        JLabel dotLabel = new JLabel("●");
        dotLabel.setForeground(new Color(52, 211, 153)); // Healing mint
        dotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblStatusBadge = new JLabel("System Online • Workstation Ready");
        lblStatusBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatusBadge.setForeground(new Color(204, 251, 241));

        bottomHero.add(dotLabel);
        bottomHero.add(lblStatusBadge);
        heroPanel.add(bottomHero, BorderLayout.SOUTH);

        return heroPanel;
    }

    // Creates a sleek translucent feature card for the left hero panel
    private JPanel createFeatureCard(String icon, String title, String desc) {
        JPanel card = new JPanel(new BorderLayout(10, 2));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 25), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        card.add(lblIcon, BorderLayout.WEST);

        JPanel textStack = new JPanel(new GridLayout(2, 1, 0, 1));
        textStack.setOpaque(false);

        JLabel lblCardTitle = new JLabel(title);
        lblCardTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCardTitle.setForeground(new Color(240, 253, 250)); // Soft mint white

        JLabel lblCardDesc = new JLabel(desc);
        lblCardDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCardDesc.setForeground(new Color(186, 230, 253)); // Serene sky tint

        textStack.add(lblCardTitle);
        textStack.add(lblCardDesc);
        card.add(textStack, BorderLayout.CENTER);

        return card;
    }

    // Creates the login input form panel
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new BorderLayout());
        formPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel centerContainer = new JPanel();
        centerContainer.setOpaque(false);
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));

        JLabel lblHeader = new JLabel("Workstation Sign In");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(new Color(15, 23, 42)); // Deep charcoal
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContainer.add(lblHeader);

        centerContainer.add(Box.createVerticalStrut(4));

        JLabel lblSubHeader = new JLabel("Enter your pharmacy credentials to access the terminal.");
        lblSubHeader.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubHeader.setForeground(new Color(100, 116, 139));
        lblSubHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContainer.add(lblSubHeader);

        centerContainer.add(Box.createVerticalStrut(18));

        alertBanner = createAlertBanner();
        alertBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContainer.add(alertBanner);

        centerContainer.add(Box.createVerticalStrut(14));

        JLabel lblUsername = new JLabel("Username or Staff ID");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUsername.setForeground(new Color(51, 65, 85));
        lblUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContainer.add(lblUsername);

        centerContainer.add(Box.createVerticalStrut(6));

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtUsername.setPreferredSize(new Dimension(360, 42));
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username");
        txtUsername.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 0, 12, 0, 12;");
        txtUsername.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPassword.requestFocusInWindow();
                }
            }
        });
        centerContainer.add(txtUsername);

        centerContainer.add(Box.createVerticalStrut(16));

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPassword.setForeground(new Color(51, 65, 85));
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContainer.add(lblPassword);

        centerContainer.add(Box.createVerticalStrut(6));

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtPassword.setPreferredSize(new Dimension(360, 42));
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 0, 12, 0, 12; showRevealButton: true;");
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleSignInAction();
                }
            }
        });
        centerContainer.add(txtPassword);

        centerContainer.add(Box.createVerticalStrut(10));

        // Show/hide password checkbox
        JPanel helperRow = new JPanel(new BorderLayout());
        helperRow.setOpaque(false);
        helperRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        helperRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        chkShowPassword = new JCheckBox("Show password");
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword.setForeground(new Color(100, 116, 139));
        chkShowPassword.setOpaque(false);
        chkShowPassword.setFocusPainted(false);
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });
        helperRow.add(chkShowPassword, BorderLayout.WEST);

        centerContainer.add(helperRow);

        centerContainer.add(Box.createVerticalStrut(22));

        btnSignIn = new JButton("Sign In to Terminal");
        btnSignIn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSignIn.setForeground(Color.WHITE);
        btnSignIn.setBackground(new Color(5, 150, 105)); // Healing green
        btnSignIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnSignIn.setPreferredSize(new Dimension(360, 44));
        btnSignIn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSignIn.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #047857; pressedBackground: #065F46;");
        btnSignIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSignIn.addActionListener(e -> handleSignInAction());
        centerContainer.add(btnSignIn);

        centerContainer.add(Box.createVerticalStrut(10));

        btnExit = new JButton("Exit Application");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnExit.setForeground(new Color(100, 116, 139));
        btnExit.setBackground(Color.WHITE);
        btnExit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnExit.setPreferredSize(new Dimension(360, 36));
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        btnExit.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #F1F5F9;");
        btnExit.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnExit.addActionListener(e -> handleExitAction());
        centerContainer.add(btnExit);

        centerContainer.add(Box.createVerticalGlue());

        formPanel.add(centerContainer, BorderLayout.CENTER);
        return formPanel;
    }

    // Creates the inline alert banner for validation errors
    private JPanel createAlertBanner() {
        JPanel banner = new JPanel(new BorderLayout(8, 0));
        banner.setBackground(new Color(254, 242, 242));
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(248, 113, 113), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        banner.setVisible(false);

        JLabel iconLabel = new JLabel("⚠");
        iconLabel.setForeground(new Color(220, 38, 38));
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblAlertText = new JLabel("Invalid credentials.");
        lblAlertText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAlertText.setForeground(new Color(185, 28, 28));

        banner.add(iconLabel, BorderLayout.WEST);
        banner.add(lblAlertText, BorderLayout.CENTER);
        return banner;
    }

    // Validates inputs and initiates authentication
    private void handleSignInAction() {
        clearFieldErrors();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty()) {
            txtUsername.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            txtUsername.requestFocusInWindow();
            showErrorMessage("Please enter your username or staff ID.");
            return;
        }

        if (password.isEmpty()) {
            txtPassword.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            txtPassword.requestFocusInWindow();
            showErrorMessage("Please enter your password.");
            return;
        }

        if (loginCallback != null) {
            setBusy(true);
            loginCallback.accept(username, password);
            return;
        }

        // Authenticate via UserDAO (MySQL DB or fallback cache)
        setBusy(true);
        Timer timer = new Timer(350, (ActionEvent e) -> {
            setBusy(false);
            UserDAO userDAO = new UserDAO();
            User authenticatedUser = userDAO.authenticate(username, password);

            if (authenticatedUser != null) {
                UserSession.initialize(authenticatedUser);
                showSuccessMessage("Signed in as " + authenticatedUser.getRole() + ". Redirecting...");

                if (authenticatedUser.isAdmin()) {
                    SwingUtilities.invokeLater(() -> {
                        AdminDashboard dashboard = new AdminDashboard();
                        dashboard.setVisible(true);
                        dispose();
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        CashierDashboard dashboard = new CashierDashboard();
                        dashboard.setVisible(true);
                        dispose();
                    });
                }
            } else {
                txtPassword.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
                showErrorMessage("Invalid username or password. Please try again.");
                txtPassword.setText("");
                txtPassword.requestFocusInWindow();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void handleExitAction() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit Fyto PIMS?",
                "Exit Application",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public void setLoginCallback(BiConsumer<String, String> callback) {
        this.loginCallback = callback;
    }

    public void showErrorMessage(String message) {
        lblAlertText.setText(message);
        lblAlertText.setForeground(new Color(185, 28, 28));
        alertBanner.setBackground(new Color(254, 242, 242));
        alertBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(248, 113, 113), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        alertBanner.setVisible(true);
        alertBanner.revalidate();
        alertBanner.repaint();
    }

    public void showSuccessMessage(String message) {
        lblAlertText.setText(message);
        lblAlertText.setForeground(new Color(21, 128, 61));
        alertBanner.setBackground(new Color(240, 253, 244));
        alertBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(74, 222, 128), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        alertBanner.setVisible(true);
        alertBanner.revalidate();
        alertBanner.repaint();
    }

    public void clearAlert() {
        alertBanner.setVisible(false);
        clearFieldErrors();
    }

    private void clearFieldErrors() {
        txtUsername.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtPassword.putClientProperty(FlatClientProperties.OUTLINE, null);
    }

    public void setBusy(boolean busy) {
        btnSignIn.setEnabled(!busy);
        btnSignIn.setText(busy ? "Signing in..." : "Sign In to Terminal");
        txtUsername.setEnabled(!busy);
        txtPassword.setEnabled(!busy);
    }

    public String getUsername() {
        return txtUsername.getText().trim();
    }

    public String getPassword() {
        return new String(txtPassword.getPassword());
    }

    // Main entry point for standalone launch
    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);

        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
