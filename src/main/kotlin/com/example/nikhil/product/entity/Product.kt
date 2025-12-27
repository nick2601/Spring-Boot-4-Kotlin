package com.example.nikhil.product.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

/**
 * Product entity - represents products in the catalog
 * Relationship: Many products belong to one category
 */
@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @field:NotBlank(message = "Product name is required")
    @field:Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    @Column(name = "name", nullable = false)
    var name: String? = null,

    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(name = "description")
    var description: String? = null,

    @field:Positive(message = "Price must be positive")
    @Column(name = "price", nullable = false)
    var price: BigDecimal? = null,

    @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category? = null
) {
    override fun toString(): String = "Product(id=$id, name=$name, price=$price)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Product) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}