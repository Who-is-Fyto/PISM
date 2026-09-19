# Phase 3: Administrator Module (Inventory, Suppliers & Users)

## Overview
Develop the administrative core of the Pharmacy Inventory Management System. Features complete CRUD management for medicines (inventory with reorder levels & expiry dates), pharmaceutical suppliers, and system cashier accounts, styled with modern FlatLaf UI elements, real-time search filtering, and custom table renderers.

---

### 1. Architectural Scope & Functional Requirements

1. **Medicine Inventory Management**:
   * Add, edit, delete, and view all pharmaceutical stock.
   * Real-time stock status computation: `In Stock` (green), `Low Stock` (amber/orange when $\le$ reorder level), `Out of Stock` (red when = 0).
   * Expiration date alerts: highlighted warning badges when expiry is within 30 days.
   * Live search bar filtering instantly by drug name or pharmaceutical company.
   * Filter dropdown by medicine category (`All`, `Tablet`, `Capsule`, `Syrup`, `Injection`, `Cream`, etc.).
2. **Supplier Directory Management**:
   * Register new vendors, update contact details, phone, email, and physical address.
   * Prevent accidental deletion of suppliers that currently have associated medicine inventory records (`ON DELETE RESTRICT` or re-assign alert).
3. **User & Access Control Management**:
   * Admin can register new Cashier accounts with full name, username, and password.
   * Reset cashier passwords.
   * Protect the master Admin account from deletion or accidental deactivation.

---

### 2. UI Layout & Component Breakdown (`ui.admin.AdminDashboard`)

```
+----------------------------------------------------------------------------------------------------+
| [Logo] OBELLION PHARMACY - ADMIN PORTAL        [Metric: 240 Meds] [Metric: 3 Low Stock]  User: Admin (Logout) |
+----------------------------------------------------------------------------------------------------+
| [NAV SIDEBAR]      |  [MAIN CONTENT AREA: MEDICINE INVENTORY TAB]                                  |
|                    |                                                                               |
| [x] Medicines      |  +-------------------------------------------------------------------------+  |
| [ ] Suppliers      |  | Search: [ Amox...             ]  Type: [ All Types v ]  [+ Add Medicine]|  |
| [ ] Users          |  +-------------------------------------------------------------------------+  |
| [ ] Reports        |  | ID | Name               | Type    | Price   | Stock | Expiry     | Status |  |
|                    |  |----+--------------------+---------+---------+-------+------------+--------|  |
|                    |  | 01 | Amoxicillin 500mg  | Capsule | $12.50  | 150   | 2027-11-20 | [Good] |  |
|                    |  | 02 | Ibuprofen 400mg    | Tablet  | $ 7.20  | 8     | 2028-03-15 | [LOW!] |  |
|                    |  | 03 | Cough Syrup DM     | Syrup   | $ 9.80  | 45    | 2026-10-10 | [EXP!] |  |
|                    |  +-------------------------------------------------------------------------+  |
|                    |  [ Edit Selected ]  [ Delete Selected ]  [ Refresh Stock ]                    |
+----------------------------------------------------------------------------------------------------+
```

#### Individual Frontend Elements to Build
1. **`ui.common.HeaderBar`**: Displays application branding, current logged-in user profile, role badge (`ADMIN`), live digital clock, and `Logout` button with confirmation.
2. **`ui.common.MetricCard`**: Reusable summary dashboard widget with an icon, title, large metric number, and background color tone (e.g., Green for Total Products, Orange for Low Stock Warning, Red for Expiring Soon).
3. **`ui.common.SearchField`**: Modern search input box with magnifying glass icon and clear button.
4. **`ui.common.StatusBadgeRenderer`**: Custom `TableCellRenderer` rendering colored rounded pill badges instead of raw text:
   * **Stock Status**: Green badge for normal stock, Orange/Amber for `Stock <= ReorderLevel`, Red for `Stock == 0`.
   * **Expiry Status**: Red badge for expired or expiring within 30 days.
5. **`ui.admin.MedicineFormDialog`**: Reusable modal dialog for both adding and editing medicines with field validation (positive price, non-negative quantity, valid future date).
6. **`ui.admin.SupplierFormDialog`**: Modal dialog for vendor data entry with email and phone format validation.
7. **`ui.admin.UserFormDialog`**: Modal dialog for registering new Cashier staff.

---

### 3. Key Controller & Data Flow

#### Medicine Form Validation
```java
public boolean validateInputs() {
    if (txtName.getText().trim().isEmpty()) {
        showError("Medicine Name cannot be empty.");
        txtName.requestFocus();
        return false;
    }
    try {
        double price = Double.parseDouble(txtPrice.getText().trim());
        if (price <= 0) throw new NumberFormatException();
    } catch (NumberFormatException e) {
        showError("Price must be a valid positive number.");
        txtPrice.requestFocus();
        return false;
    }
    try {
        int qty = Integer.parseInt(txtQuantity.getText().trim());
        if (qty < 0) throw new NumberFormatException();
    } catch (NumberFormatException e) {
        showError("Quantity must be a non-negative integer.");
        txtQuantity.requestFocus();
        return false;
    }
    if (cmbSupplier.getSelectedItem() == null) {
        showError("Please select a valid supplier.");
        return false;
    }
    return true;
}
```

#### Dynamic Table Filtering
```java
TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
table.setRowSorter(sorter);

txtSearch.getDocument().addDocumentListener(new DocumentListener() {
    public void insertUpdate(DocumentEvent e) { applyFilter(); }
    public void removeUpdate(DocumentEvent e) { applyFilter(); }
    public void changedUpdate(DocumentEvent e) { applyFilter(); }

    private void applyFilter() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        }
    }
});
```

---

### 4. Verification Checklist
- [ ] Metric cards at top reflect real-time database counts (Total Medicines, Low Stock alerts, Expiring alerts).
- [ ] Adding a medicine with selected supplier creates a database row and refreshes the table instantly.
- [ ] Table highlights low-stock rows in amber and expiring rows in red badges.
- [ ] Real-time search instantly filters table records as user types.
- [ ] Updating existing medicine fields correctly commits changes to MySQL.
- [ ] Deleting a medicine prompts a confirmation dialog before database removal.
- [ ] Registering a Cashier immediately enables that user to log in via `LoginFrame`.
