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
 * 復習スケジュール画面のE2Eテスト
 *
 * テスト項目:
 * - 復習スケジュール画面の表示確認
 * - フィルタチップ表示
 * - 空状態の表示
 * - 戻るボタンの動作
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ReviewScheduleScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        // ホーム画面のロードを確認（「今日の復習」セクションが表示されるまで待つ）
        composeTestRule.waitUntil(timeoutMillis = TestConstants.LONG_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("今日の復習", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        // "すべて見る"をスクロールして表示し、クリック
        composeTestRule.onNodeWithText("すべて見る", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        // 復習スケジュール画面への遷移を確認
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.REVIEW_SCHEDULE_TITLE, substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 復習スケジュール画面が正しく表示されることを確認
     */
    @Test
    fun reviewScheduleScreen_displaysCorrectly() {
        composeTestRule.onNodeWithText(TestConstants.REVIEW_SCHEDULE_TITLE).assertIsDisplayed()
    }

    /**
     * 戻るボタンが表示されることを確認
     */
    @Test
    fun backButton_isDisplayed() {
        composeTestRule.onNodeWithContentDescription("キャンセル").assertIsDisplayed()
    }

    /**
     * "すべて"フィルタチップが表示されることを確認
     */
    @Test
    fun filterChip_all_isDisplayed() {
        composeTestRule.onNodeWithText(TestConstants.REVIEW_FILTER_ALL).assertIsDisplayed()
    }

    /**
     * "未完了"フィルタチップが表示されることを確認
     */
    @Test
    fun filterChip_pending_isDisplayed() {
        composeTestRule.onNodeWithText(TestConstants.REVIEW_FILTER_PENDING).assertIsDisplayed()
    }

    /**
     * "完了"フィルタチップが表示されることを確認
     */
    @Test
    fun filterChip_completed_isDisplayed() {
        composeTestRule.onNodeWithText(TestConstants.REVIEW_FILTER_COMPLETED).assertIsDisplayed()
    }

    /**
     * "期限切れ"フィルタチップが表示されることを確認
     */
    @Test
    fun filterChip_overdue_isDisplayed() {
        composeTestRule.onNodeWithText(TestConstants.REVIEW_FILTER_OVERDUE).assertIsDisplayed()
    }

    /**
     * タスクがない場合に空状態メッセージが表示されることを確認
     */
    @Test
    fun emptyState_isDisplayedWhenNoTasks() {
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.REVIEW_SCHEDULE_EMPTY, substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * サマリー行に合計が表示されることを確認
     */
    @Test
    fun summaryRow_showsTotalCount() {
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("合計", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 戻るボタンでホームに遷移することを確認
     */
    @Test
    fun backButton_navigatesToHome() {
        composeTestRule.onNodeWithContentDescription("キャンセル").performClick()

        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.NAV_HOME))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * フィルタチップをクリックしてフィルタ切替が動作することを確認
     */
    @Test
    fun filterChip_canBeClicked() {
        composeTestRule.onNodeWithText(TestConstants.REVIEW_FILTER_PENDING).performClick()

        // フィルタが切り替わった後も画面が正しく表示される
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.REVIEW_FILTER_PENDING))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
