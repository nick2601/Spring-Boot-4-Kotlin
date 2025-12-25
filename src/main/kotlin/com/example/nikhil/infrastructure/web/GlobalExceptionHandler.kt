package com.example.nikhil.infrastructure.web

import com.example.nikhil.application.service.InvalidCredentialsException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.LocalDateTime

/**
 * Global Exception Handler
 * Provides consistent error responses across the application
 *
 * Note: @ExceptionHandler methods are invoked by Spring at runtime,
 * so IDE may incorrectly report them as "unused"
 */
@Suppress("unused")
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFoundException(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        logger.warn("Resource not found: ${ex.message}")
        return createErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.message ?: "Resource not found")
    }

    /**
     * Handle invalid credentials (authentication failures)
     */
    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        logger.warn("Authentication failed: ${ex.message}")
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.message ?: "Invalid credentials")
    }

    /**
     * Handle validation errors from @Valid on request body
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        logger.warn("Validation failed: $errors")
        return createValidationErrorResponse(errors)
    }

    /**
     * Handle constraint violations from entity-level validation
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.constraintViolations.associate {
            it.propertyPath.toString() to (it.message ?: "Invalid value")
        }
        logger.warn("Constraint violation: $errors")
        return createValidationErrorResponse(errors)
    }

    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParams(ex: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> {
        logger.warn("Missing parameter: ${ex.parameterName}")
        return createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Missing Parameter",
            "Required parameter '${ex.parameterName}' is missing"
        )
    }

    /**
     * Handle type mismatch errors (e.g., passing string for numeric ID)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        logger.warn("Type mismatch: ${ex.name} should be ${ex.requiredType?.simpleName}")
        return createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Type Mismatch",
            "Parameter '${ex.name}' should be of type ${ex.requiredType?.simpleName}"
        )
    }

    /**
     * Handle malformed JSON in request body
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMalformedJson(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        logger.warn("Malformed JSON: ${ex.message}")
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON", "Request body is not valid JSON")
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.warn("Bad request: ${ex.message}")
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.message ?: "Invalid request")
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred")
    }

    private fun createErrorResponse(status: HttpStatus, error: String, message: String): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = status.value(),
            error = error,
            message = message
        )
        return ResponseEntity.status(status).body(errorResponse)
    }

    private fun createValidationErrorResponse(errors: Map<String, String>): ResponseEntity<ValidationErrorResponse> {
        val errorResponse = ValidationErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "One or more fields have validation errors",
            errors = errors
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
}

/**
 * Standard error response structure for API errors
 */
data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String
)

/**
 * Validation error response with field-level errors
 */
data class ValidationErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val errors: Map<String, String>
)

