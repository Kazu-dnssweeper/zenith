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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

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
}
