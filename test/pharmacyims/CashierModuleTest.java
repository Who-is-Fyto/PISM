package pharmacyims;

import com.formdev.flatlaf.FlatLightLaf;
import pharmacyims.dao.MedicineDAO;
import pharmacyims.dao.SaleDAO;
import pharmacyims.model.Medicine;
import pharmacyims.model.Sale;
import pharmacyims.model.SaleItem;
import pharmacyims.model.User;
import pharmacyims.session.UserSession;
import pharmacyims.ui.cashier.BillReceiptDialog;
import pharmacyims.ui.cashier.CashierDashboard;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Headless-safe verification test for Phase 4: Cashier POS, Cart Calculations,
 * Atomic Checkout, and Receipt Generation.
 */
public class CashierModuleTest {

    public static void main(String[] args) {
        System.out.println(">>> Starting Fyto PIMS Phase 4: Cashier Module Verification Test...");

        // 1. Initialize FlatLaf
        FlatLightLaf.setup();

        // 2. Set active cashier session
        User cashierUser = new User(2, "cashier1", "cashier123", "Cashier", "Jane Doe (Dispenser 1)");
        UserSession.initialize(cashierUser);
        assertEq(UserSession.getInstance().isCashier(), true, "UserSession must identify Cashier role");

        MedicineDAO medDAO = new MedicineDAO();
        SaleDAO saleDAO = new SaleDAO();

        // 3. Find test medicines for dispensing
        List<Medicine> medicines = medDAO.getAllMedicines();
        assertEq(!medicines.isEmpty(), true, "Medicine catalog must not be empty");

        Medicine testMed1 = null;
        Medicine testMed2 = null;
        for (Medicine m : medicines) {
            if (m.getQuantityInStock() > 10) {
                if (testMed1 == null) testMed1 = m;
                else if (testMed2 == null) {
                    testMed2 = m;
                    break;
                }
            }
        }

        assertEq(testMed1 != null && testMed2 != null, true, "Found 2 valid in-stock medicines for checkout test");

        int initialStock1 = testMed1.getQuantityInStock();
        int initialStock2 = testMed2.getQuantityInStock();
        System.out.println("[INFO] Test Med 1: " + testMed1.getName() + " (Stock: " + initialStock1 + ", Price: $" + testMed1.getPrice() + ")");
        System.out.println("[INFO] Test Med 2: " + testMed2.getName() + " (Stock: " + initialStock2 + ", Price: $" + testMed2.getPrice() + ")");

        // 4. Test Cart Calculations & Stock Deductions
        int qty1 = 2;
        int qty2 = 1;

        SaleItem item1 = new SaleItem(testMed1.getMedicineId(), testMed1.getName(), qty1, testMed1.getPrice());
        SaleItem item2 = new SaleItem(testMed2.getMedicineId(), testMed2.getName(), qty2, testMed2.getPrice());

        BigDecimal expectedSubtotal1 = testMed1.getPrice().multiply(BigDecimal.valueOf(qty1));
        BigDecimal expectedSubtotal2 = testMed2.getPrice().multiply(BigDecimal.valueOf(qty2));
        BigDecimal expectedGrandTotal = expectedSubtotal1.add(expectedSubtotal2);

        assertEq(item1.getSubtotal().compareTo(expectedSubtotal1) == 0, true, "Item 1 subtotal calculation check");
        assertEq(item2.getSubtotal().compareTo(expectedSubtotal2) == 0, true, "Item 2 subtotal calculation check");

        // 5. Test Transactional Checkout in SaleDAO
        List<SaleItem> checkoutItems = new ArrayList<>();
        checkoutItems.add(item1);
        checkoutItems.add(item2);

        BigDecimal amountPaid = expectedGrandTotal.add(new BigDecimal("10.00")); // $10 overpayment
        BigDecimal expectedChange = amountPaid.subtract(expectedGrandTotal);

        Sale sale = new Sale(expectedGrandTotal, amountPaid, expectedChange, cashierUser.getUserId());
        sale.setCashierName(cashierUser.getFullName());

        try {
            int saleId = saleDAO.processSale(sale, checkoutItems);
            assertEq(saleId > 0, true, "SaleDAO.processSale should return positive generated saleId");
            assertEq(sale.getSaleId() == saleId, true, "Sale object updated with generated saleId");

            // Verify stock reduction in MedicineDAO
            Medicine refreshedMed1 = medDAO.getMedicineById(testMed1.getMedicineId());
            int expectedStockAfter = initialStock1 - qty1;
            assertEq(refreshedMed1.getQuantityInStock() == expectedStockAfter, true,
                    "Stock for " + testMed1.getName() + " correctly decremented from " + initialStock1 + " to " + expectedStockAfter);

            System.out.println("[PASS] Inventory stock deducted accurately via transactional checkout.");

        } catch (Exception ex) {
            System.err.println("[FAIL] processSale threw exception: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }

        final Medicine finalMed1 = testMed1;

        // 6. Test Receipt Dialog generation
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("[INFO] Testing BillReceiptDialog construction...");
                BillReceiptDialog receipt = new BillReceiptDialog(null, sale, checkoutItems);
                assertEq(receipt.getTitle().contains("Transaction Receipt"), true, "Receipt dialog title check");
                receipt.dispose();
                System.out.println("[PASS] BillReceiptDialog generated successfully.");

                // 7. Test CashierDashboard UI instantiation on EDT
                System.out.println("[INFO] Testing CashierDashboard UI construction...");
                CashierDashboard dashboard = new CashierDashboard();
                assertEq(dashboard.getTitle().contains("POS"), true, "CashierDashboard title check");

                // Test adding to cart in dashboard
                dashboard.addItemToCart(finalMed1, 1);
                assertEq(dashboard.getCartItems().size() == 1, true, "Cart contains 1 item");
                assertEq(dashboard.getCurrentGrandTotal().compareTo(finalMed1.getPrice()) == 0, true, "Dashboard grand total matches unit price");

                // Clean up dashboard
                dashboard.dispose();
                System.out.println("[PASS] CashierDashboard instantiated and validated on Swing EDT.");

                System.out.println(">>> All Phase 4: Cashier Module Verification Tests PASSED successfully! (100%)");
                System.exit(0);

            } catch (Exception ex) {
                System.err.println("[FAIL] UI test exception: " + ex.getMessage());
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
