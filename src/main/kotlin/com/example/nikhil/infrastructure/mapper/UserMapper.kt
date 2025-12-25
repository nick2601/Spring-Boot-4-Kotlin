package com.example.nikhil.infrastructure.mapper

import com.example.nikhil.infrastructure.persistence.entity.User
import com.example.nikhil.infrastructure.web.dto.UserDto
import org.springframework.stereotype.Component

/**
 * User Mapper
 * Single Responsibility: Handles conversion between User entity and UserDto
 */
@Component
class UserMapper {

    /**
     * Convert User entity to UserDto
     * Note: Password is never exposed in DTO for security
     */
    fun toDto(user: User) = UserDto(
        id = user.id,
        name = user.name,
        email = user.email,
        datetime = user.datetime
    )

    /**
     * Convert list of User entities to list of UserDto
     */
    fun toDtoList(users: Iterable<User>): List<UserDto> = users.map { toDto(it) }

    /**
     * Convert UserDto to User entity
     * Note: Password should be set separately and encoded
     */
    fun toEntity(userDto: UserDto) = User(
        id = userDto.id,
        name = userDto.name,
        email = userDto.email
    )
}
