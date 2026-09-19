# Phase 4: Cashier Module (POS, Stock Lookup & Billing)

## Overview
Develop the high-speed Point-of-Sale (POS) cashier dispensing interface. Enables quick medicine lookups, stock availability checks, dynamic cart management, atomic checkout transactions with automatic inventory reduction, and bill receipt printing.

---

### 1. Architectural Scope & Functional Requirements

1. **Quick Medicine Lookup & Stock Verification**:
   * Cashier searches by medicine name or ID.
   * Real-time preview card displays: Drug Name, Available Stock, Unit Price, Expiry Date.
   * Prevents adding out-of-stock items (stock = 0) or expired items to the cart.
2. **Interactive Cart Management**:
   * Add selected medicine with requested quantity.
   * Enforce stock validation: if requested quantity > `quantity_in_stock`, alert cashier and prevent overflow.
   * Update quantity directly in cart table or via increment/decrement buttons.
   * Remove line items or clear entire cart with shortcut.
   * Live recalculation of Subtotal, Tax/Discount (if applicable), and Grand Total.
3. **Atomic Transactional Checkout**:
   * Input `Amount Paid` by customer; automatically calculate `Change Due`.
   * Enforce: `Amount Paid >= Grand Total`.
   * Wrap database write in a database transaction (`connection.setAutoCommit(false)`):
     1. Insert record into `sales` table $\rightarrow$ obtain generated `sale_id`.
     2. Insert each item into `sale_items` table.
     3. Decrement `quantity_in_stock` in `medicines` table.
     4. Commit transaction. On any exception, rollback cleanly.
4. **Receipt Generation & Print Dialogue**:
   * Open modal dialog displaying formatted thermal/standard pharmacy receipt.
   * Provide one-click printing via Java Print API (`JTextArea.print()`).

---

### 2. UI Layout & Component Breakdown (`ui.cashier.CashierDashboard`)

```
+----------------------------------------------------------------------------------------------------+
| [Logo] OBELLION PHARMACY - CASHIER POS         Cashier: Jane Doe [Counter 1]       Time: 14:32 (Logout) |
+----------------------------------------------------------------------------------------------------+
|  [LEFT: PRODUCT SEARCH & CART SELECTION]        |  [RIGHT: ACTIVE CART & CHECKOUT PANEL]           |
|                                                 |                                                  |
|  Search Medicine:                               |  ACTIVE BILL / DISPENSING CART                   |
|  [ Paracetamol 500mg                        v ] |  +--------------------------------------------+  |
|                                                 |  | Item | Description      | Price | Qty| Total|  |
|  +-- Selected Product Info ------------------+  |  |------+------------------+-------+----+------|  |
|  | Name: Paracetamol 500mg                   |  |  | 01   | Paracetamol 500mg| $4.00 |  2 | $8.00 |  |
|  | Company: Global Generic Labs              |  |  | 02   | Amoxicillin 500mg| $12.50|  1 |$12.50 |  |
|  | Unit Price: $4.00                         |  |  +--------------------------------------------+  |
|  | In Stock:   320 units [Available]         |  |  [ Remove Selected Item ]   [ Clear Cart ]       |
|  | Expiry:     2028-06-15                    |  |                                                  |
|  +-------------------------------------------+  |  ----------------------------------------------  |
|                                                 |  BILL SUMMARY                                    |
|  Quantity to Dispense:                          |  Grand Total:   $ 20.50                          |
|  [ - ] [  2  ] [ + ]                            |  Amount Paid:   [ $ 25.00                      ] |
|                                                 |  Change Due:    $  4.50  (Green)                 |
|  [ >>> ADD TO CART (F2) <<< ]                   |                                                  |
|                                                 |  [ >>> COMPLETE SALE & PRINT BILL (F5) <<< ]     |
+----------------------------------------------------------------------------------------------------+
```

#### Individual Frontend Elements to Build
1. **`ui.cashier.StockLookupPanel`**: Input bar with auto-completer or dropdown selector, accompanied by an instant details panel showing product vitals, stock badge, and expiry status.
2. **`ui.cashier.QuantitySpinner`**: Clean increment/decrement widget with keyboard input support and bounds enforcement ($1 \le Qty \le Stock$).
3. **`ui.cashier.CartTable`**: Tailored `JTable` rendering cart items with bold item names, formatted currency cells, and a delete row action button.
4. **`ui.cashier.PaymentSummaryPanel`**: Prominent display panel showing bold Grand Total in large font, payment entry field with auto-change calculation on key release, and confirmation checkout button.
5. **`ui.cashier.BillReceiptDialog`**: Pop-up window rendering thermal-receipt style formatted text with `Print` and `Close` buttons.

---

### 3. Critical Transaction Implementation (`SaleDAO.java`)

```java
public int processSale(Sale sale, List<SaleItem> items) throws SQLException {
    String insertSaleSQL = "INSERT INTO sales (total_amount, amount_paid, change_given, user_id) VALUES (?, ?, ?, ?)";
    String insertItemSQL = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";
    String updateStockSQL = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id = ? AND quantity_in_stock >= ?";

    Connection conn = null;
    try {
        conn = DBConnection.getConnection();
        conn.setAutoCommit(false); // Begin atomic transaction

        // 1. Insert Sales Header
        int saleId = -1;
        try (PreparedStatement psSale = conn.prepareStatement(insertSaleSQL, Statement.RETURN_GENERATED_KEYS)) {
            psSale.setBigDecimal(1, sale.getTotalAmount());
            psSale.setBigDecimal(2, sale.getAmountPaid());
            psSale.setBigDecimal(3, sale.getChangeGiven());
            psSale.setInt(4, sale.getUserId());
            psSale.executeUpdate();

            try (ResultSet rs = psSale.getGeneratedKeys()) {
                if (rs.next()) {
                    saleId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to obtain generated sale ID.");
                }
            }
        }

        // 2. Insert Line Items and Deduct Inventory
        try (PreparedStatement psItem = conn.prepareStatement(insertItemSQL);
             PreparedStatement psStock = conn.prepareStatement(updateStockSQL)) {

            for (SaleItem item : items) {
                // Insert item record
                psItem.setInt(1, saleId);
                psItem.setInt(2, item.getMedicineId());
                psItem.setInt(3, item.getQuantitySold());
                psItem.setBigDecimal(4, item.getPriceAtSale());
                psItem.executeUpdate();

                // Deduct stock safely (verifying stock >= requested quantity)
                psStock.setInt(1, item.getQuantitySold());
                psStock.setInt(2, item.getMedicineId());
                psStock.setInt(3, item.getQuantitySold());
                int rowsAffected = psStock.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Insufficient stock for medicine ID: " + item.getMedicineId());
                }
            }
        }

        conn.commit(); // Commit all changes together
        return saleId;

    } catch (SQLException ex) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException rbEx) { /* log rollback error */ }
        }
        throw ex;
    } finally {
        if (conn != null) {
            try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* log */ }
        }
    }
}
```

---

### 4. Receipt Template Format (`BillReceiptDialog`)

```
==================================================
              OBELLION PHARMACY LTD              
          124 Health Avenue, Care City           
             Tel: +1 (555) 0199-CARE             
==================================================
Receipt #: REC-20260919-0042
Date/Time: 2026-09-19 14:32:10
Cashier:   Jane Doe (ID: 2)
--------------------------------------------------
Item                  Qty    Price       Subtotal
--------------------------------------------------
Paracetamol 500mg       2    $4.00          $8.00
Amoxicillin 500mg       1   $12.50         $12.50
--------------------------------------------------
TOTAL AMOUNT:                              $20.50
AMOUNT TENDERED:                           $25.00
CHANGE RETURNED:                            $4.50
==================================================
         Thank you for choosing Obellion!         
          Please store medicines safely.          
==================================================
```

---

### 5. Verification Checklist
- [ ] Fast search instantly brings up medicine details, price, and current stock.
- [ ] Attempting to add more items than in stock displays an immediate validation warning dialog.
- [ ] Cart updates totals dynamically as quantities change.
- [ ] Completing checkout creates records in `sales` and `sale_items` and decrements `medicines.quantity_in_stock`.
- [ ] Receipt dialog appears with complete itemized breakdown and functioning `Print` trigger.
- [ ] After checkout, cart resets cleanly for the next customer.
