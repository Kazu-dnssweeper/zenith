package com.iterio.app.ui.screens.backup

import com.google.gson.JsonSyntaxException
import com.iterio.app.data.cloud.NoBackupFoundException
import com.iterio.app.data.encryption.EncryptionException
import com.iterio.app.domain.usecase.PremiumRequiredException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.FileNotFoundException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * BackupError.fromException() の全分岐テスト
 */
class BackupErrorTest {

    // ==================== 特定例外 → BackupError 変換 ====================

    @Test
    fun `fromException maps PremiumRequiredException to PremiumRequired`() {
        val error = BackupError.fromException(PremiumRequiredException())
        assertEquals(BackupError.PremiumRequired, error)
    }

    @Test
    fun `fromException maps NoBackupFoundException to NoCloudBackup`() {
        val error = BackupError.fromException(NoBackupFoundException("No backup"))
        assertEquals(BackupError.NoCloudBackup, error)
    }

    @Test
    fun `fromException maps EncryptionException with decrypt message to DecryptionFailed`() {
        val error = BackupError.fromException(EncryptionException("復号に失敗"))
        assertEquals(BackupError.DecryptionFailed, error)
    }

    @Test
    fun `fromException maps EncryptionException with english decrypt to DecryptionFailed`() {
        val error = BackupError.fromException(EncryptionException("Failed to decrypt data"))
        assertEquals(BackupError.DecryptionFailed, error)
    }

    @Test
    fun `fromException maps EncryptionException without decrypt keyword to EncryptionFailed`() {
        val error = BackupError.fromException(EncryptionException("暗号化エラー"))
        assertEquals(BackupError.EncryptionFailed, error)
    }

    @Test
    fun `fromException maps JsonSyntaxException to InvalidFormat`() {
        val error = BackupError.fromException(JsonSyntaxException("Invalid JSON"))
        assertEquals(BackupError.InvalidFormat, error)
    }

    @Test
    fun `fromException maps FileNotFoundException to FileNotFound`() {
        val error = BackupError.fromException(FileNotFoundException("file.json"))
        assertEquals(BackupError.FileNotFound, error)
    }

    @Test
    fun `fromException maps UnknownHostException to NetworkError`() {
        val error = BackupError.fromException(UnknownHostException("host not found"))
        assertEquals(BackupError.NetworkError, error)
    }

    @Test
    fun `fromException maps SocketTimeoutException to NetworkError`() {
        val error = BackupError.fromException(SocketTimeoutException("timeout"))
        assertEquals(BackupError.NetworkError, error)
    }

    // ==================== IOException メッセージ分岐 ====================

    @Test
    fun `fromException maps IOException with space message to StorageError`() {
        val error = BackupError.fromException(IOException("No space left on device"))
        assertEquals(BackupError.StorageError, error)
    }

    @Test
    fun `fromException maps IOException with 容量 message to StorageError`() {
        val error = BackupError.fromException(IOException("ストレージ容量が不足"))
        assertEquals(BackupError.StorageError, error)
    }

    @Test
    fun `fromException maps IOException with permission message to StorageError`() {
        val error = BackupError.fromException(IOException("permission denied"))
        assertEquals(BackupError.StorageError, error)
    }

    @Test
    fun `fromException maps IOException with 権限 message to StorageError`() {
        val error = BackupError.fromException(IOException("権限がありません"))
        assertEquals(BackupError.StorageError, error)
    }

    @Test
    fun `fromException maps IOException with network message to NetworkError`() {
        val error = BackupError.fromException(IOException("network unreachable"))
        assertEquals(BackupError.NetworkError, error)
    }

    @Test
    fun `fromException maps IOException with connect message to NetworkError`() {
        val error = BackupError.fromException(IOException("failed to connect"))
        assertEquals(BackupError.NetworkError, error)
    }

    @Test
    fun `fromException maps IOException with unknown message to Unknown`() {
        val error = BackupError.fromException(IOException("some unknown io error"))
        assertTrue(error is BackupError.Unknown)
        assertEquals("エラーが発生しました: some unknown io error", error.message)
    }

    @Test
    fun `fromException maps IOException with null message to Unknown`() {
        val error = BackupError.fromException(IOException())
        assertTrue(error is BackupError.Unknown)
    }

    // ==================== 一般例外のメッセージ分岐 ====================

    @Test
    fun `fromException maps general exception with version message to VersionMismatch`() {
        val error = BackupError.fromException(RuntimeException("新しいバージョンのアプリが必要"))
        assertEquals(BackupError.VersionMismatch, error)
    }

    @Test
    fun `fromException maps general exception with english version to VersionMismatch`() {
        val error = BackupError.fromException(RuntimeException("incompatible version"))
        assertEquals(BackupError.VersionMismatch, error)
    }

    @Test
    fun `fromException maps general exception with format message to InvalidFormat`() {
        val error = BackupError.fromException(RuntimeException("形式が不正です"))
        assertEquals(BackupError.InvalidFormat, error)
    }

    @Test
    fun `fromException maps general exception with english invalid format to InvalidFormat`() {
        val error = BackupError.fromException(RuntimeException("invalid format detected"))
        assertEquals(BackupError.InvalidFormat, error)
    }

    @Test
    fun `fromException maps general exception with decrypt message to DecryptionFailed`() {
        val error = BackupError.fromException(RuntimeException("復号に失敗しました"))
        assertEquals(BackupError.DecryptionFailed, error)
    }

    @Test
    fun `fromException maps general exception with english decrypt to DecryptionFailed`() {
        val error = BackupError.fromException(RuntimeException("failed to decrypt"))
        assertEquals(BackupError.DecryptionFailed, error)
    }

    @Test
    fun `fromException maps general exception with encrypt message to EncryptionFailed`() {
        val error = BackupError.fromException(RuntimeException("暗号化に失敗"))
        assertEquals(BackupError.EncryptionFailed, error)
    }

    @Test
    fun `fromException maps general exception with english encrypt to EncryptionFailed`() {
        val error = BackupError.fromException(RuntimeException("failed to encrypt"))
        assertEquals(BackupError.EncryptionFailed, error)
    }

    @Test
    fun `fromException maps general exception with not found message to FileNotFound`() {
        val error = BackupError.fromException(RuntimeException("file not found"))
        assertEquals(BackupError.FileNotFound, error)
    }

    @Test
    fun `fromException maps general exception with 開けません message to FileNotFound`() {
        val error = BackupError.fromException(RuntimeException("ファイルが開けません"))
        assertEquals(BackupError.FileNotFound, error)
    }

    @Test
    fun `fromException maps general exception with unknown message to Unknown`() {
        val error = BackupError.fromException(RuntimeException("something went wrong"))
        assertTrue(error is BackupError.Unknown)
        assertEquals("エラーが発生しました: something went wrong", error.message)
    }

    @Test
    fun `fromException maps general exception with null message to Unknown`() {
        val error = BackupError.fromException(RuntimeException())
        assertTrue(error is BackupError.Unknown)
    }

    // ==================== BackupError メッセージ確認 ====================

    @Test
    fun `PremiumRequired has correct message`() {
        assertEquals(
            "この機能を使用するにはPremiumへのアップグレードが必要です",
            BackupError.PremiumRequired.message
        )
    }

    @Test
    fun `NetworkError has correct message`() {
        assertEquals(
            "ネットワークエラーが発生しました。接続を確認してください",
            BackupError.NetworkError.message
        )
    }

    @Test
    fun `StorageError has correct message`() {
        assertEquals(
            "ストレージへのアクセスに失敗しました。空き容量と権限を確認してください",
            BackupError.StorageError.message
        )
    }

    @Test
    fun `Unknown error includes detail in message`() {
        val error = BackupError.Unknown("テスト詳細")
        assertEquals("エラーが発生しました: テスト詳細", error.message)
    }
}
