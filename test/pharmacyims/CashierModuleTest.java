package pharmacyims;

import com.formdev.flatlaf.FlatLightLaf;
import pharmacyims.dao.MedicineDAO;
import pharmacyims.dao.SaleDAO;
import pharmacyims.dao.UserDAO;
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

// Tests the Cashier POS features: cart calculations, stock check, checkout, and receipt printing
public class CashierModuleTest {

    public static void main(String[] args) {
        System.out.println("Starting Cashier POS tests...");

        // Load FlatLaf UI
        FlatLightLaf.setup();

        MedicineDAO medDAO = new MedicineDAO();
        SaleDAO saleDAO = new SaleDAO();
        UserDAO userDAO = new UserDAO();

        // Log in as test cashier
        User cashierUser = userDAO.authenticate("cashier1", "cashier123");
        if (cashierUser == null) {
            cashierUser = new User(2, "cashier1", "cashier123", "Cashier", "Jane Doe (Dispenser 1)");
        }
        UserSession.initialize(cashierUser);
        check(UserSession.getInstance().isCashier(), "Session logged in as Cashier");

        // 1. Find 2 medicines in stock to test buying
        List<Medicine> medicines = medDAO.getAllMedicines();
        check(!medicines.isEmpty(), "Medicines list is not empty");

        Medicine med1 = null;
        Medicine med2 = null;
        for (Medicine m : medicines) {
            if (m.getQuantityInStock() > 10) {
                if (med1 == null) med1 = m;
                else if (med2 == null) {
                    med2 = m;
                    break;
                }
            }
        }

        check(med1 != null && med2 != null, "Found 2 in-stock medicines for checkout test");

        int stockBefore1 = med1.getQuantityInStock();
        int stockBefore2 = med2.getQuantityInStock();
        System.out.println("Medicine 1: " + med1.getName() + " (Stock: " + stockBefore1 + ", Price: $" + med1.getPrice() + ")");
        System.out.println("Medicine 2: " + med2.getName() + " (Stock: " + stockBefore2 + ", Price: $" + med2.getPrice() + ")");

        // 2. Test cart math: 2 units of med1, 1 unit of med2
        int qty1 = 2;
        int qty2 = 1;

        SaleItem item1 = new SaleItem(med1.getMedicineId(), med1.getName(), qty1, med1.getPrice());
        SaleItem item2 = new SaleItem(med2.getMedicineId(), med2.getName(), qty2, med2.getPrice());

        BigDecimal subtotal1 = med1.getPrice().multiply(BigDecimal.valueOf(qty1));
        BigDecimal subtotal2 = med2.getPrice().multiply(BigDecimal.valueOf(qty2));
        BigDecimal totalExpected = subtotal1.add(subtotal2);

        check(item1.getSubtotal().compareTo(subtotal1) == 0, "Item 1 subtotal is correct ($" + subtotal1 + ")");
        check(item2.getSubtotal().compareTo(subtotal2) == 0, "Item 2 subtotal is correct ($" + subtotal2 + ")");

        // 3. Process checkout and test paying with cash
        List<SaleItem> cart = new ArrayList<>();
        cart.add(item1);
        cart.add(item2);

        BigDecimal cashGiven = totalExpected.add(new BigDecimal("10.00")); // Customer gives $10 extra
        BigDecimal changeDue = cashGiven.subtract(totalExpected);

        Sale sale = new Sale(totalExpected, cashGiven, changeDue, cashierUser.getUserId());
        sale.setCashierName(cashierUser.getFullName());

        try {
            int saleId = saleDAO.processSale(sale, cart);
            check(saleId > 0, "Sale saved and generated sale ID: " + saleId);

            // Verify stock was reduced in the database
            Medicine medAfter = medDAO.getMedicineById(med1.getMedicineId());
            int expectedStockAfter = stockBefore1 - qty1;
            check(medAfter.getQuantityInStock() == expectedStockAfter,
                    "Stock for " + med1.getName() + " reduced properly from " + stockBefore1 + " to " + expectedStockAfter);

        } catch (Exception ex) {
            System.err.println("[FAIL] Error processing sale: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }

        final Medicine finalMed1 = med1;

        // 4. Test opening the receipt popup and Cashier Dashboard on the UI thread
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Testing receipt dialog popup...");
                BillReceiptDialog receipt = new BillReceiptDialog(null, sale, cart);
                check(receipt.getTitle().contains("Receipt"), "Receipt dialog opened with proper title");
                receipt.dispose();

                System.out.println("Testing Cashier Dashboard window...");
                CashierDashboard dashboard = new CashierDashboard();
                check(dashboard.getTitle().contains("POS"), "Cashier dashboard title is correct");

                // Test adding an item to the cart in the UI
                dashboard.addItemToCart(finalMed1, 1);
                check(dashboard.getCartItems().size() == 1, "Added 1 item to UI cart");
                check(dashboard.getCurrentGrandTotal().compareTo(finalMed1.getPrice()) == 0, "Cart total matches item price");

                dashboard.dispose();
                System.out.println("All Cashier POS tests passed!");
                System.exit(0);

            } catch (Exception ex) {
                System.err.println("[FAIL] UI test error: " + ex.getMessage());
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
