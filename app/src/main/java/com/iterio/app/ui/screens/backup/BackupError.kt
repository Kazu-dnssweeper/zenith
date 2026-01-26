package com.iterio.app.ui.screens.backup

import com.google.gson.JsonSyntaxException
import com.iterio.app.data.cloud.NoBackupFoundException
import com.iterio.app.data.encryption.EncryptionException
import com.iterio.app.domain.usecase.PremiumRequiredException
import java.io.FileNotFoundException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * バックアップ操作のエラー種別
 * ユーザーフレンドリーなメッセージを提供
 */
sealed class BackupError(val message: String) {
    object PremiumRequired : BackupError("この機能を使用するにはPremiumへのアップグレードが必要です")
    object FileNotFound : BackupError("ファイルが見つかりません")
    object InvalidFormat : BackupError("ファイル形式が不正です。Iterioで作成したバックアップファイルを選択してください")
    object DecryptionFailed : BackupError("復号化に失敗しました。別のデバイスで作成されたバックアップの可能性があります")
    object EncryptionFailed : BackupError("暗号化に失敗しました。ストレージの空き容量を確認してください")
    object VersionMismatch : BackupError("このバックアップは新しいバージョンのアプリで作成されています。アプリをアップデートしてください")
    object NoCloudBackup : BackupError("クラウドにバックアップがありません")
    object NetworkError : BackupError("ネットワークエラーが発生しました。接続を確認してください")
    object StorageError : BackupError("ストレージへのアクセスに失敗しました。空き容量と権限を確認してください")
    data class Unknown(val detail: String) : BackupError("エラーが発生しました: $detail")

    companion object {
        /**
         * 例外からBackupErrorに変換
         */
        fun fromException(e: Throwable): BackupError {
            return when (e) {
                is PremiumRequiredException -> PremiumRequired
                is NoBackupFoundException -> NoCloudBackup
                is EncryptionException -> {
                    if (e.message?.contains("復号") == true || e.message?.contains("decrypt") == true) {
                        DecryptionFailed
                    } else {
                        EncryptionFailed
                    }
                }
                is JsonSyntaxException -> InvalidFormat
                is FileNotFoundException -> FileNotFound
                is UnknownHostException, is SocketTimeoutException -> NetworkError
                is IOException -> {
                    val msg = e.message?.lowercase() ?: ""
                    when {
                        msg.contains("space") || msg.contains("容量") -> StorageError
                        msg.contains("permission") || msg.contains("権限") -> StorageError
                        msg.contains("network") || msg.contains("connect") -> NetworkError
                        else -> Unknown(e.message ?: "不明なI/Oエラー")
                    }
                }
                else -> {
                    val msg = e.message ?: "不明なエラー"
                    when {
                        msg.contains("新しいバージョン") || msg.contains("version") -> VersionMismatch
                        msg.contains("形式が不正") || msg.contains("invalid format") -> InvalidFormat
                        msg.contains("復号") || msg.contains("decrypt") -> DecryptionFailed
                        msg.contains("暗号") || msg.contains("encrypt") -> EncryptionFailed
                        msg.contains("開けません") || msg.contains("not found") -> FileNotFound
                        else -> Unknown(msg)
                    }
                }
            }
        }
    }
}
