-- Insert sample categories
INSERT INTO categories (name) VALUES
('Electronics'),
('Books'),
('Clothing'),
('Home & Kitchen'),
('Sports & Outdoors');

-- Insert sample products
INSERT INTO products (name, price, description, category_id) VALUES
-- Electronics
('Laptop Dell XPS 15', 1499.99, 'High-performance laptop with Intel i7, 16GB RAM, 512GB SSD', 1),
('iPhone 15 Pro', 999.99, 'Latest Apple smartphone with A17 Pro chip and advanced camera system', 1),
('Sony WH-1000XM5 Headphones', 399.99, 'Premium noise-cancelling wireless headphones', 1),
('Samsung 55" 4K TV', 799.99, 'Ultra HD Smart TV with HDR support', 1),
('Apple AirPods Pro', 249.99, 'Wireless earbuds with active noise cancellation', 1),

-- Books
('The Pragmatic Programmer', 44.99, 'Essential guide for software developers', 2),
('Clean Code', 39.99, 'A handbook of agile software craftsmanship', 2),
('Kotlin in Action', 49.99, 'Comprehensive guide to Kotlin programming', 2),
('Spring Boot in Action', 54.99, 'Practical guide to Spring Boot development', 2),
('Design Patterns', 59.99, 'Elements of reusable object-oriented software', 2),

-- Clothing
('Nike Air Max Sneakers', 129.99, 'Comfortable running shoes with air cushioning', 3),
('Levi''s 501 Jeans', 69.99, 'Classic straight-fit denim jeans', 3),
('North Face Jacket', 189.99, 'Waterproof winter jacket with insulation', 3),
('Adidas T-Shirt', 29.99, 'Cotton athletic t-shirt', 3),
('Ray-Ban Sunglasses', 149.99, 'Classic aviator sunglasses with UV protection', 3),

-- Home & Kitchen
('KitchenAid Stand Mixer', 349.99, 'Professional-grade stand mixer for baking', 4),
('Dyson V15 Vacuum', 649.99, 'Cordless stick vacuum with laser detection', 4),
('Instant Pot Duo', 99.99, 'Multi-functional pressure cooker', 4),
('Cuisinart Coffee Maker', 79.99, 'Programmable 12-cup coffee maker', 4),
('Le Creuset Dutch Oven', 299.99, 'Cast iron enameled dutch oven for cooking', 4),

-- Sports & Outdoors
('Yoga Mat Pro', 49.99, 'Non-slip exercise mat with extra cushioning', 5),
('Camping Tent 4-Person', 199.99, 'Waterproof tent for outdoor adventures', 5),
('Mountain Bike', 899.99, '21-speed mountain bike with suspension', 5),
('Hiking Backpack', 89.99, '40L backpack with hydration system', 5),
('Dumbbell Set', 149.99, 'Adjustable dumbbells 5-50 lbs', 5);

