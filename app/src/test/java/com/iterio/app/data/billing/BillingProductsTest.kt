package com.iterio.app.data.billing

import com.iterio.app.domain.model.SubscriptionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * BillingProducts オブジェクトの全メソッドテスト
 */
class BillingProductsTest {

    // ==================== toSubscriptionType テスト ====================

    @Test
    fun `toSubscriptionType maps monthly product to MONTHLY`() {
        assertEquals(
            SubscriptionType.MONTHLY,
            BillingProducts.toSubscriptionType(BillingProducts.SUBSCRIPTION_MONTHLY)
        )
    }

    @Test
    fun `toSubscriptionType maps quarterly product to QUARTERLY`() {
        assertEquals(
            SubscriptionType.QUARTERLY,
            BillingProducts.toSubscriptionType(BillingProducts.SUBSCRIPTION_QUARTERLY)
        )
    }

    @Test
    fun `toSubscriptionType maps half yearly product to HALF_YEARLY`() {
        assertEquals(
            SubscriptionType.HALF_YEARLY,
            BillingProducts.toSubscriptionType(BillingProducts.SUBSCRIPTION_HALF_YEARLY)
        )
    }

    @Test
    fun `toSubscriptionType maps yearly product to YEARLY`() {
        assertEquals(
            SubscriptionType.YEARLY,
            BillingProducts.toSubscriptionType(BillingProducts.SUBSCRIPTION_YEARLY)
        )
    }

    @Test
    fun `toSubscriptionType maps lifetime product to LIFETIME`() {
        assertEquals(
            SubscriptionType.LIFETIME,
            BillingProducts.toSubscriptionType(BillingProducts.INAPP_LIFETIME)
        )
    }

    @Test
    fun `toSubscriptionType maps unknown product to FREE`() {
        assertEquals(
            SubscriptionType.FREE,
            BillingProducts.toSubscriptionType("unknown_product_id")
        )
    }

    // ==================== toProductId テスト ====================

    @Test
    fun `toProductId maps MONTHLY to monthly product`() {
        assertEquals(
            BillingProducts.SUBSCRIPTION_MONTHLY,
            BillingProducts.toProductId(SubscriptionType.MONTHLY)
        )
    }

    @Test
    fun `toProductId maps QUARTERLY to quarterly product`() {
        assertEquals(
            BillingProducts.SUBSCRIPTION_QUARTERLY,
            BillingProducts.toProductId(SubscriptionType.QUARTERLY)
        )
    }

    @Test
    fun `toProductId maps HALF_YEARLY to half yearly product`() {
        assertEquals(
            BillingProducts.SUBSCRIPTION_HALF_YEARLY,
            BillingProducts.toProductId(SubscriptionType.HALF_YEARLY)
        )
    }

    @Test
    fun `toProductId maps YEARLY to yearly product`() {
        assertEquals(
            BillingProducts.SUBSCRIPTION_YEARLY,
            BillingProducts.toProductId(SubscriptionType.YEARLY)
        )
    }

    @Test
    fun `toProductId maps LIFETIME to lifetime product`() {
        assertEquals(
            BillingProducts.INAPP_LIFETIME,
            BillingProducts.toProductId(SubscriptionType.LIFETIME)
        )
    }

    @Test
    fun `toProductId maps FREE to null`() {
        assertNull(BillingProducts.toProductId(SubscriptionType.FREE))
    }

    // ==================== isSubscription テスト ====================

    @Test
    fun `isSubscription returns true for subscription products`() {
        assertTrue(BillingProducts.isSubscription(BillingProducts.SUBSCRIPTION_MONTHLY))
        assertTrue(BillingProducts.isSubscription(BillingProducts.SUBSCRIPTION_QUARTERLY))
        assertTrue(BillingProducts.isSubscription(BillingProducts.SUBSCRIPTION_HALF_YEARLY))
        assertTrue(BillingProducts.isSubscription(BillingProducts.SUBSCRIPTION_YEARLY))
    }

    @Test
    fun `isSubscription returns false for inapp product`() {
        assertFalse(BillingProducts.isSubscription(BillingProducts.INAPP_LIFETIME))
    }

    @Test
    fun `isSubscription returns false for unknown product`() {
        assertFalse(BillingProducts.isSubscription("unknown_product"))
    }

    // ==================== isInApp テスト ====================

    @Test
    fun `isInApp returns true for inapp product`() {
        assertTrue(BillingProducts.isInApp(BillingProducts.INAPP_LIFETIME))
    }

    @Test
    fun `isInApp returns false for subscription products`() {
        assertFalse(BillingProducts.isInApp(BillingProducts.SUBSCRIPTION_MONTHLY))
        assertFalse(BillingProducts.isInApp(BillingProducts.SUBSCRIPTION_YEARLY))
    }

    @Test
    fun `isInApp returns false for unknown product`() {
        assertFalse(BillingProducts.isInApp("unknown_product"))
    }

    // ==================== SKU リストテスト ====================

    @Test
    fun `SUBSCRIPTION_SKUS contains all subscription products`() {
        assertEquals(4, BillingProducts.SUBSCRIPTION_SKUS.size)
        assertTrue(BillingProducts.SUBSCRIPTION_SKUS.contains(BillingProducts.SUBSCRIPTION_MONTHLY))
        assertTrue(BillingProducts.SUBSCRIPTION_SKUS.contains(BillingProducts.SUBSCRIPTION_QUARTERLY))
        assertTrue(BillingProducts.SUBSCRIPTION_SKUS.contains(BillingProducts.SUBSCRIPTION_HALF_YEARLY))
        assertTrue(BillingProducts.SUBSCRIPTION_SKUS.contains(BillingProducts.SUBSCRIPTION_YEARLY))
    }

    @Test
    fun `INAPP_SKUS contains lifetime product`() {
        assertEquals(1, BillingProducts.INAPP_SKUS.size)
        assertTrue(BillingProducts.INAPP_SKUS.contains(BillingProducts.INAPP_LIFETIME))
    }

    @Test
    fun `ALL_SKUS equals SUBSCRIPTION_SKUS plus INAPP_SKUS`() {
        assertEquals(
            BillingProducts.SUBSCRIPTION_SKUS + BillingProducts.INAPP_SKUS,
            BillingProducts.ALL_SKUS
        )
        assertEquals(5, BillingProducts.ALL_SKUS.size)
    }

    // ==================== 双方向変換テスト ====================

    @Test
    fun `bidirectional conversion is consistent for all subscription types`() {
        val validTypes = listOf(
            SubscriptionType.MONTHLY,
            SubscriptionType.QUARTERLY,
            SubscriptionType.HALF_YEARLY,
            SubscriptionType.YEARLY,
            SubscriptionType.LIFETIME
        )
        for (type in validTypes) {
            val productId = BillingProducts.toProductId(type)!!
            val roundTripped = BillingProducts.toSubscriptionType(productId)
            assertEquals("Round-trip failed for $type", type, roundTripped)
        }
    }

    // ==================== 定数値テスト ====================

    @Test
    fun `product ID constants have expected values`() {
        assertEquals("iterio_premium_monthly", BillingProducts.SUBSCRIPTION_MONTHLY)
        assertEquals("iterio_premium_quarterly", BillingProducts.SUBSCRIPTION_QUARTERLY)
        assertEquals("iterio_premium_half_yearly", BillingProducts.SUBSCRIPTION_HALF_YEARLY)
        assertEquals("iterio_premium_yearly", BillingProducts.SUBSCRIPTION_YEARLY)
        assertEquals("iterio_premium_lifetime", BillingProducts.INAPP_LIFETIME)
    }
}
