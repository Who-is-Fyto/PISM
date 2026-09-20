package pharmacyims.ui.common;

import com.formdev.flatlaf.FlatClientProperties;
import pharmacyims.LoginFrame;
import pharmacyims.session.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Top header banner for dashboards with staff info, clock, and sign out.
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

        // Brand emblem and portal title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);

        JComponent emblem = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Healing green rounded emblem with crisp cross & serene blue accent
                g2.setColor(new Color(5, 150, 105));
                g2.fillRoundRect(0, 0, 32, 32, 8, 8);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(13, 7, 6, 18, 2, 2);
                g2.fillRoundRect(7, 13, 18, 6, 2, 2);

                g2.setColor(new Color(56, 189, 248)); // Serene sky dot
                g2.fillOval(21, 5, 4, 4);

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
        lblBrand.setForeground(new Color(15, 23, 42)); // Deep charcoal

        JLabel lblSubtitle = new JLabel(portalTitle != null ? portalTitle : "Pharmacy Management Portal");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));

        titleBlock.add(lblBrand);
        titleBlock.add(lblSubtitle);
        leftPanel.add(titleBlock);

        add(leftPanel, BorderLayout.WEST);

        // Live clock, user badge, and sign-out button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setOpaque(false);

        lblClock = new JLabel();
        lblClock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblClock.setForeground(new Color(71, 85, 105));
        startClock();
        rightPanel.add(lblClock);

        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 24));
        sep.setForeground(new Color(226, 232, 240));
        rightPanel.add(sep);

        String fullName = UserSession.getInstance() != null ? UserSession.getInstance().getFullName() : "System Administrator";
        JLabel lblUser = new JLabel(fullName);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(new Color(15, 23, 42));
        rightPanel.add(lblUser);

        String role = UserSession.getInstance() != null ? UserSession.getInstance().getRole().toUpperCase() : "ADMIN";
        JLabel lblRoleBadge = new JLabel(" " + role + " ");
        lblRoleBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRoleBadge.setOpaque(true);
        if ("ADMIN".equalsIgnoreCase(role)) {
            lblRoleBadge.setBackground(new Color(224, 242, 254)); // Serene sky tint
            lblRoleBadge.setForeground(new Color(3, 105, 161));   // Deep serene blue
            lblRoleBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(186, 230, 253), 1, true),
                    new EmptyBorder(2, 8, 2, 8)
            ));
        } else {
            lblRoleBadge.setBackground(new Color(236, 253, 245)); // Healing mint tint
            lblRoleBadge.setForeground(new Color(4, 120, 87));    // Botanical green
            lblRoleBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(167, 243, 208), 1, true),
                    new EmptyBorder(2, 8, 2, 8)
            ));
        }
        lblRoleBadge.putClientProperty(FlatClientProperties.STYLE, "arc: 6;");
        rightPanel.add(lblRoleBadge);

        JButton btnLogout = new JButton("Sign Out");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLogout.setForeground(new Color(225, 29, 72)); // Muted rose
        btnLogout.setBackground(new Color(255, 241, 242));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #FEE2E2;");
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
