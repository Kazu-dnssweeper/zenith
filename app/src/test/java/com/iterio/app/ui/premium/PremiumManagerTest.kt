package com.iterio.app.ui.premium

import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PremiumFeature
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.model.SubscriptionType
import com.iterio.app.domain.repository.PremiumRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * PremiumManager のユニットテスト
 */
class PremiumManagerTest {

    private lateinit var premiumRepository: PremiumRepository
    private lateinit var premiumManager: PremiumManager

    @Before
    fun setup() {
        premiumRepository = mockk(relaxed = true)
        // subscriptionStatus Flowのモック設定
        every { premiumRepository.subscriptionStatus } returns flowOf(SubscriptionStatus())
        premiumManager = PremiumManager(premiumRepository)
    }

    // getReviewIntervals のテスト

    @Test
    fun `getReviewIntervals returns full intervals for premium`() {
        val intervals = premiumManager.getReviewIntervals(isPremium = true)

        assertEquals(
            "Premiumユーザーは全6回の復習間隔を取得すべき",
            listOf(1, 3, 7, 14, 30, 60),
            intervals
        )
    }

    @Test
    fun `getReviewIntervals returns limited intervals for free`() {
        val intervals = premiumManager.getReviewIntervals(isPremium = false)

        assertEquals(
            "無料ユーザーは2回の復習間隔のみ取得すべき",
            listOf(1, 3),
            intervals
        )
    }

    @Test
    fun `getReviewIntervals premium intervals contain all expected values`() {
        val intervals = premiumManager.getReviewIntervals(isPremium = true)

        assertEquals("6回の復習間隔がある", 6, intervals.size)
        assertTrue("1日後が含まれる", intervals.contains(1))
        assertTrue("3日後が含まれる", intervals.contains(3))
        assertTrue("7日後が含まれる", intervals.contains(7))
        assertTrue("14日後が含まれる", intervals.contains(14))
        assertTrue("30日後が含まれる", intervals.contains(30))
        assertTrue("60日後が含まれる", intervals.contains(60))
    }

    // canAccessFeature のテスト

    @Test
    fun `canAccessFeature returns true for premium user`() = runBlocking {
        // Arrange: Premiumユーザー
        coEvery { premiumRepository.canAccessFeature(any()) } returns Result.Success(true)

        // Act
        val result = premiumManager.canAccessFeature(PremiumFeature.BACKUP)

        // Assert
        assertTrue("Premiumユーザーはバックアップ機能にアクセスできるべき", result)
    }

    @Test
    fun `canAccessFeature returns false for free user on premium feature`() = runBlocking {
        // Arrange: 無料ユーザー
        coEvery { premiumRepository.canAccessFeature(any()) } returns Result.Success(false)

        // Act
        val result = premiumManager.canAccessFeature(PremiumFeature.BACKUP)

        // Assert
        assertFalse("無料ユーザーはバックアップ機能にアクセスできないべき", result)
    }

    @Test
    fun `canAccessFeature checks correct feature`() = runBlocking {
        // Arrange
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BGM) } returns Result.Success(true)
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.COMPLETE_LOCK_MODE) } returns Result.Success(false)

        // Act & Assert
        assertTrue(
            "BGM機能へのアクセスはtrueを返すべき",
            premiumManager.canAccessFeature(PremiumFeature.BGM)
        )
        assertFalse(
            "完全ロックモードへのアクセスはfalseを返すべき",
            premiumManager.canAccessFeature(PremiumFeature.COMPLETE_LOCK_MODE)
        )
    }

    // isPremium のテスト

    @Test
    fun `isPremium returns true for lifetime subscription`() = runBlocking {
        // Arrange
        val status = SubscriptionStatus(type = SubscriptionType.LIFETIME)
        coEvery { premiumRepository.getSubscriptionStatus() } returns Result.Success(status)

        // Act
        val result = premiumManager.isPremium()

        // Assert
        assertTrue("LIFETIME契約はPremiumとして扱うべき", result)
    }

    @Test
    fun `isPremium returns true for active monthly subscription`() = runBlocking {
        // Arrange
        val status = SubscriptionStatus(
            type = SubscriptionType.MONTHLY,
            expiresAt = LocalDateTime.now().plusDays(15)
        )
        coEvery { premiumRepository.getSubscriptionStatus() } returns Result.Success(status)

        // Act
        val result = premiumManager.isPremium()

        // Assert
        assertTrue("有効な月額契約はPremiumとして扱うべき", result)
    }

    @Test
    fun `isPremium returns false for expired subscription`() = runBlocking {
        // Arrange
        val status = SubscriptionStatus(
            type = SubscriptionType.MONTHLY,
            expiresAt = LocalDateTime.now().minusDays(1)
        )
        coEvery { premiumRepository.getSubscriptionStatus() } returns Result.Success(status)

        // Act
        val result = premiumManager.isPremium()

        // Assert
        assertFalse("期限切れの契約はPremiumではないべき", result)
    }

    @Test
    fun `isPremium returns true for active trial`() = runBlocking {
        // Arrange
        val status = SubscriptionStatus(
            type = SubscriptionType.FREE,
            trialExpiresAt = LocalDateTime.now().plusDays(3)
        )
        coEvery { premiumRepository.getSubscriptionStatus() } returns Result.Success(status)

        // Act
        val result = premiumManager.isPremium()

        // Assert
        assertTrue("有効なトライアル期間中はPremiumとして扱うべき", result)
    }

    @Test
    fun `isPremium returns false for free user without trial`() = runBlocking {
        // Arrange
        val status = SubscriptionStatus(type = SubscriptionType.FREE)
        coEvery { premiumRepository.getSubscriptionStatus() } returns Result.Success(status)

        // Act
        val result = premiumManager.isPremium()

        // Assert
        assertFalse("無料ユーザー（トライアルなし）はPremiumではないべき", result)
    }

    // Companion object 定数のテスト

    @Test
    fun `companion object has correct review intervals`() {
        assertEquals(
            "Premium復習間隔定数が正しい",
            listOf(1, 3, 7, 14, 30, 60),
            PremiumManager.PREMIUM_REVIEW_INTERVALS
        )
        assertEquals(
            "Free復習間隔定数が正しい",
            listOf(1, 3),
            PremiumManager.FREE_REVIEW_INTERVALS
        )
    }
}
