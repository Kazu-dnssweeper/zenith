package com.iterio.app.fakes

import com.iterio.app.config.AppConfig
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PremiumFeature
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.model.SubscriptionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * FakePremiumRepository のテスト
 */
class FakePremiumRepositoryTest {

    private lateinit var repository: FakePremiumRepository

    @Before
    fun setup() {
        repository = FakePremiumRepository()
    }

    // Initial State Tests

    @Test
    fun `getSubscriptionStatus returns free status initially`() = runTest {
        val status = (repository.getSubscriptionStatus() as Result.Success).value

        assertEquals(SubscriptionType.FREE, status.type)
        assertFalse(status.isPremium)
    }

    @Test
    fun `subscriptionStatus flow emits initial state`() = runTest {
        val status = repository.subscriptionStatus.first()

        assertEquals(SubscriptionType.FREE, status.type)
    }

    // Trial Tests

    @Test
    fun `startTrial enables trial period`() = runTest {
        repository.startTrial()

        val status = (repository.getSubscriptionStatus() as Result.Success).value

        assertTrue(status.isInTrialPeriod)
        assertTrue(status.isPremium)
        assertNotNull(status.trialExpiresAt)
    }

    @Test
    fun `startTrial sets correct expiration`() = runTest {
        val beforeStart = LocalDateTime.now()

        repository.startTrial()

        val status = (repository.getSubscriptionStatus() as Result.Success).value
        val expectedExpiration = beforeStart.plusDays(AppConfig.Premium.TRIAL_DURATION_DAYS)

        assertNotNull(status.trialExpiresAt)
        // Allow 1 minute tolerance
        assertTrue(status.trialExpiresAt!! >= expectedExpiration.minusMinutes(1))
        assertTrue(status.trialExpiresAt!! <= expectedExpiration.plusMinutes(1))
    }

    @Test
    fun `startTrial marks trial as used`() = runTest {
        val beforeStart = (repository.getSubscriptionStatus() as Result.Success).value
        assertFalse(beforeStart.isTrialUsed)

        repository.startTrial()

        val afterStart = (repository.getSubscriptionStatus() as Result.Success).value
        assertTrue(afterStart.isTrialUsed)
    }

    @Test
    fun `startTrial marks trial offer as seen`() = runTest {
        val beforeStart = (repository.getSubscriptionStatus() as Result.Success).value
        assertFalse(beforeStart.hasSeenTrialOffer)

        repository.startTrial()

        val afterStart = (repository.getSubscriptionStatus() as Result.Success).value
        assertTrue(afterStart.hasSeenTrialOffer)
    }

    // Update Subscription Tests

    @Test
    fun `updateSubscription changes to monthly premium`() = runTest {
        val expiresAt = LocalDateTime.now().plusMonths(1)

        repository.updateSubscription(SubscriptionType.MONTHLY, expiresAt)

        val status = (repository.getSubscriptionStatus() as Result.Success).value
        assertEquals(SubscriptionType.MONTHLY, status.type)
        assertTrue(status.isPremium)
        assertEquals(expiresAt, status.expiresAt)
    }

    @Test
    fun `updateSubscription to yearly`() = runTest {
        val expiresAt = LocalDateTime.now().plusYears(1)

        repository.updateSubscription(SubscriptionType.YEARLY, expiresAt)

        val status = (repository.getSubscriptionStatus() as Result.Success).value
        assertEquals(SubscriptionType.YEARLY, status.type)
        assertTrue(status.isPremium)
    }

    @Test
    fun `updateSubscription to lifetime`() = runTest {
        repository.updateSubscription(SubscriptionType.LIFETIME, null)

        val status = (repository.getSubscriptionStatus() as Result.Success).value
        assertEquals(SubscriptionType.LIFETIME, status.type)
        assertTrue(status.isPremium)
        assertNull(status.expiresAt)
    }

    @Test
    fun `subscriptionStatus flow emits updates`() = runTest {
        val initial = repository.subscriptionStatus.first()
        assertEquals(SubscriptionType.FREE, initial.type)

        repository.startTrial()

        val updated = repository.subscriptionStatus.first()
        assertTrue(updated.isInTrialPeriod)
    }

    // Feature Access Tests

    @Test
    fun `canAccessFeature returns false for free user on premium features`() = runTest {
        val canAccess = (repository.canAccessFeature(PremiumFeature.COMPLETE_LOCK_MODE) as Result.Success).value
        assertFalse(canAccess)
    }

    @Test
    fun `canAccessFeature returns true for trial user on premium features`() = runTest {
        repository.startTrial()

        val canAccess = (repository.canAccessFeature(PremiumFeature.COMPLETE_LOCK_MODE) as Result.Success).value
        assertTrue(canAccess)
    }

    @Test
    fun `canAccessFeature returns true for premium user`() = runTest {
        repository.updateSubscription(SubscriptionType.MONTHLY, LocalDateTime.now().plusMonths(1))

        val canAccessAutoLoop = (repository.canAccessFeature(PremiumFeature.TIMER_AUTO_LOOP) as Result.Success).value
        val canAccessStrictMode = (repository.canAccessFeature(PremiumFeature.COMPLETE_LOCK_MODE) as Result.Success).value
        val canAccessCloudBackup = (repository.canAccessFeature(PremiumFeature.BACKUP) as Result.Success).value

        assertTrue(canAccessAutoLoop)
        assertTrue(canAccessStrictMode)
        assertTrue(canAccessCloudBackup)
    }

    @Test
    fun `canAccessFeature checks all premium features`() = runTest {
        repository.updateSubscription(SubscriptionType.MONTHLY, LocalDateTime.now().plusMonths(1))

        PremiumFeature.entries.forEach { feature ->
            val canAccess = (repository.canAccessFeature(feature) as Result.Success).value
            assertTrue("Should access $feature when premium", canAccess)
        }
    }

    // Trial Offer Seen Tests

    @Test
    fun `markTrialOfferSeen updates flag`() = runTest {
        val before = (repository.getSubscriptionStatus() as Result.Success).value
        assertFalse(before.hasSeenTrialOffer)

        repository.markTrialOfferSeen()

        val after = (repository.getSubscriptionStatus() as Result.Success).value
        assertTrue(after.hasSeenTrialOffer)
    }

    @Test
    fun `markTrialOfferSeen does not change subscription type`() = runTest {
        repository.markTrialOfferSeen()

        val status = (repository.getSubscriptionStatus() as Result.Success).value
        assertEquals(SubscriptionType.FREE, status.type)
    }

    // Clear Subscription Tests

    @Test
    fun `clearSubscription resets to free`() = runTest {
        repository.startTrial()
        repository.clearSubscription()

        val status = (repository.getSubscriptionStatus() as Result.Success).value
        assertEquals(SubscriptionType.FREE, status.type)
        assertFalse(status.isPremium)
        assertNull(status.expiresAt)
        assertNull(status.trialExpiresAt)
    }

    @Test
    fun `clearSubscription resets trial offer flag`() = runTest {
        repository.markTrialOfferSeen()
        repository.clearSubscription()

        val status = (repository.getSubscriptionStatus() as Result.Success).value
        assertFalse(status.hasSeenTrialOffer)
    }

    @Test
    fun `clearSubscription resets isTrialUsed`() = runTest {
        repository.startTrial()
        repository.clearSubscription()

        val status = (repository.getSubscriptionStatus() as Result.Success).value
        assertFalse(status.isTrialUsed)
    }

    // Edge Cases

    @Test
    fun `expired subscription is not premium`() = runTest {
        val expiredAt = LocalDateTime.now().minusDays(1)
        repository.updateSubscription(SubscriptionType.MONTHLY, expiredAt)

        val status = (repository.getSubscriptionStatus() as Result.Success).value
        assertEquals(SubscriptionType.MONTHLY, status.type)
        assertFalse(status.isPremium) // Expired
    }

    @Test
    fun `lifetime subscription does not expire`() = runTest {
        repository.updateSubscription(SubscriptionType.LIFETIME, null)

        val status = (repository.getSubscriptionStatus() as Result.Success).value
        assertTrue(status.isPremium)
    }
}
