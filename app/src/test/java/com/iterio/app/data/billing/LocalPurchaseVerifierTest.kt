package com.iterio.app.data.billing

import com.android.billingclient.api.Purchase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * LocalPurchaseVerifier のユニットテスト
 */
class LocalPurchaseVerifierTest {

    private lateinit var signatureVerifier: SignatureVerifier
    private lateinit var verifier: LocalPurchaseVerifier

    @Before
    fun setup() {
        signatureVerifier = mockk()
        verifier = LocalPurchaseVerifier(signatureVerifier)
    }

    @Test
    fun `verifyPurchase returns Verified for valid purchased state`() = runTest {
        val purchase = createMockPurchase(
            purchaseState = Purchase.PurchaseState.PURCHASED
        )
        every { signatureVerifier.verify(any(), any()) } returns true

        val result = verifier.verifyPurchase(purchase)

        assertTrue(result is PurchaseVerifier.VerificationResult.Verified)
        assertEquals(purchase, (result as PurchaseVerifier.VerificationResult.Verified).purchase)
    }

    @Test
    fun `verifyPurchase returns Pending for pending state`() = runTest {
        val purchase = createMockPurchase(
            purchaseState = Purchase.PurchaseState.PENDING
        )
        every { signatureVerifier.verify(any(), any()) } returns true

        val result = verifier.verifyPurchase(purchase)

        assertTrue(result is PurchaseVerifier.VerificationResult.Pending)
    }

    @Test
    fun `verifyPurchase returns Failed for invalid signature`() = runTest {
        val purchase = createMockPurchase(
            purchaseState = Purchase.PurchaseState.PURCHASED
        )
        every { signatureVerifier.verify(any(), any()) } returns false

        val result = verifier.verifyPurchase(purchase)

        assertTrue(result is PurchaseVerifier.VerificationResult.Failed)
        assertEquals(
            "Signature verification failed",
            (result as PurchaseVerifier.VerificationResult.Failed).reason
        )
    }

    @Test
    fun `verifyPurchase returns Failed for unspecified state`() = runTest {
        val purchase = createMockPurchase(
            purchaseState = Purchase.PurchaseState.UNSPECIFIED_STATE
        )
        every { signatureVerifier.verify(any(), any()) } returns true

        val result = verifier.verifyPurchase(purchase)

        assertTrue(result is PurchaseVerifier.VerificationResult.Failed)
        assertTrue(
            (result as PurchaseVerifier.VerificationResult.Failed).reason
                .contains("Invalid purchase state")
        )
    }

    @Test
    fun `verifyPurchase uses correct signature and json`() = runTest {
        val expectedJson = """{"productId":"test"}"""
        val expectedSignature = "test_signature"
        val purchase = createMockPurchase(
            purchaseState = Purchase.PurchaseState.PURCHASED,
            originalJson = expectedJson,
            signature = expectedSignature
        )
        var capturedJson: String? = null
        var capturedSignature: String? = null
        every { signatureVerifier.verify(any(), any()) } answers {
            capturedJson = firstArg()
            capturedSignature = secondArg()
            true
        }

        verifier.verifyPurchase(purchase)

        assertEquals(expectedJson, capturedJson)
        assertEquals(expectedSignature, capturedSignature)
    }

    @Test
    fun `verifyPurchase handles empty signature`() = runTest {
        val purchase = createMockPurchase(
            purchaseState = Purchase.PurchaseState.PURCHASED,
            signature = ""
        )
        every { signatureVerifier.verify(any(), "") } returns false

        val result = verifier.verifyPurchase(purchase)

        assertTrue(result is PurchaseVerifier.VerificationResult.Failed)
    }

    private fun createMockPurchase(
        purchaseState: Int = Purchase.PurchaseState.PURCHASED,
        originalJson: String = """{"productId":"test"}""",
        signature: String = "valid_signature"
    ): Purchase {
        val purchase: Purchase = mockk()
        every { purchase.purchaseState } returns purchaseState
        every { purchase.originalJson } returns originalJson
        every { purchase.signature } returns signature
        return purchase
    }
}
