package pharmacyims.ui.common;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom TableCellRenderer rendering colored rounded pill badges for statuses.
 */
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    private final JLabel badgeLabel;
    private final JPanel container;

    public StatusBadgeRenderer() {
        badgeLabel = new JLabel();
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badgeLabel.setOpaque(true);
        badgeLabel.setBorder(new EmptyBorder(3, 10, 3, 10));
        badgeLabel.putClientProperty(FlatClientProperties.STYLE, "arc: 999;"); // Pill shape

        container = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        container.setOpaque(true);
        container.add(badgeLabel);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        String text = value != null ? value.toString().trim() : "";
        badgeLabel.setText(text);

        // Color mapping
        if (text.equalsIgnoreCase("In Stock") || text.equalsIgnoreCase("Active") || text.equalsIgnoreCase("Good")) {
            badgeLabel.setBackground(new Color(220, 252, 231)); // Emerald tint (#DCFCE7)
            badgeLabel.setForeground(new Color(21, 128, 61));    // Emerald text (#15803D)
        } else if (text.equalsIgnoreCase("Low Stock") || text.equalsIgnoreCase("Warning")) {
            badgeLabel.setBackground(new Color(254, 243, 199)); // Amber tint (#FEF3C7)
            badgeLabel.setForeground(new Color(180, 83, 9));     // Amber text (#B45309)
        } else if (text.equalsIgnoreCase("Out of Stock") || text.equalsIgnoreCase("Critical") || text.equalsIgnoreCase("Expired")) {
            badgeLabel.setBackground(new Color(254, 226, 226)); // Red tint (#FEE2E2)
            badgeLabel.setForeground(new Color(185, 28, 28));    // Red text (#B91C1C)
        } else {
            badgeLabel.setBackground(new Color(241, 245, 249)); // Slate tint (#F1F5F9)
            badgeLabel.setForeground(new Color(71, 85, 105));    // Slate text (#475569)
        }

        if (isSelected) {
            container.setBackground(table.getSelectionBackground());
        } else {
            container.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
        }

        return container;
    }
}
