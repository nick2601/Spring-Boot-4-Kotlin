package com.example.nikhil.infrastructure.persistence.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * User entity - represents application users
 * Relationships:
 * - One-to-many with Address
 * - One-to-one with Profile
 * - Many-to-many with Product (wishlist)
 * - Many-to-many with Role (authorization)
 */
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false)
    var name: String? = null,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @Column(name = "email", nullable = false, unique = true)
    var email: String? = null,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, max = 255, message = "Password must be between 6 and 255 characters")
    @Column(name = "password", nullable = false)
    @JsonIgnore
    var password: String? = null,

    @Transient
    var datetime: LocalDateTime? = LocalDateTime.now(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    val addresses: MutableList<Address> = mutableListOf(),

    @OneToOne(mappedBy = "user", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    @JsonIgnore
    var profile: Profile? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "wishlist",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "product_id")]
    )
    @JsonIgnore
    val favoriteProducts: MutableSet<Product> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: MutableSet<Role> = mutableSetOf()
) {
    fun addAddress(address: Address) {
        addresses.add(address)
        address.user = this
    }

    fun removeAddress(address: Address) {
        addresses.remove(address)
        address.user = null
    }

    fun addFavoriteProduct(product: Product) {
        favoriteProducts.add(product)
    }

    fun removeFavoriteProduct(product: Product) {
        favoriteProducts.remove(product)
    }

    fun addRole(role: Role) {
        roles.add(role)
    }

    fun removeRole(role: Role) {
        roles.remove(role)
    }

    fun hasRole(roleName: RoleName): Boolean {
        return roles.any { it.name == roleName }
    }

    fun isAdmin(): Boolean = hasRole(RoleName.ROLE_ADMIN)

    fun isCustomer(): Boolean = hasRole(RoleName.ROLE_CUSTOMER)

    fun getRoleNames(): List<String> = roles.map { it.name.name }

    override fun toString(): String = "User(id=$id, name=$name, email=$email)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
