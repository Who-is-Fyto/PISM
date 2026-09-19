-- ============================================================================
-- FYTO PIMS — PHARMACY INVENTORY MANAGEMENT SYSTEM
-- MASTER DATABASE SCHEMA & SEED SCRIPT FOR MYSQL WORKBENCH
-- Compatibility: MySQL 8.0+ / MariaDB 10.5+ (InnoDB Engine)
-- Target Database: pims_db
-- ============================================================================

SET NAMES utf8mb4;
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- ----------------------------------------------------------------------------
-- 1. DATABASE CREATION
-- ----------------------------------------------------------------------------
DROP DATABASE IF EXISTS `pims_db`;
CREATE DATABASE `pims_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `pims_db`;

-- ----------------------------------------------------------------------------
-- 2. TABLE: users (Role-Based Authentication & Staff Registry)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `user_id` INT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `role` ENUM('Admin', 'Cashier') NOT NULL DEFAULT 'Cashier',
    `full_name` VARCHAR(100) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`),
    UNIQUE INDEX `uq_username` (`username` ASC) VISIBLE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Stores system users and roles for RBAC authorization';

-- ----------------------------------------------------------------------------
-- 3. TABLE: suppliers (Pharmaceutical Vendors & Distribution Partners)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `suppliers`;
CREATE TABLE `suppliers` (
    `supplier_id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `contact_person` VARCHAR(100) NULL DEFAULT NULL,
    `phone` VARCHAR(30) NULL DEFAULT NULL,
    `email` VARCHAR(100) NULL DEFAULT NULL,
    `address` TEXT NULL DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`supplier_id`),
    INDEX `idx_supplier_name` (`name` ASC) VISIBLE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Directory of pharmaceutical manufacturers and wholesale vendors';

-- ----------------------------------------------------------------------------
-- 4. TABLE: medicines (Pharmaceutical Catalog, Batches & Reorder Thresholds)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `medicines`;
CREATE TABLE `medicines` (
    `medicine_id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(150) NOT NULL,
    `company` VARCHAR(100) NULL DEFAULT NULL,
    `medicine_type` VARCHAR(50) NOT NULL, -- Tablet, Capsule, Syrup, Injection, Cream, Inhaler, Drops, Ointment, Other
    `price` DECIMAL(10,2) NOT NULL,
    `quantity_in_stock` INT NOT NULL DEFAULT 0,
    `reorder_level` INT NOT NULL DEFAULT 10,
    `expiry_date` DATE NOT NULL,
    `supplier_id` INT NULL DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`medicine_id`),
    INDEX `fk_medicines_suppliers_idx` (`supplier_id` ASC) VISIBLE,
    INDEX `idx_med_name` (`name` ASC) VISIBLE,
    INDEX `idx_med_expiry` (`expiry_date` ASC) VISIBLE,
    INDEX `idx_med_stock` (`quantity_in_stock` ASC) VISIBLE,
    CONSTRAINT `fk_medicines_suppliers`
        FOREIGN KEY (`supplier_id`)
        REFERENCES `suppliers` (`supplier_id`)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Drug inventory records with real-time stock and expiration tracking';

-- ----------------------------------------------------------------------------
-- 5. TABLE: sales (Point of Sale Transaction Receipts Header)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `sales`;
CREATE TABLE `sales` (
    `sale_id` INT NOT NULL AUTO_INCREMENT,
    `sale_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `total_amount` DECIMAL(10,2) NOT NULL,
    `amount_paid` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `change_given` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `user_id` INT NULL DEFAULT NULL,
    PRIMARY KEY (`sale_id`),
    INDEX `fk_sales_users_idx` (`user_id` ASC) VISIBLE,
    INDEX `idx_sales_date` (`sale_date` DESC) VISIBLE,
    CONSTRAINT `fk_sales_users`
        FOREIGN KEY (`user_id`)
        REFERENCES `users` (`user_id`)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Header records for completed retail point-of-sale transactions';

-- ----------------------------------------------------------------------------
-- 6. TABLE: sale_items (Dispensed Line Items per Sale)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `sale_items`;
CREATE TABLE `sale_items` (
    `sale_item_id` INT NOT NULL AUTO_INCREMENT,
    `sale_id` INT NOT NULL,
    `medicine_id` INT NOT NULL,
    `quantity_sold` INT NOT NULL,
    `price_at_sale` DECIMAL(10,2) NOT NULL,
    `subtotal` DECIMAL(10,2) GENERATED ALWAYS AS (`quantity_sold` * `price_at_sale`) STORED,
    PRIMARY KEY (`sale_item_id`),
    INDEX `fk_sale_items_sales_idx` (`sale_id` ASC) VISIBLE,
    INDEX `fk_sale_items_medicines_idx` (`medicine_id` ASC) VISIBLE,
    CONSTRAINT `fk_sale_items_sales`
        FOREIGN KEY (`sale_id`)
        REFERENCES `sales` (`sale_id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT `fk_sale_items_medicines`
        FOREIGN KEY (`medicine_id`)
        REFERENCES `medicines` (`medicine_id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Individual medication line items associated with each sale receipt';

-- ----------------------------------------------------------------------------
-- 7. ANALYTICAL VIEWS (Convenient for MySQL Workbench Inspection)
-- ----------------------------------------------------------------------------

-- View 1: Urgent Low Stock Surveillance
CREATE OR REPLACE VIEW `v_low_stock_alerts` AS
SELECT 
    m.medicine_id,
    m.name AS medicine_name,
    m.medicine_type,
    m.quantity_in_stock,
    m.reorder_level,
    (m.reorder_level - m.quantity_in_stock) AS stock_deficit,
    (m.reorder_level * 2 - m.quantity_in_stock) AS suggested_reorder_units,
    (m.price * GREATEST(0, m.reorder_level * 2 - m.quantity_in_stock)) AS est_restock_cost,
    s.name AS preferred_supplier,
    s.contact_person AS supplier_contact,
    s.phone AS supplier_phone
FROM medicines m
LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id
WHERE m.quantity_in_stock <= m.reorder_level
ORDER BY m.quantity_in_stock ASC;

-- View 2: Expiration Risk Engine (Expiring in <= 30 Days or Already Expired)
CREATE OR REPLACE VIEW `v_expiration_risk` AS
SELECT 
    m.medicine_id,
    m.name AS medicine_name,
    m.company,
    m.quantity_in_stock,
    m.price AS unit_price,
    (m.price * m.quantity_in_stock) AS capital_at_risk,
    m.expiry_date,
    DATEDIFF(m.expiry_date, CURDATE()) AS days_until_expiration,
    CASE 
        WHEN m.expiry_date <= CURDATE() THEN 'EXPIRED'
        WHEN DATEDIFF(m.expiry_date, CURDATE()) <= 30 THEN 'CRITICAL'
        WHEN DATEDIFF(m.expiry_date, CURDATE()) <= 90 THEN 'WARNING'
        ELSE 'GOOD'
    END AS risk_classification,
    s.name AS supplier_name
FROM medicines m
LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id
WHERE m.expiry_date <= DATE_ADD(CURDATE(), INTERVAL 90 DAY)
ORDER BY m.expiry_date ASC;

-- View 3: Sales Daily Summary
CREATE OR REPLACE VIEW `v_sales_performance` AS
SELECT 
    s.sale_id,
    s.sale_date,
    u.full_name AS cashier_name,
    COUNT(si.sale_item_id) AS line_items_count,
    COALESCE(SUM(si.quantity_sold), 0) AS units_dispensed,
    s.total_amount,
    s.amount_paid,
    s.change_given
FROM sales s
LEFT JOIN users u ON s.user_id = u.user_id
LEFT JOIN sale_items si ON s.sale_id = si.sale_id
GROUP BY s.sale_id, s.sale_date, u.full_name, s.total_amount, s.amount_paid, s.change_given
ORDER BY s.sale_date DESC;

-- ----------------------------------------------------------------------------
-- 8. INITIAL DATA SEEDING (Accounts, Vendors, Medicines & Sample Receipts)
-- ----------------------------------------------------------------------------

-- Seed 1: Default Staff Users (Admin & Cashiers)
INSERT INTO `users` (`user_id`, `username`, `password`, `role`, `full_name`) VALUES
(1, 'admin', 'admin123', 'Admin', 'System Administrator'),
(2, 'cashier1', 'cashier123', 'Cashier', 'Jane Doe (Dispenser 1)'),
(3, 'cashier2', 'cashier123', 'Cashier', 'John Smith (Dispenser 2)');

-- Seed 2: Pharmaceutical Suppliers
INSERT INTO `suppliers` (`supplier_id`, `name`, `contact_person`, `phone`, `email`, `address`) VALUES
(1, 'MedPharma Logistics', 'David Clark', '+1 (555) 019-2834', 'orders@medpharma.com', '124 Healthcare Industrial Park, District 4'),
(2, 'Apex Bioscience', 'Sarah Connor', '+1 (555) 024-8891', 'supply@apexbio.com', '88 Research Parkway, Biotech City'),
(3, 'Global Generic Labs', 'Marcus Vance', '+1 (555) 037-1290', 'sales@globalgeneric.com', '45 Distribution Blvd, Port Hub'),
(4, 'VitalCare Remedies', 'Elena Rostova', '+1 (555) 048-9102', 'contact@vitalcare.org', '12 South Medical Center Road');

-- Seed 3: Medicines Catalog
INSERT INTO `medicines` (`medicine_id`, `name`, `company`, `medicine_type`, `price`, `quantity_in_stock`, `reorder_level`, `expiry_date`, `supplier_id`) VALUES
(1, 'Amoxicillin 500mg', 'MedPharma Logistics', 'Capsule', 12.50, 150, 20, DATE_ADD(CURDATE(), INTERVAL 14 MONTH), 1),
(2, 'Paracetamol 500mg', 'Global Generic Labs', 'Tablet', 4.00, 320, 50, DATE_ADD(CURDATE(), INTERVAL 24 MONTH), 3),
(3, 'Ibuprofen 400mg', 'Apex Bioscience', 'Tablet', 7.20, 8, 15, DATE_ADD(CURDATE(), INTERVAL 18 MONTH), 2), -- Low Stock Alert
(4, 'Cough Syrup DM 100ml', 'MedPharma Logistics', 'Syrup', 9.80, 45, 10, DATE_ADD(CURDATE(), INTERVAL 20 DAY), 1),   -- Expiring Soon (< 30 days)
(5, 'Ceftriaxone 1g Vial', 'Apex Bioscience', 'Injection', 24.00, 60, 12, DATE_ADD(CURDATE(), INTERVAL 8 MONTH), 2),
(6, 'Hydrocortisone 1% Cream', 'Global Generic Labs', 'Cream', 8.50, 4, 10, DATE_ADD(CURDATE(), INTERVAL 15 DAY), 3), -- Low Stock & Expiring
(7, 'Salbutamol Inhaler 100mcg', 'MedPharma Logistics', 'Inhaler', 18.00, 25, 5, DATE_ADD(CURDATE(), INTERVAL 11 MONTH), 1),
(8, 'Metformin 850mg', 'VitalCare Remedies', 'Tablet', 11.00, 80, 25, DATE_ADD(CURDATE(), INTERVAL 16 MONTH), 4),
(9, 'Omeprazole 20mg', 'VitalCare Remedies', 'Capsule', 14.50, 5, 20, DATE_ADD(CURDATE(), INTERVAL 9 MONTH), 4),   -- Low Stock Alert
(10, 'Ciprofloxacin 500mg', 'Apex Bioscience', 'Tablet', 16.00, 95, 15, DATE_ADD(CURDATE(), INTERVAL 20 MONTH), 2),
(11, 'Eye Drops Tears 15ml', 'Global Generic Labs', 'Drops', 6.50, 35, 10, DATE_ADD(CURDATE(), INTERVAL 25 DAY), 3);    -- Expiring Soon

-- Seed 4: Historical POS Sales Headers
INSERT INTO `sales` (`sale_id`, `sale_date`, `total_amount`, `amount_paid`, `change_given`, `user_id`) VALUES
(1001, DATE_SUB(NOW(), INTERVAL 30 MINUTE), 29.00, 30.00, 1.00, 2),
(1002, DATE_SUB(NOW(), INTERVAL 90 MINUTE), 48.00, 50.00, 2.00, 3),
(1003, DATE_SUB(NOW(), INTERVAL 1 DAY), 36.00, 40.00, 4.00, 2),
(1004, DATE_SUB(NOW(), INTERVAL 3 DAY), 18.30, 20.00, 1.70, 3),
(1005, DATE_SUB(NOW(), INTERVAL 5 DAY), 32.00, 35.00, 3.00, 2);

-- Seed 5: Sale Line Items
INSERT INTO `sale_items` (`sale_id`, `medicine_id`, `quantity_sold`, `price_at_sale`) VALUES
(1001, 1, 2, 12.50), -- 2x Amoxicillin 500mg
(1001, 2, 1, 4.00),  -- 1x Paracetamol 500mg
(1002, 5, 2, 24.00), -- 2x Ceftriaxone 1g Vial
(1003, 7, 2, 18.00), -- 2x Salbutamol Inhaler
(1004, 4, 1, 9.80),  -- 1x Cough Syrup DM
(1004, 6, 1, 8.50),  -- 1x Hydrocortisone 1% Cream
(1005, 10, 2, 16.00);-- 2x Ciprofloxacin 500mg

-- Reset AUTO_INCREMENT counters past seeds
ALTER TABLE `users` AUTO_INCREMENT = 10;
ALTER TABLE `suppliers` AUTO_INCREMENT = 10;
ALTER TABLE `medicines` AUTO_INCREMENT = 20;
ALTER TABLE `sales` AUTO_INCREMENT = 1010;
ALTER TABLE `sale_items` AUTO_INCREMENT = 20;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- ----------------------------------------------------------------------------
-- 9. SCHEMA SUMMARY CONFIRMATION
-- ----------------------------------------------------------------------------
SELECT 'Fyto PIMS Schema Created Successfully!' AS `Status`;

SELECT 
    (SELECT COUNT(*) FROM `users`) AS `Users Count`,
    (SELECT COUNT(*) FROM `suppliers`) AS `Suppliers Count`,
    (SELECT COUNT(*) FROM `medicines`) AS `Medicines Count`,
    (SELECT COUNT(*) FROM `sales`) AS `Sales Count`,
    (SELECT COUNT(*) FROM `sale_items`) AS `Sale Items Count`;
