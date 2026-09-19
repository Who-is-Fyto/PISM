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

// Tests the Admin features: adding medicines, adding suppliers, creating cashier accounts, and opening the dashboard
public class AdminModuleTest {

    public static void main(String[] args) {
        System.out.println("Starting Admin Module tests...");

        // Set up the UI theme
        FlatLightLaf.setup();

        MedicineDAO medDAO = new MedicineDAO();
        SupplierDAO supDAO = new SupplierDAO();
        UserDAO userDAO = new UserDAO();

        // Log in as test admin
        User adminUser = userDAO.authenticate("admin", "admin123");
        if (adminUser == null) {
            adminUser = new User(1, "admin", "admin123", "Admin", "System Administrator");
        }
        UserSession.initialize(adminUser);
        check(UserSession.getInstance().isAdmin(), "Session recognized admin user");

        // 1. Check medicine catalog
        int initialMedCount = medDAO.getTotalCount();
        System.out.println("Current medicines in catalog: " + initialMedCount);
        check(initialMedCount >= 10, "Found initial medicines in database");

        // 2. Add a new medicine
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
        check(medAdded, "Added new medicine to catalog");
        check(newMed.getMedicineId() > 0, "Medicine got an ID assigned");

        // 3. Look up the newly added medicine
        Medicine fetchedMed = medDAO.getMedicineById(newMed.getMedicineId());
        check(fetchedMed != null, "Successfully retrieved the medicine");
        check("Azithromycin 250mg".equals(fetchedMed.getName()), "Medicine name matches what was entered");

        // 4. Test supplier list and adding a new supplier
        int initialSupCount = supDAO.getAllSuppliers().size();
        System.out.println("Current suppliers count: " + initialSupCount);
        check(initialSupCount >= 4, "Found initial suppliers in database");

        Supplier newSup = new Supplier("BioHealth Logistics", "Alice Cooper", "+1 (555) 777-8899", "alice@biohealth.com", "99 Harbor Boulevard");
        boolean supAdded = supDAO.addSupplier(newSup);
        check(supAdded, "Added new supplier");
        check(newSup.getSupplierId() > 0, "Supplier got an ID assigned");

        // 5. Test creating a new cashier staff account
        String testCashierUser = "cashier_test_" + System.currentTimeMillis();
        User newCashier = new User(testCashierUser, "pass12345", "Cashier", "Test Dispenser");
        boolean cashierCreated = userDAO.createCashier(newCashier);
        check(cashierCreated, "Created new cashier account");

        // Verify the new cashier can log in
        User authenticatedCashier = userDAO.authenticate(testCashierUser, "pass12345");
        check(authenticatedCashier != null, "New cashier can log in");
        check(authenticatedCashier.isCashier(), "New cashier has the Cashier role");

        // 6. Make sure admin cannot be accidentally deleted
        boolean adminDeleted = userDAO.deleteUser(adminUser.getUserId());
        check(!adminDeleted, "Admin account cannot be deleted");

        // 7. Test opening the Admin Dashboard window
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Opening AdminDashboard on Swing thread...");
                AdminDashboard dashboard = new AdminDashboard();

                check(dashboard.getTitle().contains("Fyto PIMS"), "Dashboard title is correct");
                check(dashboard.getWidth() >= 1000, "Dashboard opened with proper size");

                // Refresh data on dashboard
                dashboard.refreshAll();
                System.out.println("[PASS] Dashboard refreshed tables without error");

                // Close test window
                dashboard.dispose();

                System.out.println("All Admin Module tests passed!");
                System.exit(0);

            } catch (Exception ex) {
                System.err.println("[FAIL] Error opening AdminDashboard: " + ex.getMessage());
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static void check(boolean condition, String testName) {
        if (condition) {
            System.out.println("[PASS] " + testName);
        } else {
            System.err.println("[FAIL] " + testName);
            System.exit(1);
        }
    }
}
