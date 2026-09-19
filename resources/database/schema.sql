-- ==========================================================
-- Fyto PIMS (Pharmacy Inventory Management System)
-- Database Schema Definition (MySQL 8.0+ / InnoDB)
-- ==========================================================

CREATE DATABASE IF NOT EXISTS pims_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE pims_db;

-- ----------------------------------------------------------
-- 1. Users Table (Role-Based Access Control)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Cashier') NOT NULL DEFAULT 'Cashier',
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 2. Suppliers Table (Pharmaceutical Vendors)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(30),
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 3. Medicines Table (Inventory, Reorder Levels, Expiry Dates)
-- ----------------------------------------------------------
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
    INDEX idx_med_name (name),
    INDEX idx_med_expiry (expiry_date),
    INDEX idx_med_stock (quantity_in_stock)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 4. Sales Header Table
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales (
    sale_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    amount_paid DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    change_given DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_sales_date (sale_date)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 5. Sale Line Items Table
-- ----------------------------------------------------------
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
