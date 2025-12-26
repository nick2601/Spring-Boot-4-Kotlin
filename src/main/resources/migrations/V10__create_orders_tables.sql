-- V10: Create orders and order_items tables for e-commerce functionality

-- =====================================================
-- CREATE ORDERS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    tax DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    shipping_cost DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    payment_id VARCHAR(255),
    payment_method VARCHAR(50),
    shipping_address TEXT,
    billing_address TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_order_user (user_id),
    INDEX idx_order_number (order_number),
    INDEX idx_order_status (status),
    INDEX idx_order_created (created_at)
);

-- =====================================================
-- CREATE ORDER_ITEMS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_description TEXT,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    subtotal DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    INDEX idx_order_item_order (order_id),
    INDEX idx_order_item_product (product_id)
);

-- =====================================================
-- INSERT SAMPLE ORDERS FOR TESTING
-- =====================================================

-- Sample order for Emily Johnson (user_id = 2)
INSERT INTO orders (user_id, order_number, status, subtotal, tax, shipping_cost, total_amount, payment_id, payment_method, shipping_address, created_at) VALUES
(2, 'ORD-1735200001-1234', 'DELIVERED', 149.97, 14.99, 0.00, 164.96, 'pi_test_123456', 'STRIPE', '789 Oak Drive, Los Angeles, CA 90001', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(2, 'ORD-1735200002-5678', 'SHIPPED', 89.99, 9.00, 5.99, 104.98, 'pi_test_234567', 'STRIPE', '789 Oak Drive, Los Angeles, CA 90001', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(2, 'ORD-1735200003-9012', 'PAID', 299.99, 30.00, 0.00, 329.99, 'pi_test_345678', 'STRIPE', '789 Oak Drive, Los Angeles, CA 90001', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Sample order for Michael Davis (user_id = 3)
INSERT INTO orders (user_id, order_number, status, subtotal, tax, shipping_cost, total_amount, payment_id, payment_method, shipping_address, created_at) VALUES
(3, 'ORD-1735200004-3456', 'DELIVERED', 199.98, 20.00, 0.00, 219.98, 'pi_test_456789', 'STRIPE', '321 Elm Street, Chicago, IL 60601', DATE_SUB(NOW(), INTERVAL 45 DAY)),
(3, 'ORD-1735200005-7890', 'CANCELLED', 49.99, 5.00, 5.99, 60.98, NULL, 'STRIPE', '321 Elm Street, Chicago, IL 60601', DATE_SUB(NOW(), INTERVAL 20 DAY));

-- =====================================================
-- INSERT SAMPLE ORDER ITEMS
-- =====================================================

-- Items for Emily's first order (order_id = 1)
INSERT INTO order_items (order_id, product_id, product_name, product_description, unit_price, quantity, subtotal) VALUES
(1, 1, 'iPhone 15 Pro', 'Latest Apple smartphone with A17 chip', 49.99, 3, 149.97);

-- Items for Emily's second order (order_id = 2)
INSERT INTO order_items (order_id, product_id, product_name, product_description, unit_price, quantity, subtotal) VALUES
(2, 2, 'Samsung Galaxy S24', 'Flagship Android smartphone', 89.99, 1, 89.99);

-- Items for Emily's third order (order_id = 3)
INSERT INTO order_items (order_id, product_id, product_name, product_description, unit_price, quantity, subtotal) VALUES
(3, 3, 'MacBook Pro 14"', 'Apple M3 Pro chip laptop', 299.99, 1, 299.99);

-- Items for Michael's first order (order_id = 4)
INSERT INTO order_items (order_id, product_id, product_name, product_description, unit_price, quantity, subtotal) VALUES
(4, 1, 'iPhone 15 Pro', 'Latest Apple smartphone', 99.99, 2, 199.98);

-- Items for Michael's cancelled order (order_id = 5)
INSERT INTO order_items (order_id, product_id, product_name, product_description, unit_price, quantity, subtotal) VALUES
(5, 4, 'Clean Code', 'A Handbook of Agile Software Craftsmanship', 49.99, 1, 49.99);

