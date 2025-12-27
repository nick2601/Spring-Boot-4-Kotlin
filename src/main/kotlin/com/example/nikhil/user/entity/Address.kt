package com.example.nikhil.user.entity

import com.example.nikhil.user.entity.User
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Address entity - represents user addresses in the database
 * Relationship: Many addresses belong to one user
 */
@Entity
@Table(name = "addresses")
class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @field:NotBlank(message = "Street is required")
    @field:Size(max = 255, message = "Street cannot exceed 255 characters")
    @Column(name = "street")
    var street: String? = null,

    @field:NotBlank(message = "City is required")
    @field:Size(max = 100, message = "City cannot exceed 100 characters")
    @Column(name = "city")
    var city: String? = null,

    @field:NotBlank(message = "ZIP code is required")
    @field:Pattern(regexp = "^[0-9]{5,10}$", message = "ZIP code must be 5-10 digits")
    @Column(name = "zip")
    var zip: String? = null,

    @field:NotBlank(message = "State is required")
    @field:Size(max = 100, message = "State cannot exceed 100 characters")
    @Column(name = "state")
    var state: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null
) {
    override fun toString(): String = "Address(id=$id, street=$street, city=$city, state=$state, zip=$zip)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Address) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}