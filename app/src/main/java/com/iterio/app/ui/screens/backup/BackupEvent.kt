package com.iterio.app.ui.screens.backup

import android.content.Intent
import android.net.Uri

/**
 * バックアップ画面のイベント
 *
 * ViewModel への全ての UI イベントを統一的に扱う sealed class
 */
sealed class BackupEvent {
    // ローカルバックアップ
    /**
     * バックアップをエクスポート
     */
    data class ExportBackup(val uri: Uri) : BackupEvent()

    /**
     * バックアップをインポート
     */
    data class ImportBackup(val uri: Uri) : BackupEvent()

    /**
     * 状態をリセット
     */
    data object ResetState : BackupEvent()

    // クラウドバックアップ
    /**
     * Googleサインイン結果を処理
     */
    data class HandleGoogleSignInResult(val data: Intent?) : BackupEvent()

    /**
     * Googleからサインアウト
     */
    data object SignOutGoogle : BackupEvent()

    /**
     * クラウドにアップロード
     */
    data object UploadToCloud : BackupEvent()

    /**
     * クラウドからダウンロード
     */
    data object DownloadFromCloud : BackupEvent()

    /**
     * クラウド状態をリセット
     */
    data object ResetCloudState : BackupEvent()

    /**
     * トライアルを開始
     */
    data object StartTrial : BackupEvent()
}
