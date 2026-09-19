package pharmacyims;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

/**
 * Fyto PIMS — Pharmacy Inventory Management System
 * Main Application Bootstrap Entry Point.
 */
public class PharmacyIMS {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // 1. Initialize FlatLaf Modern Look and Feel
        FlatLightLaf.setup();

        // 2. Global UI Defaults & Polish
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("Table.alternateRowColor", new Color(248, 250, 252));
        UIManager.put("Table.rowHeight", 28);

        // 3. Launch Fyto PIMS Login Window on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
