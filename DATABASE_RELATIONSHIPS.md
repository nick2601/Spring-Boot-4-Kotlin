# 🗄️ Database Relationships & Entity Mapping Guide

## Overview

This Spring Boot REST API uses **Clean Architecture** with JPA/Hibernate for database operations. This document explains all database relationships and their corresponding JPA mappings.

---

## 📊 Database Schema Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              DATABASE SCHEMA                                      │
└─────────────────────────────────────────────────────────────────────────────────┘

                                ┌─────────────┐
                                │   users     │
                                ├─────────────┤
                                │ id (PK)     │
                                │ name        │
                                │ email       │
                                │ password    │
                                └──────┬──────┘
                                       │
       ┌───────────────┬───────────────┼───────────────┬───────────────┐
       │               │               │               │               │
       ▼               ▼               ▼               ▼               ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│  addresses  │ │  profiles   │ │  wishlist   │ │   carts     │ │  (future)   │
├─────────────┤ ├─────────────┤ ├─────────────┤ ├─────────────┤ │   orders    │
│ id (PK)     │ │ id (PK/FK)  │ │ user_id(FK) │ │ id (PK)     │ └─────────────┘
│ street      │ │ bio         │ │ product_id  │ │ user_id(FK) │
│ city        │ │ phone       │ │ (Join Table)│ │ status      │
│ state       │ │ dob         │ └─────────────┘ │ created_at  │
│ zip         │ │ loyalty_pts │       │         │ updated_at  │
│ user_id(FK) │ └─────────────┘       │         └──────┬──────┘
└─────────────┘                       │                │
                                      │                │
                                      ▼                ▼
                               ┌─────────────┐  ┌─────────────┐
                               │  products   │  │ cart_items  │
                               ├─────────────┤  ├─────────────┤
                               │ id (PK)     │◄─│ id (PK)     │
                               │ name        │  │ cart_id(FK) │
                               │ price       │  │ product_id  │
                               │ description │  │ quantity    │
                               │ category_id │  │ added_at    │
                               └──────┬──────┘  └─────────────┘
                                      │
                                      ▼
                               ┌─────────────┐
                               │ categories  │
                               ├─────────────┤
                               │ id (PK)     │
                               │ name        │
                               └─────────────┘
```

---

## 🔗 Relationship Types

### 1️⃣ ONE-TO-ONE: User ↔ Profile

> "One user has exactly one profile, one profile belongs to exactly one user"

**Database Tables:**
```sql
-- users table (parent)
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255)
);

-- profiles table (child - shares user's ID)
CREATE TABLE profiles (
    id BIGINT PRIMARY KEY,  -- Same as user.id
    bio TEXT,
    phone_number VARCHAR(15),
    date_of_birth DATE,
    loyalty_points INT,
    FOREIGN KEY (id) REFERENCES users(id)
);
```

**JPA Mapping:**
```kotlin
// User.kt (Parent)
@OneToOne(mappedBy = "user", cascade = [CascadeType.REMOVE])
var profile: Profile? = null

// Profile.kt (Child - owns FK)
@OneToOne
@JoinColumn(name = "id")
@MapsId  // Shares primary key with User
var user: User? = null
```

**Key Points:**
- `mappedBy` = "I don't have the FK, the other side does"
- `@MapsId` = Profile uses User's ID as its own primary key

---

### 2️⃣ ONE-TO-MANY: User → Addresses

> "One user can have MANY addresses, each address belongs to ONE user"

**Database Tables:**
```sql
CREATE TABLE addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    street VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    zip VARCHAR(255),
    user_id BIGINT,  -- FK to users
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**JPA Mapping:**
```kotlin
// User.kt (ONE side)
@OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
val addresses: MutableList<Address> = mutableListOf()

// Address.kt (MANY side - has FK)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
var user: User? = null
```

**Key Points:**
- The MANY side always has the Foreign Key
- `@JoinColumn` goes on the entity that HAS the FK column
- `orphanRemoval = true` = Delete address if removed from user's list

---

### 3️⃣ MANY-TO-ONE: Product → Category

> "MANY products belong to ONE category"

**Database Tables:**
```sql
CREATE TABLE categories (
    id TINYINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255)
);

CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    price DECIMAL(10,2),
    description TEXT,
    category_id TINYINT,  -- FK to categories
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
```

**JPA Mapping:**
```kotlin
// Product.kt (MANY side - has FK)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id")
var category: Category? = null

// Category.kt (ONE side - inverse mapping)
@OneToMany(mappedBy = "category")
val products: MutableSet<Product> = mutableSetOf()
```

---

### 4️⃣ MANY-TO-MANY: User ↔ Products (Wishlist)

> "Many users can have many products in their wishlist"

**Database Tables:**
```sql
-- Join table (no separate entity!)
CREATE TABLE wishlist (
    user_id BIGINT,
    product_id BIGINT,
    PRIMARY KEY (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

**JPA Mapping:**
```kotlin
// User.kt (Owner of relationship)
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "wishlist",
    joinColumns = [JoinColumn(name = "user_id")],        // FK to THIS entity
    inverseJoinColumns = [JoinColumn(name = "product_id")] // FK to OTHER entity
)
val favoriteProducts: MutableSet<Product> = mutableSetOf()

// Product.kt (Inverse side - optional)
@ManyToMany(mappedBy = "favoriteProducts")
val usersWhoFavorited: MutableSet<User> = mutableSetOf()
```

**Key Points:**
- `@JoinTable` = I own the join table
- `joinColumns` = MY foreign key
- `inverseJoinColumns` = THEIR foreign key
- No separate entity for the join table

---

### 5️⃣ ONE-TO-MANY with Extra Data: Cart → CartItems

> "One cart has many items, each with quantity and timestamp"

**Why not ManyToMany?**
- We need extra columns: `quantity`, `added_at`
- ManyToMany join tables can't have extra columns
- Solution: Create a separate entity `CartItem`

**Database Tables:**
```sql
CREATE TABLE carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE,  -- One cart per user
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT,
    product_id BIGINT,
    quantity INT DEFAULT 1,
    added_at TIMESTAMP,
    UNIQUE (cart_id, product_id),  -- No duplicate products
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

**JPA Mapping:**
```kotlin
// Cart.kt
@OneToOne
@JoinColumn(name = "user_id", unique = true)
var user: User? = null

@OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
val items: MutableList<CartItem> = mutableListOf()

// CartItem.kt
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "cart_id")
var cart: Cart? = null

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "product_id")
var product: Product? = null

var quantity: Int = 1
```

---

## 📋 Complete Relationship Summary

| Parent | Child | Relationship | FK Location | JPA Annotation |
|--------|-------|--------------|-------------|----------------|
| User | Profile | OneToOne | profiles.id | `@MapsId` |
| User | Address | OneToMany | addresses.user_id | `mappedBy` |
| User | Product (wishlist) | ManyToMany | wishlist table | `@JoinTable` |
| User | Cart | OneToOne | carts.user_id | `@JoinColumn` |
| Category | Product | OneToMany | products.category_id | `mappedBy` |
| Cart | CartItem | OneToMany | cart_items.cart_id | `mappedBy` |
| Product | CartItem | OneToMany | cart_items.product_id | `@JoinColumn` |

---

## 🎯 Golden Rules to Remember

| Rule | Explanation |
|------|-------------|
| `mappedBy` | "I don't have the FK, the other entity does" |
| `@JoinColumn` | "The FK column is in MY table" |
| `@JoinTable` | "There's a separate join table for this relationship" |
| MANY side has FK | In OneToMany, the MANY side always contains the foreign key |
| Use `LAZY` fetch | Avoid loading related entities until needed |
| `cascade` | Apply same operation (save/delete) to related entities |
| `orphanRemoval` | Delete child when removed from parent's collection |

---

## 🔄 Wishlist vs Cart Comparison

| Feature | Wishlist | Cart |
|---------|----------|------|
| Relationship | ManyToMany | OneToMany with entity |
| Extra Data | ❌ None | ✅ Quantity, timestamps |
| Join Table | Simple (user_id, product_id) | Entity with extra columns |
| JPA Approach | `@JoinTable` | Separate `CartItem` entity |
| Use Case | Simple bookmarking | E-commerce with quantities |

---

## 📁 File Structure

```
src/main/kotlin/com/example/nikhil/
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/
│   │   │   ├── User.kt          # OneToMany, OneToOne, ManyToMany
│   │   │   ├── Profile.kt       # OneToOne with @MapsId
│   │   │   ├── Address.kt       # ManyToOne to User
│   │   │   ├── Product.kt       # ManyToOne to Category
│   │   │   ├── Category.kt      # OneToMany to Products
│   │   │   ├── Cart.kt          # OneToOne to User, OneToMany to CartItem
│   │   │   └── CartItem.kt      # ManyToOne to Cart and Product
│   │   └── repository/
│   │       ├── UserRepository.kt
│   │       ├── ProductRepository.kt
│   │       ├── CategoryRepository.kt
│   │       ├── CartRepository.kt
│   │       └── CartItemRepository.kt
```

---

## 🚀 Quick Reference

### When to use what?

| Scenario | Use This |
|----------|----------|
| User has one profile | `@OneToOne` with `@MapsId` |
| User has multiple addresses | `@OneToMany` / `@ManyToOne` |
| Products in categories | `@ManyToOne` on Product |
| Simple bookmarks/favorites | `@ManyToMany` with `@JoinTable` |
| Cart with quantities | Separate entity + `@OneToMany` |
| Orders with line items | Separate entity + `@OneToMany` |

---

## 📚 Further Reading

- [JPA Relationships - Baeldung](https://www.baeldung.com/jpa-hibernate-associations)
- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Hibernate ORM Guide](https://hibernate.org/orm/documentation/)

