package com.example.nikhil.product.entity

import com.example.nikhil.product.entity.Product
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Category entity - represents product categories
 * Relationship: One category has many products
 */
@Entity
@Table(name = "categories")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Byte? = null,

    @field:NotBlank(message = "Category name is required")
    @field:Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @Column(name = "name")
    var name: String? = null,

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnore
    val products: MutableSet<Product> = mutableSetOf()
) {
    override fun toString(): String = "Category(id=$id, name=$name)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Category) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}