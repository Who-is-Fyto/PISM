# Phase 5: Reporting & Analytics Module

## Overview
Develop management reports and data visualization capabilities to extract actionable business insights from MySQL. Provides three core analytical streams: Sales Performance & Revenue, Inventory Low Stock / Reorder Alerts, and Expiration Risk Analysis, complete with CSV export and printing.

---

### 1. Architectural Scope & Functional Requirements

1. **Sales Performance & Revenue Reporting**:
   * Filter sales by date presets: `Today`, `Past 7 Days`, `This Month`, or `Custom Date Range`.
   * Aggregate total revenue, total transactions, average basket size, and top-selling drugs.
   * View line-item breakdown per transaction.
2. **Inventory Reorder & Low Stock Surveillance**:
   * Surface all medicines where `quantity_in_stock <= reorder_level`.
   * Display recommended reorder quantity, supplier contact person, and phone number for immediate procurement.
3. **Expiry Date Alert Engine**:
   * Categorize medicines expiring in:
     * `Critical`: Expiring in $\le 30$ days (immediate return or markdown).
     * `Warning`: Expiring in $\le 90$ days.
     * `Expired`: Expired today or prior (`expiry_date <= CURDATE()`).
4. **Export & Print Capabilities**:
   * One-click `Export to CSV` generating formatted `.csv` files for Excel.
   * Direct `Print Report` formatting tabular reports for physical documentation.

---

### 2. UI Layout & Component Breakdown (`ui.admin.ReportsPanel`)

```
+----------------------------------------------------------------------------------------------------+
|  REPORT TYPE:  (o) Sales Performance   ( ) Low Stock Alerts   ( ) Expiry Date Alerts              |
+----------------------------------------------------------------------------------------------------+
|  Date Filter:  [ Today ]  [ Past 7 Days ]  [ This Month ]  Custom: [ 2026-09-01 ] to [ 2026-09-19 ] |
|  [ Apply Filter ]                                                  [ Export to CSV ]  [ Print ]    |
+----------------------------------------------------------------------------------------------------+
|  SUMMARY METRICS:                                                                                  |
|  [ Total Revenue: $ 4,280.50 ]  [ Transactions: 142 ]  [ Avg Basket: $ 30.14 ]  [ Cashiers: 3 ]    |
+----------------------------------------------------------------------------------------------------+
|  REPORT TABLE DATA:                                                                                |
|  +----------------------------------------------------------------------------------------------+  |
|  | Sale ID | Date & Time         | Cashier Name | Items Sold | Payment Method | Total Amount    |  |
|  |---------+---------------------+--------------+------------+----------------+-----------------|  |
|  | #1042   | 2026-09-19 14:32:10 | Jane Doe     | 2 items    | Cash           | $ 20.50         |  |
|  | #1041   | 2026-09-19 14:15:02 | John Smith   | 4 items    | Card           | $ 48.00         |  |
|  | #1040   | 2026-09-19 13:50:22 | Jane Doe     | 1 items    | Cash           | $ 12.50         |  |
|  +----------------------------------------------------------------------------------------------+  |
|  Total: 142 Records                                                       Sum Total: $ 4,280.50    |
+----------------------------------------------------------------------------------------------------+
```

#### Individual Frontend Elements to Build
1. **`ui.reports.ReportSelectorBar`**: Segmented pill buttons or toggle radio group switching between the three core report views.
2. **`ui.reports.DateRangeFilterPanel`**: Quick date filter buttons with date inputs, validating that Start Date $\le$ End Date.
3. **`ui.reports.SummaryCardsStrip`**: Responsive KPI summary widgets rendering key metrics (Total Revenue, Transactions, Units Sold).
4. **`ui.reports.ReportDataTable`**: Clean `JTable` with auto-sorting, right-aligned monetary values, and formatted dates.
5. **`ui.reports.CSVExporter`**: Utility class converting active `TableModel` data to standard CSV with headers and automated file-save dialog (`JFileChooser`).

---

### 3. Analytics SQL Queries

#### 1. Sales Summary within Date Range
```sql
SELECT 
    s.sale_id,
    s.sale_date,
    u.full_name AS cashier_name,
    COUNT(si.sale_item_id) AS total_items,
    s.total_amount
FROM sales s
LEFT JOIN users u ON s.user_id = u.user_id
LEFT JOIN sale_items si ON s.sale_id = si.sale_id
WHERE DATE(s.sale_date) BETWEEN ? AND ?
GROUP BY s.sale_id, s.sale_date, u.full_name, s.total_amount
ORDER BY s.sale_date DESC;
```

#### 2. Low Stock / Critical Reorder List
```sql
SELECT 
    m.medicine_id,
    m.name,
    m.medicine_type,
    m.quantity_in_stock,
    m.reorder_level,
    (m.reorder_level * 2 - m.quantity_in_stock) AS suggested_reorder_qty,
    s.name AS supplier_name,
    s.phone AS supplier_phone
FROM medicines m
LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id
WHERE m.quantity_in_stock <= m.reorder_level
ORDER BY m.quantity_in_stock ASC;
```

#### 3. Expiration Surveillance (Next 30 Days & Expired)
```sql
SELECT 
    m.medicine_id,
    m.name,
    m.company,
    m.quantity_in_stock,
    m.expiry_date,
    DATEDIFF(m.expiry_date, CURDATE()) AS days_remaining,
    CASE 
        WHEN m.expiry_date <= CURDATE() THEN 'EXPIRED'
        WHEN DATEDIFF(m.expiry_date, CURDATE()) <= 30 THEN 'CRITICAL'
        ELSE 'WARNING'
    END AS risk_status,
    s.name AS supplier_name
FROM medicines m
LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id
WHERE m.expiry_date <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)
ORDER BY m.expiry_date ASC;
```

---

### 4. Verification Checklist
- [ ] Switching between report types instantly reconfigures table columns and executes the corresponding query.
- [ ] Date presets (Today, 7 Days, Month) accurately filter sales transactions.
- [ ] Summary KPIs compute correct mathematical totals from table data.
- [ ] Low Stock report displays accurate supplier contact information for procurement.
- [ ] Expiry report highlights critical items expiring in $\le 30$ days.
- [ ] CSV Export produces valid files that open seamlessly in spreadsheet software.
