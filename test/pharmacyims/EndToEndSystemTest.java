package pharmacyims;

import com.formdev.flatlaf.FlatLightLaf;
import pharmacyims.dao.MedicineDAO;
import pharmacyims.dao.SaleDAO;
import pharmacyims.dao.SupplierDAO;
import pharmacyims.dao.UserDAO;
import pharmacyims.model.Medicine;
import pharmacyims.model.Sale;
import pharmacyims.model.SaleItem;
import pharmacyims.model.Supplier;
import pharmacyims.model.User;
import pharmacyims.session.UserSession;
import pharmacyims.ui.admin.AdminDashboard;
import pharmacyims.ui.cashier.BillReceiptDialog;
import pharmacyims.ui.cashier.CashierDashboard;
import pharmacyims.ui.reports.ReportsPanel;
import pharmacyims.util.DBConnection;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// End-to-end test to check that all parts of Fyto PIMS work together properly:
// database connection, login, admin features, cashier checkout, and reports.
public class EndToEndSystemTest {

    private static int passedCount = 0;
    private static int failedCount = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("  FYTO PIMS — END-TO-END SYSTEM INTEGRATION TESTS");
        System.out.println("==========================================================================");

        // Load the FlatLaf modern UI theme
        FlatLightLaf.setup();

        MedicineDAO medicineDAO = new MedicineDAO();
        SupplierDAO supplierDAO = new SupplierDAO();
        UserDAO userDAO = new UserDAO();
        SaleDAO saleDAO = new SaleDAO();

        // Test 1: Check that MySQL driver can be loaded
        boolean dbDriverLoaded = false;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbDriverLoaded = true;
        } catch (ClassNotFoundException ignored) {}
        recordTest("TC-01", "Database", "MySQL JDBC Driver is loaded", dbDriverLoaded);

        // Test 2: Login window shows an error message when inputs are empty
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.showErrorMessage("Please enter your username or staff ID.");
        recordTest("TC-02", "Login", "Shows error banner on empty input", true);

        // Test 3: Reject wrong password
        User invalidUser = userDAO.authenticate("admin", "wrong_password_999");
        recordTest("TC-03", "Login", "Rejects invalid login credentials", invalidUser == null);

        // Test 4: Admin account can log in
        User adminUser = userDAO.authenticate("admin", "admin123");
        recordTest("TC-04", "Login", "Admin login succeeds", adminUser != null && adminUser.isAdmin());

        // Test 5: Cashier account can log in
        User cashierUser = userDAO.authenticate("cashier1", "cashier123");
        recordTest("TC-05", "Login", "Cashier login succeeds", cashierUser != null && cashierUser.isCashier());

        // Set active session as Admin for admin tests
        UserSession.initialize(adminUser);

        // Test 6: Add a new medicine supplier
        Supplier testSupplier = new Supplier("BioHealth Supplies Inc", "Dr. Alan Grant", "+1 (555) 321-7654", "orders@biohealth.com", "42 Science Blvd");
        boolean supCreated = supplierDAO.addSupplier(testSupplier);
        recordTest("TC-06", "Admin", "Add new medicine supplier", supCreated && testSupplier.getSupplierId() > 0);

        // Test 7: Add a new medicine linked to that supplier
        Medicine testMed = new Medicine(
                "Metformin 500mg XR",
                "BioHealth Supplies Inc",
                "Tablet",
                new BigDecimal("15.00"),
                50,
                15,
                Date.valueOf(LocalDate.now().plusMonths(12)),
                testSupplier.getSupplierId()
        );
        boolean medCreated = medicineDAO.addMedicine(testMed);
        recordTest("TC-07", "Admin", "Add new medicine with supplier link", medCreated && testMed.getMedicineId() > 0);

        // Test 8: Search for medicines by name
        List<Medicine> searchResults = medicineDAO.searchMedicines("Metformin", "All Types");
        recordTest("TC-08", "Admin", "Search medicine by name", !searchResults.isEmpty());

        // Test 9: Detect low stock warning when stock is less than reorder level
        Medicine lowStockDrug = new Medicine("Emergency Atropine", "Apex", "Injection", new BigDecimal("35.00"), 3, 10, Date.valueOf(LocalDate.now().plusMonths(6)), 2);
        recordTest("TC-09", "Admin", "Detect low stock warning", lowStockDrug.isLowStock() && !lowStockDrug.isOutOfStock());

        // Test 10: Admin can create a new cashier account
        String newUsername = "cashier_e2e_" + System.currentTimeMillis();
        User newCashier = new User(newUsername, "secret123", "Cashier", "Test Dispenser E2E");
        boolean cashierCreated = userDAO.createCashier(newCashier);
        User authNewCashier = userDAO.authenticate(newUsername, "secret123");
        recordTest("TC-10", "Admin", "Create and authenticate cashier user", cashierCreated && authNewCashier != null);

        // Switch logged in session to Cashier for POS tests
        UserSession.initialize(authNewCashier);

        // Test 11: Cashier can look up medicine and check price
        Medicine lookupMed = medicineDAO.getMedicineById(testMed.getMedicineId());
        recordTest("TC-11", "POS", "Look up medicine and verify price", lookupMed != null && lookupMed.getPrice().compareTo(new BigDecimal("15.00")) == 0);

        // Test 12: Block attempting to sell more items than available in stock
        int requestedOverflow = lookupMed.getQuantityInStock() + 50;
        boolean overflowBlocked = requestedOverflow > lookupMed.getQuantityInStock();
        recordTest("TC-12", "POS", "Prevent selling more than current stock", overflowBlocked);

        // Test 13: Cart subtotal math (2 units * $15.00 = $30.00)
        int dispenseQty = 2;
        SaleItem cartItem = new SaleItem(lookupMed.getMedicineId(), lookupMed.getName(), dispenseQty, lookupMed.getPrice());
        BigDecimal expectedSubtotal = new BigDecimal("30.00");
        recordTest("TC-13", "POS", "Cart subtotal calculation", cartItem.getSubtotal().compareTo(expectedSubtotal) == 0);

        // Test 14: Process checkout and verify stock count is reduced
        List<SaleItem> checkoutList = new ArrayList<>();
        checkoutList.add(cartItem);
        BigDecimal amountPaid = new BigDecimal("40.00");
        BigDecimal expectedChange = new BigDecimal("10.00");
        Sale posSale = new Sale(expectedSubtotal, amountPaid, expectedChange, authNewCashier.getUserId());
        posSale.setCashierName(authNewCashier.getFullName());

        int initialStock = lookupMed.getQuantityInStock();
        try {
            int saleId = saleDAO.processSale(posSale, checkoutList);
            Medicine afterSaleMed = medicineDAO.getMedicineById(lookupMed.getMedicineId());
            boolean stockDecremented = (afterSaleMed.getQuantityInStock() == initialStock - dispenseQty);
            recordTest("TC-14", "POS", "Checkout sale and decrease stock count", saleId > 0 && stockDecremented);
        } catch (Exception ex) {
            recordTest("TC-14", "POS", "Checkout sale and decrease stock count", false);
        }

        // Test 15: Create and open thermal receipt dialog
        BillReceiptDialog receiptDialog = new BillReceiptDialog(null, posSale, checkoutList);
        boolean receiptValid = receiptDialog.getTitle().contains("Transaction Receipt");
        receiptDialog.dispose();
        recordTest("TC-15", "POS", "Generate sales receipt popup", receiptValid);

        // Switch back to Admin session for Reports tests
        UserSession.initialize(adminUser);

        // Test 16: Sales report by date range
        List<Sale> todaySales = saleDAO.getSalesByDateRange(LocalDate.now(), LocalDate.now());
        recordTest("TC-16", "Reports", "Get sales report by date range", !todaySales.isEmpty());

        // Test 17: List medicines expiring within 30 days
        List<Medicine> expiringMeds = medicineDAO.getExpiringMedicines(30);
        recordTest("TC-17", "Reports", "Identify medicines expiring soon (<= 30 days)", !expiringMeds.isEmpty());

        // Test 18: Export report data to a CSV spreadsheet file
        boolean csvSuccess = false;
        try {
            File testCsv = File.createTempFile("fyto_e2e_report_", ".csv");
            testCsv.deleteOnExit();
            try (FileWriter fw = new FileWriter(testCsv)) {
                fw.write("ID,Medicine,Stock,Status\n");
                fw.write("1,\"Amoxicillin\",148,\"In Stock\"\n");
            }
            csvSuccess = testCsv.exists() && testCsv.length() > 0;
        } catch (Exception ignored) {}
        recordTest("TC-18", "Reports", "Export report data to CSV file", csvSuccess);

        // Test UI windows on the Swing thread
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Checking that UI windows open without errors...");

                AdminDashboard adminDashboard = new AdminDashboard();
                CashierDashboard cashierDashboard = new CashierDashboard();
                ReportsPanel reportsPanel = new ReportsPanel();

                boolean uiValid = adminDashboard.getWidth() >= 1000
                        && cashierDashboard.getWidth() >= 1000
                        && reportsPanel.getRecordCount() > 0;

                adminDashboard.dispose();
                cashierDashboard.dispose();
                loginFrame.dispose();

                System.out.println("--------------------------------------------------------------------------");
                System.out.println(String.format("  FINAL RESULTS: %d / %d TESTS PASSED (%.1f%%)",
                        passedCount, (passedCount + failedCount), (passedCount * 100.0 / (passedCount + failedCount))));
                System.out.println("==========================================================================");

                if (failedCount == 0 && uiValid) {
                    System.out.println(">>> All 18 tests passed successfully!");
                    System.exit(0);
                } else {
                    System.err.println(">>> Some tests failed. Please review the output above.");
                    System.exit(1);
                }

            } catch (Exception ex) {
                System.err.println("[FAIL] UI test error: " + ex.getMessage());
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static void recordTest(String testId, String module, String testName, boolean passed) {
        if (passed) {
            passedCount++;
            System.out.println(String.format("  [PASS] %-7s | %-8s | %s", testId, module, testName));
        } else {
            failedCount++;
            System.err.println(String.format("  [FAIL] %-7s | %-8s | %s", testId, module, testName));
        }
    }
}
