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

