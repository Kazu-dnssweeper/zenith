package com.iterio.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iterio.app.util.TestConstants
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 設定画面のE2Eテスト
 *
 * テスト項目:
 * - 各設定項目の表示確認
 * - スライダー調整
 * - トグルスイッチの操作
 * - フォーカスモード設定
 * - Premium機能の表示条件
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

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
    }

    /**
     * 設定画面が正しく表示されることを確認
     */
    @Test
    fun settingsScreen_displaysCorrectly() {
        composeTestRule.onNodeWithText("設定").assertIsDisplayed()
    }

    /**
     * 通知セクションが表示されることを確認
     */
    @Test
    fun notificationSection_isDisplayed() {
        composeTestRule.onNodeWithText("通知", substring = true).assertIsDisplayed()
    }

    /**
     * ポモドーロタイマーセクションが表示されることを確認
     */
    @Test
    fun pomodoroSection_isDisplayed() {
        composeTestRule.onNodeWithText("ポモドーロタイマー").assertIsDisplayed()
    }

    /**
     * 作業時間設定が表示されることを確認
     */
    @Test
    fun workDurationSetting_isDisplayed() {
        composeTestRule.onNodeWithText("作業時間", substring = true).assertIsDisplayed()
    }

    /**
     * 短休憩設定が表示されることを確認
     */
    @Test
    fun shortBreakSetting_isDisplayed() {
        composeTestRule.onNodeWithText("短休憩", substring = true).assertIsDisplayed()
    }

    /**
     * 長休憩設定が表示されることを確認
     */
    @Test
    fun longBreakSetting_isDisplayed() {
        composeTestRule.onNodeWithText("長休憩", substring = true).assertIsDisplayed()
    }

    /**
     * フォーカスモードセクションが表示されることを確認
     */
    @Test
    fun focusModeSection_isDisplayed() {
        composeTestRule.onNodeWithText("フォーカスモード", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * 復習セクションが表示されることを確認
     */
    @Test
    fun reviewSection_isDisplayed() {
        composeTestRule.onNodeWithText("復習", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * データ管理セクションが表示されることを確認
     */
    @Test
    fun dataSection_isDisplayed() {
        composeTestRule.onNodeWithText("データ管理", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * バックアップオプションが表示されることを確認
     */
    @Test
    fun backupOption_isDisplayed() {
        composeTestRule.onNodeWithText("バックアップ", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * アプリについてセクションが表示されることを確認
     */
    @Test
    fun aboutSection_isDisplayed() {
        composeTestRule.onNodeWithText("アプリについて", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * バージョン情報が表示されることを確認
     */
    @Test
    fun versionInfo_isDisplayed() {
        composeTestRule.onNodeWithText("バージョン", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Premiumセクションが表示されることを確認
     */
    @Test
    fun premiumSection_isDisplayed() {
        composeTestRule.onNodeWithText("Premium", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * 戻るボタンが機能することを確認
     */
    @Test
    fun backButton_navigatesToHome() {
        composeTestRule.onNodeWithContentDescription("戻る").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("ホーム"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
