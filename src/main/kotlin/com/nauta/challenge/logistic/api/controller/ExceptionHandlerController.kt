package com.nauta.challenge.logistic.api.controller

import com.nauta.challenge.logistic.api.controller.common.ApiResponse
import com.nauta.challenge.logistic.api.core.exception.LogisticConflictException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.sqlite.SQLiteException
import mu.KotlinLogging

@RestControllerAdvice
class ExceptionHandlerController {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<List<String>>> {
        val errors = ex.bindingResult.allErrors.map { it.defaultMessage ?: "Invalid" }
        val response = ApiResponse<List<String>>(
            data = null,
            error = "Validation failed",
            message = errors.joinToString(", ")
        )
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Nothing>> {
        val type = ex.requiredType?.simpleName ?: "unknown"
        val message = "Parameter '${ex.name}' must be of type $type."
        val response = ApiResponse<Nothing>(
            data = null,
            error = "TypeMismatch",
            message = message
        )
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleRequestHeaderErrors(ex: MissingRequestHeaderException): ResponseEntity<ApiResponse<Nothing>> {
        val error = "Missing required header: ${ex.headerName}"
        val response = ApiResponse<Nothing>(
            data = null,
            error = "MissingRequestHeader",
            message = error
        )
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleRequestHeaderErrors(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse<Nothing>(
            data = null,
            error = ex::class.simpleName ?: "IllegalArgumentException",
            message = ex.message ?: "Illegal argument"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse<Nothing>(
            data = null,
            error = "HttpMessageNotReadableException",
            message = ex.cause?.message ?: "Malformed JSON request"
        )
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(LogisticConflictException::class)
    fun handleLogisticConflicts(ex: LogisticConflictException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse<Nothing>(
            data = null,
            error = ex::class.simpleName ?: "LogisticConflictException",
            message = ex.message ?: "Inconsistency in logistic data"
        )
        return ResponseEntity.status(CONFLICT).body(response)
    }

    @ExceptionHandler(ExposedSQLException::class)
    fun handleExposedSQLException(ex: ExposedSQLException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error(ex) { "Database error ${ex.message}"; ex }
        val response = ApiResponse<Nothing>(
            data = null,
            error = "DatabaseError",
            message = "A database error occurred."
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    @ExceptionHandler(SQLiteException::class)
    fun handleSQLiteException(ex: SQLiteException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error(ex) { "SQLite error ${ex.message}"; ex }
        val response = ApiResponse<Nothing>(
            data = null,
            error = "SQLiteError",
            message = "A database error occurred."
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error(ex) { "Unexpected error: ${ex.message}" }
        val response = ApiResponse<Nothing>(
            data = null,
            error = "InternalServerError",
            message = ex.message ?: "An unexpected error occurred."
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    private companion object {
        val logger = KotlinLogging.logger {}
    }
}
