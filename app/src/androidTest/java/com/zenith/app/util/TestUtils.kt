package com.zenith.app.util

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

/**
 * UI テスト用のユーティリティクラス
 */
object TestUtils {

    /**
     * 指定されたテキストを持つノードが表示されるまで待機
     */
    fun ComposeTestRule.waitForText(
        text: String,
        timeoutMillis: Long = 5000
    ): SemanticsNodeInteraction {
        waitUntil(timeoutMillis) {
            onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        return onNodeWithText(text)
    }

    /**
     * 指定されたContentDescriptionを持つノードが表示されるまで待機
     */
    fun ComposeTestRule.waitForContentDescription(
        contentDescription: String,
        timeoutMillis: Long = 5000
    ): SemanticsNodeInteraction {
        waitUntil(timeoutMillis) {
            onAllNodesWithContentDescription(contentDescription).fetchSemanticsNodes().isNotEmpty()
        }
        return onNodeWithContentDescription(contentDescription)
    }

    /**
     * テキストをクリックして操作を実行
     */
    fun ComposeTestRule.clickText(text: String) {
        onNodeWithText(text).performClick()
    }

    /**
     * ContentDescriptionをクリックして操作を実行
     */
    fun ComposeTestRule.clickContentDescription(contentDescription: String) {
        onNodeWithContentDescription(contentDescription).performClick()
    }

    /**
     * テキストが表示されていることを検証
     */
    fun ComposeTestRule.assertTextDisplayed(text: String) {
        onNodeWithText(text).assertIsDisplayed()
    }

    /**
     * ContentDescriptionが表示されていることを検証
     */
    fun ComposeTestRule.assertContentDescriptionDisplayed(contentDescription: String) {
        onNodeWithContentDescription(contentDescription).assertIsDisplayed()
    }

    /**
     * テキストが消えるまで待機
     */
    fun ComposeTestRule.waitForTextToDisappear(
        text: String,
        timeoutMillis: Long = 5000
    ) {
        waitUntil(timeoutMillis) {
            onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()
        }
    }

    /**
     * 指定された条件が満たされるまで待機
     */
    fun ComposeTestRule.waitForCondition(
        timeoutMillis: Long = 5000,
        condition: () -> Boolean
    ) {
        waitUntil(timeoutMillis) {
            condition()
        }
    }
}

/**
 * テスト用の定数
 */
object TestConstants {
    // タイマー画面
    const val TIMER_START_BUTTON = "開始"
    const val TIMER_PAUSE_BUTTON = "一時停止"
    const val TIMER_RESUME_BUTTON = "再開"
    const val TIMER_STOP_BUTTON = "停止"
    const val TIMER_SKIP_BUTTON = "スキップ"

    // タイマーフェーズ
    const val PHASE_WORK = "作業中"
    const val PHASE_SHORT_BREAK = "休憩中"
    const val PHASE_LONG_BREAK = "長休憩中"
    const val PHASE_IDLE = "準備完了"

    // フォーカスモード
    const val FOCUS_MODE_DIALOG_TITLE = "フォーカスモードを開始しますか？"
    const val FOCUS_MODE_START = "開始する"
    const val FOCUS_MODE_CANCEL = "キャンセル"
    const val STRICT_MODE_BLOCKED_TITLE = "終了できません"
    const val STRICT_MODE_BLOCKED_CONFIRM = "わかりました"

    // 設定画面
    const val SETTINGS_TITLE = "設定"
    const val SETTINGS_NOTIFICATIONS = "リマインダー通知"
    const val SETTINGS_FOCUS_MODE = "フォーカスモードを有効化"
    const val SETTINGS_STRICT_MODE = "完全ロックモード（デフォルト）"
    const val SETTINGS_AUTO_LOOP = "自動ループ"
    const val SETTINGS_BACKUP = "バックアップ"
    const val SETTINGS_ALLOWED_APPS = "許可アプリ"

    // プレミアム画面
    const val PREMIUM_TITLE = "Premium"
    const val PREMIUM_TRIAL_BUTTON = "7日間無料で試す"
    const val PREMIUM_PURCHASE_BUTTON = "購入する"
    const val PREMIUM_RESTORE_BUTTON = "購入を復元"
    const val PREMIUM_MONTHLY = "月額プラン"
    const val PREMIUM_YEARLY = "年額プラン"
    const val PREMIUM_LIFETIME = "買い切り"

    // バックアップ画面
    const val BACKUP_TITLE = "バックアップ"
    const val BACKUP_EXPORT = "エクスポート"
    const val BACKUP_IMPORT = "インポート"
    const val BACKUP_CLOUD = "クラウドバックアップ"
    const val BACKUP_GOOGLE_CONNECT = "Googleアカウントに接続"

    // ダイアログ
    const val DIALOG_CONFIRM = "確認"
    const val DIALOG_CANCEL = "キャンセル"
    const val DIALOG_OK = "OK"
    const val DIALOG_YES = "はい"
    const val DIALOG_NO = "いいえ"

    // タイムアウト
    const val DEFAULT_TIMEOUT = 5000L
    const val LONG_TIMEOUT = 10000L
    const val ANIMATION_TIMEOUT = 1000L
}
