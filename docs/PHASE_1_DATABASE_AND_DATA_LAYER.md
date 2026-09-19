# Phase 1: Database Schema & Core Data Access Layer

## Overview
Establish the MySQL database schema (`pims_db`), configure JDBC connectivity with connection pooling/properties, and build reusable Data Access Objects (DAOs) and Model POJOs.

---

### 1. Database Schema DDL (MySQL)

Execute the DDL script to create the 5 relational tables with exact types, indexes, and foreign key constraints:

```sql
CREATE DATABASE IF NOT EXISTS pims_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pims_db;

-- 1. Users Table (Multi-user with Role-Based Access Control)
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Cashier') NOT NULL DEFAULT 'Cashier',
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Suppliers Table (Directory for pharmaceutical vendors)
CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Medicines Table (Inventory tracking with reorder levels & expiry alerts)
CREATE TABLE IF NOT EXISTS medicines (
    medicine_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    company VARCHAR(100),
    medicine_type VARCHAR(50) NOT NULL, -- Tablet, Capsule, Syrup, Injection, Cream, Inhaler, Drops
    price DECIMAL(10,2) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 10,
    expiry_date DATE NOT NULL,
    supplier_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE SET NULL,
    INDEX idx_medicine_name (name),
    INDEX idx_expiry_date (expiry_date),
    INDEX idx_stock (quantity_in_stock)
) ENGINE=InnoDB;

-- 4. Sales Header Table
CREATE TABLE IF NOT EXISTS sales (
    sale_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    amount_paid DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    change_given DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_sale_date (sale_date)
) ENGINE=InnoDB;

-- 5. Sale Line Items Table
CREATE TABLE IF NOT EXISTS sale_items (
    sale_item_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity_sold INT NOT NULL,
    price_at_sale DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) GENERATED ALWAYS AS (quantity_sold * price_at_sale) STORED,
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) ON DELETE RESTRICT,
    INDEX idx_sale_items (sale_id, medicine_id)
) ENGINE=InnoDB;
```

---

### 2. Seed Data for Testing & Demonstration

```sql
USE pims_db;

-- Initial Admin and Cashier accounts
INSERT INTO users (username, password, role, full_name) VALUES
('admin', 'admin123', 'Admin', 'System Administrator'),
('cashier1', 'cashier123', 'Cashier', 'Jane Doe - Dispenser'),
('cashier2', 'cashier123', 'Cashier', 'John Smith - Counter 2')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- Sample Suppliers
INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES
('MedPharma Logistics', 'David Clark', '+1-555-0199', 'orders@medpharma.com', '124 Industrial Way, Healthcare District'),
('Apex Bioscience', 'Sarah Connor', '+1-555-0244', 'supply@apexbio.com', '88 Research Parkway, Biotech City'),
('Global Generic Labs', 'Marcus Vance', '+1-555-0377', 'sales@globalgeneric.com', '45 Distribution Blvd, Port Hub');

-- Sample Medicines (including normal stock, low stock, and expiring soon)
INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES
('Amoxicillin 500mg', 'MedPharma Logistics', 'Capsule', 12.50, 150, 20, DATE_ADD(CURDATE(), INTERVAL 14 MONTH), 1),
('Paracetamol 500mg', 'Global Generic Labs', 'Tablet', 4.00, 320, 50, DATE_ADD(CURDATE(), INTERVAL 24 MONTH), 3),
('Ibuprofen 400mg', 'Apex Bioscience', 'Tablet', 7.20, 8, 15, DATE_ADD(CURDATE(), INTERVAL 18 MONTH), 2), -- Low Stock Alert
('Cough Syrup DM 100ml', 'MedPharma Logistics', 'Syrup', 9.80, 45, 10, DATE_ADD(CURDATE(), INTERVAL 20 DAY), 1),   -- Expiring Soon (< 30 days)
('Ceftriaxone 1g Vial', 'Apex Bioscience', 'Injection', 24.00, 60, 12, DATE_ADD(CURDATE(), INTERVAL 8 MONTH), 2),
('Hydrocortisone 1% Cream', 'Global Generic Labs', 'Cream', 8.50, 4, 10, DATE_ADD(CURDATE(), INTERVAL 15 DAY), 3), -- Low Stock & Expiring
('Salbutamol Inhaler 100mcg', 'MedPharma Logistics', 'Inhaler', 18.00, 25, 5, DATE_ADD(CURDATE(), INTERVAL 11 MONTH), 1);
```

---

### 3. Java Classes & Data Layer Architecture

#### Directory Structure
```
src/
├── pharmacyims/
│   ├── model/
│   │   ├── User.java
│   │   ├── Supplier.java
│   │   ├── Medicine.java
│   │   ├── Sale.java
│   │   └── SaleItem.java
│   ├── dao/
│   │   ├── UserDAO.java
│   │   ├── SupplierDAO.java
│   │   ├── MedicineDAO.java
│   │   └── SaleDAO.java
│   └── util/
│       ├── DBConnection.java
│       └── PasswordHasher.java
└── resources/
    └── db.properties
```

#### Key Component Responsibilities
* **`util.DBConnection`**: Thread-safe singleton connection manager reading from `db.properties` (driver, URL, username, password). Includes automatic driver loading (`com.mysql.cj.jdbc.Driver`).
* **`util.PasswordHasher`**: Provides SHA-256 or BCrypt hashing to ensure credentials are not stored in raw plaintext.
* **`model.*`**: Strongly typed POJOs with getters, setters, constructors, and `toString()` representation.
* **`dao.UserDAO`**:
  * `authenticate(String username, String password): User`
  * `createCashier(User user): boolean`
  * `getAllCashiers(): List<User>`
  * `updateUser(User user): boolean`
  * `deleteUser(int userId): boolean`
* **`dao.SupplierDAO`**:
  * `getAllSuppliers(): List<Supplier>`
  * `getSupplierById(int id): Supplier`
  * `addSupplier(Supplier supplier): boolean`
  * `updateSupplier(Supplier supplier): boolean`
  * `deleteSupplier(int id): boolean`
* **`dao.MedicineDAO`**:
  * `getAllMedicines(): List<Medicine>`
  * `getMedicineById(int id): Medicine`
  * `searchMedicines(String query, String typeFilter): List<Medicine>`
  * `addMedicine(Medicine m): boolean`
  * `updateMedicine(Medicine m): boolean`
  * `deleteMedicine(int id): boolean`
  * `getLowStockMedicines(): List<Medicine>`
  * `getExpiringMedicines(int daysThreshold): List<Medicine>`
* **`dao.SaleDAO`**:
  * `processSale(Sale sale, List<SaleItem> items): int` (Wrapped in atomic transaction: `connection.setAutoCommit(false)`, inserts sale header, inserts line items, updates medicine stock, executes commit/rollback).
  * `getSalesByDateRange(LocalDate start, LocalDate end): List<Sale>`
  * `getSaleDetails(int saleId): List<SaleItem>`

---

### 4. Verification Checklist
- [ ] Database schema executed in MySQL with 0 syntax or foreign key errors.
- [ ] Seed data successfully inserted and verified via SQL queries.
- [ ] `DBConnection.getConnection()` returns an active connection.
- [ ] Model classes cleanly map all database columns.
- [ ] Standalone test runner executes CRUD operations across all 4 DAOs with 100% assertions passing.
