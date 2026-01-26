package com.iterio.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.iterio.app.R
import com.iterio.app.domain.repository.PremiumRepository
import com.iterio.app.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@HiltWorker
class TrialExpirationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val premiumRepository: PremiumRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "trial_expiration_channel"
        const val NOTIFICATION_ID_3_DAYS = 200
        const val NOTIFICATION_ID_1_DAY = 201
        const val NOTIFICATION_ID_EXPIRED = 202
        const val WORK_NAME = "trial_expiration_work"

        fun scheduleDailyCheck(context: Context) {
            // Schedule to run at 10:00 AM every day
            val now = LocalTime.now()
            val targetTime = LocalTime.of(10, 0)

            val initialDelay = if (now.isBefore(targetTime)) {
                Duration.between(now, targetTime).toMinutes()
            } else {
                // If it's already past 10 AM, schedule for tomorrow
                Duration.between(now, targetTime).plusHours(24).toMinutes()
            }

            val workRequest = PeriodicWorkRequestBuilder<TrialExpirationWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(initialDelay, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }

        fun cancelSchedule(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        createNotificationChannel()

        val status = premiumRepository.getSubscriptionStatus()

        // Only check if user is in trial period
        if (!status.isInTrialPeriod) {
            // Check if trial just expired (within last 24 hours)
            status.trialExpiresAt?.let { expiresAt ->
                val now = LocalDateTime.now()
                if (expiresAt.isBefore(now) && expiresAt.plusDays(1).isAfter(now)) {
                    showExpiredNotification()
                }
            }
            return Result.success()
        }

        val daysRemaining = status.daysRemainingInTrial

        when (daysRemaining) {
            3 -> showNotification(
                notificationId = NOTIFICATION_ID_3_DAYS,
                title = "トライアル終了まであと3日",
                message = "Iterioプレミアムのトライアルがあと3日で終了します。今すぐアップグレードして、すべての機能を引き続きご利用ください。"
            )
            1 -> showNotification(
                notificationId = NOTIFICATION_ID_1_DAY,
                title = "トライアル終了まであと1日",
                message = "Iterioプレミアムのトライアルが明日終了します。継続してご利用いただくには、今すぐアップグレードしてください。"
            )
            0 -> showNotification(
                notificationId = NOTIFICATION_ID_EXPIRED,
                title = "トライアル本日終了",
                message = "Iterioプレミアムのトライアルが本日終了します。プレミアム機能を継続するには、今すぐアップグレードしてください。"
            )
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "トライアル通知",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "トライアル期間の終了をお知らせします"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun showNotification(notificationId: Int, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("SHOW_PREMIUM", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(notificationId, notification)
    }

    private fun showExpiredNotification() {
        showNotification(
            notificationId = NOTIFICATION_ID_EXPIRED,
            title = "トライアル終了",
            message = "Iterioプレミアムのトライアルが終了しました。プレミアム機能をご利用いただくには、アップグレードしてください。"
        )
    }
}
