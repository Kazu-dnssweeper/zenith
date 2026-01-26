package com.iterio.app.domain.repository

import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PremiumFeature
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.model.SubscriptionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface PremiumRepository {
    val subscriptionStatus: Flow<SubscriptionStatus>

    suspend fun getSubscriptionStatus(): Result<SubscriptionStatus, DomainError>
    suspend fun startTrial(): Result<Unit, DomainError>
    suspend fun updateSubscription(type: SubscriptionType, expiresAt: LocalDateTime?): Result<Unit, DomainError>
    suspend fun canAccessFeature(feature: PremiumFeature): Result<Boolean, DomainError>
    suspend fun markTrialOfferSeen(): Result<Unit, DomainError>
    suspend fun clearSubscription(): Result<Unit, DomainError> // デバッグ用
}
