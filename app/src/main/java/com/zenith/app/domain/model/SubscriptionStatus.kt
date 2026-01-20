package com.zenith.app.domain.model

import java.time.Duration
import java.time.LocalDateTime

enum class SubscriptionType {
    FREE,
    MONTHLY,      // 月額 ¥480
    QUARTERLY,    // 3ヶ月 ¥1,200
    HALF_YEARLY,  // 6ヶ月 ¥2,000
    YEARLY,       // 年額 ¥3,000
    LIFETIME      // 買い切り ¥4,000
}

data class SubscriptionStatus(
    val type: SubscriptionType = SubscriptionType.FREE,
    val expiresAt: LocalDateTime? = null,
    val trialStartedAt: LocalDateTime? = null,
    val trialExpiresAt: LocalDateTime? = null,
    val isTrialUsed: Boolean = false,
    val hasSeenTrialOffer: Boolean = false
) {
    val isPremium: Boolean
        get() = when (type) {
            SubscriptionType.FREE -> isInTrialPeriod
            SubscriptionType.LIFETIME -> true
            else -> expiresAt?.isAfter(LocalDateTime.now()) == true
        }

    val isInTrialPeriod: Boolean
        get() = trialExpiresAt?.isAfter(LocalDateTime.now()) == true

    val daysRemainingInTrial: Int
        get() = if (isInTrialPeriod && trialExpiresAt != null) {
            Duration.between(LocalDateTime.now(), trialExpiresAt).toDays().toInt().coerceAtLeast(0)
        } else 0

    val canStartTrial: Boolean
        get() = !isTrialUsed && type == SubscriptionType.FREE
}
