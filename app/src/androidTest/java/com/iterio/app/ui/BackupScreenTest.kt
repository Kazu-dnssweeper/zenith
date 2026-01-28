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
 * バックアップ画面のE2Eテスト
 *
 * テスト項目:
 * - ローカルバックアップUI表示
 * - クラウドバックアップUI表示
 * - エクスポート・インポートボタン
 * - Premium機能の表示条件
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BackupScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        // 設定画面に遷移
        composeTestRule.onNodeWithText("設定").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        // バックアップ画面に遷移
        composeTestRule.onNodeWithText("バックアップ", substring = true)
            .performScrollTo()
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("ローカルバックアップ", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * バックアップ画面が正しく表示されることを確認
     */
    @Test
    fun backupScreen_displaysCorrectly() {
        composeTestRule.onAllNodes(hasText("バックアップ")).get(0).assertIsDisplayed()
    }

    /**
     * 戻るボタンが表示されることを確認
     */
    @Test
    fun backButton_isDisplayed() {
        composeTestRule.onNodeWithContentDescription("戻る").assertIsDisplayed()
    }

    /**
     * ローカルバックアップセクションが表示されることを確認
     */
    @Test
    fun localBackupSection_isDisplayed() {
        composeTestRule.onAllNodes(hasText("ローカルバックアップ", substring = true)).get(0).assertIsDisplayed()
    }

    /**
     * エクスポートセクションが表示されることを確認
     */
    @Test
    fun exportSection_isDisplayed() {
        composeTestRule.onAllNodes(hasText("エクスポート", substring = true)).get(0).assertIsDisplayed()
    }

    /**
     * インポートセクションが表示されることを確認
     */
    @Test
    fun importSection_isDisplayed() {
        composeTestRule.onAllNodes(hasText("インポート", substring = true)).get(0).assertIsDisplayed()
    }

    /**
     * クラウドバックアップセクションが表示されることを確認
     */
    @Test
    fun cloudBackupSection_isDisplayed() {
        composeTestRule.onAllNodes(hasText("クラウドバックアップ", substring = true))
            .get(0)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Google Driveセクションが表示されることを確認
     */
    @Test
    fun googleDriveSection_isDisplayed() {
        composeTestRule.onAllNodes(hasText("Google Drive", substring = true))
            .get(0)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Premium機能のロック表示が確認できること
     */
    @Test
    fun premiumLockedSection_isDisplayed() {
        // Premium未加入時、何らかのロック表示がある
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Premium", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty() ||
            composeTestRule.onAllNodes(hasText("ロック", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
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
     * 注意事項や説明テキストが表示されることを確認
     */
    @Test
    fun explanationText_isDisplayed() {
        // エクスポートの説明テキストが表示されている
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("データ", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
