package com.iterio.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * フォーカスモードのE2E（End-to-End）テスト
 *
 * テスト項目:
 * - フォーカスモード設定の表示
 * - 設定画面からの有効化
 * - フォーカスモード関連UI要素の確認
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FocusModeFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        hiltRule.inject()
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    /**
     * ホーム画面が正しく表示されることを確認
     */
    @Test
    fun homeScreen_displaysCorrectly() {
        composeTestRule.onNodeWithText("ホーム").assertIsDisplayed()
    }

    /**
     * 設定画面に遷移できることを確認
     */
    @Test
    fun canNavigateToSettings() {
        composeTestRule.onNodeWithText("設定").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 設定画面でフォーカスモードセクションが表示されることを確認
     */
    @Test
    fun settingsScreen_showsFocusModeSection() {
        composeTestRule.onNodeWithText("設定").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onAllNodes(hasText("フォーカスモード", substring = true))
            .get(0)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * フォーカスモード有効化トグルが表示されることを確認
     */
    @Test
    fun focusModeToggle_isDisplayed() {
        composeTestRule.onNodeWithText("設定").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithText("フォーカスモードを有効化", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * 完全ロックモード設定が表示されることを確認
     */
    @Test
    fun completeLockModeSetting_isDisplayed() {
        composeTestRule.onNodeWithText("設定").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("完全ロック", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty() ||
            composeTestRule.onAllNodes(hasText("ロックモード", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 許可アプリ設定が表示されることを確認
     */
    @Test
    fun allowedAppsSetting_isDisplayed() {
        composeTestRule.onNodeWithText("設定").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithText("許可アプリ", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * 設定画面から戻れることを確認
     */
    @Test
    fun canNavigateBackFromSettings() {
        composeTestRule.onNodeWithText("設定").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // 設定はトップレベルタブなので、ボトムナビでホームに戻る
        composeTestRule.onNodeWithText("ホーム").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Iterio"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * タイマー画面でタイマー関連UIが表示されることを確認
     */
    @Test
    fun homeScreen_showsTimerElements() {
        // ホーム画面でタイマー関連の要素が表示される
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("今日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * ナビゲーションバーが全画面で機能することを確認
     */
    @Test
    fun navigationBar_worksAcrossScreens() {
        // タスク画面に移動
        composeTestRule.onNodeWithText("タスク").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("科目グループ", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // ホーム画面に戻る
        composeTestRule.onNodeWithText("ホーム").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("今日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 自動ループ設定が表示されることを確認（Premium機能）
     */
    @Test
    fun autoLoopSetting_isDisplayed() {
        composeTestRule.onNodeWithText("設定").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("自動ループ", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty() ||
            composeTestRule.onAllNodes(hasText("ループ", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
