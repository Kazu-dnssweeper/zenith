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
 * カレンダー画面のE2Eテスト
 *
 * テスト項目:
 * - カレンダー画面の表示確認
 * - 月ヘッダー・曜日ヘッダー表示
 * - Premium機能のロック表示
 * - ボトムナビゲーション
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CalendarScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        // カレンダー画面に遷移
        composeTestRule.onNodeWithText(TestConstants.NAV_CALENDAR).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * カレンダー画面が正しく表示されることを確認
     */
    @Test
    fun calendarScreen_displaysCorrectly() {
        composeTestRule.onAllNodes(hasText(TestConstants.CALENDAR_TITLE)).get(0).assertIsDisplayed()
    }

    /**
     * 月ヘッダーに年月テキストが表示されることを確認
     */
    @Test
    fun monthHeader_displaysCurrentMonth() {
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("年", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 曜日ヘッダーが全日表示されることを確認
     */
    @Test
    fun weekdayHeader_displaysAllDays() {
        val weekdays = listOf("日", "月", "火", "水", "木", "金", "土")
        weekdays.forEach { day ->
            composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
                composeTestRule.onAllNodes(hasText(day, substring = true))
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
        }
    }

    /**
     * Free状態でPremiumテキストが表示されることを確認
     */
    @Test
    fun freeUser_showsLockedFeatureCard() {
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
        composeTestRule.onAllNodes(hasText(TestConstants.NAV_CALENDAR)).get(0).assertIsDisplayed()
        composeTestRule.onNodeWithText(TestConstants.NAV_STATS).assertIsDisplayed()
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
     * タスク画面に遷移できることを確認
     */
    @Test
    fun canNavigateToTasks() {
        composeTestRule.onNodeWithText(TestConstants.NAV_TASKS).performClick()

        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("科目グループ", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 統計画面に遷移できることを確認
     */
    @Test
    fun canNavigateToStats() {
        composeTestRule.onNodeWithText(TestConstants.NAV_STATS).performClick()

        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.STATS_TODAY_STUDY_TIME, substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 設定画面に遷移できることを確認
     */
    @Test
    fun canNavigateToSettings() {
        composeTestRule.onNodeWithText(TestConstants.NAV_SETTINGS).performClick()

        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 複数のナビゲーション遷移が正しく機能することを確認
     * カレンダー → 統計 → ホーム → カレンダー
     */
    @Test
    fun multipleNavigation_worksFromCalendar() {
        // 統計に遷移
        composeTestRule.onNodeWithText(TestConstants.NAV_STATS).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.STATS_TODAY_STUDY_TIME, substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // ホームに遷移
        composeTestRule.onNodeWithText(TestConstants.NAV_HOME).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("今日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // カレンダーに戻る
        composeTestRule.onNodeWithText(TestConstants.NAV_CALENDAR).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
