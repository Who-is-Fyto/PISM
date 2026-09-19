package pharmacyims.model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String username;
    private String password;
    private String role; // "Admin", "Cashier"
    private String fullName;
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String username, String password, String role, String fullName) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
    }

    public User(String username, String password, String role, String fullName) {
        this(0, username, password, role, fullName);
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() { return "Admin".equalsIgnoreCase(role); }
    public boolean isCashier() { return "Cashier".equalsIgnoreCase(role); }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
