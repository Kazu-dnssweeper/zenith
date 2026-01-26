package com.iterio.app.domain.repository

import com.iterio.app.domain.model.PremiumFeature
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.model.SubscriptionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface PremiumRepository {
    val subscriptionStatus: Flow<SubscriptionStatus>

    suspend fun getSubscriptionStatus(): SubscriptionStatus
    suspend fun startTrial()
    suspend fun updateSubscription(type: SubscriptionType, expiresAt: LocalDateTime?)
    suspend fun canAccessFeature(feature: PremiumFeature): Boolean
    suspend fun markTrialOfferSeen()
    suspend fun clearSubscription() // デバッグ用
}
