package com.iterio.app.domain.usecase

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.iterio.app.data.billing.BillingClientWrapper
import com.iterio.app.data.billing.BillingProducts
import com.iterio.app.data.billing.PurchaseVerifier
import com.iterio.app.domain.model.SubscriptionType
import com.iterio.app.domain.repository.PremiumRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * BillingUseCase のユニットテスト
 */
class BillingUseCaseTest {

    private lateinit var billingClientWrapper: BillingClientWrapper
    private lateinit var purchaseVerifier: PurchaseVerifier
    private lateinit var premiumRepository: PremiumRepository
    private lateinit var billingUseCase: BillingUseCase

    private val newPurchasesFlow = MutableSharedFlow<List<Purchase>>()

    @Before
    fun setup() {
        billingClientWrapper = mockk(relaxed = true)
        purchaseVerifier = mockk(relaxed = true)
        premiumRepository = mockk(relaxed = true)

        every { billingClientWrapper.newPurchases } returns newPurchasesFlow

        billingUseCase = BillingUseCase(
            billingClientWrapper = billingClientWrapper,
            purchaseVerifier = purchaseVerifier,
            premiumRepository = premiumRepository
        )
    }

    // ensureConnected のテスト

    @Test
    fun `ensureConnected returns true when connection succeeds`() = runBlocking {
        // Arrange
        coEvery { billingClientWrapper.startConnection() } returns true

        // Act
        val result = billingUseCase.ensureConnected()

        // Assert
        assertTrue("接続成功時はtrueを返すべき", result)
    }

    @Test
    fun `ensureConnected returns false when connection fails`() = runBlocking {
        // Arrange
        coEvery { billingClientWrapper.startConnection() } returns false

        // Act
        val result = billingUseCase.ensureConnected()

        // Assert
        assertFalse("接続失敗時はfalseを返すべき", result)
    }

    // getAvailableProducts のテスト

    @Test
    fun `getAvailableProducts returns failure when connection fails`() = runBlocking {
        // Arrange
        coEvery { billingClientWrapper.startConnection() } returns false

        // Act
        val result = billingUseCase.getAvailableProducts()

        // Assert
        assertTrue("接続失敗時はfailureを返すべき", result.isFailure)
        assertTrue(
            "ConnectionFailed例外であるべき",
            result.exceptionOrNull() is BillingUseCase.BillingException.ConnectionFailed
        )
    }

    @Test
    fun `getAvailableProducts returns empty list when no products`() = runBlocking {
        // Arrange
        coEvery { billingClientWrapper.startConnection() } returns true
        coEvery { billingClientWrapper.queryAllProductDetails() } returns emptyList()

        // Act
        val result = billingUseCase.getAvailableProducts()

        // Assert
        assertTrue("成功を返すべき", result.isSuccess)
        assertTrue("空のリストを返すべき", result.getOrNull()?.isEmpty() == true)
    }

    // startPurchase のテスト

    @Test
    fun `startPurchase returns failure when connection fails`() = runBlocking {
        // Arrange
        val activity = mockk<android.app.Activity>()
        coEvery { billingClientWrapper.startConnection() } returns false

        // Act
        val result = billingUseCase.startPurchase(activity, SubscriptionType.MONTHLY)

        // Assert
        assertTrue("接続失敗時はfailureを返すべき", result.isFailure)
        assertTrue(
            "ConnectionFailed例外であるべき",
            result.exceptionOrNull() is BillingUseCase.BillingException.ConnectionFailed
        )
    }

    @Test
    fun `startPurchase returns failure for FREE subscription type`() = runBlocking {
        // Arrange
        val activity = mockk<android.app.Activity>()
        coEvery { billingClientWrapper.startConnection() } returns true

        // Act
        val result = billingUseCase.startPurchase(activity, SubscriptionType.FREE)

        // Assert
        assertTrue("FREE型はfailureを返すべき", result.isFailure)
        assertTrue(
            "InvalidProduct例外であるべき",
            result.exceptionOrNull() is BillingUseCase.BillingException.InvalidProduct
        )
    }

    @Test
    fun `startPurchase returns failure when product not found`() = runBlocking {
        // Arrange
        val activity = mockk<android.app.Activity>()
        coEvery { billingClientWrapper.startConnection() } returns true
        coEvery { billingClientWrapper.queryAllProductDetails() } returns emptyList()

        // Act
        val result = billingUseCase.startPurchase(activity, SubscriptionType.MONTHLY)

        // Assert
        assertTrue("商品が見つからない場合はfailureを返すべき", result.isFailure)
        assertTrue(
            "ProductNotFound例外であるべき",
            result.exceptionOrNull() is BillingUseCase.BillingException.ProductNotFound
        )
    }

    // restorePurchases のテスト

    @Test
    fun `restorePurchases returns failure when connection fails`() = runBlocking {
        // Arrange
        coEvery { billingClientWrapper.startConnection() } returns false

        // Act
        val result = billingUseCase.restorePurchases()

        // Assert
        assertTrue("接続失敗時はfailureを返すべき", result.isFailure)
        assertTrue(
            "ConnectionFailed例外であるべき",
            result.exceptionOrNull() is BillingUseCase.BillingException.ConnectionFailed
        )
    }

    @Test
    fun `restorePurchases returns NoPurchasesFound when no purchases`() = runBlocking {
        // Arrange
        coEvery { billingClientWrapper.startConnection() } returns true
        coEvery { billingClientWrapper.queryPurchases() } returns emptyList()

        // Act
        val result = billingUseCase.restorePurchases()

        // Assert
        assertTrue("成功を返すべき", result.isSuccess)
        assertTrue(
            "NoPurchasesFoundを返すべき",
            result.getOrNull() is BillingUseCase.RestoreResult.NoPurchasesFound
        )
    }

    @Test
    fun `restorePurchases processes active purchase`() = runBlocking {
        // Arrange
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.purchaseTime } returns System.currentTimeMillis()
        every { purchase.products } returns listOf(BillingProducts.SUBSCRIPTION_MONTHLY)
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "test_token"
        every { purchase.originalJson } returns "{}"
        every { purchase.signature } returns "test_signature"

        coEvery { billingClientWrapper.startConnection() } returns true
        coEvery { billingClientWrapper.queryPurchases() } returns listOf(purchase)
        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        // Act
        val result = billingUseCase.restorePurchases()

        // Assert
        assertTrue("成功を返すべき", result.isSuccess)
        val restoreResult = result.getOrNull()
        assertTrue(
            "Success結果を返すべき",
            restoreResult is BillingUseCase.RestoreResult.Success
        )
        assertEquals(
            "MONTHLYタイプであるべき",
            SubscriptionType.MONTHLY,
            (restoreResult as BillingUseCase.RestoreResult.Success).subscriptionType
        )
    }

    @Test
    fun `restorePurchases returns Pending for pending purchase`() = runBlocking {
        // Arrange
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.purchaseTime } returns System.currentTimeMillis()
        every { purchase.originalJson } returns "{}"
        every { purchase.signature } returns "test_signature"

        coEvery { billingClientWrapper.startConnection() } returns true
        coEvery { billingClientWrapper.queryPurchases() } returns listOf(purchase)
        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Pending

        // Act
        val result = billingUseCase.restorePurchases()

        // Assert
        assertTrue("成功を返すべき", result.isSuccess)
        assertTrue(
            "Pending結果を返すべき",
            result.getOrNull() is BillingUseCase.RestoreResult.Pending
        )
    }

    // processPurchase のテスト

    @Test
    fun `processPurchase returns Success when verified and acknowledged`() = runBlocking {
        // Arrange
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(BillingProducts.SUBSCRIPTION_YEARLY)
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "test_token"
        every { purchase.purchaseTime } returns System.currentTimeMillis()
        every { purchase.originalJson } returns "{}"
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        // Act
        val result = billingUseCase.processPurchase(purchase)

        // Assert
        assertTrue("Success結果を返すべき", result is BillingUseCase.ProcessPurchaseResult.Success)
        assertEquals(
            "YEARLYタイプであるべき",
            SubscriptionType.YEARLY,
            (result as BillingUseCase.ProcessPurchaseResult.Success).subscriptionType
        )
    }

    @Test
    fun `processPurchase acknowledges unacknowledged purchase`() = runBlocking {
        // Arrange
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(BillingProducts.SUBSCRIPTION_MONTHLY)
        every { purchase.isAcknowledged } returns false
        every { purchase.purchaseToken } returns "test_token"
        every { purchase.purchaseTime } returns System.currentTimeMillis()
        every { purchase.originalJson } returns "{}"
        every { purchase.signature } returns "test_signature"

        val billingResult = mockk<BillingResult>()
        every { billingResult.responseCode } returns BillingClient.BillingResponseCode.OK

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)
        coEvery { billingClientWrapper.acknowledgePurchase("test_token") } returns billingResult

        // Act
        val result = billingUseCase.processPurchase(purchase)

        // Assert
        coVerify { billingClientWrapper.acknowledgePurchase("test_token") }
        assertTrue("Success結果を返すべき", result is BillingUseCase.ProcessPurchaseResult.Success)
    }

    @Test
    fun `processPurchase returns Pending when verification is pending`() = runBlocking {
        // Arrange
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.originalJson } returns "{}"
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Pending

        // Act
        val result = billingUseCase.processPurchase(purchase)

        // Assert
        assertTrue("Pending結果を返すべき", result is BillingUseCase.ProcessPurchaseResult.Pending)
    }

    @Test
    fun `processPurchase returns Error when verification fails`() = runBlocking {
        // Arrange
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.originalJson } returns "{}"
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Failed("Verification failed")

        // Act
        val result = billingUseCase.processPurchase(purchase)

        // Assert
        assertTrue("Error結果を返すべき", result is BillingUseCase.ProcessPurchaseResult.Error)
        assertEquals(
            "エラーメッセージが正しいべき",
            "Verification failed",
            (result as BillingUseCase.ProcessPurchaseResult.Error).message
        )
    }

    @Test
    fun `processPurchase updates subscription for lifetime purchase`() = runBlocking {
        // Arrange
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(BillingProducts.INAPP_LIFETIME)
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "test_token"
        every { purchase.purchaseTime } returns System.currentTimeMillis()
        every { purchase.originalJson } returns "{}"
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        // Act
        val result = billingUseCase.processPurchase(purchase)

        // Assert
        assertTrue("Success結果を返すべき", result is BillingUseCase.ProcessPurchaseResult.Success)
        assertEquals(
            "LIFETIMEタイプであるべき",
            SubscriptionType.LIFETIME,
            (result as BillingUseCase.ProcessPurchaseResult.Success).subscriptionType
        )
        // LIFETIMEの場合、expiresAtはnullで更新される
        coVerify { premiumRepository.updateSubscription(SubscriptionType.LIFETIME, null) }
    }

    // BillingProducts変換テスト

    @Test
    fun `toSubscriptionType converts monthly correctly`() {
        assertEquals(
            SubscriptionType.MONTHLY,
            BillingProducts.toSubscriptionType(BillingProducts.SUBSCRIPTION_MONTHLY)
        )
    }

    @Test
    fun `toSubscriptionType converts quarterly correctly`() {
        assertEquals(
            SubscriptionType.QUARTERLY,
            BillingProducts.toSubscriptionType(BillingProducts.SUBSCRIPTION_QUARTERLY)
        )
    }

    @Test
    fun `toSubscriptionType converts half yearly correctly`() {
        assertEquals(
            SubscriptionType.HALF_YEARLY,
            BillingProducts.toSubscriptionType(BillingProducts.SUBSCRIPTION_HALF_YEARLY)
        )
    }

    @Test
    fun `toSubscriptionType converts yearly correctly`() {
        assertEquals(
            SubscriptionType.YEARLY,
            BillingProducts.toSubscriptionType(BillingProducts.SUBSCRIPTION_YEARLY)
        )
    }

    @Test
    fun `toSubscriptionType converts lifetime correctly`() {
        assertEquals(
            SubscriptionType.LIFETIME,
            BillingProducts.toSubscriptionType(BillingProducts.INAPP_LIFETIME)
        )
    }

    @Test
    fun `toSubscriptionType returns FREE for unknown product`() {
        assertEquals(
            SubscriptionType.FREE,
            BillingProducts.toSubscriptionType("unknown_product")
        )
    }

    @Test
    fun `toProductId converts all types correctly`() {
        assertEquals(
            BillingProducts.SUBSCRIPTION_MONTHLY,
            BillingProducts.toProductId(SubscriptionType.MONTHLY)
        )
        assertEquals(
            BillingProducts.SUBSCRIPTION_QUARTERLY,
            BillingProducts.toProductId(SubscriptionType.QUARTERLY)
        )
        assertEquals(
            BillingProducts.SUBSCRIPTION_HALF_YEARLY,
            BillingProducts.toProductId(SubscriptionType.HALF_YEARLY)
        )
        assertEquals(
            BillingProducts.SUBSCRIPTION_YEARLY,
            BillingProducts.toProductId(SubscriptionType.YEARLY)
        )
        assertEquals(
            BillingProducts.INAPP_LIFETIME,
            BillingProducts.toProductId(SubscriptionType.LIFETIME)
        )
        assertNull(
            "FREEはnullを返すべき",
            BillingProducts.toProductId(SubscriptionType.FREE)
        )
    }

    @Test
    fun `isSubscription returns true for subscription products`() {
        assertTrue(BillingProducts.isSubscription(BillingProducts.SUBSCRIPTION_MONTHLY))
        assertTrue(BillingProducts.isSubscription(BillingProducts.SUBSCRIPTION_QUARTERLY))
        assertTrue(BillingProducts.isSubscription(BillingProducts.SUBSCRIPTION_HALF_YEARLY))
        assertTrue(BillingProducts.isSubscription(BillingProducts.SUBSCRIPTION_YEARLY))
    }

    @Test
    fun `isSubscription returns false for inapp products`() {
        assertFalse(BillingProducts.isSubscription(BillingProducts.INAPP_LIFETIME))
    }

    @Test
    fun `isInApp returns true for inapp products`() {
        assertTrue(BillingProducts.isInApp(BillingProducts.INAPP_LIFETIME))
    }

    @Test
    fun `isInApp returns false for subscription products`() {
        assertFalse(BillingProducts.isInApp(BillingProducts.SUBSCRIPTION_MONTHLY))
    }

    // refreshPurchaseStatus のテスト

    @Test
    fun `refreshPurchaseStatus does nothing when connection fails`() = runBlocking {
        // Arrange
        coEvery { billingClientWrapper.startConnection() } returns false

        // Act
        billingUseCase.refreshPurchaseStatus()

        // Assert
        coVerify(exactly = 0) { billingClientWrapper.queryPurchases() }
    }

    @Test
    fun `refreshPurchaseStatus processes active purchase`() = runBlocking {
        // Arrange
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.purchaseTime } returns System.currentTimeMillis()
        every { purchase.products } returns listOf(BillingProducts.SUBSCRIPTION_MONTHLY)
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "test_token"
        every { purchase.originalJson } returns "{}"
        every { purchase.signature } returns "test_signature"

        coEvery { billingClientWrapper.startConnection() } returns true
        coEvery { billingClientWrapper.queryPurchases() } returns listOf(purchase)
        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        // Act
        billingUseCase.refreshPurchaseStatus()

        // Assert
        coVerify { purchaseVerifier.verifyPurchase(purchase) }
    }

    // 重複トークン処理のテスト

    @Test
    fun `processPurchase with duplicate token returns Success immediately`() = runBlocking {
        // Arrange: 最初の購入を処理して processedPurchaseTokens に登録
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(BillingProducts.SUBSCRIPTION_MONTHLY)
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "duplicate_token"
        every { purchase.purchaseTime } returns System.currentTimeMillis()
        every { purchase.originalJson } returns "{}"
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        // Act: 1回目の処理
        val firstResult = billingUseCase.processPurchase(purchase)
        assertTrue("1回目はSuccess結果を返すべき", firstResult is BillingUseCase.ProcessPurchaseResult.Success)

        // Act: 2回目の処理（同じトークン）
        val secondResult = billingUseCase.processPurchase(purchase)

        // Assert: 2回目はverifyPurchaseを呼ばずにSuccessを返す
        assertTrue("2回目もSuccess結果を返すべき", secondResult is BillingUseCase.ProcessPurchaseResult.Success)
        coVerify(exactly = 1) { purchaseVerifier.verifyPurchase(purchase) }
    }

    @Test
    fun `processPurchase with duplicate token uses correct subscription type`() = runBlocking {
        // Arrange
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(BillingProducts.SUBSCRIPTION_YEARLY)
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "dup_yearly_token"
        every { purchase.purchaseTime } returns System.currentTimeMillis()
        every { purchase.originalJson } returns "{}"
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        // 1回目の処理でトークンを登録
        billingUseCase.processPurchase(purchase)

        // Act: 2回目の処理
        val result = billingUseCase.processPurchase(purchase)

        // Assert: 重複処理時もproductsからサブスクリプションタイプを正しく取得
        assertTrue("Success結果を返すべき", result is BillingUseCase.ProcessPurchaseResult.Success)
        assertEquals(
            "YEARLYタイプであるべき",
            SubscriptionType.YEARLY,
            (result as BillingUseCase.ProcessPurchaseResult.Success).subscriptionType
        )
    }

    @Test
    fun `processPurchase returns Error when acknowledge fails`() = runBlocking {
        // Arrange
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(BillingProducts.SUBSCRIPTION_MONTHLY)
        every { purchase.isAcknowledged } returns false
        every { purchase.purchaseToken } returns "ack_fail_token"
        every { purchase.purchaseTime } returns System.currentTimeMillis()
        every { purchase.originalJson } returns "{}"
        every { purchase.signature } returns "test_signature"

        val billingResult = mockk<BillingResult>()
        every { billingResult.responseCode } returns BillingClient.BillingResponseCode.ERROR

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)
        coEvery { billingClientWrapper.acknowledgePurchase("ack_fail_token") } returns billingResult

        // Act
        val result = billingUseCase.processPurchase(purchase)

        // Assert
        assertTrue("Error結果を返すべき", result is BillingUseCase.ProcessPurchaseResult.Error)
        assertEquals(
            "エラーメッセージが正しいべき",
            "Failed to acknowledge purchase",
            (result as BillingUseCase.ProcessPurchaseResult.Error).message
        )
    }

    // calculateExpiryDate のテスト (processPurchaseを介して間接的にテスト)

    @Test
    fun `calculateExpiryDate returns null for LIFETIME purchase via processPurchase`() = runBlocking {
        // Arrange
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(BillingProducts.INAPP_LIFETIME)
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "lifetime_expiry_token"
        every { purchase.purchaseTime } returns System.currentTimeMillis()
        every { purchase.originalJson } returns "{}"
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        // Act
        billingUseCase.processPurchase(purchase)

        // Assert: LIFETIMEの場合、expiresAtはnullで更新される
        coVerify { premiumRepository.updateSubscription(SubscriptionType.LIFETIME, null) }
    }

    @Test
    fun `calculateExpiryDate returns null for FREE type via processPurchase`() = runBlocking {
        // Arrange: 不明なproductIdはFREEタイプに変換される
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf("unknown_product_id")
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "free_expiry_token"
        every { purchase.purchaseTime } returns System.currentTimeMillis()
        every { purchase.originalJson } returns "{}"
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        // Act
        billingUseCase.processPurchase(purchase)

        // Assert: FREEの場合、expiresAtはnullで更新される
        coVerify { premiumRepository.updateSubscription(SubscriptionType.FREE, null) }
    }

    @Test
    fun `calculateExpiryDate falls back to purchaseTime when JSON parsing unavailable`() = runBlocking {
        // Note: org.json.JSONObject is stubbed in Android JVM unit tests,
        // so calculateExpiryDate always falls back to calculateExpiryFromPurchaseTime
        val purchaseTimeMillis = 1735689600000L // 2025-01-01T00:00:00 UTC
        val purchaseTime = Instant.ofEpochMilli(purchaseTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val expectedExpiry = purchaseTime.plusMonths(1) // MONTHLY = +1 month

        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(BillingProducts.SUBSCRIPTION_MONTHLY)
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "json_expiry_token"
        every { purchase.purchaseTime } returns purchaseTimeMillis
        every { purchase.originalJson } returns """{"expiryTimeMillis":9999999999999}"""
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        val expiresAtSlot = slot<LocalDateTime>()
        coEvery { premiumRepository.updateSubscription(any(), capture(expiresAtSlot)) } returns mockk()

        // Act
        billingUseCase.processPurchase(purchase)

        // Assert: JVM test env causes JSON fallback to purchaseTime calculation
        assertTrue("expiresAtがキャプチャされるべき", expiresAtSlot.isCaptured)
        assertEquals(
            "purchaseTimeからMONTHLY (+1 month) で算出されるべき",
            expectedExpiry,
            expiresAtSlot.captured
        )
    }

    @Test
    fun `calculateExpiryDate falls back to purchaseTime when expiryTimeMillis is missing`() = runBlocking {
        // Arrange: originalJsonにexpiryTimeMillisがない場合
        val purchaseTimeMillis = 1704067200000L // 2024-01-01T00:00:00 UTC
        val purchaseTime = Instant.ofEpochMilli(purchaseTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val expectedExpiry = purchaseTime.plusMonths(1) // MONTHLYなので1ヶ月追加

        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(BillingProducts.SUBSCRIPTION_MONTHLY)
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "no_expiry_json_token"
        every { purchase.purchaseTime } returns purchaseTimeMillis
        every { purchase.originalJson } returns """{"productId":"iterio_premium_monthly"}"""
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        val expiresAtSlot = slot<LocalDateTime>()
        coEvery { premiumRepository.updateSubscription(any(), capture(expiresAtSlot)) } returns mockk()

        // Act
        billingUseCase.processPurchase(purchase)

        // Assert: purchaseTime + 1ヶ月 がフォールバックとして使われる
        assertTrue("expiresAtがキャプチャされるべき", expiresAtSlot.isCaptured)
        assertEquals(
            "purchaseTime + 1ヶ月であるべき",
            expectedExpiry,
            expiresAtSlot.captured
        )
    }

    @Test
    fun `calculateExpiryDate falls back to purchaseTime when originalJson is empty`() = runBlocking {
        // Arrange: originalJsonが空文字列の場合
        val purchaseTimeMillis = 1704067200000L // 2024-01-01T00:00:00 UTC
        val purchaseTime = Instant.ofEpochMilli(purchaseTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val expectedExpiry = purchaseTime.plusMonths(3) // QUARTERLYなので3ヶ月追加

        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(BillingProducts.SUBSCRIPTION_QUARTERLY)
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "empty_json_token"
        every { purchase.purchaseTime } returns purchaseTimeMillis
        every { purchase.originalJson } returns ""
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        val expiresAtSlot = slot<LocalDateTime>()
        coEvery { premiumRepository.updateSubscription(any(), capture(expiresAtSlot)) } returns mockk()

        // Act
        billingUseCase.processPurchase(purchase)

        // Assert: 空JSONの場合はpurchaseTime + 3ヶ月がフォールバック
        assertTrue("expiresAtがキャプチャされるべき", expiresAtSlot.isCaptured)
        assertEquals(
            "purchaseTime + 3ヶ月であるべき",
            expectedExpiry,
            expiresAtSlot.captured
        )
    }

    // calculateExpiryFromPurchaseTime のテスト (processPurchaseを介して間接的にテスト)

    @Test
    fun `calculateExpiryFromPurchaseTime adds 1 month for MONTHLY`() = runBlocking {
        // Arrange
        val purchaseTimeMillis = 1704067200000L // 2024-01-01T00:00:00 UTC
        val purchaseTime = Instant.ofEpochMilli(purchaseTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val expectedExpiry = purchaseTime.plusMonths(1)

        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(BillingProducts.SUBSCRIPTION_MONTHLY)
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "monthly_calc_token"
        every { purchase.purchaseTime } returns purchaseTimeMillis
        every { purchase.originalJson } returns "{}" // expiryTimeMillisなし
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        val expiresAtSlot = slot<LocalDateTime>()
        coEvery { premiumRepository.updateSubscription(any(), capture(expiresAtSlot)) } returns mockk()

        // Act
        billingUseCase.processPurchase(purchase)

        // Assert
        assertTrue("expiresAtがキャプチャされるべき", expiresAtSlot.isCaptured)
        assertEquals(
            "MONTHLYは購入日 + 1ヶ月であるべき",
            expectedExpiry,
            expiresAtSlot.captured
        )
    }

    @Test
    fun `calculateExpiryFromPurchaseTime adds 3 months for QUARTERLY`() = runBlocking {
        // Arrange
        val purchaseTimeMillis = 1704067200000L // 2024-01-01T00:00:00 UTC
        val purchaseTime = Instant.ofEpochMilli(purchaseTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val expectedExpiry = purchaseTime.plusMonths(3)

        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(BillingProducts.SUBSCRIPTION_QUARTERLY)
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "quarterly_calc_token"
        every { purchase.purchaseTime } returns purchaseTimeMillis
        every { purchase.originalJson } returns "{}" // expiryTimeMillisなし
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        val expiresAtSlot = slot<LocalDateTime>()
        coEvery { premiumRepository.updateSubscription(any(), capture(expiresAtSlot)) } returns mockk()

        // Act
        billingUseCase.processPurchase(purchase)

        // Assert
        assertTrue("expiresAtがキャプチャされるべき", expiresAtSlot.isCaptured)
        assertEquals(
            "QUARTERLYは購入日 + 3ヶ月であるべき",
            expectedExpiry,
            expiresAtSlot.captured
        )
    }

    @Test
    fun `calculateExpiryFromPurchaseTime adds 6 months for HALF_YEARLY`() = runBlocking {
        // Arrange
        val purchaseTimeMillis = 1704067200000L // 2024-01-01T00:00:00 UTC
        val purchaseTime = Instant.ofEpochMilli(purchaseTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val expectedExpiry = purchaseTime.plusMonths(6)

        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(BillingProducts.SUBSCRIPTION_HALF_YEARLY)
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "half_yearly_calc_token"
        every { purchase.purchaseTime } returns purchaseTimeMillis
        every { purchase.originalJson } returns "{}" // expiryTimeMillisなし
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        val expiresAtSlot = slot<LocalDateTime>()
        coEvery { premiumRepository.updateSubscription(any(), capture(expiresAtSlot)) } returns mockk()

        // Act
        billingUseCase.processPurchase(purchase)

        // Assert
        assertTrue("expiresAtがキャプチャされるべき", expiresAtSlot.isCaptured)
        assertEquals(
            "HALF_YEARLYは購入日 + 6ヶ月であるべき",
            expectedExpiry,
            expiresAtSlot.captured
        )
    }

    @Test
    fun `calculateExpiryFromPurchaseTime adds 1 year for YEARLY`() = runBlocking {
        // Arrange
        val purchaseTimeMillis = 1704067200000L // 2024-01-01T00:00:00 UTC
        val purchaseTime = Instant.ofEpochMilli(purchaseTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val expectedExpiry = purchaseTime.plusYears(1)

        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(BillingProducts.SUBSCRIPTION_YEARLY)
        every { purchase.isAcknowledged } returns true
        every { purchase.purchaseToken } returns "yearly_calc_token"
        every { purchase.purchaseTime } returns purchaseTimeMillis
        every { purchase.originalJson } returns "{}" // expiryTimeMillisなし
        every { purchase.signature } returns "test_signature"

        coEvery { purchaseVerifier.verifyPurchase(purchase) } returns
                PurchaseVerifier.VerificationResult.Verified(purchase)

        val expiresAtSlot = slot<LocalDateTime>()
        coEvery { premiumRepository.updateSubscription(any(), capture(expiresAtSlot)) } returns mockk()

        // Act
        billingUseCase.processPurchase(purchase)

        // Assert
        assertTrue("expiresAtがキャプチャされるべき", expiresAtSlot.isCaptured)
        assertEquals(
            "YEARLYは購入日 + 1年であるべき",
            expectedExpiry,
            expiresAtSlot.captured
        )
    }
}
