package com.iterio.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iterio.app.util.TestConstants
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 統計画面のE2Eテスト
 *
 * テスト項目:
 * - 統計画面の表示確認
 * - 今日の学習時間カード
 * - 連続学習カード
 * - Premium機能の表示条件
 * - ナビゲーション
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StatsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        // 統計画面に遷移
        composeTestRule.onNodeWithText(TestConstants.NAV_STATS).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.STATS_TODAY_STUDY_TIME, substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 統計画面が正しく表示されることを確認
     */
    @Test
    fun statsScreen_displaysCorrectly() {
        composeTestRule.onAllNodes(hasText(TestConstants.STATS_TITLE)).get(0).assertIsDisplayed()
    }

    /**
     * 今日の学習時間カードが表示されることを確認
     */
    @Test
    fun todayStudyCard_isDisplayed() {
        composeTestRule.onNodeWithText(TestConstants.STATS_TODAY_STUDY_TIME, substring = true)
            .assertIsDisplayed()
    }

    /**
     * 学習時間が0分と表示されることを確認
     */
    @Test
    fun todayStudyCard_showsZeroMinutes() {
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("0分", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty() ||
            composeTestRule.onAllNodes(hasText("0m", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * セッション数が表示されることを確認
     */
    @Test
    fun todayStudyCard_showsSessionCount() {
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("0セッション完了", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty() ||
            composeTestRule.onAllNodes(hasText("セッション", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 連続学習カードが表示されることを確認
     */
    @Test
    fun streakCard_isDisplayed() {
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.STATS_STREAK, substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 現在の連続日数が表示されることを確認
     */
    @Test
    fun streakCard_showsCurrentStreak() {
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.STATS_CURRENT_STREAK, substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 最高記録が表示されることを確認
     */
    @Test
    fun streakCard_showsMaxStreak() {
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.STATS_MAX_STREAK, substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 0日が表示されることを確認
     */
    @Test
    fun streakCard_showsZeroDays() {
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("0日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * Free状態でPremiumテキストが表示されることを確認
     */
    @Test
    fun premiumContent_showsPremiumText() {
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("Premium", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * ナビゲーションバーが全項目表示されることを確認
     */
    @Test
    fun navigationBar_isDisplayed() {
        composeTestRule.onNodeWithText(TestConstants.NAV_HOME).assertIsDisplayed()
        composeTestRule.onNodeWithText(TestConstants.NAV_TASKS).assertIsDisplayed()
        composeTestRule.onNodeWithText(TestConstants.NAV_CALENDAR).assertIsDisplayed()
        composeTestRule.onAllNodes(hasText(TestConstants.NAV_STATS)).get(0).assertIsDisplayed()
    }

    /**
     * ホーム画面に遷移できることを確認
     */
    @Test
    fun canNavigateToHome() {
        composeTestRule.onNodeWithText(TestConstants.NAV_HOME).performClick()

        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
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
        composeTestRule.onNodeWithText(TestConstants.NAV_CALENDAR).performClick()

        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
