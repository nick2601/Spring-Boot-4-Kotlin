package com.example.nikhil.infrastructure.persistence.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

/**
 * Profile entity - represents user profile information
 * Relationship: One-to-one with User (shares primary key)
 */

@Entity
@Table(name = "profiles")
class Profile(
    @Id
    @Column(name = "id")
    var id: Long? = null,

    @field:Size(max = 500, message = "Bio cannot exceed 500 characters")
    @Column(name = "bio")
    var bio: String? = null,

    @field:Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    @Column(name = "phone_number")
    var phoneNumber: String? = null,

    @field:Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth")
    var dateOfBirth: LocalDate? = null,

    @field:Min(value = 0, message = "Loyalty points cannot be negative")
    @Column(name = "loyalty_points")
    var loyaltyPoints: Int? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @MapsId
    var user: User? = null
) {
    override fun toString(): String = "Profile(id=$id, phoneNumber=$phoneNumber, loyaltyPoints=$loyaltyPoints)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Profile) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}