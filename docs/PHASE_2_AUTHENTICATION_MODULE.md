# Phase 2: Authentication & Role Redirection Module

## Overview
Build the gateway to the Pharmacy Inventory Management System: a modern, user-friendly login interface powered by FlatLaf, robust credential validation against the database, session context tracking, and dynamic role-based routing (`Admin` vs `Cashier`).

---

### 1. Architectural Scope & Functional Requirements

1. **Secure Credential Verification**: Query `users` via `UserDAO` to match username and verify hashed/stored password.
2. **Session Context Management**: Initialize a global `UserSession` singleton storing `userId`, `username`, `fullName`, and `role`.
3. **Role-Based Window Routing**:
   * If `role == 'Admin'`: Open `AdminDashboard` and dispose the login frame.
   * If `role == 'Cashier'`: Open `CashierDashboard` and dispose the login frame.
4. **Visual Error & Feedback Protocol**:
   * Inline status label (color-coded red) for invalid credentials, deactivated accounts, or database offline.
   * Shake animation or red highlight on input fields upon failure.
   * Clear button states (normal, hover, pressed, disabled during verification).
5. **Accessibility & Keyboard Navigation**:
   * Pressing `Enter` in either username or password field triggers the login action immediately.
   * Pressing `Escape` prompts for exit confirmation.
   * Standard `Tab` traversal between fields.

---

### 2. Frontend UI Elements & Modern Styling (`LoginFrame`)

To provide a modern, commercial-grade presentation rather than default dated Swing gray, we leverage **FlatLaf** (Modern Flat Look and Feel) and a modular two-panel card layout.

```
+-----------------------------------------------------------------------+
|  PHARMACY IMS - SECURE LOGIN                                    - X   |
+-----------------------------------+-----------------------------------+
|  [LEFT BRANDING HERO PANEL]       |  [RIGHT AUTHENTICATION FORM]      |
|                                   |                                   |
|   +---------------------------+   |   Welcome Back                    |
|   |  [+] Pharmacy Cross Icon  |   |   Please sign in to continue      |
|   +---------------------------+   |                                   |
|                                   |   Username                        |
|   OBELLION PHARMACY               |   [ admin                     ]   |
|   Inventory & Dispensing Suite    |                                   |
|                                   |   Password                        |
|   "Accurate Dispensing,           |   [ **********             (o)]   |
|    Real-time Stock Control,       |                                   |
|    Actionable Analytics"          |   [X] Show Password               |
|                                   |                                   |
|   Version 1.0.0                   |   [!] Invalid username/password   |
|   System Status: Connected (v)    |                                   |
|                                   |   [   SIGN IN   ]                 |
|                                   |   [   Cancel    ]                 |
+-----------------------------------+-----------------------------------+
```

#### Modular UI Components to Build
1. **`ui.common.BrandingPanel`**: Left-side gradient hero banner featuring a medical cross emblem, application title, tagline, and live database connectivity status badge.
2. **`ui.common.CustomTextField`**: Styled input field with placeholder text, rounded borders (`FlatClientProperties.STYLE = "arc: 10"`), and clear inner padding.
3. **`ui.common.CustomPasswordField`**: Includes trailing eye icon to toggle password visibility.
4. **`ui.common.PrimaryButton`**: High-contrast accent button with smooth hover transition and loading spinner indicator.

---

### 3. Session State Management (`session.UserSession`)

```java
package pharmacyims.session;

import pharmacyims.model.User;

public class UserSession {
    private static UserSession instance;
    private final User currentUser;
    private final long loginTimestamp;

    private UserSession(User user) {
        this.currentUser = user;
        this.loginTimestamp = System.currentTimeMillis();
    }

    public static void initialize(User user) {
        instance = new UserSession(user);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public static boolean isLoggedIn() {
        return instance != null && instance.currentUser != null;
    }

    public static void clear() {
        instance = null;
    }

    public User getCurrentUser() { return currentUser; }
    public int getUserId() { return currentUser.getUserId(); }
    public String getUsername() { return currentUser.getUsername(); }
    public String getFullName() { return currentUser.getFullName(); }
    public String getRole() { return currentUser.getRole(); }
    public boolean isAdmin() { return "Admin".equalsIgnoreCase(getRole()); }
    public boolean isCashier() { return "Cashier".equalsIgnoreCase(getRole()); }
}
```

---

### 4. Router & Navigation Logic

```java
private void performLogin() {
    String username = txtUsername.getText().trim();
    String password = new String(txtPassword.getPassword());

    if (username.isEmpty() || password.isEmpty()) {
        lblError.setText("Please enter both username and password.");
        return;
    }

    btnLogin.setEnabled(false);
    lblError.setText("Authenticating...");

    // Background verification using SwingWorker to prevent UI freezing
    SwingWorker<User, Void> worker = new SwingWorker<>() {
        @Override
        protected User doInBackground() {
            return userDAO.authenticate(username, password);
        }

        @Override
        protected void done() {
            try {
                User user = get();
                if (user != null) {
                    UserSession.initialize(user);
                    if (user.isAdmin()) {
                        new AdminDashboard().setVisible(true);
                    } else {
                        new CashierDashboard().setVisible(true);
                    }
                    dispose();
                } else {
                    lblError.setText("Invalid username or password. Please try again.");
                    btnLogin.setEnabled(true);
                    txtPassword.setText("");
                    txtPassword.requestFocusInWindow();
                }
            } catch (Exception ex) {
                lblError.setText("Database connection error: " + ex.getMessage());
                btnLogin.setEnabled(true);
            }
        }
    };
    worker.execute();
}
```

---

### 5. Verification Checklist
- [ ] UI rendered using FlatLaf with high-DPI scaling and clean typography.
- [ ] Empty username or password halts submission and displays inline validation error.
- [ ] Entering incorrect credentials shows clear red error message without clearing username.
- [ ] Entering valid Cashier credentials opens `CashierDashboard` and passes user context.
- [ ] Entering valid Admin credentials opens `AdminDashboard` and passes user context.
- [ ] Enter key triggers submission from both input fields.
- [ ] Password visibility toggle successfully reveals/masks characters.
