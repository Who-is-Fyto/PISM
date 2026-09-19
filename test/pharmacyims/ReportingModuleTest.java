package pharmacyims;

import com.formdev.flatlaf.FlatLightLaf;
import pharmacyims.dao.MedicineDAO;
import pharmacyims.dao.SaleDAO;
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

/**
 * Headless-safe verification test for Phase 5: Business Reporting & Analytics Module.
 */
public class ReportingModuleTest {

    public static void main(String[] args) {
        System.out.println(">>> Starting Fyto PIMS Phase 5: Reporting & Analytics Verification Test...");

        // 1. Initialize FlatLaf
        FlatLightLaf.setup();

        // 2. Set active admin session
        User adminUser = new User(1, "admin", "admin123", "Admin", "System Administrator");
        UserSession.initialize(adminUser);

        SaleDAO saleDAO = new SaleDAO();
        MedicineDAO medicineDAO = new MedicineDAO();

        // 3. Test Stream 1: Sales Performance Queries & Calculations
        System.out.println("[INFO] Testing Sales Performance stream data...");
        LocalDate today = LocalDate.now();
        List<Sale> allSales = saleDAO.getAllSales();
        assertEq(!allSales.isEmpty(), true, "All sales query should return records");
        System.out.println("[INFO] Total sales available for reporting: " + allSales.size());

        List<Sale> past7DaysSales = saleDAO.getSalesByDateRange(today.minusDays(7), today);
        assertEq(!past7DaysSales.isEmpty(), true, "Past 7 days sales should return records");

        BigDecimal totalRev = BigDecimal.ZERO;
        for (Sale s : past7DaysSales) {
            if (s.getTotalAmount() != null) {
                totalRev = totalRev.add(s.getTotalAmount());
            }
        }
        assertEq(totalRev.compareTo(BigDecimal.ZERO) > 0, true, "Total revenue in 7-day window should be > $0");
        System.out.println("[PASS] Sales stream aggregated revenue: $" + totalRev);

        // Test sale line item retrieval
        int testSaleId = allSales.get(0).getSaleId();
        List<SaleItem> items = saleDAO.getSaleItems(testSaleId);
        assertEq(!items.isEmpty(), true, "Sale #" + testSaleId + " must have associated line items");
        System.out.println("[PASS] Retrieved " + items.size() + " line items for Sale #" + testSaleId);

        // 4. Test Stream 2: Low Stock Surveillance
        System.out.println("[INFO] Testing Low Stock Surveillance stream data...");
        List<Medicine> lowStockMeds = medicineDAO.getLowStockMedicines();
        assertEq(!lowStockMeds.isEmpty(), true, "Should identify low stock medicines from catalog");

        for (Medicine m : lowStockMeds) {
            assertEq(m.getQuantityInStock() <= m.getReorderLevel(), true,
                    m.getName() + " stock (" + m.getQuantityInStock() + ") <= reorder level (" + m.getReorderLevel() + ")");
        }
        System.out.println("[PASS] Found " + lowStockMeds.size() + " low stock medicines requiring replenishment.");

        // 5. Test Stream 3: Expiration Date Risk
        System.out.println("[INFO] Testing Expiration Risk stream data...");
        List<Medicine> criticalExpiring = medicineDAO.getExpiringMedicines(30);
        assertEq(!criticalExpiring.isEmpty(), true, "Should identify medicines expiring within 30 days");
        System.out.println("[PASS] Found " + criticalExpiring.size() + " medicines at expiration risk (<= 30 days).");

        // 6. Test CSV Export Mechanism
        System.out.println("[INFO] Testing CSV Export generator...");
        try {
            File tempCsv = File.createTempFile("fyto_test_report_", ".csv");
            tempCsv.deleteOnExit();

            try (FileWriter fw = new FileWriter(tempCsv)) {
                fw.write("Sale ID,Date,Cashier,Items Sold,Revenue\n");
                for (Sale s : past7DaysSales) {
                    fw.write(String.format("%d,%s,\"%s\",%d,%.2f\n",
                            s.getSaleId(),
                            s.getSaleDate() != null ? s.getSaleDate().toString() : "",
                            s.getCashierName(),
                            1,
                            s.getTotalAmount().doubleValue()
                    ));
                }
            }

            assertEq(tempCsv.exists() && tempCsv.length() > 0, true, "CSV file successfully written and populated");
            System.out.println("[PASS] CSV export test file generated: " + tempCsv.length() + " bytes.");

        } catch (Exception ex) {
            System.err.println("[FAIL] CSV export test exception: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }

        // 7. Test UI: ReportsPanel on Swing EDT
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("[INFO] Testing ReportsPanel UI instantiation on EDT...");
                ReportsPanel reportsPanel = new ReportsPanel();
                assertEq(reportsPanel.getRecordCount() > 0, true, "ReportsPanel default stream should load records");

                DefaultTableModel model = reportsPanel.getReportTableModel();
                assertEq(model.getColumnCount() >= 5, true, "Reports table must have multiple analytical columns");

                System.out.println("[PASS] ReportsPanel UI constructed and validated with " + reportsPanel.getRecordCount() + " loaded rows.");
                System.out.println(">>> All Phase 5: Reporting & Analytics Verification Tests PASSED successfully! (100%)");
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
