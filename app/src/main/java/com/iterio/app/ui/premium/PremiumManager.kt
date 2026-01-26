package com.iterio.app.ui.premium

import com.iterio.app.config.AppConfig
import com.iterio.app.domain.model.PremiumFeature
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumManager @Inject constructor(
    private val premiumRepository: PremiumRepository
) {
    val subscriptionStatus: Flow<SubscriptionStatus> = premiumRepository.subscriptionStatus

    suspend fun isPremium(): Boolean =
        premiumRepository.getSubscriptionStatus().getOrDefault(SubscriptionStatus()).isPremium

    suspend fun getSubscriptionStatus(): SubscriptionStatus =
        premiumRepository.getSubscriptionStatus().getOrDefault(SubscriptionStatus())

    suspend fun canAccessFeature(feature: PremiumFeature): Boolean {
        return premiumRepository.canAccessFeature(feature).getOrDefault(false)
    }

    suspend fun startTrial() {
        premiumRepository.startTrial()
    }

    suspend fun markTrialOfferSeen() {
        premiumRepository.markTrialOfferSeen()
    }

    /**
     * 復習間隔を取得
     * @param isPremium プレミアムかどうか
     * @param reviewCount 復習回数（nullの場合はプランのデフォルト）
     * @return 復習間隔のリスト（日数）
     */
    fun getReviewIntervals(isPremium: Boolean, reviewCount: Int? = null): List<Int> {
        val effectiveCount = reviewCount ?: getDefaultReviewCount(isPremium)
        // プランダウングレード時は無料版の最大回数に制限
        val limitedCount = if (isPremium) {
            effectiveCount
        } else {
            effectiveCount.coerceAtMost(AppConfig.Premium.DEFAULT_REVIEW_COUNT_FREE)
        }
        return AppConfig.Premium.getIntervalsForCount(limitedCount, isPremium)
    }

    /**
     * 復習回数のオプションを取得
     * @param isPremium プレミアムかどうか
     * @return 選択可能な復習回数のリスト
     */
    fun getReviewCountOptions(isPremium: Boolean): List<Int> {
        return if (isPremium) {
            AppConfig.Premium.PREMIUM_REVIEW_COUNT_OPTIONS
        } else {
            AppConfig.Premium.FREE_REVIEW_COUNT_OPTIONS
        }
    }

    /**
     * デフォルトの復習回数を取得
     * @param isPremium プレミアムかどうか
     * @return デフォルト復習回数
     */
    fun getDefaultReviewCount(isPremium: Boolean): Int {
        return if (isPremium) {
            AppConfig.Premium.DEFAULT_REVIEW_COUNT_PREMIUM
        } else {
            AppConfig.Premium.DEFAULT_REVIEW_COUNT_FREE
        }
    }

    /**
     * 復習回数の最大値を取得
     * @param isPremium プレミアムかどうか
     * @return 最大復習回数
     */
    fun getMaxReviewCount(isPremium: Boolean): Int {
        return if (isPremium) {
            AppConfig.Premium.MAX_REVIEW_COUNT_PREMIUM
        } else {
            AppConfig.Premium.MAX_REVIEW_COUNT_FREE
        }
    }

    companion object {
        val PREMIUM_REVIEW_INTERVALS = AppConfig.Premium.PREMIUM_REVIEW_INTERVALS
        val FREE_REVIEW_INTERVALS = AppConfig.Premium.FREE_REVIEW_INTERVALS
    }
}
