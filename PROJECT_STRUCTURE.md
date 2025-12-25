# 📁 Project Structure - Clean Architecture

This Spring Boot project follows **Clean Architecture** and **SOLID principles**.

## Directory Structure

```
src/main/kotlin/com/example/nikhil/
│
├── LearningSpringRestApiApplication.kt    # Main Application Entry Point
│
├── application/                            # APPLICATION LAYER (Use Cases)
│   └── usecase/
│       ├── AuthService.kt                 # Authentication business logic
│       ├── CartService.kt                 # Cart business logic
│       ├── CustomUserDetailsService.kt    # Spring Security user details
│       ├── ProductService.kt              # Product business logic
│       └── UserService.kt                 # User business logic
│
└── infrastructure/                         # INFRASTRUCTURE LAYER
    │
    ├── config/                            # Configuration Classes
    │   └── JacksonConfig.kt               # JSON serialization config
    │
    ├── mapper/                            # Entity <-> DTO Mappers
    │   ├── CartMapper.kt                  # Cart entity to DTO conversion
    │   ├── ProductMapper.kt               # Product entity to DTO conversion
    │   └── UserMapper.kt                  # User entity to DTO conversion
    │
    ├── persistence/                       # Database Layer
    │   ├── entity/                        # JPA Entities
    │   │   ├── Address.kt
    │   │   ├── Cart.kt
    │   │   ├── CartItem.kt
    │   │   ├── Category.kt
    │   │   ├── Product.kt
    │   │   ├── Profile.kt
    │   │   └── User.kt
    │   │
    │   └── repository/                    # Spring Data Repositories
    │       ├── AddressRepository.kt
    │       ├── CartItemRepository.kt
    │       ├── CartRepository.kt
    │       ├── CategoryRepository.kt
    │       ├── ProductRepository.kt
    │       ├── ProfileRepository.kt
    │       └── UserRepository.kt
    │
    ├── security/                          # Security Configuration
    │   ├── JwtAuthenticationFilter.kt     # JWT token filter
    │   ├── JwtTokenUtil.kt                # JWT utility methods
    │   └── SecurityConfig.kt              # Spring Security config
    │
    └── web/                               # Web Layer
        ├── GlobalExceptionHandler.kt      # Centralized exception handling
        │
        ├── controller/                    # REST Controllers
        │   ├── CartController.kt
        │   ├── HomeController.kt
        │   ├── ProductsController.kt
        │   └── UserController.kt
        │
        └── dto/                           # Data Transfer Objects
            ├── AddToCartRequest.kt        # Cart: Add item request
            ├── AuthRequest.kt             # Auth: Login request
            ├── AuthResponse.kt            # Auth: JWT response
            ├── CartDto.kt                 # Cart: Response DTO
            ├── CartItemDto.kt             # Cart: Item DTO
            ├── ChangePasswordRequest.kt   # User: Change password request
            ├── MessageResponse.kt         # Generic message response
            ├── ProductDto.kt              # Product: Request/Response DTO
            ├── RegisterUserRequest.kt     # User: Registration request
            ├── UpdateCartItemRequest.kt   # Cart: Update quantity request
            └── UserDto.kt                 # User: Response DTO
```

## SOLID Principles Applied

### 1. Single Responsibility Principle (SRP)
- **Controllers**: Only handle HTTP requests/responses
- **Services**: Only contain business logic
- **Mappers**: Only convert between entities and DTOs
- **Repositories**: Only handle database operations
- **DTOs**: One DTO per use case (request/response)

### 2. Open/Closed Principle (OCP)
- Services are open for extension via new methods
- Base exception handling can be extended without modification

### 3. Liskov Substitution Principle (LSP)
- All repositories extend `JpaRepository`
- Custom implementations can replace default behavior

### 4. Interface Segregation Principle (ISP)
- Small, focused interfaces (repository methods)
- DTOs contain only required fields

### 5. Dependency Inversion Principle (DIP)
- Controllers depend on Service abstractions
- Services depend on Repository abstractions
- All dependencies injected via constructor

## Layer Responsibilities

| Layer | Responsibility |
|-------|----------------|
| **Controller** | HTTP handling, validation, routing |
| **Service** | Business logic, transactions |
| **Mapper** | Entity ↔ DTO conversion |
| **Repository** | Database operations |
| **Entity** | Database table representation |
| **DTO** | API request/response data |
| **Config** | Application configuration |
| **Security** | Authentication & authorization |

## Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Entity | Singular noun | `User`, `Product` |
| Repository | Entity + "Repository" | `UserRepository` |
| Service | Entity + "Service" | `UserService` |
| Controller | Entity + "Controller" | `UserController` |
| Request DTO | Action + "Request" | `RegisterUserRequest` |
| Response DTO | Entity + "Dto" | `UserDto` |
| Mapper | Entity + "Mapper" | `UserMapper` |

## Best Practices Followed

✅ **No business logic in controllers** - Controllers only delegate to services  
✅ **No entity exposure** - Entities are never returned directly from APIs  
✅ **Centralized exception handling** - GlobalExceptionHandler catches all errors  
✅ **Validation at DTO level** - Jakarta validation on request DTOs  
✅ **Transactional services** - `@Transactional` on service methods  
✅ **Lazy loading** - `FetchType.LAZY` on all relationships  
✅ **Separate request/response DTOs** - Different DTOs for different operations  
✅ **Consistent error responses** - ErrorResponse and ValidationErrorResponse  

