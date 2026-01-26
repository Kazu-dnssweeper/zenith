package com.iterio.app.fakes

import com.iterio.app.config.AppConfig
import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
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

    override suspend fun getSubscriptionStatus(): Result<SubscriptionStatus, DomainError> =
        Result.Success(_subscriptionStatus.value)

    override suspend fun startTrial(): Result<Unit, DomainError> {
        val now = LocalDateTime.now()
        _subscriptionStatus.value = _subscriptionStatus.value.copy(
            trialStartedAt = now,
            trialExpiresAt = now.plusDays(AppConfig.Premium.TRIAL_DURATION_DAYS),
            isTrialUsed = true,
            hasSeenTrialOffer = true
        )
        return Result.Success(Unit)
    }

    override suspend fun updateSubscription(type: SubscriptionType, expiresAt: LocalDateTime?): Result<Unit, DomainError> {
        _subscriptionStatus.value = _subscriptionStatus.value.copy(
            type = type,
            expiresAt = expiresAt
        )
        return Result.Success(Unit)
    }

    override suspend fun canAccessFeature(feature: PremiumFeature): Result<Boolean, DomainError> {
        return Result.Success(_subscriptionStatus.value.isPremium)
    }

    override suspend fun markTrialOfferSeen(): Result<Unit, DomainError> {
        _subscriptionStatus.value = _subscriptionStatus.value.copy(
            hasSeenTrialOffer = true
        )
        return Result.Success(Unit)
    }

    override suspend fun clearSubscription(): Result<Unit, DomainError> {
        _subscriptionStatus.value = SubscriptionStatus()
        return Result.Success(Unit)
    }

    // Test helpers
    fun reset() {
        _subscriptionStatus.value = SubscriptionStatus()
    }
}
