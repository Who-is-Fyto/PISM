package pharmacyims.ui.common;

import com.formdev.flatlaf.FlatClientProperties;
import pharmacyims.LoginFrame;
import pharmacyims.session.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Reusable top header banner for Fyto PIMS dashboards.
 * Displays brand logo, active staff profile, role badge, live digital clock, and logout trigger.
 */
public class HeaderBar extends JPanel {

    private final JFrame parentFrame;
    private JLabel lblClock;

    public HeaderBar(JFrame parentFrame, String portalTitle) {
        this.parentFrame = parentFrame;
        initUI(portalTitle);
    }

    private void initUI(String portalTitle) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(12, 24, 12, 24)
        ));

        // 1. Left: Brand Emblem & Portal Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);

        // Custom Mini Cross Emblem
        JComponent emblem = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(13, 148, 136)); // Medical Teal (#0D9488)
                g2.fillRoundRect(0, 0, 32, 32, 8, 8);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(13, 7, 6, 18, 2, 2);
                g2.fillRoundRect(7, 13, 18, 6, 2, 2);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(32, 32);
            }
        };
        leftPanel.add(emblem);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 0));
        titleBlock.setOpaque(false);

        JLabel lblBrand = new JLabel("Fyto PIMS");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBrand.setForeground(new Color(15, 23, 42)); // #0F172A

        JLabel lblSubtitle = new JLabel(portalTitle != null ? portalTitle : "Pharmacy Management Portal");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));

        titleBlock.add(lblBrand);
        titleBlock.add(lblSubtitle);
        leftPanel.add(titleBlock);

        add(leftPanel, BorderLayout.WEST);

        // 2. Right: Live Clock, User Profile, Role Badge, and Logout Button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setOpaque(false);

        // Live Clock
        lblClock = new JLabel();
        lblClock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblClock.setForeground(new Color(71, 85, 105));
        startClock();
        rightPanel.add(lblClock);

        // Vertical separator
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 24));
        sep.setForeground(new Color(226, 232, 240));
        rightPanel.add(sep);

        // User Name
        String fullName = UserSession.getInstance() != null ? UserSession.getInstance().getFullName() : "System Administrator";
        JLabel lblUser = new JLabel(fullName);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(new Color(15, 23, 42));
        rightPanel.add(lblUser);

        // Role Badge
        String role = UserSession.getInstance() != null ? UserSession.getInstance().getRole().toUpperCase() : "ADMIN";
        JLabel lblRoleBadge = new JLabel(" " + role + " ");
        lblRoleBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRoleBadge.setOpaque(true);
        if ("ADMIN".equalsIgnoreCase(role)) {
            lblRoleBadge.setBackground(new Color(224, 231, 255)); // Indigo tint (#E0E7FF)
            lblRoleBadge.setForeground(new Color(67, 56, 202));   // Indigo text (#4338CA)
        } else {
            lblRoleBadge.setBackground(new Color(220, 252, 231)); // Emerald tint (#DCFCE7)
            lblRoleBadge.setForeground(new Color(21, 128, 61));    // Emerald text (#15803D)
        }
        lblRoleBadge.setBorder(new EmptyBorder(2, 6, 2, 6));
        lblRoleBadge.putClientProperty(FlatClientProperties.STYLE, "arc: 6");
        rightPanel.add(lblRoleBadge);

        // Logout Button
        JButton btnLogout = new JButton("Sign Out");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLogout.setForeground(new Color(220, 38, 38)); // Red text (#DC2626)
        btnLogout.setBackground(new Color(254, 242, 242)); // Soft red tint (#FEF2F2)
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.putClientProperty(FlatClientProperties.STYLE, "arc: 8; hoverBackground: #FEE2E2;");
        btnLogout.addActionListener(e -> handleLogout());
        rightPanel.add(btnLogout);

        add(rightPanel, BorderLayout.EAST);
    }

    private void startClock() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy • HH:mm:ss");
        lblClock.setText(LocalDateTime.now().format(dtf));

        Timer timer = new Timer(1000, e -> {
            lblClock.setText(LocalDateTime.now().format(dtf));
        });
        timer.setRepeats(true);
        timer.start();
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                parentFrame,
                "Are you sure you want to sign out of Fyto PIMS?",
                "Sign Out Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            UserSession.clear();
            if (parentFrame != null) {
                parentFrame.dispose();
            }
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
        }
    }
}
