package com.iterio.app.fakes

import com.iterio.app.config.AppConfig
import com.iterio.app.domain.model.PremiumFeature
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.model.SubscriptionType
import com.iterio.app.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime

/**
 * テスト用の PremiumRepository 実装
 */
class FakePremiumRepository : PremiumRepository {

    private val _subscriptionStatus = MutableStateFlow(SubscriptionStatus())

    override val subscriptionStatus: Flow<SubscriptionStatus> = _subscriptionStatus

    override suspend fun getSubscriptionStatus(): SubscriptionStatus = _subscriptionStatus.value

    override suspend fun startTrial() {
        val now = LocalDateTime.now()
        _subscriptionStatus.value = _subscriptionStatus.value.copy(
            trialStartedAt = now,
            trialExpiresAt = now.plusDays(AppConfig.Premium.TRIAL_DURATION_DAYS),
            isTrialUsed = true,
            hasSeenTrialOffer = true
        )
    }

    override suspend fun updateSubscription(type: SubscriptionType, expiresAt: LocalDateTime?) {
        _subscriptionStatus.value = _subscriptionStatus.value.copy(
            type = type,
            expiresAt = expiresAt
        )
    }

    override suspend fun canAccessFeature(feature: PremiumFeature): Boolean {
        return _subscriptionStatus.value.isPremium
    }

    override suspend fun markTrialOfferSeen() {
        _subscriptionStatus.value = _subscriptionStatus.value.copy(
            hasSeenTrialOffer = true
        )
    }

    override suspend fun clearSubscription() {
        _subscriptionStatus.value = SubscriptionStatus()
    }

    // Test helpers
    fun reset() {
        _subscriptionStatus.value = SubscriptionStatus()
    }
}
