package pharmacyims.ui.common;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// KPI summary metric card widget.
public class MetricCard extends JPanel {

    private final JLabel lblValue;
    private final JLabel lblSubtitle;

    public enum CardTheme {
        PRIMARY(new Color(2, 132, 199), new Color(240, 249, 255)),  // Serene Sky
        SUCCESS(new Color(5, 150, 105), new Color(236, 253, 245)),  // Botanical Healing Green
        WARNING(new Color(217, 119, 6), new Color(255, 251, 235)),  // Calm Ochre
        DANGER(new Color(225, 29, 72), new Color(255, 241, 242));   // Muted Rose

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

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(100, 116, 139));
        topRow.add(lblTitle, BorderLayout.WEST);

        // Serene pill indicator
        JPanel accentPill = new JPanel();
        accentPill.setPreferredSize(new Dimension(10, 10));
        accentPill.setBackground(theme.accentColor);
        accentPill.putClientProperty(FlatClientProperties.STYLE, "arc: 999;");
        topRow.add(accentPill, BorderLayout.EAST);

        add(topRow, BorderLayout.NORTH);

        // Center: Bold Metric Value
        lblValue = new JLabel(initialValue);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(new Color(15, 23, 42)); // Deep charcoal
        lblValue.setBorder(new EmptyBorder(8, 0, 4, 0));
        add(lblValue, BorderLayout.CENTER);

        // Bottom: Subtitle
        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));
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
