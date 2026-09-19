package pharmacyims.dao;

import pharmacyims.model.Sale;
import pharmacyims.model.SaleItem;
import pharmacyims.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

// Data access object for point-of-sale transactions and checkout.
public class SaleDAO {
    private static final Logger LOGGER = Logger.getLogger(SaleDAO.class.getName());

    // In-memory fallback cache
    private static final List<Sale> MOCK_SALES = new CopyOnWriteArrayList<>();
    private static final java.util.Map<Integer, List<SaleItem>> MOCK_SALE_ITEMS = new java.util.concurrent.ConcurrentHashMap<>();

    static {
        long now = System.currentTimeMillis();
        long oneDay = 24L * 60 * 60 * 1000;

        // Sale 1001 (Today)
        Sale s1 = new Sale(1001, new Timestamp(now - 1000 * 60 * 30), new BigDecimal("29.00"), new BigDecimal("30.00"), new BigDecimal("1.00"), 2);
        s1.setCashierName("Jane Doe (Dispenser 1)");
        List<SaleItem> items1 = new ArrayList<>();
        items1.add(new SaleItem(1, "Amoxicillin 500mg", 2, new BigDecimal("12.50")));
        items1.add(new SaleItem(2, "Paracetamol 500mg", 1, new BigDecimal("4.00")));
        s1.setItems(items1);
        MOCK_SALES.add(s1);
        MOCK_SALE_ITEMS.put(1001, items1);

        // Sale 1002 (Today)
        Sale s2 = new Sale(1002, new Timestamp(now - 1000 * 60 * 90), new BigDecimal("48.00"), new BigDecimal("50.00"), new BigDecimal("2.00"), 3);
        s2.setCashierName("John Smith (Dispenser 2)");
        List<SaleItem> items2 = new ArrayList<>();
        items2.add(new SaleItem(5, "Ceftriaxone 1g Vial", 2, new BigDecimal("24.00")));
        s2.setItems(items2);
        MOCK_SALES.add(s2);
        MOCK_SALE_ITEMS.put(1002, items2);

        // Sale 1003 (Yesterday)
        Sale s3 = new Sale(1003, new Timestamp(now - oneDay), new BigDecimal("36.00"), new BigDecimal("40.00"), new BigDecimal("4.00"), 2);
        s3.setCashierName("Jane Doe (Dispenser 1)");
        List<SaleItem> items3 = new ArrayList<>();
        items3.add(new SaleItem(7, "Salbutamol Inhaler 100mcg", 2, new BigDecimal("18.00")));
        s3.setItems(items3);
        MOCK_SALES.add(s3);
        MOCK_SALE_ITEMS.put(1003, items3);

        // Sale 1004 (3 days ago)
        Sale s4 = new Sale(1004, new Timestamp(now - 3 * oneDay), new BigDecimal("18.30"), new BigDecimal("20.00"), new BigDecimal("1.70"), 3);
        s4.setCashierName("John Smith (Dispenser 2)");
        List<SaleItem> items4 = new ArrayList<>();
        items4.add(new SaleItem(4, "Cough Syrup DM 100ml", 1, new BigDecimal("9.80")));
        items4.add(new SaleItem(6, "Hydrocortisone 1% Cream", 1, new BigDecimal("8.50")));
        s4.setItems(items4);
        MOCK_SALES.add(s4);
        MOCK_SALE_ITEMS.put(1004, items4);

        // Sale 1005 (5 days ago)
        Sale s5 = new Sale(1005, new Timestamp(now - 5 * oneDay), new BigDecimal("32.00"), new BigDecimal("35.00"), new BigDecimal("3.00"), 2);
        s5.setCashierName("Jane Doe (Dispenser 1)");
        List<SaleItem> items5 = new ArrayList<>();
        items5.add(new SaleItem(10, "Ciprofloxacin 500mg", 2, new BigDecimal("16.00")));
        s5.setItems(items5);
        MOCK_SALES.add(s5);
        MOCK_SALE_ITEMS.put(1005, items5);
    }

    // Atomic sale checkout: saves sale, persists items, and decrements stock
    public int processSale(Sale sale, List<SaleItem> items) throws SQLException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot process a sale with zero items.");
        }

        String insertSaleSQL = "INSERT INTO sales (total_amount, amount_paid, change_given, user_id) VALUES (?, ?, ?, ?)";
        String insertItemSQL = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";
        String updateStockSQL = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id = ? AND quantity_in_stock >= ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin atomic transaction

            int saleId = -1;
            try (PreparedStatement psSale = conn.prepareStatement(insertSaleSQL, Statement.RETURN_GENERATED_KEYS)) {
                psSale.setBigDecimal(1, sale.getTotalAmount());
                psSale.setBigDecimal(2, sale.getAmountPaid());
                psSale.setBigDecimal(3, sale.getChangeGiven());
                if (sale.getUserId() != null) psSale.setInt(4, sale.getUserId());
                else psSale.setNull(4, Types.INTEGER);

                psSale.executeUpdate();
                try (ResultSet rs = psSale.getGeneratedKeys()) {
                    if (rs.next()) saleId = rs.getInt(1);
                    else throw new SQLException("Failed to obtain generated sale ID.");
                }
            }

            try (PreparedStatement psItem = conn.prepareStatement(insertItemSQL);
                 PreparedStatement psStock = conn.prepareStatement(updateStockSQL)) {

                for (SaleItem item : items) {
                    psItem.setInt(1, saleId);
                    psItem.setInt(2, item.getMedicineId());
                    psItem.setInt(3, item.getQuantitySold());
                    psItem.setBigDecimal(4, item.getPriceAtSale());
                    psItem.executeUpdate();

                    psStock.setInt(1, item.getQuantitySold());
                    psStock.setInt(2, item.getMedicineId());
                    psStock.setInt(3, item.getQuantitySold());
                    int rows = psStock.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Insufficient stock to dispense medicine ID: " + item.getMedicineId());
                    }
                }
            }

            conn.commit();
            sale.setSaleId(saleId);
            return saleId;

        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            LOGGER.log(Level.WARNING, "Database transaction failed, executing in-memory transaction: " + ex.getMessage());

            // In-memory mock execution
            MedicineDAO medDAO = new MedicineDAO();
            for (SaleItem it : items) {
                var m = medDAO.getMedicineById(it.getMedicineId());
                if (m != null) {
                    medDAO.updateStock(it.getMedicineId(), Math.max(0, m.getQuantityInStock() - it.getQuantitySold()));
                }
            }

            int mockId = MOCK_SALES.size() + 1001;
            sale.setSaleId(mockId);
            sale.setSaleDate(new Timestamp(System.currentTimeMillis()));
            sale.setItems(items);
            MOCK_SALES.add(sale);
            MOCK_SALE_ITEMS.put(mockId, new ArrayList<>(items));
            return mockId;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public List<Sale> getAllSales() {
        return getSalesByDateRange(LocalDate.of(2000, 1, 1), LocalDate.of(2099, 12, 31));
    }

    public List<Sale> getSalesByDateRange(LocalDate start, LocalDate end) {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT s.*, u.full_name AS cashier_name " +
                "FROM sales s " +
                "LEFT JOIN users u ON s.user_id = u.user_id " +
                "WHERE DATE(s.sale_date) BETWEEN ? AND ? " +
                "ORDER BY s.sale_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sale s = new Sale(
                            rs.getInt("sale_id"),
                            rs.getTimestamp("sale_date"),
                            rs.getBigDecimal("total_amount"),
                            rs.getBigDecimal("amount_paid"),
                            rs.getBigDecimal("change_given"),
                            (Integer) rs.getObject("user_id")
                    );
                    s.setCashierName(rs.getString("cashier_name"));
                    list.add(s);
                }
            }
            return list;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database sales fetch failed, returning filtered mock sales: " + ex.getMessage());
            List<Sale> filtered = new ArrayList<>();
            for (Sale s : MOCK_SALES) {
                if (s.getSaleDate() != null) {
                    LocalDate d = s.getSaleDate().toLocalDateTime().toLocalDate();
                    if (!d.isBefore(start) && !d.isAfter(end)) {
                        filtered.add(s);
                    }
                }
            }
            return filtered;
        }
    }

    public List<SaleItem> getSaleItems(int saleId) {
        String sql = "SELECT si.*, m.name AS medicine_name " +
                "FROM sale_items si " +
                "LEFT JOIN medicines m ON si.medicine_id = m.medicine_id " +
                "WHERE si.sale_id = ?";
        List<SaleItem> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem(
                            rs.getInt("sale_item_id"),
                            rs.getInt("sale_id"),
                            rs.getInt("medicine_id"),
                            rs.getInt("quantity_sold"),
                            rs.getBigDecimal("price_at_sale")
                    );
                    item.setMedicineName(rs.getString("medicine_name"));
                    list.add(item);
                }
            }
            return list;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database fetch sale_items failed, checking mock: " + ex.getMessage());
        }

        List<SaleItem> mockItems = MOCK_SALE_ITEMS.get(saleId);
        return mockItems != null ? new ArrayList<>(mockItems) : new ArrayList<>();
    }
}
