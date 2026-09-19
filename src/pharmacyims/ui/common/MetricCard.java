package pharmacyims.ui.common;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern KPI / summary statistic card widget for Fyto PIMS dashboards.
 */
public class MetricCard extends JPanel {

    private final JLabel lblValue;
    private final JLabel lblSubtitle;

    public enum CardTheme {
        PRIMARY(new Color(2, 132, 199), new Color(240, 249, 255)),   // Azure (#0284C7 / #F0F9FF)
        SUCCESS(new Color(16, 185, 129), new Color(236, 253, 245)), // Emerald (#10B981 / #ECFDF5)
        WARNING(new Color(245, 158, 11), new Color(255, 251, 235)), // Amber (#F59E0B / #FFFBEB)
        DANGER(new Color(239, 68, 68), new Color(254, 242, 242));   // Crimson (#EF4444 / #FEF2F2)

        final Color accentColor;
        final Color backgroundColor;

        CardTheme(Color accent, Color bg) {
            this.accentColor = accent;
            this.backgroundColor = bg;
        }
    }

    public MetricCard(String title, String initialValue, String subtitle, CardTheme theme) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        // Top Header: Title + Accent Pill
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(100, 116, 139)); // Slate muted
        topRow.add(lblTitle, BorderLayout.WEST);

        // Accent indicator bar/pill
        JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(8, 8));
        accentBar.setBackground(theme.accentColor);
        accentBar.putClientProperty(FlatClientProperties.STYLE, "arc: 999;");
        topRow.add(accentBar, BorderLayout.EAST);

        add(topRow, BorderLayout.NORTH);

        // Center: Big Bold Metric
        lblValue = new JLabel(initialValue);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(new Color(15, 23, 42)); // #0F172A
        lblValue.setBorder(new EmptyBorder(8, 0, 4, 0));
        add(lblValue, BorderLayout.CENTER);

        // Bottom: Subtitle / Context
        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitle.setForeground(theme.accentColor);
        add(lblSubtitle, BorderLayout.SOUTH);
    }

    public void setValue(int value) {
        lblValue.setText(String.valueOf(value));
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }
}
