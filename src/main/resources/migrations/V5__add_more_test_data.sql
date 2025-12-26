-- V5: Add more comprehensive test data for all tables

-- =====================================================
-- ADD MORE CATEGORIES
-- =====================================================
INSERT INTO categories (name) VALUES
('Toys & Games'),
('Beauty & Personal Care'),
('Automotive'),
('Garden & Outdoor'),
('Pet Supplies');

-- =====================================================
-- ADD MORE PRODUCTS (for new and existing categories)
-- =====================================================
INSERT INTO products (name, price, description, category_id) VALUES
-- More Electronics (category 1)
('MacBook Pro 16"', 2499.99, 'Apple M3 Pro chip, 18GB RAM, 512GB SSD, Space Black', 1),
('Samsung Galaxy S24 Ultra', 1199.99, 'Flagship Android smartphone with S Pen and AI features', 1),
('Nintendo Switch OLED', 349.99, 'Portable gaming console with vibrant OLED screen', 1),
('Bose QuietComfort Ultra', 429.99, 'Premium noise-cancelling headphones with spatial audio', 1),
('LG 27" 4K Monitor', 449.99, 'Professional-grade monitor for creators and gamers', 1),

-- More Books (category 2)
('Domain-Driven Design', 54.99, 'Tackling complexity in the heart of software by Eric Evans', 2),
('Effective Kotlin', 44.99, 'Best practices for Kotlin development', 2),
('System Design Interview', 39.99, 'An insider guide to system design interviews', 2),
('Microservices Patterns', 49.99, 'With examples in Java by Chris Richardson', 2),
('Head First Design Patterns', 59.99, 'Brain-friendly guide to design patterns', 2),

-- More Clothing (category 3)
('Patagonia Fleece Jacket', 149.99, 'Recycled polyester fleece for outdoor adventures', 3),
('Nike Dri-FIT Shorts', 35.99, 'Moisture-wicking athletic shorts', 3),
('Columbia Hiking Boots', 129.99, 'Waterproof trail boots with excellent grip', 3),
('Under Armour Hoodie', 65.99, 'Lightweight performance hoodie', 3),
('Timberland Casual Shoes', 119.99, 'Classic leather casual shoes', 3),

-- More Home & Kitchen (category 4)
('Ninja Air Fryer XL', 129.99, '5.5-quart air fryer with multiple cooking functions', 4),
('Vitamix Professional Blender', 549.99, 'High-performance blender for smoothies and soups', 4),
('Roomba i7+', 799.99, 'Self-emptying robot vacuum with smart mapping', 4),
('All-Clad Stainless Cookware Set', 699.99, '10-piece professional cookware set', 4),
('Philips Hue Starter Kit', 179.99, 'Smart LED lighting system with hub', 4),

-- More Sports & Outdoors (category 5)
('Peloton Bike+', 2495.00, 'Interactive exercise bike with rotating screen', 5),
('REI Co-op Tent 6-Person', 399.99, 'Spacious family camping tent with vestibule', 5),
('Garmin Fenix 7', 699.99, 'Multisport GPS smartwatch for athletes', 5),
('TRX Suspension Trainer', 179.99, 'Professional suspension training system', 5),
('Yeti Cooler 45', 325.00, 'Heavy-duty rotomolded cooler for adventures', 5),

-- Toys & Games (category 6)
('LEGO Star Wars Millennium Falcon', 169.99, 'Iconic starship building set with 1351 pieces', 6),
('PlayStation 5 Console', 499.99, 'Next-gen gaming console with ultra-fast SSD', 6),
('Monopoly Classic Edition', 24.99, 'Classic family board game', 6),
('Nintendo Switch Pro Controller', 69.99, 'Premium wireless gaming controller', 6),
('Magic: The Gathering Starter Kit', 29.99, 'Trading card game starter set for two players', 6),

-- Beauty & Personal Care (category 7)
('Dyson Airwrap Complete', 599.99, 'Multi-styler with attachments for all hair types', 7),
('La Mer Moisturizing Cream', 190.00, 'Luxury skincare moisturizer', 7),
('Philips Sonicare DiamondClean', 199.99, 'Smart electric toothbrush', 7),
('Olaplex Hair Repair Set', 84.99, 'Professional bond-building hair treatment', 7),
('Theragun Pro', 599.00, 'Deep tissue massage gun for recovery', 7),

-- Automotive (category 8)
('Michelin All-Season Tires (Set of 4)', 599.99, 'Premier all-season tires for sedans', 8),
('NOCO Jump Starter', 149.99, 'Portable lithium car battery jump starter', 8),
('Thule Roof Cargo Box', 549.99, 'Aerodynamic rooftop cargo carrier', 8),
('Meguiars Complete Car Care Kit', 79.99, 'Professional detailing kit', 8),
('Garmin DashCam 67W', 229.99, 'Wide-angle 1440p dash camera', 8),

-- Garden & Outdoor (category 9)
('Weber Spirit II Gas Grill', 449.99, '3-burner propane grill with side tables', 9),
('DeWalt Cordless Lawn Mower', 399.99, '20V MAX brushless push mower', 9),
('Adirondack Chair Set', 299.99, 'Weather-resistant outdoor seating pair', 9),
('Ring Floodlight Cam', 249.99, 'Smart security camera with motion-activated lights', 9),
('Greenworks Electric Pressure Washer', 199.99, '2000 PSI electric pressure washer', 9),

-- Pet Supplies (category 10)
('Whisker Litter-Robot 4', 699.99, 'Automatic self-cleaning litter box', 10),
('Kong Classic Dog Toy XL', 16.99, 'Durable rubber toy for large dogs', 10),
('PetSafe Automatic Feeder', 89.99, 'Programmable pet food dispenser', 10),
('Furminator Deshedding Tool', 34.99, 'Professional grooming brush for dogs', 10),
('Seresto Flea Collar for Dogs', 59.99, '8-month protection flea and tick collar', 10);

-- =====================================================
-- ADD MORE USERS
-- =====================================================
INSERT INTO users (name, email, password) VALUES
-- Tech professionals
('Alex Thompson', 'alex.thompson@techcorp.com', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe'),
('Sophia Chen', 'sophia.chen@innovate.io', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe'),
('Ryan O''Brien', 'ryan.obrien@developer.net', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe'),
('Priya Patel', 'priya.patel@cloud.tech', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe'),

-- Creative professionals
('Emma Rodriguez', 'emma.rodriguez@design.studio', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe'),
('Lucas Kim', 'lucas.kim@creative.agency', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe'),
('Olivia Foster', 'olivia.foster@media.co', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe'),

-- Healthcare professionals
('Dr. William Chang', 'william.chang@healthcare.org', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe'),
('Dr. Rachel Green', 'rachel.green@medical.center', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe'),

-- Students
('Tyler Johnson', 'tyler.johnson@university.edu', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe'),
('Mia Williams', 'mia.williams@college.edu', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe'),
('Ethan Brown', 'ethan.brown@student.edu', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe'),

-- Retirees
('George Miller', 'george.miller@retired.com', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe'),
('Patricia Moore', 'patricia.moore@senior.net', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe'),

-- International
('Yuki Tanaka', 'yuki.tanaka@japan.co.jp', '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe');

-- =====================================================
-- ADD ADDRESSES FOR NEW USERS (users 16-30)
-- =====================================================
INSERT INTO addresses (street, city, state, zip, user_id) VALUES
-- Alex Thompson (User 16)
('100 Silicon Valley Blvd', 'San Jose', 'CA', '95110', 16),

-- Sophia Chen (User 17) - 2 addresses
('200 Innovation Way', 'Seattle', 'WA', '98101', 17),
('201 Lake Union Dr', 'Seattle', 'WA', '98102', 17),

-- Ryan O'Brien (User 18)
('300 Developer Lane', 'Boston', 'MA', '02101', 18),

-- Priya Patel (User 19)
('400 Cloud Street', 'Denver', 'CO', '80201', 19),

-- Emma Rodriguez (User 20)
('500 Art District Ave', 'Miami', 'FL', '33101', 20),

-- Lucas Kim (User 21)
('600 Creative Blvd', 'Portland', 'OR', '97201', 21),

-- Olivia Foster (User 22) - 2 addresses
('700 Media Row', 'Nashville', 'TN', '37201', 22),
('701 Music City Dr', 'Nashville', 'TN', '37202', 22),

-- Dr. William Chang (User 23)
('800 Medical Center Dr', 'Baltimore', 'MD', '21201', 23),

-- Dr. Rachel Green (User 24)
('900 Healthcare Blvd', 'Minneapolis', 'MN', '55401', 24),

-- Tyler Johnson (User 25)
('1000 University Ave', 'Ann Arbor', 'MI', '48104', 25),

-- Mia Williams (User 26)
('1100 College Street', 'Madison', 'WI', '53703', 26),

-- Ethan Brown (User 27)
('1200 Student Housing Rd', 'Berkeley', 'CA', '94704', 27),

-- George Miller (User 28)
('1300 Retirement Village Ln', 'Scottsdale', 'AZ', '85251', 28),

-- Patricia Moore (User 29)
('1400 Senior Living Ct', 'Sarasota', 'FL', '34230', 29),

-- Yuki Tanaka (User 30)
('1500 International Plaza', 'Honolulu', 'HI', '96801', 30);

-- =====================================================
-- ADD PROFILES FOR NEW USERS (users 16-30)
-- =====================================================
INSERT INTO profiles (id, bio, phone_number, date_of_birth, loyalty_points) VALUES
(16, 'Senior software engineer at a Fortune 500 tech company. Specializing in distributed systems and cloud architecture.', '555-0116', '1987-04-20', 3200),
(17, 'AI/ML engineer passionate about building intelligent systems. Stanford PhD candidate.', '555-0117', '1992-08-15', 2800),
(18, 'Full-stack developer and open source maintainer. Creator of popular NPM packages.', '555-0118', '1989-12-03', 4100),
(19, 'Cloud solutions architect helping enterprises modernize their infrastructure.', '555-0119', '1991-06-28', 2400),
(20, 'UI/UX designer creating beautiful and accessible digital experiences. Adobe certified.', '555-0120', '1994-02-14', 1650),
(21, 'Creative director at a leading advertising agency. Award-winning campaign strategist.', '555-0121', '1986-10-09', 2900),
(22, 'Digital content creator and podcast host. Building authentic brand partnerships.', '555-0122', '1993-07-22', 3500),
(23, 'Cardiologist with 20 years of experience. Researcher in preventive medicine.', '555-0123', '1975-03-18', 5200),
(24, 'Pediatrician dedicated to children health and wellness. Community health advocate.', '555-0124', '1982-11-30', 4800),
(25, 'Computer Science major focusing on cybersecurity. CTF competition enthusiast.', '555-0125', '2002-05-12', 450),
(26, 'Business administration student with a minor in data analytics. Future entrepreneur.', '555-0126', '2003-09-08', 320),
(27, 'Engineering student passionate about robotics and autonomous systems.', '555-0127', '2001-01-25', 580),
(28, 'Retired military veteran enjoying travel and woodworking. Volunteer mentor.', '555-0128', '1958-07-04', 6500),
(29, 'Former school principal now enjoying gardening and book clubs. Grandma of 6.', '555-0129', '1955-12-19', 7200),
(30, 'International business consultant bridging cultures between US and Japan.', '555-0130', '1988-04-01', 3800);

-- =====================================================
-- ADD WISHLIST ITEMS (users favoriting products)
-- =====================================================
INSERT INTO wishlist (user_id, product_id) VALUES
-- John Smith likes electronics
(1, 1), (1, 2), (1, 3),
-- Emily Johnson likes fitness products
(2, 21), (2, 22), (2, 23),
-- Michael Davis likes tech books
(3, 6), (3, 7), (3, 8),
-- Sarah Williams has varied interests
(4, 11), (4, 16), (4, 25),
-- David Brown likes home items
(5, 16), (5, 17), (5, 18),
-- Jennifer Martinez - business professional
(6, 1), (6, 6), (6, 7),
-- Robert Taylor - enterprise user
(7, 1), (7, 4), (7, 5),
-- Lisa Anderson - e-commerce
(8, 16), (8, 19), (8, 20),
-- New users with wishlists
(16, 1), (16, 26), (16, 31),
(17, 2), (17, 27), (17, 32),
(18, 6), (18, 7), (18, 33),
(20, 11), (20, 12), (20, 56),
(25, 41), (25, 42), (25, 46),
(28, 61), (28, 66), (28, 71);

