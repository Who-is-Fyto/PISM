package pharmacyims.ui.common;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

// Table cell renderer displaying colored rounded badges for status indicators.
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    private final JLabel badgeLabel;
    private final JPanel container;

    public StatusBadgeRenderer() {
        badgeLabel = new JLabel();
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badgeLabel.setOpaque(true);
        badgeLabel.setBorder(new EmptyBorder(3, 10, 3, 10));
        badgeLabel.putClientProperty(FlatClientProperties.STYLE, "arc: 999;");

        container = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        container.setOpaque(true);
        container.add(badgeLabel);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        String text = value != null ? value.toString().trim() : "";
        badgeLabel.setText(text);

        // Status color styling: healing greens, calm ambers, muted roses, and crisp neutrals
        if (text.equalsIgnoreCase("In Stock") || text.equalsIgnoreCase("Active") || text.equalsIgnoreCase("Good")) {
            badgeLabel.setBackground(new Color(236, 253, 245)); // Healing mint wash
            badgeLabel.setForeground(new Color(4, 120, 87));    // Botanical green
            badgeLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(167, 243, 208), 1, true),
                    new EmptyBorder(2, 9, 2, 9)
            ));
        } else if (text.equalsIgnoreCase("Low Stock") || text.equalsIgnoreCase("Warning")) {
            badgeLabel.setBackground(new Color(254, 243, 199)); // Calm honey wash
            badgeLabel.setForeground(new Color(180, 83, 9));    // Soft ochre
            badgeLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(253, 230, 138), 1, true),
                    new EmptyBorder(2, 9, 2, 9)
            ));
        } else if (text.equalsIgnoreCase("Out of Stock") || text.equalsIgnoreCase("Critical") || text.equalsIgnoreCase("Expired")) {
            badgeLabel.setBackground(new Color(255, 241, 242)); // Soft rose wash
            badgeLabel.setForeground(new Color(225, 29, 72));   // Muted crimson
            badgeLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(254, 205, 211), 1, true),
                    new EmptyBorder(2, 9, 2, 9)
            ));
        } else {
            badgeLabel.setBackground(new Color(241, 245, 249));
            badgeLabel.setForeground(new Color(71, 85, 105));
            badgeLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                    new EmptyBorder(2, 9, 2, 9)
            ));
        }

        if (isSelected) {
            container.setBackground(table.getSelectionBackground());
        } else {
            container.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
        }

        return container;
    }
}
