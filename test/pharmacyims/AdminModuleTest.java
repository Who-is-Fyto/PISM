package pharmacyims;

import com.formdev.flatlaf.FlatLightLaf;
import pharmacyims.dao.MedicineDAO;
import pharmacyims.dao.SupplierDAO;
import pharmacyims.dao.UserDAO;
import pharmacyims.model.Medicine;
import pharmacyims.model.Supplier;
import pharmacyims.model.User;
import pharmacyims.session.UserSession;
import pharmacyims.ui.admin.AdminDashboard;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * Headless-safe verification test for Phase 3: Administrator Module & Dashboard UI.
 */
public class AdminModuleTest {

    public static void main(String[] args) {
        System.out.println(">>> Starting Fyto PIMS Phase 3: Admin Module Verification Test...");

        // 1. Initialize FlatLaf
        FlatLightLaf.setup();

        // 2. Set active admin session
        User adminUser = new User(1, "admin", "admin123", "Admin", "System Administrator");
        UserSession.initialize(adminUser);
        assertEq(UserSession.getInstance().isAdmin(), true, "UserSession must identify admin user");

        // 3. Test DAOs directly
        MedicineDAO medDAO = new MedicineDAO();
        SupplierDAO supDAO = new SupplierDAO();
        UserDAO userDAO = new UserDAO();

        // Test Medicine CRUD & metrics
        int initialMedCount = medDAO.getTotalCount();
        System.out.println("[INFO] Initial medicines in catalog: " + initialMedCount);
        assertEq(initialMedCount >= 10, true, "Should have initial seed medicines");

        Medicine newMed = new Medicine(
                "Azithromycin 250mg",
                "Apex Bioscience",
                "Capsule",
                new BigDecimal("15.75"),
                80,
                20,
                Date.valueOf(LocalDate.now().plusMonths(18)),
                2
        );
        boolean medAdded = medDAO.addMedicine(newMed);
        assertEq(medAdded, true, "MedicineDAO.addMedicine should succeed");
        assertEq(newMed.getMedicineId() > 0, true, "Generated medicine ID should be > 0");

        Medicine fetchedMed = medDAO.getMedicineById(newMed.getMedicineId());
        assertEq(fetchedMed != null, true, "Fetched medicine must not be null");
        assertEq("Azithromycin 250mg".equals(fetchedMed.getName()), true, "Medicine name should match");

        // Test Supplier CRUD
        int initialSupCount = supDAO.getAllSuppliers().size();
        System.out.println("[INFO] Initial suppliers in catalog: " + initialSupCount);
        assertEq(initialSupCount >= 4, true, "Should have initial seed suppliers");

        Supplier newSup = new Supplier("BioHealth Logistics", "Alice Cooper", "+1 (555) 777-8899", "alice@biohealth.com", "99 Harbor Boulevard");
        boolean supAdded = supDAO.addSupplier(newSup);
        assertEq(supAdded, true, "SupplierDAO.addSupplier should succeed");
        assertEq(newSup.getSupplierId() > 0, true, "Generated supplier ID should be > 0");

        // Test Cashier User creation & authentication
        String testCashierUser = "test_cashier_" + System.currentTimeMillis();
        User newCashier = new User(testCashierUser, "pass12345", "Cashier", "Test Dispenser");
        boolean cashierCreated = userDAO.createCashier(newCashier);
        assertEq(cashierCreated, true, "UserDAO.createCashier should succeed");

        User authenticatedCashier = userDAO.authenticate(testCashierUser, "pass12345");
        assertEq(authenticatedCashier != null, true, "Newly created cashier must authenticate");
        assertEq(authenticatedCashier.isCashier(), true, "Authenticated user must have Cashier role");

        // Test Admin protection from deletion
        boolean adminDeleted = userDAO.deleteUser(adminUser.getUserId());
        assertEq(!adminDeleted, true, "Admin account must be protected from deletion");

        // 4. Test UI Instantiation on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("[INFO] Instantiating AdminDashboard on Swing EDT...");
                AdminDashboard dashboard = new AdminDashboard();

                assertEq(dashboard.getTitle().contains("Fyto PIMS"), true, "Dashboard window title check");
                assertEq(dashboard.getWidth() >= 1000, true, "Dashboard width check");

                // Test global refresh and metrics recalculation
                dashboard.refreshAll();
                System.out.println("[PASS] AdminDashboard instantiated and refreshed all tabs successfully.");

                // Clean up UI
                dashboard.dispose();

                System.out.println(">>> All Phase 3: Admin Module Verification Tests PASSED successfully! (100%)");
                System.exit(0);
            } catch (Exception ex) {
                System.err.println("[FAIL] Exception during AdminDashboard UI test: " + ex.getMessage());
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static void assertEq(boolean condition, boolean expected, String testName) {
        if (condition == expected) {
            System.out.println("[PASS] " + testName);
        } else {
            System.err.println("[FAIL] " + testName + " (expected: " + expected + ", got: " + condition + ")");
            System.exit(1);
        }
    }
}
