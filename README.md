# 🛒 Spring Boot E-Commerce REST API

A production-ready e-commerce REST API built with **Spring Boot 4.x**, **Kotlin**, and **Clean Architecture** principles. Features JWT authentication, Stripe payments, Apache Kafka event streaming, and comprehensive Swagger documentation.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Flow for New Developers](#-project-flow-for-new-developers)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [API Endpoints](#-api-endpoints)
- [Database Schema](#-database-schema)
- [Configuration](#-configuration)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)

---

## ✨ Features

### Core Features
- ✅ **User Management** - Registration, authentication, profile management
- ✅ **Product Catalog** - CRUD operations with category filtering
- ✅ **Shopping Cart** - Add, update, remove items with real-time totals
- ✅ **JWT Authentication** - Secure stateless authentication with token refresh
- ✅ **Role-Based Access Control** - Protected endpoints with authorization

### Payment Integration
- 💳 **Stripe Checkout** - Seamless payment processing
- 💳 **Payment Intents** - Custom payment flows
- 💳 **Webhooks** - Real-time payment event handling

### Event-Driven Architecture
- 📨 **Apache Kafka** - Async event streaming (optional)
- 📨 **User Events** - Login, logout, registration tracking
- 📨 **Order Events** - Cart and payment lifecycle events
- 📨 **Notification Events** - Email, SMS, push notifications

### Developer Experience
- 📖 **Swagger UI** - Interactive API documentation
- 🔄 **Flyway Migrations** - Database version control
- ✅ **Jakarta Validation** - Request validation
- 🐛 **Global Exception Handling** - Consistent error responses

---

## 🛠 Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin 2.1.0 |
| **Framework** | Spring Boot 4.0.1 |
| **Database** | MariaDB / MySQL |
| **ORM** | Spring Data JPA / Hibernate |
| **Migrations** | Flyway |
| **Security** | Spring Security 7.x + JWT |
| **Payments** | Stripe API |
| **Messaging** | Apache Kafka (optional) |
| **Documentation** | SpringDoc OpenAPI 2.8.4 |
| **Build Tool** | Gradle (Kotlin DSL) |
| **Java Version** | JDK 21+ |

---

## 🏗 Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

```
src/main/kotlin/com/example/nikhil/
├── application/                    # Use Cases / Business Logic
│   └── service/
│       ├── AuthService.kt          # Authentication logic
│       ├── UserService.kt          # User management
│       ├── ProductService.kt       # Product operations
│       ├── CartService.kt          # Shopping cart logic
│       └── StripeService.kt        # Payment processing
│
├── infrastructure/                 # External Concerns
│   ├── config/                     # Configuration classes
│   │   ├── JacksonConfig.kt
│   │   ├── KafkaConfig.kt
│   │   ├── OpenApiConfig.kt
│   │   └── StripeConfig.kt
│   │
│   ├── kafka/                      # Event streaming
│   │   ├── consumer/
│   │   ├── producer/
│   │   └── event/
│   │
│   ├── mapper/                     # Entity <-> DTO mappers
│   │   ├── CartMapper.kt
│   │   ├── ProductMapper.kt
│   │   └── UserMapper.kt
│   │
│   ├── persistence/                # Database layer
│   │   ├── entity/                 # JPA Entities
│   │   └── repository/             # Spring Data Repositories
│   │
│   ├── security/                   # Security configuration
│   │   ├── JwtAuthenticationFilter.kt
│   │   ├── JwtTokenUtil.kt
│   │   └── SecurityConfig.kt
│   │
│   └── web/                        # Web layer
│       ├── controller/             # REST Controllers
│       ├── dto/                    # Data Transfer Objects
│       └── GlobalExceptionHandler.kt
│
└── LearningSpringRestApiApplication.kt
```

---

## 📚 Project Flow for New Developers

This section explains how a request flows through the application, helping you understand the complete lifecycle from HTTP request to database and back.

### 🎯 Request Flow Overview

```
HTTP Request → Security Filter → Controller → Service → Repository → Database
     ↓              ↓               ↓           ↓           ↓            ↓
Response ← DTO Mapper ← Exception Handler ← Business Logic ← Entity ← Query
                              ↓
                      Kafka Event (Optional)
```

---

### 🔐 1. Authentication Flow (Login Example)

**Step-by-Step Breakdown:**

#### **Step 1: Client sends login request**
```bash
POST /auth/login
Content-Type: application/json
{
  "email": "emily.johnson@email.com",
  "password": "password123"
}
```

#### **Step 2: Request hits Security Configuration**
- **File**: `infrastructure/security/SecurityConfig.kt`
- `/auth/login` is in `PUBLIC_ENDPOINTS` array, so no JWT required
- Request bypasses JWT authentication filter

#### **Step 3: Controller receives request**
- **File**: `infrastructure/web/controller/AuthController.kt`
- `@PostMapping("/login")` method catches the request
- `@Valid` annotation triggers validation on `AuthRequest` DTO
- If validation fails → `GlobalExceptionHandler` catches it
- If validation passes → calls `authService.login()`

#### **Step 4: Service handles business logic**
- **File**: `application/service/AuthService.kt`
- **Actions performed:**
  1. Query `UserRepository` to find user by email
  2. If user not found → throw `InvalidCredentialsException`
  3. Verify password using `BCryptPasswordEncoder`
  4. If password wrong → throw `InvalidCredentialsException`
  5. Generate JWT token using `JwtTokenUtil`
  6. Publish `UserEvent` to Kafka (login action)
  7. Return `AuthResponse` with token

#### **Step 5: Response mapped and returned**
- **File**: `infrastructure/web/dto/AuthResponse.kt`
- Service returns DTO to controller
- Jackson automatically serializes to JSON
- HTTP 200 OK with JWT token returned to client

**Exception Handling:**
- If `InvalidCredentialsException` thrown → `GlobalExceptionHandler` catches it
- Returns HTTP 401 with error message

---

### 🛒 2. Shopping Cart Flow (Add to Cart Example)

**Step-by-Step Breakdown:**

#### **Step 1: Client sends add to cart request**
```bash
POST /carts/1/items
Content-Type: application/json
{
  "productId": 5,
  "quantity": 2
}
```

#### **Step 2: Security Filter validates request**
- **File**: `infrastructure/security/JwtAuthenticationFilter.kt`
- Extracts JWT token from `Authorization: Bearer <token>` header
- Validates token using `JwtTokenUtil`
- Loads user details from `CustomUserDetailsService`
- Sets authentication in `SecurityContext`
- If token invalid → HTTP 401 Unauthorized

#### **Step 3: Controller processes request**
- **File**: `infrastructure/web/controller/CartController.kt`
- `@PostMapping("/{cartId}/items")` catches request
- Path variable `{cartId}` extracted (e.g., 1)
- `@Valid @RequestBody` validates `AddToCartRequest`
- Calls `cartService.addItemToCart(cartId, request)`

#### **Step 4: Service executes business logic**
- **File**: `application/service/CartService.kt`
- **Actions performed:**
  1. Find cart by ID from `CartRepository`
  2. Verify cart exists and status is ACTIVE
  3. Find product by ID from `ProductRepository`
  4. Check if product already in cart
  5. If exists → update quantity
  6. If new → create `CartItem` entity
  7. Recalculate cart totals
  8. Save to database via `CartItemRepository`
  9. Publish `OrderEvent` to Kafka (item added)
  10. Use `CartMapper` to convert entity to DTO

#### **Step 5: Entity-DTO mapping**
- **File**: `infrastructure/mapper/CartMapper.kt`
- Converts `Cart` entity (with JPA relationships) to `CartDto`
- Calculates total items and total price
- Maps nested `CartItem` entities to `CartItemDto`
- Returns clean DTO without JPA proxies

#### **Step 6: Response returned**
- HTTP 200 OK with complete cart details in JSON
- Client receives updated cart with new item

**Database Interactions:**
```sql
-- 1. Find cart
SELECT * FROM carts WHERE id = 1;

-- 2. Find product
SELECT * FROM products WHERE id = 5;

-- 3. Check existing cart item
SELECT * FROM cart_items WHERE cart_id = 1 AND product_id = 5;

-- 4. Insert or update cart item
INSERT INTO cart_items (cart_id, product_id, quantity, added_at)
VALUES (1, 5, 2, NOW());
```

---

### 💳 3. Payment Flow (Stripe Checkout)

**Step-by-Step Breakdown:**

#### **Step 1: Client initiates checkout**
```bash
POST /payments/checkout
Content-Type: application/json
{
  "cartId": 1,
  "successUrl": "http://localhost:3000/success",
  "cancelUrl": "http://localhost:3000/cancel"
}
```

#### **Step 2: Controller receives request**
- **File**: `infrastructure/web/controller/StripeController.kt`
- Validates checkout request
- Calls `stripeService.createCheckoutSession()`

#### **Step 3: Service creates Stripe session**
- **File**: `application/service/StripeService.kt`
- **Actions performed:**
  1. Fetch cart details from `CartService`
  2. Validate cart has items and is ACTIVE
  3. Build line items from cart items
  4. Call Stripe API to create checkout session
  5. Return session ID and checkout URL

#### **Step 4: Client redirects to Stripe**
- Frontend redirects user to Stripe hosted checkout
- User completes payment on Stripe's secure page

#### **Step 5: Stripe webhook callback**
- **Endpoint**: `POST /payments/webhook`
- **File**: `infrastructure/web/controller/StripeController.kt`
- Stripe sends webhook event when payment succeeds
- Webhook signature verified for security
- If `checkout.session.completed` event:
  1. Update cart status to COMPLETED
  2. Publish `OrderEvent` to Kafka
  3. Trigger notification events

---

### 🔄 4. Data Flow Layers Explained

#### **Layer 1: Web Layer (Entry Point)**
**Location**: `infrastructure/web/`

**Components:**
- **Controllers**: Handle HTTP requests/responses
  - `AuthController.kt` - Authentication endpoints
  - `UserController.kt` - User management
  - `ProductsController.kt` - Product catalog
  - `CartController.kt` - Shopping cart
  - `StripeController.kt` - Payment processing

- **DTOs**: Data Transfer Objects for API contracts
  - `AuthRequest.kt`, `AuthResponse.kt`
  - `ProductDto.kt`, `CartDto.kt`
  - No JPA entities exposed to clients

- **GlobalExceptionHandler**: Centralized error handling
  - Catches all exceptions
  - Returns consistent error responses
  - Converts domain exceptions to HTTP status codes

**Responsibilities:**
- ✅ HTTP protocol concerns
- ✅ Request/response serialization
- ✅ Input validation (`@Valid`)
- ✅ Swagger/OpenAPI documentation
- ❌ NO business logic
- ❌ NO database queries

---

#### **Layer 2: Application Layer (Business Logic)**
**Location**: `application/service/`

**Components:**
- `AuthService.kt` - Authentication & JWT management
- `UserService.kt` - User CRUD operations
- `ProductService.kt` - Product management
- `CartService.kt` - Shopping cart business logic
- `StripeService.kt` - Payment processing logic

**Responsibilities:**
- ✅ Business rules and validation
- ✅ Transaction management (`@Transactional`)
- ✅ Orchestrating multiple repositories
- ✅ Publishing Kafka events
- ✅ Calling external APIs (Stripe)
- ❌ NO HTTP concerns
- ❌ NO direct entity exposure

**Example Business Logic:**
```kotlin
// CartService.kt - Business rule enforcement
fun addItemToCart(cartId: Long, request: AddToCartRequest): CartDto {
    // 1. Validate cart exists and is active
    val cart = cartRepository.findById(cartId)
        .orElseThrow { NoSuchElementException("Cart not found") }
    
    if (cart.status != CartStatus.ACTIVE) {
        throw IllegalStateException("Cannot modify inactive cart")
    }
    
    // 2. Validate product exists and has stock
    val product = productRepository.findById(request.productId)
        .orElseThrow { NoSuchElementException("Product not found") }
    
    // 3. Business logic: prevent duplicate items
    val existingItem = cart.items.find { it.product?.id == product.id }
    if (existingItem != null) {
        existingItem.quantity += request.quantity
    } else {
        cart.items.add(CartItem(cart = cart, product = product, quantity = request.quantity))
    }
    
    // 4. Persist changes
    val savedCart = cartRepository.save(cart)
    
    // 5. Publish event (create event object with relevant data)
    kafkaProducerService.publishOrderEvent(
        OrderEvent(userId = cart.user?.id, action = OrderAction.ITEM_ADDED)
    )
    
    // 6. Return DTO
    return cartMapper.toDto(savedCart)
}
```

---

#### **Layer 3: Infrastructure Layer**

##### **A. Persistence Sub-layer**
**Location**: `infrastructure/persistence/`

**Components:**
- **Entities**: JPA database models
  - `User.kt`, `Product.kt`, `Cart.kt`, `CartItem.kt`
  - Annotated with `@Entity`, `@Table`, `@Column`
  - Define relationships: `@OneToMany`, `@ManyToOne`, etc.

- **Repositories**: Database access interfaces
  - Extend `JpaRepository<Entity, ID>`
  - Custom query methods: `findByEmail()`, `findByStatus()`
  - `@Query` annotations for complex queries

**Example Entity:**
```kotlin
@Entity
@Table(name = "carts")
class Cart(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    
    @Enumerated(EnumType.STRING)
    var status: CartStatus = CartStatus.ACTIVE,
    
    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<CartItem> = mutableListOf()
)
```

---

##### **B. Security Sub-layer**
**Location**: `infrastructure/security/`

**Components:**
- **SecurityConfig.kt**: Configures Spring Security
  - Defines public vs protected endpoints
  - Enables JWT authentication
  - Disables session management (stateless)

- **JwtAuthenticationFilter.kt**: JWT validation filter
  - Runs on EVERY request
  - Extracts and validates JWT token
  - Sets authentication in SecurityContext

- **JwtTokenUtil.kt**: JWT operations
  - Generate token with email and expiration
  - Parse token to extract claims
  - Validate token signature and expiration

**Filter Flow:**
```
Request → JwtAuthenticationFilter
            ↓
    Extract "Authorization" header
            ↓
    Parse "Bearer <token>"
            ↓
    Validate token signature
            ↓
    Extract email from token
            ↓
    Load UserDetails from database
            ↓
    Set Authentication in SecurityContext
            ↓
Request proceeds to Controller
```

---

##### **C. Kafka Sub-layer (Event Streaming)**
**Location**: `infrastructure/kafka/`

**Components:**
- **KafkaProducerService.kt**: Publishes events
  - `publishUserEvent()` - Login, logout, registration
  - `publishOrderEvent()` - Cart actions, checkout
  - `publishNotificationEvent()` - Email, SMS triggers

- **KafkaConsumerService.kt**: Listens to events
  - Processes events asynchronously
  - Triggers side effects (emails, analytics)

- **Events.kt**: Event data models
  - `UserEvent`, `OrderEvent`, `NotificationEvent`

**Event Flow:**
```
Service completes action
    ↓
Creates event object
    ↓
KafkaProducerService.publish()
    ↓
Kafka Topic
    ↓
KafkaConsumerService listens
    ↓
Process event (send email, log analytics)
```

---

##### **D. Mapper Sub-layer**
**Location**: `infrastructure/mapper/`

**Components:**
- `CartMapper.kt` - Cart entity ↔ CartDto
- `ProductMapper.kt` - Product entity ↔ ProductDto
- `UserMapper.kt` - User entity ↔ UserDto

**Why Mappers?**
- ✅ Decouple database entities from API contracts
- ✅ Prevent exposing sensitive fields (passwords)
- ✅ Avoid JPA lazy loading issues in JSON serialization
- ✅ Calculate derived fields (totals, counts)

**Example:**
```kotlin
fun toDto(cart: Cart): CartDto {
    return CartDto(
        id = cart.id,
        userId = cart.user?.id,
        status = cart.status.name,
        items = cart.items.map { toItemDto(it) },
        totalItems = cart.items.sumOf { it.quantity },
        totalPrice = calculateTotalPrice(cart.items)
    )
}
```

---

##### **E. Configuration Sub-layer**
**Location**: `infrastructure/config/`

**Components:**
- **OpenApiConfig.kt**: Swagger UI configuration
- **StripeConfig.kt**: Stripe API setup
- **KafkaConfig.kt**: Kafka producer/consumer config
- **JacksonConfig.kt**: JSON serialization rules

---

### 🗄️ 5. Database Migration Flow (Flyway)

**Location**: `src/main/resources/migrations/`

**How it works:**
1. Application starts
2. Flyway checks `flyway_schema_history` table
3. Runs new migrations in version order
4. Records execution in history table

**Migration Files:**
- `V1__initial_migration.sql` - Create tables
- `V2__insert_sample_products.sql` - Seed data
- `V3__reset_products_autoincrement.sql` - Fix sequences
- `V4__clean_and_populate_users_addresses.sql` - User data
- `V5__add_more_test_data.sql` - Additional test data
- `V6__create_cart_tables.sql` - Cart functionality
- `V7__fix_bcrypt_for_spring_security.sql` - Password hashing

**Rules:**
- ✅ Never modify existing migrations
- ✅ Always create new migration for changes
- ✅ Use semantic versioning (V1, V2, V3...)
- ✅ Migrations run once and are immutable

---

### 🎨 6. Complete Request-Response Example

**Scenario**: Get all products with category filter

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. HTTP Request                                                 │
│    GET /products?categoryId=2                                   │
│    Authorization: Bearer eyJhbGc...                             │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. JwtAuthenticationFilter                                      │
│    ✓ Validate JWT token                                         │
│    ✓ Extract user email                                         │
│    ✓ Set SecurityContext                                        │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. ProductsController                                           │
│    @GetMapping("/products")                                     │
│    fun getAllProducts(@RequestParam categoryId: Long?)          │
│    → productService.getAllProducts(categoryId)                  │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4. ProductService                                               │
│    if (categoryId != null) {                                    │
│        productRepository.findByCategoryId(categoryId)           │
│    } else {                                                     │
│        productRepository.findAll()                              │
│    }                                                            │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ 5. ProductRepository (Spring Data JPA)                          │
│    SELECT * FROM products WHERE category_id = ?                 │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ 6. Database (MariaDB)                                           │
│    Returns List<Product> entities                               │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ 7. ProductMapper                                                │
│    entities.map { productMapper.toDto(it) }                     │
│    Converts Product → ProductDto                                │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ 8. HTTP Response                                                │
│    200 OK                                                       │
│    [                                                            │
│      {                                                          │
│        "id": 5,                                                 │
│        "name": "Laptop",                                        │
│        "price": 999.99,                                         │
│        "categoryId": 2                                          │
│      }                                                          │
│    ]                                                            │
└─────────────────────────────────────────────────────────────────┘
```

---

### 🚨 7. Error Handling Flow

**Example**: Invalid login attempt

```
POST /auth/login with wrong password
    ↓
AuthController.login()
    ↓
AuthService.login()
    ↓
Password doesn't match
    ↓
throw InvalidCredentialsException("Invalid email or password")
    ↓
GlobalExceptionHandler catches exception
    ↓
@ExceptionHandler(InvalidCredentialsException::class)
    ↓
Returns ErrorResponse:
{
  "timestamp": "2025-12-26T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/auth/login"
}
```

**Exception Hierarchy:**
- `InvalidCredentialsException` → 401 Unauthorized
- `NoSuchElementException` → 404 Not Found
- `IllegalStateException` → 400 Bad Request
- `MethodArgumentNotValidException` → 400 Bad Request (validation)
- `Exception` → 500 Internal Server Error

---

### 📊 8. Key Design Patterns Used

1. **Layered Architecture**: Web → Application → Infrastructure
2. **Dependency Injection**: Constructor injection via Spring
3. **Repository Pattern**: Spring Data JPA repositories
4. **DTO Pattern**: Separate API models from database entities
5. **Mapper Pattern**: Entity-DTO conversion
6. **Service Pattern**: Business logic encapsulation
7. **Filter Chain Pattern**: JWT authentication filter
8. **Event-Driven Architecture**: Kafka event publishing
9. **Exception Handler Pattern**: Global exception handling
10. **Factory Pattern**: JPA entity creation

---

### 🎓 Learning Path for New Developers

**Week 1: Understand the basics**
1. Read `LearningSpringRestApiApplication.kt` - Entry point
2. Explore `application.properties` - Configuration
3. Check Flyway migrations - Database schema
4. Review entity classes - Data model

**Week 2: Follow a simple flow**
1. Start with `ProductsController.kt`
2. Follow to `ProductService.kt`
3. Trace to `ProductRepository.kt`
4. Understand `ProductMapper.kt`

**Week 3: Authentication & Security**
1. Study `SecurityConfig.kt` - Security setup
2. Understand `JwtAuthenticationFilter.kt` - Filter flow
3. Review `AuthService.kt` - Login logic
4. Test with Swagger UI

**Week 4: Complex flows**
1. Shopping cart flow (`CartController` → `CartService`)
2. Payment integration (`StripeController` → `StripeService`)
3. Event streaming (Kafka producers/consumers)
4. Exception handling (`GlobalExceptionHandler`)

**Week 5: Best practices**
1. Transaction management (`@Transactional`)
2. Validation (`@Valid`, `@Validated`)
3. API documentation (Swagger annotations)
4. Testing strategies

---

### 🛠️ Debugging Tips

**1. Enable debug logging:**
```properties
# application.properties
logging.level.com.example.nikhil=DEBUG
logging.level.org.springframework.security=DEBUG
```

**2. Use Swagger UI for testing:**
- Navigate to http://localhost:8080/swagger-ui.html
- Test endpoints interactively
- See request/response examples

**3. Check logs for request flow:**
```
[AuthService] Attempting login for email: user@example.com
[JwtTokenUtil] Generating token for email: user@example.com
[AuthService] Login successful for email: user@example.com
[KafkaProducerService] Published user event: LOGIN for user 1
```

**4. Breakpoint locations:**
- Controllers: Method entry points
- Services: Business logic
- Repositories: Before database queries
- Filters: JWT validation
- Exception handlers: Error processing

---

### 📖 Additional Resources

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Spring Security**: https://spring.io/projects/spring-security
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **Kotlin Docs**: https://kotlinlang.org/docs/home.html
- **Stripe API**: https://stripe.com/docs/api

---

## 🚀 Getting Started

### Prerequisites

- **JDK 21+** - [Download](https://adoptium.net/)
- **MariaDB/MySQL** - [Download](https://mariadb.org/download/)
- **Gradle 8.x** - Included via wrapper
- **Docker** (optional) - For Kafka

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/spring-boot-ecommerce-api.git
cd spring-boot-ecommerce-api
```

### 2. Database Setup

```sql
-- Create database
CREATE DATABASE clean_db;

-- Create user
CREATE USER 'cleanuser'@'localhost' IDENTIFIED BY 'cleanpass';
GRANT ALL PRIVILEGES ON clean_db.* TO 'cleanuser'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configure Application

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mariadb://localhost:3306/clean_db
spring.datasource.username=cleanuser
spring.datasource.password=cleanpass

# JWT Secret (generate new one for production!)
jwt.secret=your-secret-key-here

# Stripe (get from https://dashboard.stripe.com/apikeys)
stripe.api.key=sk_test_your_key_here
stripe.webhook.secret=whsec_your_secret_here
```

### 4. Run the Application

```bash
# Using Gradle wrapper
./gradlew bootRun

# Or build and run JAR
./gradlew build
java -jar build/libs/demon-0.0.1-SNAPSHOT.jar
```

### 5. Access the Application

| Resource | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **API Docs (JSON)** | http://localhost:8080/api-docs |
| **Health Check** | http://localhost:8080/actuator/health |

---

## 📖 API Documentation

### Swagger UI

Access interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### Authentication Flow

1. **Login** to get JWT token:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "emily.johnson@email.com", "password": "password123"}'
```

2. **Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "email": "emily.johnson@email.com",
  "name": "Emily Johnson",
  "expiresIn": 86400
}
```

3. **Use token** for protected endpoints:
```bash
curl -X GET http://localhost:8080/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## 📍 API Endpoints

### Authentication

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/auth/login` | User login | ❌ |
| `POST` | `/auth/logout` | User logout | ✅ |
| `GET` | `/auth/validate` | Validate JWT token | ✅ |
| `POST` | `/auth/refresh` | Refresh JWT token | ✅ |

### Users

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/users` | Get all users | ✅ |
| `GET` | `/users/{id}` | Get user by ID | ✅ |
| `POST` | `/users` | Register new user | ❌ |
| `PUT` | `/users/{id}` | Update user | ✅ |
| `DELETE` | `/users/{id}` | Delete user | ✅ |
| `GET` | `/users/me` | Get current user | ✅ |

### Products

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/products` | Get all products | ❌ |
| `GET` | `/products/{id}` | Get product by ID | ❌ |
| `GET` | `/products?categoryId={id}` | Filter by category | ❌ |
| `POST` | `/products` | Create product | ✅ |
| `PUT` | `/products/{id}` | Update product | ✅ |
| `DELETE` | `/products/{id}` | Delete product | ✅ |

### Cart

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/carts?userId={id}` | Create cart | ❌ |
| `GET` | `/carts/{cartId}` | Get cart by ID | ❌ |
| `GET` | `/carts/{cartId}/summary` | Get cart summary | ❌ |
| `POST` | `/carts/{cartId}/items` | Add item to cart | ❌ |
| `PUT` | `/carts/{cartId}/items/{productId}` | Update item quantity | ❌ |
| `DELETE` | `/carts/{cartId}/items/{productId}` | Remove item | ❌ |
| `POST` | `/carts/{cartId}/checkout` | Start checkout | ❌ |
| `DELETE` | `/carts/{cartId}` | Delete cart | ❌ |

### Payments (Stripe)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/payments/checkout` | Create checkout session | ❌ |
| `POST` | `/payments/payment-intent` | Create payment intent | ❌ |
| `GET` | `/payments/checkout/{sessionId}` | Get session details | ❌ |
| `POST` | `/payments/webhook` | Stripe webhook | ❌ |

---

## 🗄 Database Schema

```
┌─────────────────┐       ┌─────────────────┐
│     users       │       │   addresses     │
├─────────────────┤       ├─────────────────┤
│ id (PK)         │───┐   │ id (PK)         │
│ name            │   │   │ street          │
│ email (unique)  │   └──▶│ city            │
│ password        │       │ state           │
└─────────────────┘       │ zip             │
         │                │ user_id (FK)    │
         │                └─────────────────┘
         │
         ▼
┌─────────────────┐       ┌─────────────────┐
│     carts       │       │   cart_items    │
├─────────────────┤       ├─────────────────┤
│ id (PK)         │───┐   │ id (PK)         │
│ user_id (FK)    │   │   │ cart_id (FK)    │◀──┐
│ status          │   └──▶│ product_id (FK) │   │
│ created_at      │       │ quantity        │   │
└─────────────────┘       │ added_at        │   │
                          └─────────────────┘   │
                                                │
┌─────────────────┐       ┌─────────────────┐   │
│   categories    │       │    products     │   │
├─────────────────┤       ├─────────────────┤   │
│ id (PK)         │───┐   │ id (PK)         │───┘
│ name            │   └──▶│ name            │
│ description     │       │ price           │
└─────────────────┘       │ category_id (FK)│
                          └─────────────────┘
```

---

## ⚙️ Configuration

### Environment Variables

For production, use environment variables instead of hardcoded values:

```bash
export DB_URL=jdbc:mariadb://localhost:3306/clean_db
export DB_USERNAME=cleanuser
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your_256_bit_secret_key
export STRIPE_API_KEY=sk_live_your_key
export STRIPE_WEBHOOK_SECRET=whsec_your_secret
```

### Kafka (Optional)

To enable Kafka event streaming:

1. Start Kafka:
```bash
docker-compose up -d kafka zookeeper
```

2. Enable in `application.properties`:
```properties
spring.kafka.enabled=true
```

### Stripe Webhooks (Local Testing)

```bash
# Install Stripe CLI
brew install stripe/stripe-cli/stripe

# Login and forward webhooks
stripe login
stripe listen --forward-to localhost:8080/payments/webhook
```

---

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests "UserServiceTest"
```

---

## 🐳 Docker Deployment

### Build Docker Image

```bash
# Build JAR
./gradlew build

# Build Docker image
docker build -t spring-ecommerce-api .
```

### Docker Compose

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mariadb://db:3306/clean_db
      - SPRING_DATASOURCE_USERNAME=cleanuser
      - SPRING_DATASOURCE_PASSWORD=cleanpass
    depends_on:
      - db

  db:
    image: mariadb:10.11
    environment:
      - MYSQL_DATABASE=clean_db
      - MYSQL_USER=cleanuser
      - MYSQL_PASSWORD=cleanpass
      - MYSQL_ROOT_PASSWORD=rootpass
    ports:
      - "3306:3306"
    volumes:
      - db_data:/var/lib/mysql

volumes:
  db_data:
```

---

## 📁 Project Structure

```
spring_boot_mosh_rest_api/
├── build.gradle.kts            # Gradle build configuration
├── settings.gradle.kts         # Gradle settings
├── gradlew                     # Gradle wrapper (Unix)
├── gradlew.bat                 # Gradle wrapper (Windows)
├── .gitignore                  # Git ignore rules
├── README.md                   # This file
├── DATABASE_RELATIONSHIPS.md   # Database documentation
│
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── src/
│   ├── main/
│   │   ├── kotlin/             # Kotlin source code
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── migrations/     # Flyway SQL migrations
│   │       ├── static/         # Static files
│   │       └── templates/      # Thymeleaf templates
│   │
│   └── test/
│       └── kotlin/             # Test source code
│
└── build/                      # Build output (ignored)
```

---

## 🔐 Security Best Practices

1. **Never commit secrets** - Use environment variables
2. **Rotate JWT secrets** regularly
3. **Use HTTPS** in production
4. **Validate all inputs** with Jakarta Validation
5. **Keep dependencies updated** - Check for CVEs

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Nikhil**

https://nikhilmule26.netlify.app/

---

## 🙏 Acknowledgments

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Stripe API Documentation](https://stripe.com/docs/api)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)

---

<p align="center">
  Made with ❤️ using Spring Boot & Kotlin
</p>

