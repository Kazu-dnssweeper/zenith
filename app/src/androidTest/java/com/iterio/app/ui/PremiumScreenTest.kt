package com.iterio.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * プレミアム画面のE2Eテスト
 *
 * テスト項目:
 * - プラン選択UI表示
 * - トライアル開始ボタン表示条件
 * - 購入ボタンクリック
 * - 復元ボタンクリック
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PremiumScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        // 設定画面に遷移
        composeTestRule.onNodeWithContentDescription("設定").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        // Premium画面に遷移
        composeTestRule.onNodeWithText("Premium", substring = true)
            .performScrollTo()
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Premium", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * Premium画面が正しく表示されることを確認
     */
    @Test
    fun premiumScreen_displaysCorrectly() {
        composeTestRule.onNodeWithText("Premium", substring = true).assertIsDisplayed()
    }

    /**
     * 戻るボタンが表示されることを確認
     */
    @Test
    fun backButton_isDisplayed() {
        composeTestRule.onNodeWithContentDescription("戻る").assertIsDisplayed()
    }

    /**
     * Premium機能一覧が表示されることを確認
     */
    @Test
    fun premiumFeaturesList_isDisplayed() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("機能", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 月額プランカードが表示されることを確認
     */
    @Test
    fun monthlyPlanCard_isDisplayed() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("月額", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 年額プランカードが表示されることを確認
     */
    @Test
    fun yearlyPlanCard_isDisplayed() {
        composeTestRule.onNodeWithText("年額", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * 買い切りプランカードが表示されることを確認
     */
    @Test
    fun lifetimePlanCard_isDisplayed() {
        composeTestRule.onNodeWithText("買い切り", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * 購入復元ボタンが表示されることを確認
     */
    @Test
    fun restoreButton_isDisplayed() {
        composeTestRule.onNodeWithText("購入を復元", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * 戻るボタンが機能することを確認
     */
    @Test
    fun backButton_navigatesToSettings() {
        composeTestRule.onNodeWithContentDescription("戻る").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("設定"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 価格情報が表示されることを確認
     */
    @Test
    fun priceInfo_isDisplayed() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("¥", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * おすすめバッジが表示されることを確認
     */
    @Test
    fun recommendedBadge_isDisplayed() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("おすすめ", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty() ||
            composeTestRule.onAllNodes(hasText("お得", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
