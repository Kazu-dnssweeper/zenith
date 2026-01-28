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
 * マルチスクリーンユーザーフローのE2Eテスト
 *
 * テスト項目:
 * - ボトムナビゲーション全タブ巡回
 * - 設定画面の遷移と戻り
 * - 復習スケジュール画面の遷移と戻り
 * - 深い階層のナビゲーション
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UserFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    /**
     * ボトムナビゲーション全タブを巡回できることを確認
     * ホーム → タスク → カレンダー → 統計 → ホーム
     */
    @Test
    fun fullBottomNavRoundTrip() {
        // ホーム画面の確認
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("今日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // タスクに遷移
        composeTestRule.onNodeWithText(TestConstants.NAV_TASKS).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("科目グループ", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // カレンダーに遷移
        composeTestRule.onNodeWithText(TestConstants.NAV_CALENDAR).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // 統計に遷移
        composeTestRule.onNodeWithText(TestConstants.NAV_STATS).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.STATS_TODAY_STUDY_TIME, substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // ホームに戻る
        composeTestRule.onNodeWithText(TestConstants.NAV_HOME).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("今日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * ホームから設定画面への遷移と戻りを確認
     */
    @Test
    fun homeToSettingsAndBack() {
        // 設定に遷移
        composeTestRule.onNodeWithText(TestConstants.NAV_SETTINGS).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onAllNodes(hasText(TestConstants.SETTINGS_TITLE)).get(0).assertIsDisplayed()

        // ホームに戻る（設定はトップレベルタブなのでボトムナビを使用）
        composeTestRule.onNodeWithText(TestConstants.NAV_HOME).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("Iterio"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * ホームから復習スケジュール画面への遷移と戻りを確認
     */
    @Test
    fun homeToReviewScheduleAndBack() {
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

        // 復習スケジュール画面確認
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.REVIEW_SCHEDULE_TITLE, substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // 戻る（キャンセルボタン）
        composeTestRule.onNodeWithContentDescription("キャンセル").performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.NAV_HOME))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 設定からバックアップ画面への遷移と戻りを確認
     */
    @Test
    fun settingsToBackupAndBack() {
        // 設定に遷移
        composeTestRule.onNodeWithText(TestConstants.NAV_SETTINGS).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // バックアップに遷移
        composeTestRule.onAllNodes(hasText(TestConstants.SETTINGS_BACKUP, substring = true)).get(0)
            .performScrollTo()
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("ローカルバックアップ", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // 戻る
        composeTestRule.onNodeWithContentDescription("戻る").performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.SETTINGS_TITLE))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 設定からPremium画面への遷移と戻りを確認
     */
    @Test
    fun settingsToPremiumAndBack() {
        // 設定に遷移
        composeTestRule.onNodeWithText(TestConstants.NAV_SETTINGS).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Premiumに遷移
        composeTestRule.onNodeWithText("Premiumにアップグレード")
            .performScrollTo()
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("Premium", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // 戻る
        composeTestRule.onNodeWithContentDescription("戻る").performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.SETTINGS_TITLE))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * 深い階層のナビゲーションを確認
     * 設定 → バックアップ → 戻る → Premium → 戻る → 戻る → ホーム
     */
    @Test
    fun deepNavigation_settingsBackupAndPremium() {
        // 設定に遷移
        composeTestRule.onNodeWithText(TestConstants.NAV_SETTINGS).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // バックアップに遷移
        composeTestRule.onAllNodes(hasText(TestConstants.SETTINGS_BACKUP, substring = true)).get(0)
            .performScrollTo()
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("ローカルバックアップ", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // 設定に戻る
        composeTestRule.onNodeWithContentDescription("戻る").performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Premiumに遷移
        composeTestRule.onNodeWithText("Premiumにアップグレード")
            .performScrollTo()
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("Premium", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // 設定に戻る
        composeTestRule.onNodeWithContentDescription("戻る").performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("ポモドーロタイマー", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // ホームに戻る（設定はトップレベルタブなのでボトムナビを使用）
        composeTestRule.onNodeWithText(TestConstants.NAV_HOME).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("Iterio"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * クロスタブナビゲーションを確認
     * タスク → カレンダー → 統計
     */
    @Test
    fun crossTabNavigation_tasksToCalendarToStats() {
        // タスクに遷移
        composeTestRule.onNodeWithText(TestConstants.NAV_TASKS).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("科目グループ", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // カレンダーに遷移
        composeTestRule.onNodeWithText(TestConstants.NAV_CALENDAR).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("日", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // 統計に遷移
        composeTestRule.onNodeWithText(TestConstants.NAV_STATS).performClick()
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(TestConstants.STATS_TODAY_STUDY_TIME, substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * ホーム画面で全セクションが表示されることを確認
     */
    @Test
    fun homeScreen_showsAllSections() {
        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("今日のタスク", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("今日の復習", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText("週", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
