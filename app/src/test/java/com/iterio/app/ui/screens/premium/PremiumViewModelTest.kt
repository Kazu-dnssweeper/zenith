package com.iterio.app.ui.screens.premium

import android.app.Activity
import app.cash.turbine.test
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.model.SubscriptionType
import com.iterio.app.domain.usecase.BillingUseCase
import com.iterio.app.testutil.CoroutineTestRule
import com.iterio.app.ui.premium.PremiumManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

/**
 * PremiumViewModel のユニットテスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PremiumViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var premiumManager: PremiumManager
    private lateinit var billingUseCase: BillingUseCase
    private val subscriptionStatusFlow = MutableStateFlow(SubscriptionStatus())
    private val newPurchasesFlow = MutableSharedFlow<List<Purchase>>()

    @Before
    fun setup() {
        premiumManager = mockk()
        billingUseCase = mockk()

        every { premiumManager.subscriptionStatus } returns subscriptionStatusFlow
        every { billingUseCase.newPurchases } returns newPurchasesFlow
        coEvery { billingUseCase.getAvailableProducts() } returns Result.success(emptyList())
    }

    private fun createViewModel() = PremiumViewModel(
        premiumManager = premiumManager,
        billingUseCase = billingUseCase
    )

    @Test
    fun `initial purchase state is Loading then Idle`() = runTest {
        val vm = createViewModel()

        vm.purchaseState.test {
            // May start with Loading then move to Idle
            val states = mutableListOf<PremiumViewModel.PurchaseState>()
            states.add(awaitItem())
            advanceUntilIdle()
            if (states.last() != PremiumViewModel.PurchaseState.Idle) {
                states.add(awaitItem())
            }
            assertTrue(states.any { it == PremiumViewModel.PurchaseState.Idle })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads products on init`() = runTest {
        val mockProductDetails: ProductDetails = mockk()
        val products = listOf(
            BillingUseCase.ProductInfo(
                type = SubscriptionType.MONTHLY,
                productId = "monthly",
                price = "¥500",
                priceMicros = 500000000L,
                priceCurrencyCode = "JPY",
                billingPeriod = "P1M",
                offerToken = "test_token",
                productDetails = mockProductDetails
            )
        )
        coEvery { billingUseCase.getAvailableProducts() } returns Result.success(products)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.products.test {
            val loadedProducts = awaitItem()
            assertEquals(1, loadedProducts.size)
            assertEquals("monthly", loadedProducts[0].productId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `product load failure emits billing error`() = runTest {
        coEvery { billingUseCase.getAvailableProducts() } returns Result.failure(
            RuntimeException("Network error")
        )

        val vm = createViewModel()

        // Start collecting BEFORE advancing to catch the emission
        vm.billingError.test {
            advanceUntilIdle()
            val error = awaitItem()
            assertTrue(error is PremiumViewModel.BillingError.ProductLoadFailed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `startTrial calls premiumManager`() = runTest {
        coEvery { premiumManager.startTrial() } returns Unit

        val vm = createViewModel()
        advanceUntilIdle()

        vm.startTrial()
        advanceUntilIdle()

        coVerify { premiumManager.startTrial() }
    }

    @Test
    fun `purchase sets state to Processing`() = runTest {
        val activity: Activity = mockk()
        coEvery { billingUseCase.startPurchase(activity, any()) } returns Result.success(Unit)

        val vm = createViewModel()
        advanceUntilIdle()

        // Start collecting BEFORE calling purchase to capture Processing state
        vm.purchaseState.test {
            // Skip initial Idle state
            assertEquals(PremiumViewModel.PurchaseState.Idle, awaitItem())

            vm.purchase(activity, SubscriptionType.MONTHLY)
            advanceUntilIdle()

            // Should transition to Processing
            val state = awaitItem()
            assertEquals(PremiumViewModel.PurchaseState.Processing, state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `purchase failure with UserCanceled resets to Idle`() = runTest {
        val activity: Activity = mockk()
        coEvery { billingUseCase.startPurchase(activity, any()) } returns Result.failure(
            BillingUseCase.BillingException.UserCanceled("User canceled")
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.purchase(activity, SubscriptionType.MONTHLY)
        advanceUntilIdle()

        vm.purchaseState.test {
            assertEquals(PremiumViewModel.PurchaseState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `purchase failure with AlreadyOwned sets appropriate state`() = runTest {
        val activity: Activity = mockk()
        coEvery { billingUseCase.startPurchase(activity, any()) } returns Result.failure(
            BillingUseCase.BillingException.AlreadyOwned("Item already owned")
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.purchase(activity, SubscriptionType.MONTHLY)
        advanceUntilIdle()

        vm.purchaseState.test {
            assertEquals(PremiumViewModel.PurchaseState.AlreadyOwned, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `restorePurchases sets state to Restoring`() = runTest {
        coEvery { billingUseCase.restorePurchases() } returns Result.success(
            BillingUseCase.RestoreResult.NoPurchasesFound
        )

        val vm = createViewModel()
        advanceUntilIdle()

        // Start collecting BEFORE calling restorePurchases to capture Restoring state
        vm.purchaseState.test {
            // Skip initial Idle state
            assertEquals(PremiumViewModel.PurchaseState.Idle, awaitItem())

            vm.restorePurchases()
            advanceUntilIdle()

            // Should transition to Restoring then NoPurchasesFound
            val states = mutableListOf<PremiumViewModel.PurchaseState>()
            states.add(awaitItem())
            states.add(awaitItem())
            assertTrue(states.contains(PremiumViewModel.PurchaseState.Restoring))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `restorePurchases success updates state`() = runTest {
        coEvery { billingUseCase.restorePurchases() } returns Result.success(
            BillingUseCase.RestoreResult.Success(SubscriptionType.MONTHLY)
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.restorePurchases()
        advanceUntilIdle()

        vm.purchaseState.test {
            val state = awaitItem()
            assertTrue(state is PremiumViewModel.PurchaseState.Restored)
            assertEquals(
                SubscriptionType.MONTHLY,
                (state as PremiumViewModel.PurchaseState.Restored).type
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `restorePurchases with no purchases found`() = runTest {
        coEvery { billingUseCase.restorePurchases() } returns Result.success(
            BillingUseCase.RestoreResult.NoPurchasesFound
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.restorePurchases()
        advanceUntilIdle()

        vm.purchaseState.test {
            assertEquals(PremiumViewModel.PurchaseState.NoPurchasesFound, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetPurchaseState sets to Idle`() = runTest {
        coEvery { billingUseCase.restorePurchases() } returns Result.success(
            BillingUseCase.RestoreResult.NoPurchasesFound
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.restorePurchases()
        advanceUntilIdle()
        vm.resetPurchaseState()

        vm.purchaseState.test {
            assertEquals(PremiumViewModel.PurchaseState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshProducts reloads products`() = runTest {
        val mockProductDetails: ProductDetails = mockk()
        val newProducts = listOf(
            BillingUseCase.ProductInfo(
                type = SubscriptionType.YEARLY,
                productId = "yearly",
                price = "¥5000",
                priceMicros = 5000000000L,
                priceCurrencyCode = "JPY",
                billingPeriod = "P1Y",
                offerToken = "yearly_token",
                productDetails = mockProductDetails
            )
        )
        coEvery { billingUseCase.getAvailableProducts() } returns Result.success(emptyList()) andThen Result.success(newProducts)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.refreshProducts()
        advanceUntilIdle()

        vm.products.test {
            val products = awaitItem()
            assertEquals(1, products.size)
            assertEquals("yearly", products[0].productId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subscriptionStatus reflects premium manager state`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.subscriptionStatus.test {
            assertEquals(SubscriptionType.FREE, awaitItem().type)

            subscriptionStatusFlow.value = SubscriptionStatus(
                type = SubscriptionType.MONTHLY,
                expiresAt = LocalDateTime.now().plusMonths(1)
            )
            assertEquals(SubscriptionType.MONTHLY, awaitItem().type)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
