-- V6: Create cart and cart_items tables for shopping cart functionality

-- =====================================================
-- CART TABLE
-- Each user can have one active cart
-- Status: ACTIVE, CHECKOUT, COMPLETED, ABANDONED
-- =====================================================
CREATE TABLE carts
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    user_id    BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    status     VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT uk_cart_user UNIQUE (user_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_cart_status CHECK (status IN ('ACTIVE', 'CHECKOUT', 'COMPLETED', 'ABANDONED'))
);

CREATE INDEX idx_cart_user ON carts (user_id);
CREATE INDEX idx_cart_status ON carts (status);

-- =====================================================
-- CART ITEMS TABLE
-- Each cart can have multiple items with quantity
-- =====================================================
CREATE TABLE cart_items
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    cart_id    BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT UNSIGNED DEFAULT 1 NOT NULL,
    added_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT uk_cart_product UNIQUE (cart_id, product_id),
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

CREATE INDEX idx_cart_item_cart ON cart_items (cart_id);
CREATE INDEX idx_cart_item_product ON cart_items (product_id);

-- =====================================================
-- INSERT SAMPLE CART DATA
-- =====================================================

-- Create carts for existing users
INSERT INTO carts (user_id, status) VALUES
(1, 'ACTIVE'),
(2, 'ACTIVE'),
(3, 'ACTIVE');

-- Add items to carts
-- User 1's cart
INSERT INTO cart_items (cart_id, product_id, quantity) VALUES
(1, 1, 2),   -- 2x first product
(1, 3, 1),  -- 1x third product
(1, 5, 3);  -- 3x fifth product

-- User 2's cart
INSERT INTO cart_items (cart_id, product_id, quantity) VALUES
(2, 2, 1),  -- 1x second product
(2, 4, 2); -- 2x fourth product

-- User 3's cart
INSERT INTO cart_items (cart_id, product_id, quantity) VALUES
(3, 1, 1),  -- 1x first product
(3, 2, 1),  -- 1x second product
(3, 6, 4); -- 4x sixth product

