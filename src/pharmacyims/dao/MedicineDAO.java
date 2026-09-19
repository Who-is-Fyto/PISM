package pharmacyims.dao;

import pharmacyims.model.Medicine;
import pharmacyims.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

// Data access object for medicine catalog and stock inventory.
public class MedicineDAO {
    private static final Logger LOGGER = Logger.getLogger(MedicineDAO.class.getName());

    // In-memory fallback cache
    private static final List<Medicine> MOCK_MEDICINES = new CopyOnWriteArrayList<>();
    static {
        LocalDate today = LocalDate.now();
        MOCK_MEDICINES.add(new Medicine(1, "Amoxicillin 500mg", "MedPharma Logistics", "Capsule", new BigDecimal("12.50"), 150, 20, Date.valueOf(today.plusMonths(14)), 1));
        MOCK_MEDICINES.add(new Medicine(2, "Paracetamol 500mg", "Global Generic Labs", "Tablet", new BigDecimal("4.00"), 320, 50, Date.valueOf(today.plusMonths(24)), 3));
        MOCK_MEDICINES.add(new Medicine(3, "Ibuprofen 400mg", "Apex Bioscience", "Tablet", new BigDecimal("7.20"), 8, 15, Date.valueOf(today.plusMonths(18)), 2)); // Low stock
        MOCK_MEDICINES.add(new Medicine(4, "Cough Syrup DM 100ml", "MedPharma Logistics", "Syrup", new BigDecimal("9.80"), 45, 10, Date.valueOf(today.plusDays(20)), 1)); // Expiring soon (<30 days)
        MOCK_MEDICINES.add(new Medicine(5, "Ceftriaxone 1g Vial", "Apex Bioscience", "Injection", new BigDecimal("24.00"), 60, 12, Date.valueOf(today.plusMonths(8)), 2));
        MOCK_MEDICINES.add(new Medicine(6, "Hydrocortisone 1% Cream", "Global Generic Labs", "Cream", new BigDecimal("8.50"), 4, 10, Date.valueOf(today.plusDays(15)), 3)); // Low stock & expiring
        MOCK_MEDICINES.add(new Medicine(7, "Salbutamol Inhaler 100mcg", "MedPharma Logistics", "Inhaler", new BigDecimal("18.00"), 25, 5, Date.valueOf(today.plusMonths(11)), 1));
        MOCK_MEDICINES.add(new Medicine(8, "Metformin 850mg", "VitalCare Remedies", "Tablet", new BigDecimal("11.00"), 80, 25, Date.valueOf(today.plusMonths(16)), 4));
        MOCK_MEDICINES.add(new Medicine(9, "Omeprazole 20mg", "VitalCare Remedies", "Capsule", new BigDecimal("14.50"), 5, 20, Date.valueOf(today.plusMonths(9)), 4)); // Low stock
        MOCK_MEDICINES.add(new Medicine(10, "Ciprofloxacin 500mg", "Apex Bioscience", "Tablet", new BigDecimal("16.00"), 95, 15, Date.valueOf(today.plusMonths(20)), 2));
        MOCK_MEDICINES.add(new Medicine(11, "Eye Drops Tears 15ml", "Global Generic Labs", "Drops", new BigDecimal("6.50"), 35, 10, Date.valueOf(today.plusDays(25)), 3)); // Expiring soon

        MOCK_MEDICINES.get(0).setSupplierName("MedPharma Logistics");
        MOCK_MEDICINES.get(1).setSupplierName("Global Generic Labs");
        MOCK_MEDICINES.get(2).setSupplierName("Apex Bioscience");
        MOCK_MEDICINES.get(3).setSupplierName("MedPharma Logistics");
        MOCK_MEDICINES.get(4).setSupplierName("Apex Bioscience");
        MOCK_MEDICINES.get(5).setSupplierName("Global Generic Labs");
        MOCK_MEDICINES.get(6).setSupplierName("MedPharma Logistics");
        MOCK_MEDICINES.get(7).setSupplierName("VitalCare Remedies");
        MOCK_MEDICINES.get(8).setSupplierName("VitalCare Remedies");
        MOCK_MEDICINES.get(9).setSupplierName("Apex Bioscience");
        MOCK_MEDICINES.get(10).setSupplierName("Global Generic Labs");
    }

    private Medicine mapResultSet(ResultSet rs) throws SQLException {
        Medicine m = new Medicine(
                rs.getInt("medicine_id"),
                rs.getString("name"),
                rs.getString("company"),
                rs.getString("medicine_type"),
                rs.getBigDecimal("price"),
                rs.getInt("quantity_in_stock"),
                rs.getInt("reorder_level"),
                rs.getDate("expiry_date"),
                (Integer) rs.getObject("supplier_id")
        );
        try {
            m.setSupplierName(rs.getString("supplier_name"));
        } catch (SQLException ignored) {}
        return m;
    }

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT m.*, s.name AS supplier_name " +
                "FROM medicines m " +
                "LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id " +
                "ORDER BY m.name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            return list;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database fetch medicines failed, using mock list: " + ex.getMessage());
            return new ArrayList<>(MOCK_MEDICINES);
        }
    }

    public Medicine getMedicineById(int id) {
        String sql = "SELECT m.*, s.name AS supplier_name " +
                "FROM medicines m " +
                "LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id " +
                "WHERE m.medicine_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database getMedicineById failed: " + ex.getMessage());
        }

        for (Medicine m : MOCK_MEDICINES) {
            if (m.getMedicineId() == id) return m;
        }
        return null;
    }

    public List<Medicine> searchMedicines(String query, String typeFilter) {
        List<Medicine> all = getAllMedicines();
        List<Medicine> filtered = new ArrayList<>();
        String q = query != null ? query.trim().toLowerCase() : "";
        String type = typeFilter != null ? typeFilter.trim() : "All Types";

        for (Medicine m : all) {
            boolean matchesQuery = q.isEmpty()
                    || m.getName().toLowerCase().contains(q)
                    || (m.getCompany() != null && m.getCompany().toLowerCase().contains(q));
            boolean matchesType = type.equalsIgnoreCase("All Types")
                    || type.equalsIgnoreCase("All")
                    || m.getMedicineType().equalsIgnoreCase(type);

            if (matchesQuery && matchesType) {
                filtered.add(m);
            }
        }
        return filtered;
    }

    public boolean addMedicine(Medicine m) {
        String sql = "INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getCompany());
            ps.setString(3, m.getMedicineType());
            ps.setBigDecimal(4, m.getPrice());
            ps.setInt(5, m.getQuantityInStock());
            ps.setInt(6, m.getReorderLevel());
            ps.setDate(7, m.getExpiryDate());
            if (m.getSupplierId() != null) ps.setInt(8, m.getSupplierId());
            else ps.setNull(8, Types.INTEGER);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) m.setMedicineId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database addMedicine failed, saving to memory: " + ex.getMessage());
        }

        int newId = MOCK_MEDICINES.stream().mapToInt(Medicine::getMedicineId).max().orElse(0) + 1;
        m.setMedicineId(newId);
        MOCK_MEDICINES.add(m);
        return true;
    }

    public boolean updateMedicine(Medicine m) {
        String sql = "UPDATE medicines SET name = ?, company = ?, medicine_type = ?, price = ?, quantity_in_stock = ?, " +
                "reorder_level = ?, expiry_date = ?, supplier_id = ? WHERE medicine_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getCompany());
            ps.setString(3, m.getMedicineType());
            ps.setBigDecimal(4, m.getPrice());
            ps.setInt(5, m.getQuantityInStock());
            ps.setInt(6, m.getReorderLevel());
            ps.setDate(7, m.getExpiryDate());
            if (m.getSupplierId() != null) ps.setInt(8, m.getSupplierId());
            else ps.setNull(8, Types.INTEGER);
            ps.setInt(9, m.getMedicineId());

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database updateMedicine failed: " + ex.getMessage());
        }

        for (int i = 0; i < MOCK_MEDICINES.size(); i++) {
            if (MOCK_MEDICINES.get(i).getMedicineId() == m.getMedicineId()) {
                MOCK_MEDICINES.set(i, m);
                return true;
            }
        }
        return false;
    }

    public boolean deleteMedicine(int id) {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database deleteMedicine failed: " + ex.getMessage());
        }

        return MOCK_MEDICINES.removeIf(m -> m.getMedicineId() == id);
    }

    public boolean updateStock(int medicineId, int newQuantity) {
        String sql = "UPDATE medicines SET quantity_in_stock = ? WHERE medicine_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, medicineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database updateStock failed: " + ex.getMessage());
        }

        for (Medicine m : MOCK_MEDICINES) {
            if (m.getMedicineId() == medicineId) {
                m.setQuantityInStock(newQuantity);
                return true;
            }
        }
        return false;
    }

    public List<Medicine> getLowStockMedicines() {
        List<Medicine> all = getAllMedicines();
        List<Medicine> result = new ArrayList<>();
        for (Medicine m : all) {
            if (m.isLowStock() || m.isOutOfStock()) result.add(m);
        }
        return result;
    }

    public List<Medicine> getExpiringMedicines(int daysThreshold) {
        List<Medicine> all = getAllMedicines();
        List<Medicine> result = new ArrayList<>();
        for (Medicine m : all) {
            if (m.isExpiringSoon(daysThreshold) || m.isExpired()) result.add(m);
        }
        return result;
    }

    public int getTotalCount() {
        return getAllMedicines().size();
    }

    public int getLowStockCount() {
        return getLowStockMedicines().size();
    }

    public int getExpiringCount(int daysThreshold) {
        return getExpiringMedicines(daysThreshold).size();
    }
}
