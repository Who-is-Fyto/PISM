package pharmacyims;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

// Main application entry point for Fyto PIMS.
public class PharmacyIMS {

    public static void main(String[] args) {
        // Initialize FlatLaf Look and Feel
        FlatLightLaf.setup();

        // Configure global UI theme defaults
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("Table.alternateRowColor", new Color(248, 250, 252));
        UIManager.put("Table.rowHeight", 28);

        // Launch login window on the Swing event thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
