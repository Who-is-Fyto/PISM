package pharmacyims.ui.common;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

// Table cell renderer for right-aligned currency values.
public class CurrencyTableCellRenderer extends DefaultTableCellRenderer {

    private final NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);

    public CurrencyTableCellRenderer() {
        setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof BigDecimal) {
            setText(formatter.format(value));
        } else if (value instanceof Number) {
            setText(formatter.format(((Number) value).doubleValue()));
        } else if (value != null) {
            try {
                double val = Double.parseDouble(value.toString());
                setText(formatter.format(val));
            } catch (NumberFormatException e) {
                setText(value.toString());
            }
        } else {
            setText("$0.00");
        }

        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setForeground(new Color(15, 23, 42));

        if (!isSelected) {
            setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
        }

        return this;
    }
}
