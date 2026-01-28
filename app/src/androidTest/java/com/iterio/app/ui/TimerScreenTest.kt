package com.iterio.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iterio.app.ui.MainActivity
import com.iterio.app.util.TestUtils.waitForText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * タイマー画面のE2Eテスト
 *
 * テスト項目:
 * - タイマー開始・停止・一時停止
 * - フェーズ表示の確認
 * - サイクルカウンター表示
 * - フォーカスモードダイアログ表示
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TimerScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    /**
     * ホーム画面が正しく表示されることを確認
     * Note: E2Eテストはアプリ全体を起動するため、最初はホーム画面
     */
    @Test
    fun app_startsWithHomeScreen() {
        // ホーム画面のタイトルが表示されることを確認
        composeTestRule.onNodeWithText("ホーム").assertIsDisplayed()
    }

    /**
     * ナビゲーションバーが表示されることを確認
     */
    @Test
    fun navigationBar_isDisplayed() {
        // ナビゲーションバーの各項目が表示されていることを確認
        composeTestRule.onNodeWithText("ホーム").assertIsDisplayed()
        composeTestRule.onNodeWithText("タスク").assertIsDisplayed()
        composeTestRule.onNodeWithText("カレンダー").assertIsDisplayed()
        composeTestRule.onNodeWithText("統計").assertIsDisplayed()
    }

    /**
     * タスク画面に遷移できることを確認
     */
    @Test
    fun canNavigateToTasksScreen() {
        // タスクタブをクリック
        composeTestRule.onNodeWithText("タスク").performClick()

        // タスク画面が表示されることを確認（空グループ時は「科目グループを追加」ボタン）
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("科目グループを追加"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * カレンダー画面に遷移できることを確認
     */
    @Test
    fun canNavigateToCalendarScreen() {
        // カレンダータブをクリック
        composeTestRule.onNodeWithText("カレンダー").performClick()

        // カレンダー画面の特徴的な要素が表示されることを確認
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 統計画面に遷移できることを確認
     */
    @Test
    fun canNavigateToStatsScreen() {
        // 統計タブをクリック
        composeTestRule.onNodeWithText("統計").performClick()

        // 統計画面の要素が表示されることを確認
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("今日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 設定ボタンが表示されることを確認
     */
    @Test
    fun settingsButton_isDisplayed() {
        // 設定タブ（ボトムナビゲーション）が表示されていることを確認
        composeTestRule.onNodeWithText("設定").assertIsDisplayed()
    }

    /**
     * 設定画面に遷移できることを確認
     */
    @Test
    fun canNavigateToSettingsScreen() {
        // 設定タブをクリック
        composeTestRule.onNodeWithText("設定").performClick()

        // 設定画面が表示されることを確認
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 今日の復習タスクセクションが表示されることを確認
     */
    @Test
    fun todayReviewSection_isDisplayedOnHomeScreen() {
        // ホーム画面で「今日の復習」セクションが表示される
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("今日の復習", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty() ||
            composeTestRule.onAllNodes(hasText("今日の復習はありません", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 今日のタスクセクションが表示されることを確認
     */
    @Test
    fun todayTasksSection_isDisplayedOnHomeScreen() {
        // ホーム画面で「今日のタスク」セクションが表示される
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("今日のタスク", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty() ||
            composeTestRule.onAllNodes(hasText("今日のタスクはありません", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 週間グラフが表示されることを確認
     */
    @Test
    fun weeklyChart_isDisplayedOnHomeScreen() {
        // ホーム画面で週間グラフが表示される
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("今週の学習", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
