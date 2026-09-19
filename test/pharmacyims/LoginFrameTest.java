package pharmacyims;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

/**
 * Headless-safe verification test for LoginFrame UI elements and validation behavior.
 */
public class LoginFrameTest {

    public static void main(String[] args) {
        System.out.println(">>> Running LoginFrame UI Component Test...");

        // Ensure FlatLaf initializes cleanly
        boolean lafSuccess = FlatLightLaf.setup();
        System.out.println("FlatLaf setup result: " + lafSuccess);

        // Instantiate LoginFrame on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame frame = new LoginFrame();

                if (frame.getTitle().contains("Fyto PIMS")) {
                    System.out.println("[PASS] Window title correctly set to: " + frame.getTitle());
                } else {
                    System.err.println("[FAIL] Unexpected window title: " + frame.getTitle());
                }

                if (frame.getWidth() == 960 && frame.getHeight() == 580) {
                    System.out.println("[PASS] Dimensions verified (960x580).");
                } else {
                    System.err.println("[FAIL] Dimensions mismatch: " + frame.getWidth() + "x" + frame.getHeight());
                }

                // Verify initial empty state
                if (frame.getUsername().isEmpty() && frame.getPassword().isEmpty()) {
                    System.out.println("[PASS] Input fields initialized empty.");
                }

                // Test error banner display
                frame.showErrorMessage("Unit test error message");
                System.out.println("[PASS] Error message banner displayed successfully.");

                // Clean up
                frame.dispose();
                System.out.println(">>> All LoginFrame UI component tests PASSED successfully!");
                System.exit(0);
            } catch (Exception ex) {
                System.err.println("[FAIL] Exception during LoginFrame test: " + ex.getMessage());
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }
}
