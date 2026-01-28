package com.iterio.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * SubscriptionStatus の計算プロパティテスト
 */
class SubscriptionStatusTest {

    // ==================== isPremium テスト ====================

    @Test
    fun `isPremium returns true for LIFETIME type`() {
        val status = SubscriptionStatus(type = SubscriptionType.LIFETIME)
        assertTrue(status.isPremium)
    }

    @Test
    fun `isPremium returns true for FREE type during trial period`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.FREE,
            trialExpiresAt = LocalDateTime.now().plusDays(3)
        )
        assertTrue(status.isPremium)
    }

    @Test
    fun `isPremium returns false for FREE type without trial`() {
        val status = SubscriptionStatus(type = SubscriptionType.FREE)
        assertFalse(status.isPremium)
    }

    @Test
    fun `isPremium returns false for FREE type with expired trial`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.FREE,
            trialExpiresAt = LocalDateTime.now().minusDays(1)
        )
        assertFalse(status.isPremium)
    }

    @Test
    fun `isPremium returns true for MONTHLY with future expiration`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.MONTHLY,
            expiresAt = LocalDateTime.now().plusMonths(1)
        )
        assertTrue(status.isPremium)
    }

    @Test
    fun `isPremium returns false for MONTHLY with past expiration`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.MONTHLY,
            expiresAt = LocalDateTime.now().minusDays(1)
        )
        assertFalse(status.isPremium)
    }

    @Test
    fun `isPremium returns false for MONTHLY with null expiration`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.MONTHLY,
            expiresAt = null
        )
        assertFalse(status.isPremium)
    }

    @Test
    fun `isPremium returns true for YEARLY with future expiration`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.YEARLY,
            expiresAt = LocalDateTime.now().plusYears(1)
        )
        assertTrue(status.isPremium)
    }

    @Test
    fun `isPremium returns true for QUARTERLY with future expiration`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.QUARTERLY,
            expiresAt = LocalDateTime.now().plusMonths(3)
        )
        assertTrue(status.isPremium)
    }

    @Test
    fun `isPremium returns true for HALF_YEARLY with future expiration`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.HALF_YEARLY,
            expiresAt = LocalDateTime.now().plusMonths(6)
        )
        assertTrue(status.isPremium)
    }

    // ==================== isInTrialPeriod テスト ====================

    @Test
    fun `isInTrialPeriod returns true when trialExpiresAt is in future`() {
        val status = SubscriptionStatus(
            trialExpiresAt = LocalDateTime.now().plusDays(5)
        )
        assertTrue(status.isInTrialPeriod)
    }

    @Test
    fun `isInTrialPeriod returns false when trialExpiresAt is in past`() {
        val status = SubscriptionStatus(
            trialExpiresAt = LocalDateTime.now().minusDays(1)
        )
        assertFalse(status.isInTrialPeriod)
    }

    @Test
    fun `isInTrialPeriod returns false when trialExpiresAt is null`() {
        val status = SubscriptionStatus(trialExpiresAt = null)
        assertFalse(status.isInTrialPeriod)
    }

    // ==================== daysRemainingInTrial テスト ====================

    @Test
    fun `daysRemainingInTrial returns correct days when in trial`() {
        val status = SubscriptionStatus(
            trialExpiresAt = LocalDateTime.now().plusDays(5)
        )
        // Allow for slight timing variance - should be 4 or 5
        assertTrue(status.daysRemainingInTrial in 4..5)
    }

    @Test
    fun `daysRemainingInTrial returns 0 when trial expired`() {
        val status = SubscriptionStatus(
            trialExpiresAt = LocalDateTime.now().minusDays(1)
        )
        assertEquals(0, status.daysRemainingInTrial)
    }

    @Test
    fun `daysRemainingInTrial returns 0 when trialExpiresAt is null`() {
        val status = SubscriptionStatus(trialExpiresAt = null)
        assertEquals(0, status.daysRemainingInTrial)
    }

    // ==================== canStartTrial テスト ====================

    @Test
    fun `canStartTrial returns true when trial unused and type is FREE`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.FREE,
            isTrialUsed = false
        )
        assertTrue(status.canStartTrial)
    }

    @Test
    fun `canStartTrial returns false when trial already used`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.FREE,
            isTrialUsed = true
        )
        assertFalse(status.canStartTrial)
    }

    @Test
    fun `canStartTrial returns false when type is not FREE`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.MONTHLY,
            isTrialUsed = false
        )
        assertFalse(status.canStartTrial)
    }

    @Test
    fun `canStartTrial returns false when type is LIFETIME`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.LIFETIME,
            isTrialUsed = false
        )
        assertFalse(status.canStartTrial)
    }

    // ==================== デフォルト値テスト ====================

    @Test
    fun `default SubscriptionStatus has FREE type`() {
        val status = SubscriptionStatus()
        assertEquals(SubscriptionType.FREE, status.type)
        assertFalse(status.isPremium)
        assertFalse(status.isInTrialPeriod)
        assertFalse(status.isTrialUsed)
        assertFalse(status.hasSeenTrialOffer)
    }
}
