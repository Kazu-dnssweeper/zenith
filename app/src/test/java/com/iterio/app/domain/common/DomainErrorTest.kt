package com.iterio.app.domain.common

import org.junit.Assert.*
import org.junit.Test

/**
 * DomainError sealed class のユニットテスト
 *
 * TDD RED Phase: これらのテストは最初は失敗する
 */
class DomainErrorTest {

    // ==================== DatabaseError テスト ====================

    @Test
    fun `DatabaseError holds message correctly`() {
        val error = DomainError.DatabaseError("Failed to insert record")

        assertEquals("Failed to insert record", error.message)
        assertTrue(error is DomainError)
    }

    @Test
    fun `DatabaseError with cause holds both message and cause`() {
        val cause = RuntimeException("Underlying error")
        val error = DomainError.DatabaseError("DB operation failed", cause)

        assertEquals("DB operation failed", error.message)
        assertEquals(cause, error.cause)
    }

    // ==================== NotFoundError テスト ====================

    @Test
    fun `NotFoundError holds message correctly`() {
        val error = DomainError.NotFoundError("Task with id 123 not found")

        assertEquals("Task with id 123 not found", error.message)
    }

    @Test
    fun `NotFoundError can be distinguished from other errors`() {
        val error: DomainError = DomainError.NotFoundError("Not found")

        assertTrue(error is DomainError.NotFoundError)
        assertFalse(error is DomainError.DatabaseError)
    }

    // ==================== ValidationError テスト ====================

    @Test
    fun `ValidationError holds message correctly`() {
        val error = DomainError.ValidationError("Name cannot be empty")

        assertEquals("Name cannot be empty", error.message)
    }

    @Test
    fun `ValidationError with field information`() {
        val error = DomainError.ValidationError(
            message = "Invalid value",
            field = "email"
        )

        assertEquals("Invalid value", error.message)
        assertEquals("email", error.field)
    }

    // ==================== NetworkError テスト ====================

    @Test
    fun `NetworkError holds message correctly`() {
        val error = DomainError.NetworkError("Connection timeout")

        assertEquals("Connection timeout", error.message)
    }

    @Test
    fun `NetworkError with cause`() {
        val cause = java.io.IOException("Network unreachable")
        val error = DomainError.NetworkError("Failed to connect", cause)

        assertEquals("Failed to connect", error.message)
        assertEquals(cause, error.cause)
    }

    // ==================== PremiumRequiredError テスト ====================

    @Test
    fun `PremiumRequiredError holds feature name`() {
        val error = DomainError.PremiumRequiredError("Cloud Backup")

        assertEquals("Cloud Backup", error.featureName)
        assertTrue(error.message.contains("Cloud Backup"))
    }

    @Test
    fun `PremiumRequiredError default message includes feature name`() {
        val error = DomainError.PremiumRequiredError("Advanced Statistics")

        assertTrue(error.message.contains("Advanced Statistics"))
        assertTrue(error.message.contains("premium") || error.message.contains("Premium"))
    }

    // ==================== UnauthorizedError テスト ====================

    @Test
    fun `UnauthorizedError holds message correctly`() {
        val error = DomainError.UnauthorizedError("Session expired")

        assertEquals("Session expired", error.message)
    }

    // ==================== UnknownError テスト ====================

    @Test
    fun `UnknownError holds message and cause`() {
        val cause = IllegalStateException("Unexpected state")
        val error = DomainError.UnknownError("An unknown error occurred", cause)

        assertEquals("An unknown error occurred", error.message)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `UnknownError can be created with just message`() {
        val error = DomainError.UnknownError("Something went wrong")

        assertEquals("Something went wrong", error.message)
        assertNull(error.cause)
    }

    // ==================== when 式での網羅性テスト ====================

    @Test
    fun `all error types can be handled with when expression`() {
        val errors = listOf(
            DomainError.DatabaseError("db"),
            DomainError.NotFoundError("not found"),
            DomainError.ValidationError("invalid"),
            DomainError.NetworkError("network"),
            DomainError.PremiumRequiredError("feature"),
            DomainError.UnauthorizedError("unauthorized"),
            DomainError.UnknownError("unknown")
        )

        errors.forEach { error ->
            val result = when (error) {
                is DomainError.DatabaseError -> "database"
                is DomainError.NotFoundError -> "notfound"
                is DomainError.ValidationError -> "validation"
                is DomainError.NetworkError -> "network"
                is DomainError.PremiumRequiredError -> "premium"
                is DomainError.UnauthorizedError -> "unauthorized"
                is DomainError.UnknownError -> "unknown"
            }
            assertNotNull(result)
        }
    }

    // ==================== equals/hashCode テスト ====================

    @Test
    fun `errors with same properties are equal`() {
        val error1 = DomainError.NotFoundError("Task not found")
        val error2 = DomainError.NotFoundError("Task not found")

        assertEquals(error1, error2)
        assertEquals(error1.hashCode(), error2.hashCode())
    }

    @Test
    fun `errors with different properties are not equal`() {
        val error1 = DomainError.NotFoundError("Task not found")
        val error2 = DomainError.NotFoundError("User not found")

        assertNotEquals(error1, error2)
    }

    @Test
    fun `different error types are not equal even with same message`() {
        val error1 = DomainError.NotFoundError("Error occurred")
        val error2 = DomainError.ValidationError("Error occurred")

        assertNotEquals(error1, error2)
    }
}
