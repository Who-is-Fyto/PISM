package pharmacyims;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

// Main application entry point for Fyto PIMS.
public class PharmacyIMS {

    public static void main(String[] args) {
        // Initialize FlatLaf Look and Feel
        FlatLightLaf.setup();

        // Configure global UI theme defaults: serene blues, healing greens, and neutral base
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("Component.focusColor", new Color(2, 132, 199, 90)); // Serene sky blue focus
        UIManager.put("Component.borderColor", new Color(226, 232, 240));
        UIManager.put("Table.alternateRowColor", new Color(248, 250, 252)); // Crisp neutral tint
        UIManager.put("Table.selectionBackground", new Color(224, 242, 254)); // Tranquil ice blue selection
        UIManager.put("Table.selectionForeground", new Color(15, 23, 42));
        UIManager.put("Table.gridColor", new Color(241, 245, 249));
        UIManager.put("Table.rowHeight", 30);
        UIManager.put("TableHeader.background", new Color(241, 245, 249));
        UIManager.put("TableHeader.foreground", new Color(51, 65, 85));

        // Launch login window on the Swing event thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
