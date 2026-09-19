package pharmacyims;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;

/**
 * Fyto PIMS — Pharmacy Inventory Management System
 * Modern, responsive Authentication Frame built with FlatLaf styling.
 * Features a medical branding hero panel, real-time validation highlights,
 * show/hide password toggle, and keyboard navigation.
 */
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

        // Main 2-column container: Left Hero (400px), Right Form (560px)
        JPanel mainContainer = new JPanel(new GridLayout(1, 2));

        // 1. Left Brand Hero Panel
        JPanel heroPanel = createHeroPanel();
        mainContainer.add(heroPanel);

        // 2. Right Authentication Form Panel
        JPanel formPanel = createFormPanel();
        mainContainer.add(formPanel);

        setContentPane(mainContainer);

        // Global Enter key listener for the window
        getRootPane().setDefaultButton(btnSignIn);
    }

    /**
     * Constructs the left-side branding hero panel with custom gradient and medical iconography.
     */
    private JPanel createHeroPanel() {
        JPanel heroPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Elegant Medical Teal Gradient (Deep Emerald -> Ocean Cyan)
                Color c1 = new Color(6, 78, 59);    // Deep Forest Teal (#064E3B)
                Color c2 = new Color(13, 148, 136); // Medical Teal (#0D9488)
                Color c3 = new Color(2, 132, 199);  // Clean Azure (#0284C7)

                LinearGradientPaint gradient = new LinearGradientPaint(
                        0, 0, getWidth(), getHeight(),
                        new float[]{0.0f, 0.65f, 1.0f},
                        new Color[]{c1, c2, c3}
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle circular decorative background ambient glow
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(-60, -60, 240, 240);
                g2.fillOval(getWidth() - 140, getHeight() - 160, 220, 220);

                g2.dispose();
            }
        };

        heroPanel.setLayout(new BorderLayout());
        heroPanel.setBorder(new EmptyBorder(45, 45, 40, 45));

        // Top / Center content in hero
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Medical Cross Emblem Component
        JComponent crossBadge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Badge Container Rounded Square (Emerald Glow)
                g2.setColor(new Color(16, 185, 129)); // #10B981 Emerald
                g2.fillRoundRect(0, 0, 56, 56, 16, 16);

                // White Cross Geometry
                g2.setColor(Color.WHITE);
                // Vertical bar
                g2.fillRoundRect(22, 12, 12, 32, 4, 4);
                // Horizontal bar
                g2.fillRoundRect(12, 22, 32, 12, 4, 4);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(56, 56);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(56, 56);
            }
        };
        crossBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(crossBadge);
        contentPanel.add(Box.createVerticalStrut(22));

        // Title: Fyto PIMS
        JLabel lblTitle = new JLabel("Fyto PIMS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblTitle);

        contentPanel.add(Box.createVerticalStrut(6));

        // Subtitle
        JLabel lblSubtitle = new JLabel("Pharmacy Inventory & Dispensing Suite");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSubtitle.setForeground(new Color(209, 250, 229)); // Mint tint (#D1FAE5)
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblSubtitle);

        contentPanel.add(Box.createVerticalStrut(28));

        // Thin divider
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(320, 2));
        sep.setForeground(new Color(255, 255, 255, 60));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(sep);

        contentPanel.add(Box.createVerticalStrut(24));

        // Feature Highlights Bullet List
        String[] features = {
                "Intelligent Stock & Reorder Tracking",
                "Rapid Point-of-Sale Dispensing",
                "Automated Expiration Surveillance",
                "Multi-User Role-Based Security"
        };

        for (String feature : features) {
            JPanel bulletItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
            bulletItem.setOpaque(false);
            bulletItem.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel checkIcon = new JLabel("✓ ");
            checkIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));
            checkIcon.setForeground(new Color(52, 211, 153)); // Soft emerald

            JLabel textLabel = new JLabel(feature);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            textLabel.setForeground(new Color(240, 253, 250));

            bulletItem.add(checkIcon);
            bulletItem.add(textLabel);
            contentPanel.add(bulletItem);
            contentPanel.add(Box.createVerticalStrut(4));
        }

        heroPanel.add(contentPanel, BorderLayout.CENTER);

        // Bottom status indicator in hero panel
        JPanel bottomHero = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bottomHero.setOpaque(false);

        JLabel dotLabel = new JLabel("●");
        dotLabel.setForeground(new Color(52, 211, 153));
        dotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        lblStatusBadge = new JLabel("System Online • v1.0.0");
        lblStatusBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatusBadge.setForeground(new Color(204, 251, 241));

        bottomHero.add(dotLabel);
        bottomHero.add(lblStatusBadge);
        heroPanel.add(bottomHero, BorderLayout.SOUTH);

        return heroPanel;
    }

    /**
     * Constructs the right-side modern authentication form.
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new BorderLayout());
        formPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel centerContainer = new JPanel();
        centerContainer.setOpaque(false);
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));

        // Form Greeting
        JLabel lblHeader = new JLabel("Sign In");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblHeader.setForeground(new Color(15, 23, 42)); // #0F172A
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContainer.add(lblHeader);

        centerContainer.add(Box.createVerticalStrut(4));

        JLabel lblSubHeader = new JLabel("Enter your pharmacy credentials to access your terminal.");
        lblSubHeader.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubHeader.setForeground(new Color(100, 116, 139)); // #64748B
        lblSubHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContainer.add(lblSubHeader);

        centerContainer.add(Box.createVerticalStrut(18));

        // Inline Alert / Error Banner (Hidden by default)
        alertBanner = createAlertBanner();
        alertBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContainer.add(alertBanner);

        centerContainer.add(Box.createVerticalStrut(14));

        // 1. Username Field
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
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. admin or cashier1");
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

        // 2. Password Field
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

        // Show password checkbox and role helper row
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

        // 3. Primary Sign In Button
        btnSignIn = new JButton("Sign In to Terminal");
        btnSignIn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSignIn.setForeground(Color.WHITE);
        btnSignIn.setBackground(new Color(13, 148, 136)); // Medical Teal (#0D9488)
        btnSignIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnSignIn.setPreferredSize(new Dimension(360, 44));
        btnSignIn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSignIn.putClientProperty(FlatClientProperties.STYLE, "arc: 10; hoverBackground: #0F766E; pressedBackground: #115E59;");
        btnSignIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSignIn.addActionListener(e -> handleSignInAction());
        centerContainer.add(btnSignIn);

        centerContainer.add(Box.createVerticalStrut(10));

        // 4. Secondary Exit Button
        btnExit = new JButton("Exit Application");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnExit.setForeground(new Color(100, 116, 139));
        btnExit.setBackground(Color.WHITE);
        btnExit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnExit.setPreferredSize(new Dimension(360, 36));
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        btnExit.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnExit.addActionListener(e -> handleExitAction());
        centerContainer.add(btnExit);

        centerContainer.add(Box.createVerticalGlue());

        // Quick Demo Credentials Card at bottom of form
        JPanel demoCard = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        demoCard.setBackground(new Color(248, 250, 252));
        demoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(4, 12, 4, 12)
        ));
        demoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        demoCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDemo = new JLabel("Demo: admin / admin123 (Admin) • cashier1 / cashier123 (POS)");
        lblDemo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDemo.setForeground(new Color(100, 116, 139));
        demoCard.add(lblDemo);
        centerContainer.add(demoCard);

        formPanel.add(centerContainer, BorderLayout.CENTER);
        return formPanel;
    }

    /**
     * Alert banner rendering inline errors or status messages cleanly.
     */
    private JPanel createAlertBanner() {
        JPanel banner = new JPanel(new BorderLayout(8, 0));
        banner.setBackground(new Color(254, 242, 242)); // Soft red tint (#FEF2F2)
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

    private void handleSignInAction() {
        clearFieldErrors();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        // 1. Validate non-empty username
        if (username.isEmpty()) {
            txtUsername.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            txtUsername.requestFocusInWindow();
            showErrorMessage("Please enter your username or staff ID.");
            return;
        }

        // 2. Validate non-empty password
        if (password.isEmpty()) {
            txtPassword.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            txtPassword.requestFocusInWindow();
            showErrorMessage("Please enter your password.");
            return;
        }

        // 3. Delegate to callback if provided
        if (loginCallback != null) {
            setBusy(true);
            loginCallback.accept(username, password);
            return;
        }

        // 4. Default Demonstration Evaluation (Standalone Prototype Mode)
        setBusy(true);
        Timer timer = new Timer(500, (ActionEvent e) -> {
            setBusy(false);
            if ("admin".equalsIgnoreCase(username) && "admin123".equals(password)) {
                showSuccessMessage("Signed in as Administrator. Welcome!");
                JOptionPane.showMessageDialog(
                        this,
                        "Successfully authenticated as [Administrator]!\nUser: System Admin (admin)\n\n"
                                + "Routing to AdminDashboard in Phase 3...",
                        "Fyto PIMS — Login Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else if ("cashier1".equalsIgnoreCase(username) && "cashier123".equals(password)) {
                showSuccessMessage("Signed in as Cashier. Welcome!");
                JOptionPane.showMessageDialog(
                        this,
                        "Successfully authenticated as [Cashier]!\nUser: Jane Doe (cashier1)\n\n"
                                + "Routing to Cashier POS Dashboard in Phase 4...",
                        "Fyto PIMS — Login Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );
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

    /**
     * Standalone main runner with FlatLaf setup.
     */
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
