package com.iterio.app.domain.common

import org.junit.Assert.*
import org.junit.Test

/**
 * Result<T, E> sealed class のユニットテスト
 *
 * TDD RED Phase: これらのテストは最初は失敗する
 */
class ResultTest {

    // ==================== Success/Failure 生成テスト ====================

    @Test
    fun `Success should hold value correctly`() {
        val result: Result<String, DomainError> = Result.Success("test value")

        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
        assertEquals("test value", (result as Result.Success).value)
    }

    @Test
    fun `Failure should hold error correctly`() {
        val error = DomainError.NotFoundError("Task not found")
        val result: Result<String, DomainError> = Result.Failure(error)

        assertFalse(result.isSuccess)
        assertTrue(result.isFailure)
        assertEquals(error, (result as Result.Failure).error)
    }

    @Test
    fun `Success with null value should work correctly`() {
        val result: Result<String?, DomainError> = Result.Success(null)

        assertTrue(result.isSuccess)
        assertNull((result as Result.Success).value)
    }

    // ==================== getOrNull テスト ====================

    @Test
    fun `getOrNull returns value for Success`() {
        val result: Result<Int, DomainError> = Result.Success(42)

        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `getOrNull returns null for Failure`() {
        val result: Result<Int, DomainError> = Result.Failure(
            DomainError.DatabaseError("DB error")
        )

        assertNull(result.getOrNull())
    }

    // ==================== getOrDefault テスト ====================

    @Test
    fun `getOrDefault returns value for Success`() {
        val result: Result<Int, DomainError> = Result.Success(42)

        assertEquals(42, result.getOrDefault(0))
    }

    @Test
    fun `getOrDefault returns default for Failure`() {
        val result: Result<Int, DomainError> = Result.Failure(
            DomainError.DatabaseError("DB error")
        )

        assertEquals(0, result.getOrDefault(0))
    }

    // ==================== errorOrNull テスト ====================

    @Test
    fun `errorOrNull returns null for Success`() {
        val result: Result<Int, DomainError> = Result.Success(42)

        assertNull(result.errorOrNull())
    }

    @Test
    fun `errorOrNull returns error for Failure`() {
        val error = DomainError.ValidationError("Invalid input")
        val result: Result<Int, DomainError> = Result.Failure(error)

        assertEquals(error, result.errorOrNull())
    }

    // ==================== map テスト ====================

    @Test
    fun `map transforms Success value`() {
        val result: Result<Int, DomainError> = Result.Success(5)

        val mapped = result.map { it * 2 }

        assertTrue(mapped.isSuccess)
        assertEquals(10, mapped.getOrNull())
    }

    @Test
    fun `map preserves Failure`() {
        val error = DomainError.NotFoundError("Not found")
        val result: Result<Int, DomainError> = Result.Failure(error)

        val mapped = result.map { it * 2 }

        assertTrue(mapped.isFailure)
        assertEquals(error, mapped.errorOrNull())
    }

    // ==================== flatMap テスト ====================

    @Test
    fun `flatMap chains Success operations`() {
        val result: Result<Int, DomainError> = Result.Success(5)

        val chained = result.flatMap { value ->
            if (value > 0) {
                Result.Success(value.toString())
            } else {
                Result.Failure(DomainError.ValidationError("Must be positive"))
            }
        }

        assertTrue(chained.isSuccess)
        assertEquals("5", chained.getOrNull())
    }

    @Test
    fun `flatMap returns Failure from chain`() {
        val result: Result<Int, DomainError> = Result.Success(-1)

        val chained = result.flatMap { value ->
            if (value > 0) {
                Result.Success(value.toString())
            } else {
                Result.Failure(DomainError.ValidationError("Must be positive"))
            }
        }

        assertTrue(chained.isFailure)
        assertTrue(chained.errorOrNull() is DomainError.ValidationError)
    }

    @Test
    fun `flatMap preserves original Failure`() {
        val originalError = DomainError.DatabaseError("DB error")
        val result: Result<Int, DomainError> = Result.Failure(originalError)

        val chained = result.flatMap { value ->
            Result.Success(value.toString())
        }

        assertTrue(chained.isFailure)
        assertEquals(originalError, chained.errorOrNull())
    }

    // ==================== fold テスト ====================

    @Test
    fun `fold applies onSuccess for Success`() {
        val result: Result<Int, DomainError> = Result.Success(5)

        val folded = result.fold(
            onSuccess = { "Value: $it" },
            onFailure = { "Error: ${it.message}" }
        )

        assertEquals("Value: 5", folded)
    }

    @Test
    fun `fold applies onFailure for Failure`() {
        val result: Result<Int, DomainError> = Result.Failure(
            DomainError.NotFoundError("Not found")
        )

        val folded = result.fold(
            onSuccess = { "Value: $it" },
            onFailure = { "Error: ${it.message}" }
        )

        assertEquals("Error: Not found", folded)
    }

    // ==================== onSuccess/onFailure テスト ====================

    @Test
    fun `onSuccess executes action for Success`() {
        var executed = false
        var capturedValue: Int? = null
        val result: Result<Int, DomainError> = Result.Success(42)

        val returned = result.onSuccess {
            executed = true
            capturedValue = it
        }

        assertTrue(executed)
        assertEquals(42, capturedValue)
        assertSame(result, returned)
    }

    @Test
    fun `onSuccess does not execute for Failure`() {
        var executed = false
        val result: Result<Int, DomainError> = Result.Failure(
            DomainError.DatabaseError("Error")
        )

        result.onSuccess { executed = true }

        assertFalse(executed)
    }

    @Test
    fun `onFailure executes action for Failure`() {
        var executed = false
        var capturedError: DomainError? = null
        val error = DomainError.ValidationError("Invalid")
        val result: Result<Int, DomainError> = Result.Failure(error)

        val returned = result.onFailure {
            executed = true
            capturedError = it
        }

        assertTrue(executed)
        assertEquals(error, capturedError)
        assertSame(result, returned)
    }

    @Test
    fun `onFailure does not execute for Success`() {
        var executed = false
        val result: Result<Int, DomainError> = Result.Success(42)

        result.onFailure { executed = true }

        assertFalse(executed)
    }

    // ==================== mapError テスト ====================

    @Test
    fun `mapError transforms Failure error`() {
        val result: Result<Int, DomainError> = Result.Failure(
            DomainError.NotFoundError("Item not found")
        )

        val mapped = result.mapError {
            DomainError.ValidationError("Mapped: ${it.message}")
        }

        assertTrue(mapped.isFailure)
        val error = mapped.errorOrNull()
        assertTrue(error is DomainError.ValidationError)
        assertEquals("Mapped: Item not found", error?.message)
    }

    @Test
    fun `mapError preserves Success`() {
        val result: Result<Int, DomainError> = Result.Success(42)

        val mapped = result.mapError {
            DomainError.ValidationError("Should not be called")
        }

        assertTrue(mapped.isSuccess)
        assertEquals(42, mapped.getOrNull())
    }

    // ==================== recover テスト ====================

    @Test
    fun `recover provides fallback value for Failure`() {
        val result: Result<Int, DomainError> = Result.Failure(
            DomainError.NotFoundError("Not found")
        )

        val recovered = result.recover { 0 }

        assertTrue(recovered.isSuccess)
        assertEquals(0, recovered.getOrNull())
    }

    @Test
    fun `recover preserves Success value`() {
        val result: Result<Int, DomainError> = Result.Success(42)

        val recovered = result.recover { 0 }

        assertTrue(recovered.isSuccess)
        assertEquals(42, recovered.getOrNull())
    }
}
