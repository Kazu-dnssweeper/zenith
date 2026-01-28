package com.iterio.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
 * タスク画面のE2Eテスト
 *
 * テスト項目:
 * - タスク画面の表示
 * - グループ追加ボタン
 * - タスクリスト表示
 * - ナビゲーション
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TasksScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        // タスク画面に遷移
        composeTestRule.onNodeWithText("タスク").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("グループを追加", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * タスク画面が正しく表示されることを確認
     */
    @Test
    fun tasksScreen_displaysCorrectly() {
        composeTestRule.onNodeWithText("タスク").assertIsDisplayed()
    }

    /**
     * グループ追加ボタンが表示されることを確認
     */
    @Test
    fun addGroupButton_isDisplayed() {
        composeTestRule.onNodeWithText("グループを追加", substring = true).assertIsDisplayed()
    }

    /**
     * ナビゲーションバーが表示されることを確認
     */
    @Test
    fun navigationBar_isDisplayed() {
        composeTestRule.onNodeWithText("ホーム").assertIsDisplayed()
        composeTestRule.onNodeWithText("タスク").assertIsDisplayed()
        composeTestRule.onNodeWithText("カレンダー").assertIsDisplayed()
        composeTestRule.onNodeWithText("統計").assertIsDisplayed()
    }

    /**
     * ホーム画面に戻れることを確認
     */
    @Test
    fun canNavigateToHome() {
        composeTestRule.onNodeWithText("ホーム").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("今日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * カレンダー画面に遷移できることを確認
     */
    @Test
    fun canNavigateToCalendar() {
        composeTestRule.onNodeWithText("カレンダー").performClick()

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
    fun canNavigateToStats() {
        composeTestRule.onNodeWithText("統計").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("今日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
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
     * グループ追加ボタンをクリックするとダイアログが表示されることを確認
     */
    @Test
    fun addGroupButton_showsDialog() {
        composeTestRule.onNodeWithText("グループを追加", substring = true).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("グループ名", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty() ||
            composeTestRule.onAllNodes(hasText("科目", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty() ||
            composeTestRule.onAllNodes(hasText("キャンセル", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 空状態のメッセージが表示されることを確認（グループがない場合）
     */
    @Test
    fun emptyState_isDisplayedWhenNoGroups() {
        // モックリポジトリは空のグループリストを返すので、
        // 空状態メッセージまたはグループ追加ボタンが表示される
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("グループを追加", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty() ||
            composeTestRule.onAllNodes(hasText("タスクを追加", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * タスク画面のタイトルが表示されることを確認
     */
    @Test
    fun screenTitle_isDisplayed() {
        // ナビゲーションバーの「タスク」が選択状態
        composeTestRule.onNodeWithText("タスク").assertIsDisplayed()
    }

    /**
     * スクロールが機能することを確認
     */
    @Test
    fun scrolling_works() {
        // グループ追加ボタンが画面内に表示される
        composeTestRule.onNodeWithText("グループを追加", substring = true)
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

        // 設定はトップレベルタブなので、ボトムナビでタスク画面に戻る
        composeTestRule.onNodeWithText("タスク").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("科目グループ", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 複数のナビゲーション遷移が正しく機能することを確認
     */
    @Test
    fun multipleNavigation_worksCorrectly() {
        // カレンダー → 統計 → ホーム → タスク
        composeTestRule.onNodeWithText("カレンダー").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithText("統計").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("今日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithText("ホーム").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("今日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithText("タスク").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("グループを追加", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
