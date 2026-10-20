-- =====================================================================
-- DATABASE SCRIPT - RESTAURANT MANAGEMENT SYSTEM
-- Phase 4: Relational Model and SQL Persistence (3NF)
-- Group 04 - Cohort 3239188 - ADSO SENA
-- =====================================================================

-- Drop database if exists and create new
DROP DATABASE IF EXISTS restaurante_db;
CREATE DATABASE restaurante_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE restaurante_db;

-- =====================================================================
-- TABLE: customer
-- =====================================================================
CREATE TABLE customer (
    id_customer       INT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    phone             VARCHAR(20),
    email             VARCHAR(100),
    address           VARCHAR(200),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active            BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT uk_customer_email UNIQUE (email),
    CONSTRAINT chk_customer_phone CHECK (phone IS NULL OR phone REGEXP '^[0-9+\-\s()]{7,20}$')
) ENGINE=InnoDB COMMENT='Restaurant customers';

-- =====================================================================
-- TABLE: table_restaurant (reserved word 'table' avoided)
-- =====================================================================
CREATE TABLE table_restaurant (
    id_table      INT AUTO_INCREMENT PRIMARY KEY,
    number        INT NOT NULL,
    capacity      INT NOT NULL,
    status        ENUM('FREE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE') NOT NULL DEFAULT 'FREE',
    location      VARCHAR(50),
    
    CONSTRAINT uk_table_number UNIQUE (number),
    CONSTRAINT chk_table_capacity CHECK (capacity > 0 AND capacity <= 20),
    CONSTRAINT chk_table_number CHECK (number > 0)
) ENGINE=InnoDB COMMENT='Restaurant tables';

-- =====================================================================
-- TABLE: dish_category
-- =====================================================================
CREATE TABLE dish_category (
    id_category     INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(200),
    display_order   INT DEFAULT 0,
    
    CONSTRAINT uk_category_name UNIQUE (name)
) ENGINE=InnoDB COMMENT='Menu categories (Appetizers, Mains, Desserts, Drinks, etc.)';

-- =====================================================================
-- TABLE: dish
-- =====================================================================
CREATE TABLE dish (
    id_dish             INT AUTO_INCREMENT PRIMARY KEY,
    id_category         INT NOT NULL,
    name                VARCHAR(100) NOT NULL,
    description         VARCHAR(300),
    price               DECIMAL(10,2) NOT NULL,
    available           BOOLEAN DEFAULT TRUE,
    prep_time_minutes   INT DEFAULT 15 COMMENT 'Estimated time in minutes',
    
    CONSTRAINT fk_dish_category FOREIGN KEY (id_category) REFERENCES dish_category(id_category)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_dish_price CHECK (price >= 0),
    CONSTRAINT chk_dish_prep_time CHECK (prep_time_minutes > 0 AND prep_time_minutes <= 180)
) ENGINE=InnoDB COMMENT='Menu dishes';

-- =====================================================================
-- TABLE: order_restaurant (reserved word 'order' avoided)
-- =====================================================================
CREATE TABLE order_restaurant (
    id_order        INT AUTO_INCREMENT PRIMARY KEY,
    id_table        INT NOT NULL,
    id_customer     INT NULL,
    order_datetime  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status          ENUM('PENDING', 'IN_PREPARATION', 'READY', 'DELIVERED', 'CANCELLED', 'INVOICED') NOT NULL DEFAULT 'PENDING',
    notes           VARCHAR(500),
    total           DECIMAL(10,2) DEFAULT 0.00,
    
    CONSTRAINT fk_order_table FOREIGN KEY (id_table) REFERENCES table_restaurant(id_table)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_order_customer FOREIGN KEY (id_customer) REFERENCES customer(id_customer)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT chk_order_total CHECK (total >= 0)
) ENGINE=InnoDB COMMENT='Orders placed at tables';

-- =====================================================================
-- TABLE: order_detail (N:M relationship between order and dish)
-- =====================================================================
CREATE TABLE order_detail (
    id_detail       INT AUTO_INCREMENT PRIMARY KEY,
    id_order        INT NOT NULL,
    id_dish         INT NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    unit_price      DECIMAL(10,2) NOT NULL,
    subtotal        DECIMAL(10,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    notes           VARCHAR(300),
    
    CONSTRAINT fk_detail_order FOREIGN KEY (id_order) REFERENCES order_restaurant(id_order)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_detail_dish FOREIGN KEY (id_dish) REFERENCES dish(id_dish)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uk_detail_order_dish UNIQUE (id_order, id_dish),
    CONSTRAINT chk_detail_quantity CHECK (quantity > 0),
    CONSTRAINT chk_detail_price CHECK (unit_price >= 0)
) ENGINE=InnoDB COMMENT='Order detail lines';

-- =====================================================================
-- TABLE: invoice
-- =====================================================================
CREATE TABLE invoice (
    id_invoice      INT AUTO_INCREMENT PRIMARY KEY,
    id_order        INT NOT NULL,
    issue_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal        DECIMAL(10,2) NOT NULL,
    taxes           DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total           DECIMAL(10,2) NOT NULL,
    payment_method  ENUM('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'TRANSFER', 'QR', 'MIXED') NOT NULL,
    payment_status  ENUM('PENDING', 'PAID', 'VOIDED', 'PARTIAL') NOT NULL DEFAULT 'PENDING',
    invoice_number  VARCHAR(20) NOT NULL,
    notes           VARCHAR(500),
    
    CONSTRAINT fk_invoice_order FOREIGN KEY (id_order) REFERENCES order_restaurant(id_order)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uk_invoice_order UNIQUE (id_order),
    CONSTRAINT uk_invoice_number UNIQUE (invoice_number),
    CONSTRAINT chk_invoice_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_invoice_taxes CHECK (taxes >= 0),
    CONSTRAINT chk_invoice_total CHECK (total >= 0)
) ENGINE=InnoDB COMMENT='Invoices generated from orders';

-- =====================================================================
-- TABLE: invoice_payment (for mixed or partial payments)
-- =====================================================================
CREATE TABLE invoice_payment (
    id_payment      INT AUTO_INCREMENT PRIMARY KEY,
    id_invoice      INT NOT NULL,
    payment_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount          DECIMAL(10,2) NOT NULL,
    payment_method  ENUM('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'TRANSFER', 'QR') NOT NULL,
    reference       VARCHAR(100),
    
    CONSTRAINT fk_payment_invoice FOREIGN KEY (id_invoice) REFERENCES invoice(id_invoice)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT chk_payment_amount CHECK (amount > 0)
) ENGINE=InnoDB COMMENT='Payment details applied to an invoice (mixed payments support)';

-- =====================================================================
-- ADDITIONAL INDEXES FOR QUERY OPTIMIZATION
-- =====================================================================
CREATE INDEX idx_order_table_status ON order_restaurant(id_table, status);
CREATE INDEX idx_order_date ON order_restaurant(order_datetime);
CREATE INDEX idx_order_customer ON order_restaurant(id_customer);
CREATE INDEX idx_detail_order ON order_detail(id_order);
CREATE INDEX idx_invoice_date ON invoice(issue_date);
CREATE INDEX idx_invoice_payment_status ON invoice(payment_status);
CREATE INDEX idx_dish_category ON dish(id_category, available);

-- =====================================================================
-- USEFUL VIEWS
-- =====================================================================

-- View: Available tables with capacity
CREATE VIEW v_available_tables AS
SELECT id_table, number, capacity, location
FROM table_restaurant
WHERE status = 'FREE' AND capacity > 0;

-- View: Active menu by category
CREATE VIEW v_active_menu AS
SELECT d.id_dish, d.name, d.description, d.price, d.prep_time_minutes,
       c.name AS category
FROM dish d
JOIN dish_category c ON d.id_category = c.id_category
WHERE d.available = TRUE
ORDER BY c.display_order, d.name;

-- View: Orders pending in kitchen
CREATE VIEW v_kitchen_orders AS
SELECT o.id_order, o.id_table, t.number AS table_number, o.order_datetime,
       o.status, o.notes,
       GROUP_CONCAT(CONCAT(od.quantity, 'x ', d.name) SEPARATOR ', ') AS items
FROM order_restaurant o
JOIN table_restaurant t ON o.id_table = t.id_table
JOIN order_detail od ON o.id_order = od.id_order
JOIN dish d ON od.id_dish = d.id_dish
WHERE o.status IN ('PENDING', 'IN_PREPARATION')
GROUP BY o.id_order
ORDER BY o.order_datetime;

-- View: Pending invoices
CREATE VIEW v_pending_invoices AS
SELECT i.id_invoice, i.invoice_number, i.issue_date, i.total, i.payment_status,
       o.id_table, t.number AS table_number, c.name AS customer
FROM invoice i
JOIN order_restaurant o ON i.id_order = o.id_order
JOIN table_restaurant t ON o.id_table = t.id_table
LEFT JOIN customer c ON o.id_customer = c.id_customer
WHERE i.payment_status IN ('PENDING', 'PARTIAL')
ORDER BY i.issue_date;

-- View: Daily sales
CREATE VIEW v_daily_sales AS
SELECT DATE(issue_date) AS date,
       COUNT(*) AS total_invoices,
       SUM(total) AS total_sales,
       SUM(CASE WHEN payment_method = 'CASH' THEN total ELSE 0 END) AS cash_sales,
       SUM(CASE WHEN payment_method IN ('CREDIT_CARD', 'DEBIT_CARD') THEN total ELSE 0 END) AS card_sales,
       SUM(CASE WHEN payment_method = 'TRANSFER' THEN total ELSE 0 END) AS transfer_sales,
       SUM(CASE WHEN payment_method = 'QR' THEN total ELSE 0 END) AS qr_sales
FROM invoice
WHERE payment_status = 'PAID'
GROUP BY DATE(issue_date)
ORDER BY date DESC;

-- =====================================================================
-- TRIGGERS FOR CONSISTENCY
-- =====================================================================

DELIMITER //

-- Trigger: Update order total on detail insert/update/delete
CREATE TRIGGER trg_order_detail_after_insert
AFTER INSERT ON order_detail
FOR EACH ROW
BEGIN
    UPDATE order_restaurant
    SET total = (
        SELECT COALESCE(SUM(subtotal), 0)
        FROM order_detail
        WHERE id_order = NEW.id_order
    )
    WHERE id_order = NEW.id_order;
END//

CREATE TRIGGER trg_order_detail_after_update
AFTER UPDATE ON order_detail
FOR EACH ROW
BEGIN
    UPDATE order_restaurant
    SET total = (
        SELECT COALESCE(SUM(subtotal), 0)
        FROM order_detail
        WHERE id_order = NEW.id_order
    )
    WHERE id_order = NEW.id_order;
END//

CREATE TRIGGER trg_order_detail_after_delete
AFTER DELETE ON order_detail
FOR EACH ROW
BEGIN
    UPDATE order_restaurant
    SET total = (
        SELECT COALESCE(SUM(subtotal), 0)
        FROM order_detail
        WHERE id_order = OLD.id_order
    )
    WHERE id_order = OLD.id_order;
END//

-- Trigger: On invoice creation, change order status to INVOICED
CREATE TRIGGER trg_invoice_after_insert
AFTER INSERT ON invoice
FOR EACH ROW
BEGIN
    UPDATE order_restaurant
    SET status = 'INVOICED'
    WHERE id_order = NEW.id_order;
END//

-- Trigger: Update invoice payment_status based on payments applied
CREATE TRIGGER trg_invoice_payment_after_insert
AFTER INSERT ON invoice_payment
FOR EACH ROW
BEGIN
    DECLARE total_paid DECIMAL(10,2);
    DECLARE total_invoice DECIMAL(10,2);
    
    SELECT SUM(amount) INTO total_paid FROM invoice_payment WHERE id_invoice = NEW.id_invoice;
    SELECT total INTO total_invoice FROM invoice WHERE id_invoice = NEW.id_invoice;
    
    IF total_paid >= total_invoice THEN
        UPDATE invoice SET payment_status = 'PAID' WHERE id_invoice = NEW.id_invoice;
    ELSEIF total_paid > 0 THEN
        UPDATE invoice SET payment_status = 'PARTIAL' WHERE id_invoice = NEW.id_invoice;
    END IF;
END//

-- Trigger: On table release (invoiced and paid order), change table status to FREE
CREATE TRIGGER trg_order_after_update
AFTER UPDATE ON order_restaurant
FOR EACH ROW
BEGIN
    IF NEW.status = 'INVOICED' AND OLD.status != 'INVOICED' THEN
        -- Check if invoice is paid
        IF EXISTS (
            SELECT 1 FROM invoice i
            WHERE i.id_order = NEW.id_order AND i.payment_status = 'PAID'
        ) THEN
            UPDATE table_restaurant SET status = 'FREE' WHERE id_table = NEW.id_table;
        END IF;
    ELSEIF NEW.status = 'CANCELLED' AND OLD.status != 'CANCELLED' THEN
        UPDATE table_restaurant SET status = 'FREE' WHERE id_table = NEW.id_table;
    ELSEIF NEW.status IN ('PENDING', 'IN_PREPARATION', 'READY', 'DELIVERED') AND OLD.status = 'FREE' THEN
        UPDATE table_restaurant SET status = 'OCCUPIED' WHERE id_table = NEW.id_table;
    END IF;
END//

DELIMITER ;

-- =====================================================================
-- USEFUL STORED PROCEDURES
-- =====================================================================

DELIMITER //

-- SP: Create new order
CREATE PROCEDURE sp_create_order(
    IN p_id_table INT,
    IN p_id_customer INT,
    IN p_notes VARCHAR(500)
)
BEGIN
    DECLARE v_table_status VARCHAR(20);
    
    SELECT status INTO v_table_status FROM table_restaurant WHERE id_table = p_id_table;
    
    IF v_table_status != 'FREE' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Table is not available';
    END IF;
    
    INSERT INTO order_restaurant (id_table, id_customer, notes, status)
    VALUES (p_id_table, p_id_customer, p_notes, 'PENDING');
    
    UPDATE table_restaurant SET status = 'OCCUPIED' WHERE id_table = p_id_table;
    
    SELECT LAST_INSERT_ID() AS id_order;
END//

-- SP: Add dish to order
CREATE PROCEDURE sp_add_dish_to_order(
    IN p_id_order INT,
    IN p_id_dish INT,
    IN p_quantity INT
)
BEGIN
    DECLARE v_price DECIMAL(10,2);
    DECLARE v_order_status VARCHAR(20);
    DECLARE v_dish_available BOOLEAN;
    
    SELECT status INTO v_order_status FROM order_restaurant WHERE id_order = p_id_order;
    SELECT available, price INTO v_dish_available, v_price FROM dish WHERE id_dish = p_id_dish;
    
    IF v_order_status NOT IN ('PENDING', 'IN_PREPARATION') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot modify order in this state';
    END IF;
    
    IF NOT v_dish_available THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Dish is not available';
    END IF;
    
    INSERT INTO order_detail (id_order, id_dish, quantity, unit_price)
    VALUES (p_id_order, p_id_dish, p_quantity, v_price)
    ON DUPLICATE KEY UPDATE 
        quantity = quantity + p_quantity,
        unit_price = v_price;
    
    UPDATE order_restaurant SET status = 'IN_PREPARATION' 
    WHERE id_order = p_id_order AND status = 'PENDING';
END//

-- SP: Generate invoice for an order
CREATE PROCEDURE sp_generate_invoice(
    IN p_id_order INT,
    IN p_payment_method VARCHAR(20),
    IN p_invoice_number VARCHAR(20)
)
BEGIN
    DECLARE v_total DECIMAL(10,2);
    DECLARE v_taxes DECIMAL(10,2);
    DECLARE v_order_status VARCHAR(20);
    DECLARE v_invoice_exists INT;
    
    SELECT status, total INTO v_order_status, v_total FROM order_restaurant WHERE id_order = p_id_order;
    SELECT COUNT(*) INTO v_invoice_exists FROM invoice WHERE id_order = p_id_order;
    
    IF v_invoice_exists > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Order already has an invoice generated';
    END IF;
    
    IF v_order_status NOT IN ('DELIVERED', 'IN_PREPARATION', 'READY') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Order is not ready for invoicing';
    END IF;
    
    SET v_taxes = v_total * 0.19; -- VAT 19%
    
    INSERT INTO invoice (id_order, subtotal, taxes, total, payment_method, invoice_number, payment_status)
    VALUES (p_id_order, v_total, v_taxes, v_total + v_taxes, p_payment_method, p_invoice_number, 
            CASE WHEN p_payment_method = 'CASH' THEN 'PAID' ELSE 'PENDING' END);
    
    UPDATE order_restaurant SET status = 'INVOICED' WHERE id_order = p_id_order;
    
    SELECT LAST_INSERT_ID() AS id_invoice;
END//

-- SP: Register invoice payment
CREATE PROCEDURE sp_register_payment(
    IN p_id_invoice INT,
    IN p_amount DECIMAL(10,2),
    IN p_payment_method VARCHAR(20),
    IN p_reference VARCHAR(100)
)
BEGIN
    DECLARE v_current_status VARCHAR(20);
    DECLARE v_invoice_total DECIMAL(10,2);
    
    SELECT payment_status, total INTO v_current_status, v_invoice_total FROM invoice WHERE id_invoice = p_id_invoice;
    
    IF v_current_status = 'PAID' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invoice is already paid';
    END IF;
    
    INSERT INTO invoice_payment (id_invoice, amount, payment_method, reference)
    VALUES (p_id_invoice, p_amount, p_payment_method, p_reference);
END//

-- SP: Change table status
CREATE PROCEDURE sp_change_table_status(
    IN p_id_table INT,
    IN p_new_status VARCHAR(20)
)
BEGIN
    DECLARE v_current_status VARCHAR(20);
    
    SELECT status INTO v_current_status FROM table_restaurant WHERE id_table = p_id_table;
    
    IF v_current_status = 'OCCUPIED' AND p_new_status = 'FREE' THEN
        -- Verify no active orders
        IF EXISTS (
            SELECT 1 FROM order_restaurant 
            WHERE id_table = p_id_table AND status NOT IN ('INVOICED', 'CANCELLED')
        ) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Table has active orders';
        END IF;
    END IF;
    
    UPDATE table_restaurant SET status = p_new_status WHERE id_table = p_id_table;
END//

DELIMITER ;

-- =====================================================================
-- SEED DATA
-- =====================================================================

-- Menu categories
INSERT INTO dish_category (name, description, display_order) VALUES
('Appetizers', 'Starters to begin the meal', 1),
('Main Courses', 'Main dishes', 2),
('Desserts', 'Sweets and desserts', 3),
('Cold Drinks', 'Soft drinks, juices, waters', 4),
('Hot Drinks', 'Coffee, tea, chocolate', 5),
('Alcoholic Drinks', 'Beers, wines, cocktails', 6);

-- Menu dishes
INSERT INTO dish (id_category, name, description, price, prep_time_minutes) VALUES
-- Appetizers
(1, 'Tomato Soup', 'Homemade tomato soup with basil', 12000.00, 10),
(1, 'Caesar Salad', 'Lettuce, croutons, parmesan, caesar dressing', 15000.00, 8),
(1, 'Beef Empanadas', '3 Colombian-style empanadas with hogao', 10000.00, 12),
(1, 'Shrimp Ceviche', 'Fresh shrimp in lime with onion and cilantro', 18000.00, 15),

-- Main Courses
(2, 'Bandeja Paisa', 'Beans, rice, meat, chorizo, egg, plantain, arepa, avocado', 35000.00, 25),
(2, 'Grilled Chicken Breast', 'Chicken breast, rice, salad, salted potatoes', 22000.00, 20),
(2, 'Baked Salmon', 'Salmon with roasted vegetables and mashed potatoes', 38000.00, 30),
(2, 'Meat Lasagna', 'Homemade lasagna with meat, bechamel, gratinated cheese', 25000.00, 25),
(2, 'Chicken Rice', 'Rice with chicken, vegetables, hogao', 20000.00, 18),
(2, 'Artisan Burger', '200g beef, cheese, lettuce, tomato, fries', 24000.00, 15),

-- Desserts
(3, 'Tiramisu', 'Italian coffee and mascarpone dessert', 12000.00, 5),
(3, 'Caramel Flan', 'Homemade flan with liquid caramel', 8000.00, 3),
(3, 'Brownie with Ice Cream', 'Warm brownie with vanilla ice cream scoop', 14000.00, 8),
(3, 'Fruit Cup', 'Seasonal fruits with yogurt and honey', 10000.00, 5),

-- Cold Drinks
(4, 'Natural Juice', 'Fruit of the day juice (passion fruit, blackberry, lulo)', 6000.00, 3),
(4, 'Coconut Lemonade', 'Lemonade with coconut milk', 7000.00, 3),
(4, 'Soda', 'Coca Cola, Sprite, Fanta (350ml can)', 4000.00, 1),
(4, 'Mineral Water', '600ml bottle', 3000.00, 1),
(4, 'Milkshake', 'Vanilla, chocolate or strawberry', 9000.00, 5),

-- Hot Drinks
(5, 'American Coffee', 'Black Colombian coffee', 4000.00, 3),
(5, 'Coffee with Milk', 'Coffee with whole milk', 4500.00, 3),
(5, 'Hot Chocolate', 'Colombian chocolate with cheese', 6000.00, 5),
(5, 'Herbal Tea', 'Chamomile, mint, lemon', 4000.00, 3),

-- Alcoholic Drinks
(6, 'National Beer', 'Aguila, Club Colombia, Poker (330ml)', 5000.00, 2),
(6, 'Craft Beer', 'Local craft beer (330ml)', 8000.00, 2),
(6, 'Red Wine Glass', 'Reserve red wine (150ml)', 15000.00, 2),
(6, 'Mojito', 'Rum, mint, lime, sugar, soda', 18000.00, 5);

-- Restaurant tables
INSERT INTO table_restaurant (number, capacity, status, location) VALUES
(1, 2, 'FREE', 'Terrace'),
(2, 2, 'FREE', 'Terrace'),
(3, 4, 'FREE', 'Main Hall'),
(4, 4, 'FREE', 'Main Hall'),
(5, 4, 'FREE', 'Main Hall'),
(6, 6, 'FREE', 'Main Hall'),
(7, 6, 'FREE', 'Main Hall'),
(8, 8, 'FREE', 'VIP Room'),
(9, 8, 'FREE', 'VIP Room'),
(10, 10, 'FREE', 'VIP Room'),
(11, 4, 'MAINTENANCE', 'Main Hall'),
(12, 2, 'FREE', 'Bar');

-- Test customers
INSERT INTO customer (name, phone, email, address) VALUES
('Juan Perez Gonzalez', '3101234567', 'juan.perez@email.com', '45th St # 12-34, Bogota'),
('Maria Rodriguez Lopez', '3159876543', 'maria.rodriguez@email.com', '15th Ave # 89-20, Bogota'),
('Carlos Andres Gomez', '3205551234', 'carlos.gomez@email.com', '68th Ave # 45-67, Bogota'),
('Ana Lucia Martinez', '3124445566', 'ana.martinez@email.com', '100th St # 7-89, Bogota'),
('Pedro Sanchez', '3187778899', 'pedro.sanchez@email.com', '7th Ave # 150-25, Bogota'),
('Laura Fernandez', '3112223344', 'laura.fernandez@email.com', '80th St # 10-15, Bogota'),
('Diego Ramirez', '3163334455', 'diego.ramirez@email.com', 'Boyaca Ave # 65-40, Bogota'),
('Sofia Herrera', '3146667788', 'sofia.herrera@email.com', '127th St # 15-30, Bogota');

-- Test orders (various states)
-- Order 1: Table 3, Customer 1 - DELIVERED (ready for invoicing)
INSERT INTO order_restaurant (id_table, id_customer, order_datetime, status, notes, total) VALUES
(3, 1, '2026-10-19 12:30:00', 'DELIVERED', 'No onion in salad', 47000.00),
(4, 2, '2026-10-19 13:15:00', 'IN_PREPARATION', 'Well done chicken', 0.00),
(5, NULL, '2026-10-19 13:45:00', 'PENDING', 'Take away', 0.00),
(6, 3, '2026-10-19 14:00:00', 'READY', 'No spicy', 0.00),
(8, 4, '2026-10-19 19:30:00', 'PENDING', 'Birthday celebration', 0.00),
(3, 5, '2026-10-18 20:00:00', 'INVOICED', '', 89000.00),
(4, 6, '2026-10-18 21:30:00', 'CANCELLED', 'Customer left', 0.00);

-- Order details
-- Order 1 (id=1): Appetizer + Main + Drink
INSERT INTO order_detail (id_order, id_dish, quantity, unit_price) VALUES
(1, 2, 1, 15000.00),  -- Caesar Salad
(1, 5, 1, 35000.00),  -- Bandeja Paisa
(1, 17, 1, 6000.00);  -- Natural Juice

-- Order 2 (id=2): Main + Dessert
INSERT INTO order_detail (id_order, id_dish, quantity, unit_price) VALUES
(2, 6, 1, 22000.00),  -- Grilled Chicken Breast
(2, 12, 1, 12000.00); -- Tiramisu

-- Order 3 (id=3): 2 Burgers + 2 Milkshakes
INSERT INTO order_detail (id_order, id_dish, quantity, unit_price) VALUES
(3, 10, 2, 24000.00), -- Artisan Burger
(3, 21, 2, 9000.00);  -- Milkshake

-- Order 4 (id=4): Salmon + Wine Glass
INSERT INTO order_detail (id_order, id_dish, quantity, unit_price) VALUES
(4, 7, 1, 38000.00),  -- Baked Salmon
(4, 27, 1, 15000.00); -- Red Wine Glass

-- Order 5 (id=5): Group - 2 Bandejas + 2 Juices + 2 Flans
INSERT INTO order_detail (id_order, id_dish, quantity, unit_price) VALUES
(5, 5, 2, 35000.00),  -- Bandeja Paisa x2
(5, 17, 2, 6000.00),  -- Natural Juice x2
(5, 13, 2, 8000.00);  -- Caramel Flan x2

-- Order 6 (id=6): Already invoiced - Lasagna + Coffee + Tiramisu
INSERT INTO order_detail (id_order, id_dish, quantity, unit_price) VALUES
(6, 8, 1, 25000.00),  -- Lasagna
(6, 22, 1, 4500.00),  -- Coffee with Milk
(6, 12, 1, 12000.00); -- Tiramisu

-- Order 7 (id=7): Cancelled - no details

-- Invoices
-- Invoice for order 6 (already paid in cash)
INSERT INTO invoice (id_order, issue_date, subtotal, taxes, total, payment_method, payment_status, invoice_number) VALUES
(6, '2026-10-18 21:00:00', 41500.00, 7885.00, 49385.00, 'CASH', 'PAID', 'INV-20261018-001');

-- Invoice for order 1 (pending payment)
INSERT INTO invoice (id_order, issue_date, subtotal, taxes, total, payment_method, payment_status, invoice_number) VALUES
(1, '2026-10-19 13:45:00', 47000.00, 8930.00, 55930.00, 'DEBIT_CARD', 'PENDING', 'INV-20261019-001');

-- Invoice payments (for invoice 2 - partial payment with card)
INSERT INTO invoice_payment (id_invoice, amount, payment_method, reference) VALUES
(2, 30000.00, 'DEBIT_CARD', 'AUTH-123456'),
(2, 25930.00, 'CASH', 'Remaining cash payment');

-- Update order totals (triggers would do this automatically, but forced for seed)
UPDATE order_restaurant SET total = (
    SELECT COALESCE(SUM(subtotal), 0) FROM order_detail WHERE id_order = order_restaurant.id_order
) WHERE id_order IN (1,2,3,4,5,6);

-- =====================================================================
-- VERIFICATION QUERIES
-- =====================================================================

-- Verify structure
-- SHOW TABLES;
-- DESCRIBE table_restaurant;
-- DESCRIBE customer;
-- DESCRIBE dish;
-- DESCRIBE order_restaurant;
-- DESCRIBE order_detail;
-- DESCRIBE invoice;

-- Verify data
-- SELECT * FROM v_active_menu LIMIT 5;
-- SELECT * FROM v_available_tables;
-- SELECT * FROM v_kitchen_orders;
-- SELECT * FROM v_pending_invoices;
-- SELECT * FROM v_daily_sales;

-- =====================================================================
-- END OF SCRIPT
-- =====================================================================