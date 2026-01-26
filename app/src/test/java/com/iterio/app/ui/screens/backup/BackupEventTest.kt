package com.iterio.app.ui.screens.backup

import android.content.Intent
import android.net.Uri
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

/**
 * BackupEvent のユニットテスト
 */
class BackupEventTest {

    // Local backup events

    @Test
    fun `ExportBackup contains uri`() {
        val uri = mockk<Uri>()
        val event = BackupEvent.ExportBackup(uri)
        assertSame(uri, event.uri)
    }

    @Test
    fun `ImportBackup contains uri`() {
        val uri = mockk<Uri>()
        val event = BackupEvent.ImportBackup(uri)
        assertSame(uri, event.uri)
    }

    @Test
    fun `ResetState is a singleton object`() {
        val event1 = BackupEvent.ResetState
        val event2 = BackupEvent.ResetState
        assertSame(event1, event2)
    }

    // Cloud backup events

    @Test
    fun `HandleGoogleSignInResult contains nullable intent`() {
        val intent = mockk<Intent>()
        val event = BackupEvent.HandleGoogleSignInResult(intent)
        assertSame(intent, event.data)

        val nullEvent = BackupEvent.HandleGoogleSignInResult(null)
        assertNull(nullEvent.data)
    }

    @Test
    fun `SignOutGoogle is a singleton object`() {
        assertSame(BackupEvent.SignOutGoogle, BackupEvent.SignOutGoogle)
    }

    @Test
    fun `UploadToCloud is a singleton object`() {
        assertSame(BackupEvent.UploadToCloud, BackupEvent.UploadToCloud)
    }

    @Test
    fun `DownloadFromCloud is a singleton object`() {
        assertSame(BackupEvent.DownloadFromCloud, BackupEvent.DownloadFromCloud)
    }

    @Test
    fun `ResetCloudState is a singleton object`() {
        assertSame(BackupEvent.ResetCloudState, BackupEvent.ResetCloudState)
    }

    @Test
    fun `StartTrial is a singleton object`() {
        assertSame(BackupEvent.StartTrial, BackupEvent.StartTrial)
    }

    // Exhaustive event check

    @Test
    fun `all events are BackupEvent subtypes`() {
        val mockUri = mockk<Uri>()
        val mockIntent = mockk<Intent>()

        val events: List<BackupEvent> = listOf(
            BackupEvent.ExportBackup(mockUri),
            BackupEvent.ImportBackup(mockUri),
            BackupEvent.ResetState,
            BackupEvent.HandleGoogleSignInResult(mockIntent),
            BackupEvent.SignOutGoogle,
            BackupEvent.UploadToCloud,
            BackupEvent.DownloadFromCloud,
            BackupEvent.ResetCloudState,
            BackupEvent.StartTrial
        )
        assertEquals(9, events.size)
    }
}
