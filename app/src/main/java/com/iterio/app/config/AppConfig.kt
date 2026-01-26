package com.iterio.app.config

/**
 * アプリケーション全体の設定値を集約
 * マジックナンバーを避け、一元管理することで保守性を向上
 */
object AppConfig {

    /**
     * タイマー関連の設定値
     */
    object Timer {
        /** デフォルトの作業時間（分） */
        const val DEFAULT_WORK_MINUTES = 25

        /** デフォルトの短い休憩時間（分） */
        const val DEFAULT_SHORT_BREAK_MINUTES = 5

        /** デフォルトの長い休憩時間（分） */
        const val DEFAULT_LONG_BREAK_MINUTES = 15

        /** デフォルトのサイクル数 */
        const val DEFAULT_CYCLES = 4

        /** 最小作業時間（分） */
        const val MIN_WORK_MINUTES = 1

        /** 最大作業時間（分） */
        const val MAX_WORK_MINUTES = 120

        /** 最小休憩時間（分） */
        const val MIN_BREAK_MINUTES = 1

        /** 最大休憩時間（分） */
        const val MAX_BREAK_MINUTES = 60

        /** 最小サイクル数 */
        const val MIN_CYCLES = 1

        /** 最大サイクル数 */
        const val MAX_CYCLES = 10
    }

    /**
     * プレミアム機能関連の設定値
     */
    object Premium {
        /** プレミアム版の復習間隔（日） */
        val PREMIUM_REVIEW_INTERVALS = listOf(1, 3, 7, 14, 30, 60)

        /** 無料版の復習間隔（日） */
        val FREE_REVIEW_INTERVALS = listOf(1, 3)

        /** トライアル期間（日） */
        const val TRIAL_DURATION_DAYS = 3L

        /** プレミアム版の復習回数オプション */
        val PREMIUM_REVIEW_COUNT_OPTIONS = listOf(2, 4, 6)

        /** 無料版の復習回数オプション */
        val FREE_REVIEW_COUNT_OPTIONS = listOf(2)

        /** プレミアム版のデフォルト復習回数 */
        const val DEFAULT_REVIEW_COUNT_PREMIUM = 6

        /** 無料版のデフォルト復習回数 */
        const val DEFAULT_REVIEW_COUNT_FREE = 2

        /** 復習回数の最小値 */
        const val MIN_REVIEW_COUNT = 1

        /** 無料版の復習回数の最大値 */
        const val MAX_REVIEW_COUNT_FREE = 2

        /** プレミアム版の復習回数の最大値 */
        const val MAX_REVIEW_COUNT_PREMIUM = 6

        /**
         * 復習回数に応じた復習間隔を取得
         * @param count 復習回数
         * @param isPremium プレミアムかどうか
         * @return 復習間隔のリスト（日数）
         */
        fun getIntervalsForCount(count: Int, isPremium: Boolean): List<Int> {
            val intervals = if (isPremium) PREMIUM_REVIEW_INTERVALS else FREE_REVIEW_INTERVALS
            return intervals.take(count)
        }
    }

    /**
     * 日次目標関連の設定値
     */
    object DailyGoal {
        /** デフォルトの日次目標（分） */
        const val DEFAULT_MINUTES = 60

        /** 最小日次目標（分） */
        const val MIN_MINUTES = 15

        /** 最大日次目標（分） */
        const val MAX_MINUTES = 480

        /** 日次目標の刻み幅（分） */
        const val STEP_MINUTES = 15
    }

    /**
     * UI関連の設定値
     */
    object UI {
        /** 検索デバウンス時間（ミリ秒） */
        const val SEARCH_DEBOUNCE_MS = 300L

        /** アニメーション時間（ミリ秒） */
        const val ANIMATION_DURATION_MS = 300L

        /** スナックバー表示時間（ミリ秒） */
        const val SNACKBAR_DURATION_MS = 3000L
    }

    /**
     * バックアップ関連の設定値
     */
    object Backup {
        /** バックアップファイル名のプレフィックス */
        const val FILE_NAME_PREFIX = "iterio_backup"

        /** クラウドバックアップのフォルダ名 */
        const val CLOUD_FOLDER_NAME = "Iterio"

        /** バックアップファイルのMIMEタイプ */
        const val MIME_TYPE = "application/json"
    }

    /**
     * ウィジェット関連の設定値
     */
    object Widget {
        /** ウィジェット更新間隔（ミリ秒） */
        const val UPDATE_INTERVAL_MS = 1000L
    }
}
