-- ==========================================================
-- Fyto PIMS (Pharmacy Inventory Management System)
-- Initial Seed & Demonstration Dataset
-- ==========================================================

USE pims_db;

-- 1. Default Accounts (Admin & Cashier)
INSERT INTO users (username, password, role, full_name) VALUES
('admin', 'admin123', 'Admin', 'System Administrator'),
('cashier1', 'cashier123', 'Cashier', 'Jane Doe (Dispenser 1)'),
('cashier2', 'cashier123', 'Cashier', 'John Smith (Dispenser 2)')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 2. Pharmaceutical Suppliers
INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES
('MedPharma Logistics', 'David Clark', '+1 (555) 019-2834', 'orders@medpharma.com', '124 Healthcare Industrial Park, District 4'),
('Apex Bioscience', 'Sarah Connor', '+1 (555) 024-8891', 'supply@apexbio.com', '88 Research Parkway, Biotech City'),
('Global Generic Labs', 'Marcus Vance', '+1 (555) 037-1290', 'sales@globalgeneric.com', '45 Distribution Blvd, Port Hub'),
('VitalCare Remedies', 'Elena Rostova', '+1 (555) 048-9102', 'contact@vitalcare.org', '12 South Medical Center Road');

-- 3. Core Pharmaceutical Inventory (Mix of healthy stock, low stock, and expiring soon)
INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES
('Amoxicillin 500mg', 'MedPharma Logistics', 'Capsule', 12.50, 150, 20, DATE_ADD(CURDATE(), INTERVAL 14 MONTH), 1),
('Paracetamol 500mg', 'Global Generic Labs', 'Tablet', 4.00, 320, 50, DATE_ADD(CURDATE(), INTERVAL 24 MONTH), 3),
('Ibuprofen 400mg', 'Apex Bioscience', 'Tablet', 7.20, 8, 15, DATE_ADD(CURDATE(), INTERVAL 18 MONTH), 2), -- Low Stock Alert
('Cough Syrup DM 100ml', 'MedPharma Logistics', 'Syrup', 9.80, 45, 10, DATE_ADD(CURDATE(), INTERVAL 20 DAY), 1),   -- Expiring Soon (< 30 days)
('Ceftriaxone 1g Vial', 'Apex Bioscience', 'Injection', 24.00, 60, 12, DATE_ADD(CURDATE(), INTERVAL 8 MONTH), 2),
('Hydrocortisone 1% Cream', 'Global Generic Labs', 'Cream', 8.50, 4, 10, DATE_ADD(CURDATE(), INTERVAL 15 DAY), 3), -- Low Stock & Expiring
('Salbutamol Inhaler 100mcg', 'MedPharma Logistics', 'Inhaler', 18.00, 25, 5, DATE_ADD(CURDATE(), INTERVAL 11 MONTH), 1),
('Metformin 850mg', 'VitalCare Remedies', 'Tablet', 11.00, 80, 25, DATE_ADD(CURDATE(), INTERVAL 16 MONTH), 4),
('Omeprazole 20mg', 'VitalCare Remedies', 'Capsule', 14.50, 5, 20, DATE_ADD(CURDATE(), INTERVAL 9 MONTH), 4),   -- Low Stock Alert
('Ciprofloxacin 500mg', 'Apex Bioscience', 'Tablet', 16.00, 95, 15, DATE_ADD(CURDATE(), INTERVAL 20 MONTH), 2),
('Eye Drops Tears 15ml', 'Global Generic Labs', 'Drops', 6.50, 35, 10, DATE_ADD(CURDATE(), INTERVAL 25 DAY), 3);    -- Expiring Soon
