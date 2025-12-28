-- V13: Insert additional sample carts and cart_items for testing

-- Create carts for users 4 and 5
INSERT INTO carts (user_id, status) VALUES
(4, 'ACTIVE'),
(5, 'ACTIVE');

-- Add items to user 4's cart (cart_id 4)
INSERT INTO cart_items (cart_id, product_id, quantity) VALUES
(4, 7, 2),   -- 2x Clean Code (product_id 7)
(4, 12, 1);  -- 1x Levi's 501 Jeans (product_id 12)

-- Add items to user 5's cart (cart_id 5)
INSERT INTO cart_items (cart_id, product_id, quantity) VALUES
(5, 15, 3),  -- 3x Ray-Ban Sunglasses (product_id 15)
(5, 21, 1);  -- 1x Le Creuset Dutch Oven (product_id 21)

