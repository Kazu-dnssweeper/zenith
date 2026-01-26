package com.iterio.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PremiumFeature
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.model.SubscriptionType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * PremiumRepositoryImpl のユニットテスト
 *
 * DataStore.edit は拡張関数のためモックが困難。
 * このテストでは subscriptionStatus の Flow と SubscriptionStatus ドメインモデルのロジックをテストする。
 */
class PremiumRepositoryImplTest {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // Preference keys (matching the implementation)
    private val keySubscriptionType = stringPreferencesKey("subscription_type")
    private val keyExpiresAt = stringPreferencesKey("expires_at")
    private val keyTrialStartedAt = stringPreferencesKey("trial_started_at")
    private val keyTrialExpiresAt = stringPreferencesKey("trial_expires_at")
    private val keyIsTrialUsed = booleanPreferencesKey("is_trial_used")
    private val keyHasSeenTrialOffer = booleanPreferencesKey("has_seen_trial_offer")

    private fun createRepository(prefs: Preferences): PremiumRepositoryImpl {
        val dataStore = mockk<DataStore<Preferences>>()
        every { dataStore.data } returns flowOf(prefs)
        return PremiumRepositoryImpl(dataStore)
    }

    private fun createPreferences(
        subscriptionType: String? = null,
        expiresAt: LocalDateTime? = null,
        trialStartedAt: LocalDateTime? = null,
        trialExpiresAt: LocalDateTime? = null,
        isTrialUsed: Boolean? = null,
        hasSeenTrialOffer: Boolean? = null
    ): Preferences {
        val prefs = mockk<Preferences>()
        every { prefs[keySubscriptionType] } returns subscriptionType
        every { prefs[keyExpiresAt] } returns expiresAt?.format(formatter)
        every { prefs[keyTrialStartedAt] } returns trialStartedAt?.format(formatter)
        every { prefs[keyTrialExpiresAt] } returns trialExpiresAt?.format(formatter)
        every { prefs[keyIsTrialUsed] } returns isTrialUsed
        every { prefs[keyHasSeenTrialOffer] } returns hasSeenTrialOffer
        return prefs
    }

    // ==================== subscriptionStatus Flow Tests ====================

    @Test
    fun `subscriptionStatus returns FREE by default`() = runTest {
        val prefs = createPreferences()
        val repository = createRepository(prefs)

        repository.subscriptionStatus.test {
            val status = awaitItem()
            assertEquals(SubscriptionType.FREE, status.type)
            assertFalse(status.isPremium)
            assertFalse(status.isTrialUsed)
            assertFalse(status.hasSeenTrialOffer)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subscriptionStatus returns MONTHLY when set`() = runTest {
        val expiresAt = LocalDateTime.now().plusDays(30)
        val prefs = createPreferences(
            subscriptionType = "MONTHLY",
            expiresAt = expiresAt
        )
        val repository = createRepository(prefs)

        repository.subscriptionStatus.test {
            val status = awaitItem()
            assertEquals(SubscriptionType.MONTHLY, status.type)
            assertTrue(status.isPremium)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subscriptionStatus returns QUARTERLY when set`() = runTest {
        val expiresAt = LocalDateTime.now().plusDays(90)
        val prefs = createPreferences(
            subscriptionType = "QUARTERLY",
            expiresAt = expiresAt
        )
        val repository = createRepository(prefs)

        repository.subscriptionStatus.test {
            val status = awaitItem()
            assertEquals(SubscriptionType.QUARTERLY, status.type)
            assertTrue(status.isPremium)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subscriptionStatus returns HALF_YEARLY when set`() = runTest {
        val expiresAt = LocalDateTime.now().plusDays(180)
        val prefs = createPreferences(
            subscriptionType = "HALF_YEARLY",
            expiresAt = expiresAt
        )
        val repository = createRepository(prefs)

        repository.subscriptionStatus.test {
            val status = awaitItem()
            assertEquals(SubscriptionType.HALF_YEARLY, status.type)
            assertTrue(status.isPremium)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subscriptionStatus returns YEARLY when set`() = runTest {
        val expiresAt = LocalDateTime.now().plusDays(365)
        val prefs = createPreferences(
            subscriptionType = "YEARLY",
            expiresAt = expiresAt
        )
        val repository = createRepository(prefs)

        repository.subscriptionStatus.test {
            val status = awaitItem()
            assertEquals(SubscriptionType.YEARLY, status.type)
            assertTrue(status.isPremium)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subscriptionStatus returns expired when past expiry date`() = runTest {
        val expiresAt = LocalDateTime.now().minusDays(1)
        val prefs = createPreferences(
            subscriptionType = "MONTHLY",
            expiresAt = expiresAt
        )
        val repository = createRepository(prefs)

        repository.subscriptionStatus.test {
            val status = awaitItem()
            assertEquals(SubscriptionType.MONTHLY, status.type)
            assertFalse(status.isPremium) // Expired
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subscriptionStatus returns LIFETIME always premium`() = runTest {
        val prefs = createPreferences(subscriptionType = "LIFETIME")
        val repository = createRepository(prefs)

        repository.subscriptionStatus.test {
            val status = awaitItem()
            assertEquals(SubscriptionType.LIFETIME, status.type)
            assertTrue(status.isPremium)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subscriptionStatus LIFETIME is premium even with null expiry`() = runTest {
        val prefs = createPreferences(
            subscriptionType = "LIFETIME",
            expiresAt = null
        )
        val repository = createRepository(prefs)

        repository.subscriptionStatus.test {
            val status = awaitItem()
            assertTrue(status.isPremium)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subscriptionStatus handles active trial`() = runTest {
        val trialStart = LocalDateTime.now().minusDays(3)
        val trialExpires = LocalDateTime.now().plusDays(4)
        val prefs = createPreferences(
            subscriptionType = "FREE",
            trialStartedAt = trialStart,
            trialExpiresAt = trialExpires,
            isTrialUsed = true,
            hasSeenTrialOffer = true
        )
        val repository = createRepository(prefs)

        repository.subscriptionStatus.test {
            val status = awaitItem()
            assertEquals(SubscriptionType.FREE, status.type)
            assertTrue(status.isPremium) // In trial
            assertTrue(status.isInTrialPeriod)
            assertTrue(status.isTrialUsed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subscriptionStatus handles expired trial`() = runTest {
        val trialStart = LocalDateTime.now().minusDays(10)
        val trialExpires = LocalDateTime.now().minusDays(3)
        val prefs = createPreferences(
            subscriptionType = "FREE",
            trialStartedAt = trialStart,
            trialExpiresAt = trialExpires,
            isTrialUsed = true,
            hasSeenTrialOffer = true
        )
        val repository = createRepository(prefs)

        repository.subscriptionStatus.test {
            val status = awaitItem()
            assertEquals(SubscriptionType.FREE, status.type)
            assertFalse(status.isPremium) // Trial expired
            assertFalse(status.isInTrialPeriod)
            assertTrue(status.isTrialUsed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subscriptionStatus handles unknown type gracefully`() = runTest {
        val prefs = createPreferences(subscriptionType = "UNKNOWN_TYPE")
        val repository = createRepository(prefs)

        repository.subscriptionStatus.test {
            val status = awaitItem()
            assertEquals(SubscriptionType.FREE, status.type) // Defaults to FREE
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getSubscriptionStatus returns current status`() = runTest {
        val prefs = createPreferences(
            subscriptionType = "YEARLY",
            expiresAt = LocalDateTime.now().plusDays(365)
        )
        val repository = createRepository(prefs)

        val result = repository.getSubscriptionStatus()

        assertTrue(result is Result.Success)
        val status = (result as Result.Success).value
        assertEquals(SubscriptionType.YEARLY, status.type)
        assertTrue(status.isPremium)
    }

    @Test
    fun `canAccessFeature returns true when premium`() = runTest {
        val prefs = createPreferences(subscriptionType = "LIFETIME")
        val repository = createRepository(prefs)

        val result = repository.canAccessFeature(PremiumFeature.BACKUP)

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).value)
    }

    @Test
    fun `canAccessFeature returns false when not premium`() = runTest {
        val prefs = createPreferences(subscriptionType = "FREE")
        val repository = createRepository(prefs)

        val result = repository.canAccessFeature(PremiumFeature.BACKUP)

        assertTrue(result is Result.Success)
        assertFalse((result as Result.Success).value)
    }

    @Test
    fun `canAccessFeature returns true during active trial`() = runTest {
        val prefs = createPreferences(
            subscriptionType = "FREE",
            trialExpiresAt = LocalDateTime.now().plusDays(3),
            isTrialUsed = true
        )
        val repository = createRepository(prefs)

        val result = repository.canAccessFeature(PremiumFeature.BGM)

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).value)
    }

    // ==================== SubscriptionStatus Domain Model Tests ====================

    @Test
    fun `SubscriptionStatus canStartTrial is true when free and unused`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.FREE,
            isTrialUsed = false
        )

        assertTrue(status.canStartTrial)
    }

    @Test
    fun `SubscriptionStatus canStartTrial is false when trial used`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.FREE,
            isTrialUsed = true
        )

        assertFalse(status.canStartTrial)
    }

    @Test
    fun `SubscriptionStatus canStartTrial is false when premium`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.MONTHLY,
            expiresAt = LocalDateTime.now().plusDays(30),
            isTrialUsed = false
        )

        assertFalse(status.canStartTrial)
    }

    @Test
    fun `SubscriptionStatus daysRemainingInTrial calculates correctly`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.FREE,
            trialExpiresAt = LocalDateTime.now().plusDays(5),
            isTrialUsed = true
        )

        assertTrue(status.daysRemainingInTrial >= 4) // At least 4 days
        assertTrue(status.daysRemainingInTrial <= 5) // At most 5 days
    }

    @Test
    fun `SubscriptionStatus daysRemainingInTrial returns 0 when no trial`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.FREE,
            trialExpiresAt = null,
            isTrialUsed = false
        )

        assertEquals(0, status.daysRemainingInTrial)
    }

    @Test
    fun `SubscriptionStatus daysRemainingInTrial returns 0 when trial expired`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.FREE,
            trialExpiresAt = LocalDateTime.now().minusDays(2),
            isTrialUsed = true
        )

        assertEquals(0, status.daysRemainingInTrial)
    }

    @Test
    fun `SubscriptionStatus isInTrialPeriod is true when trial active`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.FREE,
            trialExpiresAt = LocalDateTime.now().plusDays(3),
            isTrialUsed = true
        )

        assertTrue(status.isInTrialPeriod)
    }

    @Test
    fun `SubscriptionStatus isInTrialPeriod is false when trial expired`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.FREE,
            trialExpiresAt = LocalDateTime.now().minusDays(1),
            isTrialUsed = true
        )

        assertFalse(status.isInTrialPeriod)
    }

    @Test
    fun `SubscriptionStatus isInTrialPeriod is false when no trial`() {
        val status = SubscriptionStatus(
            type = SubscriptionType.FREE,
            trialExpiresAt = null,
            isTrialUsed = false
        )

        assertFalse(status.isInTrialPeriod)
    }
}
