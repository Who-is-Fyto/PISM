# Phase 6: System Integration, UI Polish & Final Testing

## Overview
Connect all individual frontend components, modules, and database pipelines into a cohesive, production-ready desktop application. Apply consistent FlatLaf styling, configure graceful window lifecycle management, implement security checks, and execute a comprehensive end-to-end verification test suite.

---

### 1. UI Standardization & Design Polish

1. **FlatLaf Look-and-Feel Setup**:
   Initialize `FlatLightLaf` or `FlatDarkLaf` in the application entry point (`PharmacyIMS.java`) before any UI component is created:
   ```java
   public static void main(String[] args) {
       FlatLightLaf.setup();
       // Custom UI defaults: rounded controls, consistent font
       UIManager.put("Button.arc", 8);
       UIManager.put("Component.arc", 8);
       UIManager.put("TextComponent.arc", 8);
       UIManager.put("Table.alternateRowColor", new Color(248, 250, 252));
       UIManager.put("Table.rowHeight", 28);
       
       SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
   }
   ```
2. **Consistent Color Palette**:
   * Primary Accent: Medical Blue (`#0284C7` / `rgb(2, 132, 199)`)
   * Success / Healthy Stock: Emerald (`#10B981` / `rgb(16, 185, 129)`)
   * Warning / Low Stock: Amber (`#F59E0B` / `rgb(245, 158, 11)`)
   * Danger / Critical / Expired: Crimson (`#EF4444` / `rgb(239, 68, 68)`)
   * Neutral Dark: Slate (`#1E293B` / `rgb(30, 41, 59)`)
   * Neutral Light: Soft White (`#F8FAFC` / `rgb(248, 250, 252)`)
3. **Window Close & Logout Safeguards**:
   * Prevent abrupt accidental exits using `setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE)`.
   * Prompt confirmation: *"Are you sure you want to log out / exit?"*.
   * If in an active cashier transaction with items in cart, warn the user before closing.

---

### 2. End-to-End Verification Test Matrix

| Test ID | Module | Action / Test Case | Input / Condition | Expected Outcome | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-01** | Database | Test DB Connection | Execute `DBConnection.getConnection()` | Active MySQL connection returned | [ ] |
| **TC-02** | Auth | Empty Credentials | Click Sign In with blank fields | Red inline error: *"Please enter username and password"* | [ ] |
| **TC-03** | Auth | Invalid Password | Enter `admin` + `wrongpass` | Red error: *"Invalid credentials"*; password cleared | [ ] |
| **TC-04** | Auth | Admin Login | Enter `admin` + `admin123` | Closes `LoginFrame`, launches `AdminDashboard` | [ ] |
| **TC-05** | Auth | Cashier Login | Enter `cashier1` + `cashier123` | Closes `LoginFrame`, launches `CashierDashboard` | [ ] |
| **TC-06** | Admin | Add Supplier | Name: `PharmaCorp`, Phone: `555-0100` | Supplier inserted in DB and visible in table | [ ] |
| **TC-07** | Admin | Add Medicine | `Metformin 500mg`, Price: `15.00`, Stock: `50` | Medicine stored with supplier FK; table refreshed | [ ] |
| **TC-08** | Admin | Search Filter | Type `Met` into search bar | Table filters immediately to matching rows | [ ] |
| **TC-09** | Admin | Low Stock Badge | Set stock `4` with reorder level `10` | Status column displays orange `[LOW STOCK]` pill | [ ] |
| **TC-10** | Admin | Create Cashier | Username: `cashier3`, Role: `Cashier` | Record written to DB; immediately authenticates | [ ] |
| **TC-11** | POS | Stock Lookup | Search `Metformin 500mg` | Product preview card shows $15.00, 50 in stock | [ ] |
| **TC-12** | POS | Stock Exceeded | Request `60` units when only `50` exist | Warning dialog halts addition: *"Insufficient stock"* | [ ] |
| **TC-13** | POS | Cart Math | Add 2x `Metformin 500mg` ($15.00) | Cart subtotal = $30.00; Grand Total = $30.00 | [ ] |
| **TC-14** | POS | Checkout Atomic | Paid `$40.00`, Grand Total `$30.00` | Change = `$10.00`; stock drops from `50` to `48` in DB | [ ] |
| **TC-15** | POS | Receipt Print | Click Print in `BillReceiptDialog` | OS Print dialog appears with formatted receipt | [ ] |
| **TC-16** | Reports | Sales Summary | Date: Today | Lists TC-14 transaction with accurate $30.00 total | [ ] |
| **TC-17** | Reports | Expiry Alert | Query items expiring $\le 30$ days | Items highlighted with days remaining | [ ] |
| **TC-18** | Reports | CSV Export | Click `Export to CSV` | Valid `.csv` file saved to selected directory | [ ] |

---

### 3. NetBeans Ant Build & Distribution Packaging

To generate a standalone executable JAR distribution:
1. Ensure all external library JARs (`mysql-connector-j-*.jar`, `flatlaf-*.jar`) are referenced in `nbproject/project.properties` under `javac.classpath` and `run.classpath`.
2. Clean and build via Ant:
   ```bash
   /snap/netbeans/current/netbeans/extide/ant/bin/ant clean jar
   ```
3. The executable JAR is packaged into `dist/pharmacyIMS.jar` with external dependencies copied to `dist/lib/`.
4. Run standalone test:
   ```bash
   java -jar dist/pharmacyIMS.jar
   ```

---

### 4. Final Sign-off Criteria
- [ ] Zero unhandled SQL or NullPointer exceptions in console logs.
- [ ] All 18 Test Matrix cases pass.
- [ ] Modern, consistent FlatLaf UI theme across all dialogs, tables, and frames.
- [ ] Role segregation verified: Cashiers cannot open Admin screens, alter prices, or access user accounts.
