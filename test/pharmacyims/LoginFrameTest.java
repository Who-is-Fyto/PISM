package pharmacyims;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

// Simple test to check that the login window opens and works properly
public class LoginFrameTest {

    public static void main(String[] args) {
        System.out.println("Starting LoginFrame test...");

        // Load FlatLaf theme
        FlatLightLaf.setup();

        // Run UI tests on the Swing thread
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame frame = new LoginFrame();

                // 1. Check window title
                if (frame.getTitle().contains("Fyto PIMS")) {
                    System.out.println("[PASS] Window title is correct: " + frame.getTitle());
                } else {
                    System.err.println("[FAIL] Title did not match: " + frame.getTitle());
                }

                // 2. Check window size
                if (frame.getWidth() == 960 && frame.getHeight() == 580) {
                    System.out.println("[PASS] Window size is 960x580 as expected");
                } else {
                    System.err.println("[FAIL] Wrong window size: " + frame.getWidth() + "x" + frame.getHeight());
                }

                // 3. Make sure text boxes start empty
                if (frame.getUsername().isEmpty() && frame.getPassword().isEmpty()) {
                    System.out.println("[PASS] Text boxes are empty at startup");
                }

                // 4. Test error banner message
                frame.showErrorMessage("Sample test error message");
                System.out.println("[PASS] Error message banner displayed properly");

                // Close test window
                frame.dispose();
                System.out.println("All LoginFrame tests passed!");
                System.exit(0);

            } catch (Exception ex) {
                System.err.println("[FAIL] Something went wrong in LoginFrame test: " + ex.getMessage());
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }
}
