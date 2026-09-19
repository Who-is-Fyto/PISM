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
import pharmacyims.ui.reports.ReportsPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Tests the Reports module: sales totals, low stock alerts, expiring medicines, and CSV export
public class ReportingModuleTest {

    public static void main(String[] args) {
        System.out.println("Starting Reports & Analytics tests...");

        // Load FlatLaf theme
        FlatLightLaf.setup();

        SaleDAO saleDAO = new SaleDAO();
        MedicineDAO medicineDAO = new MedicineDAO();
        UserDAO userDAO = new UserDAO();

        // Log in as test admin
        User adminUser = userDAO.authenticate("admin", "admin123");
        if (adminUser == null) {
            adminUser = new User(1, "admin", "admin123", "Admin", "System Administrator");
        }
        UserSession.initialize(adminUser);

        // 1. Test fetching sales history
        LocalDate today = LocalDate.now();
        List<Sale> allSales = saleDAO.getAllSales();
        check(!allSales.isEmpty(), "Sales history has records");
        System.out.println("Found " + allSales.size() + " sales records in database");

        // Test filtering by past 7 days
        List<Sale> past7Days = saleDAO.getSalesByDateRange(today.minusDays(7), today);
        check(!past7Days.isEmpty(), "Found sales within the last 7 days");

        // Calculate total revenue from the 7-day sales
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Sale s : past7Days) {
            if (s.getTotalAmount() != null) {
                totalRevenue = totalRevenue.add(s.getTotalAmount());
            }
        }
        check(totalRevenue.compareTo(BigDecimal.ZERO) > 0, "Total revenue is positive ($" + totalRevenue + ")");

        // Test getting items for a specific sale
        int testSaleId = allSales.get(0).getSaleId();
        List<SaleItem> items = saleDAO.getSaleItems(testSaleId);
        check(!items.isEmpty(), "Sale #" + testSaleId + " has line items");

        // 2. Test finding medicines running low on stock
        List<Medicine> lowStockMeds = medicineDAO.getLowStockMedicines();
        check(!lowStockMeds.isEmpty(), "Found medicines needing restock");
        for (Medicine m : lowStockMeds) {
            check(m.getQuantityInStock() <= m.getReorderLevel(),
                    m.getName() + " is at or below reorder level (stock: " + m.getQuantityInStock() + ", level: " + m.getReorderLevel() + ")");
        }
        System.out.println("Low stock count: " + lowStockMeds.size());

        // 3. Test finding medicines expiring soon (within 30 days)
        List<Medicine> expiringMeds = medicineDAO.getExpiringMedicines(30);
        check(!expiringMeds.isEmpty(), "Found medicines expiring within 30 days");
        System.out.println("Expiring medicines count: " + expiringMeds.size());

        // 4. Test creating a CSV spreadsheet file
        try {
            File testCsv = File.createTempFile("fyto_report_", ".csv");
            testCsv.deleteOnExit();

            try (FileWriter fw = new FileWriter(testCsv)) {
                fw.write("Sale ID,Date,Cashier,Revenue\n");
                for (Sale s : past7Days) {
                    fw.write(String.format("%d,%s,\"%s\",%.2f\n",
                            s.getSaleId(),
                            s.getSaleDate() != null ? s.getSaleDate().toString() : "",
                            s.getCashierName(),
                            s.getTotalAmount().doubleValue()
                    ));
                }
            }

            check(testCsv.exists() && testCsv.length() > 0, "CSV file was created and written successfully");
            System.out.println("Test CSV file size: " + testCsv.length() + " bytes");

        } catch (Exception ex) {
            System.err.println("[FAIL] CSV export error: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }

        // 5. Test opening ReportsPanel on the Swing UI thread
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Testing ReportsPanel UI component...");
                ReportsPanel panel = new ReportsPanel();
                check(panel.getRecordCount() > 0, "Reports panel loaded records into table");

                DefaultTableModel model = panel.getReportTableModel();
                check(model.getColumnCount() >= 5, "Reports table has expected columns");

                System.out.println("All Reports tests passed!");
                System.exit(0);

            } catch (Exception ex) {
                System.err.println("[FAIL] Error opening ReportsPanel: " + ex.getMessage());
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
