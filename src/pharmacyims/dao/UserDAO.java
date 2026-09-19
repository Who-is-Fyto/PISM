package pharmacyims.dao;

import pharmacyims.model.User;
import pharmacyims.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for User entities & Role-Based Access Control.
 */
public class UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    // In-memory fallback mock cache for offline development/prototyping
    private static final List<User> MOCK_USERS = new CopyOnWriteArrayList<>();
    static {
        MOCK_USERS.add(new User(1, "admin", "admin123", "Admin", "System Administrator"));
        MOCK_USERS.add(new User(2, "cashier1", "cashier123", "Cashier", "Jane Doe (Dispenser 1)"));
        MOCK_USERS.add(new User(3, "cashier2", "cashier123", "Cashier", "John Smith (Dispenser 2)"));
    }

    public User authenticate(String username, String password) {
        String sql = "SELECT user_id, username, password, role, full_name, created_at FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("full_name")
                    );
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database unavailable, falling back to mock authentication: " + ex.getMessage());
        }

        // Mock Fallback
        for (User u : MOCK_USERS) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public boolean createCashier(User user) {
        String sql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, 'Cashier', ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) user.setUserId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database insert failed, recording to in-memory store: " + ex.getMessage());
        }

        user.setUserId(MOCK_USERS.size() + 1);
        user.setRole("Cashier");
        MOCK_USERS.add(user);
        return true;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, username, password, role, full_name, created_at FROM users ORDER BY user_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("full_name")
                );
                u.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(u);
            }
            return list;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database fetch failed, using mock list: " + ex.getMessage());
            return new ArrayList<>(MOCK_USERS);
        }
    }

    public List<User> getAllCashiers() {
        List<User> all = getAllUsers();
        List<User> cashiers = new ArrayList<>();
        for (User u : all) {
            if (u.isCashier()) cashiers.add(u);
        }
        return cashiers;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ? AND role != 'Admin'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database delete failed: " + ex.getMessage());
        }

        return MOCK_USERS.removeIf(u -> u.getUserId() == userId && !u.isAdmin());
    }

    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database password update failed: " + ex.getMessage());
        }

        for (User u : MOCK_USERS) {
            if (u.getUserId() == userId) {
                u.setPassword(newPassword);
                return true;
            }
        }
        return false;
    }
}
