package pharmacyims.session;

import pharmacyims.model.User;

// Global session context for the authenticated user.
public class UserSession {
    private static volatile UserSession instance;
    private final User currentUser;
    private final long loginTimestamp;

    private UserSession(User user) {
        this.currentUser = user;
        this.loginTimestamp = System.currentTimeMillis();
    }

    public static synchronized void initialize(User user) {
        instance = new UserSession(user);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public static boolean isLoggedIn() {
        return instance != null && instance.currentUser != null;
    }

    public static synchronized void clear() {
        instance = null;
    }

    public User getCurrentUser() { return currentUser; }
    public int getUserId() { return currentUser != null ? currentUser.getUserId() : 0; }
    public String getUsername() { return currentUser != null ? currentUser.getUsername() : ""; }
    public String getFullName() { return currentUser != null ? currentUser.getFullName() : "Guest"; }
    public String getRole() { return currentUser != null ? currentUser.getRole() : "Cashier"; }
    public boolean isAdmin() { return "Admin".equalsIgnoreCase(getRole()); }
    public boolean isCashier() { return "Cashier".equalsIgnoreCase(getRole()); }
    public long getLoginTimestamp() { return loginTimestamp; }
}
