package pharmacyims.dao;

import pharmacyims.model.Supplier;
import pharmacyims.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Supplier entities.
 */
public class SupplierDAO {
    private static final Logger LOGGER = Logger.getLogger(SupplierDAO.class.getName());

    // In-memory fallback mock cache
    private static final List<Supplier> MOCK_SUPPLIERS = new CopyOnWriteArrayList<>();
    static {
        MOCK_SUPPLIERS.add(new Supplier(1, "MedPharma Logistics", "David Clark", "+1 (555) 019-2834", "orders@medpharma.com", "124 Healthcare Industrial Park, District 4"));
        MOCK_SUPPLIERS.add(new Supplier(2, "Apex Bioscience", "Sarah Connor", "+1 (555) 024-8891", "supply@apexbio.com", "88 Research Parkway, Biotech City"));
        MOCK_SUPPLIERS.add(new Supplier(3, "Global Generic Labs", "Marcus Vance", "+1 (555) 037-1290", "sales@globalgeneric.com", "45 Distribution Blvd, Port Hub"));
        MOCK_SUPPLIERS.add(new Supplier(4, "VitalCare Remedies", "Elena Rostova", "+1 (555) 048-9102", "contact@vitalcare.org", "12 South Medical Center Road"));
    }

    public List<Supplier> getAllSuppliers() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT supplier_id, name, contact_person, phone, email, address, created_at FROM suppliers ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Supplier s = new Supplier(
                        rs.getInt("supplier_id"),
                        rs.getString("name"),
                        rs.getString("contact_person"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                );
                s.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(s);
            }
            return list;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database fetch suppliers failed, using mock list: " + ex.getMessage());
            return new ArrayList<>(MOCK_SUPPLIERS);
        }
    }

    public Supplier getSupplierById(int id) {
        String sql = "SELECT supplier_id, name, contact_person, phone, email, address, created_at FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Supplier s = new Supplier(
                            rs.getInt("supplier_id"),
                            rs.getString("name"),
                            rs.getString("contact_person"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("address")
                    );
                    s.setCreatedAt(rs.getTimestamp("created_at"));
                    return s;
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database getSupplierById failed: " + ex.getMessage());
        }

        for (Supplier s : MOCK_SUPPLIERS) {
            if (s.getSupplierId() == id) return s;
        }
        return null;
    }

    public boolean addSupplier(Supplier supplier) {
        String sql = "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getContactPerson());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) supplier.setSupplierId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database addSupplier failed, saving to memory: " + ex.getMessage());
        }

        int newId = MOCK_SUPPLIERS.stream().mapToInt(Supplier::getSupplierId).max().orElse(0) + 1;
        supplier.setSupplierId(newId);
        MOCK_SUPPLIERS.add(supplier);
        return true;
    }

    public boolean updateSupplier(Supplier supplier) {
        String sql = "UPDATE suppliers SET name = ?, contact_person = ?, phone = ?, email = ?, address = ? WHERE supplier_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getContactPerson());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            ps.setInt(6, supplier.getSupplierId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database updateSupplier failed: " + ex.getMessage());
        }

        for (int i = 0; i < MOCK_SUPPLIERS.size(); i++) {
            if (MOCK_SUPPLIERS.get(i).getSupplierId() == supplier.getSupplierId()) {
                MOCK_SUPPLIERS.set(i, supplier);
                return true;
            }
        }
        return false;
    }

    public boolean deleteSupplier(int id) {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database deleteSupplier failed: " + ex.getMessage());
        }

        return MOCK_SUPPLIERS.removeIf(s -> s.getSupplierId() == id);
    }
}
