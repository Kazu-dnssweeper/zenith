package com.iterio.app.widget

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import androidx.room.Room
import com.iterio.app.data.local.IterioDatabase
import com.iterio.app.service.TimerPhase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val Context.premiumDataStore by preferencesDataStore(name = "premium_prefs")

object IterioWidgetStateHelper {

    @Volatile
    private var database: IterioDatabase? = null

    private fun getDatabase(context: Context): IterioDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                IterioDatabase::class.java,
                IterioDatabase.DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { database = it }
        }
    }

    suspend fun getWidgetState(context: Context): WidgetState {
        return try {
            // Premium状態をチェック
            val isPremium = checkPremiumStatus(context)

            val db = getDatabase(context)
            val dailyStatsDao = db.dailyStatsDao()
            val reviewTaskDao = db.reviewTaskDao()
            val taskDao = db.taskDao()
            val subjectGroupDao = db.subjectGroupDao()

            val today = LocalDate.now()
            val todayStats = dailyStatsDao.getByDate(today)
            val streak = dailyStatsDao.getCurrentStreak(today)
            val pendingReviewCount = try {
                reviewTaskDao.getPendingTaskCountForDate(today)
            } catch (e: Exception) {
                0
            }

            // 今日のタスクリスト取得（最大5件）
            val todayTasks = try {
                val dayOfWeek = today.dayOfWeek.value.toString()
                val todayStr = today.toString()
                val taskEntities = taskDao.getTasksForDate(todayStr, dayOfWeek)

                // グループ名をキャッシュして取得
                val groupCache = mutableMapOf<Long, String>()
                taskEntities.take(MAX_WIDGET_TASKS).map { entity ->
                    val groupName = groupCache.getOrPut(entity.groupId) {
                        subjectGroupDao.getGroupById(entity.groupId)?.name ?: ""
                    }
                    WidgetTaskItem(
                        name = entity.name,
                        groupName = groupName
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }

            // TimerServiceから状態を取得（SharedPreferences経由）
            val timerState = getTimerStateFromPrefs(context)

            WidgetState(
                todayStudyMinutes = todayStats?.totalStudyMinutes ?: 0,
                currentStreak = streak,
                timerPhase = timerState.phase,
                timeRemainingSeconds = timerState.timeRemainingSeconds,
                isTimerRunning = timerState.isRunning,
                isPremium = isPremium,
                pendingReviewCount = pendingReviewCount,
                todayTasks = todayTasks
            )
        } catch (e: Exception) {
            WidgetState()
        }
    }

    private suspend fun checkPremiumStatus(context: Context): Boolean {
        return try {
            val prefs = context.premiumDataStore.data.first()
            val subscriptionType = prefs[stringPreferencesKey("subscription_type")]
            val trialExpiresAtStr = prefs[stringPreferencesKey("trial_expires_at")]

            // LIFETIME または有効なサブスクリプション
            if (subscriptionType == "LIFETIME") return true
            if (subscriptionType == "MONTHLY" || subscriptionType == "YEARLY") {
                val expiresAtStr = prefs[stringPreferencesKey("expires_at")]
                if (expiresAtStr != null) {
                    val expiresAt = LocalDateTime.parse(expiresAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    if (expiresAt.isAfter(LocalDateTime.now())) return true
                }
            }

            // トライアル期間中
            if (trialExpiresAtStr != null) {
                val trialExpiresAt = LocalDateTime.parse(trialExpiresAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                if (trialExpiresAt.isAfter(LocalDateTime.now())) return true
            }

            false
        } catch (e: Exception) {
            false
        }
    }

    private fun getTimerStateFromPrefs(context: Context): TimerStateData {
        val prefs = context.getSharedPreferences(TIMER_PREFS_NAME, Context.MODE_PRIVATE)
        val phaseOrdinal = prefs.getInt(KEY_TIMER_PHASE, TimerPhase.IDLE.ordinal)
        val timeRemaining = prefs.getInt(KEY_TIME_REMAINING, 0)
        val isRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
        val lastUpdatedAt = prefs.getLong(KEY_LAST_UPDATED_AT, 0L)

        // Staleness check: if no update for 60+ seconds and timer should be running,
        // the process was likely killed — fall back to IDLE
        val isStale = isRunning &&
            lastUpdatedAt > 0 &&
            (System.currentTimeMillis() - lastUpdatedAt) > STALENESS_THRESHOLD_MS

        if (isStale) {
            return TimerStateData(
                phase = TimerPhase.IDLE,
                timeRemainingSeconds = 0,
                isRunning = false
            )
        }

        return TimerStateData(
            phase = TimerPhase.entries.getOrElse(phaseOrdinal) { TimerPhase.IDLE },
            timeRemainingSeconds = timeRemaining,
            isRunning = isRunning
        )
    }

    fun saveTimerStateToPrefs(context: Context, phase: TimerPhase, timeRemaining: Int, isRunning: Boolean) {
        val prefs = context.getSharedPreferences(TIMER_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_TIMER_PHASE, phase.ordinal)
            .putInt(KEY_TIME_REMAINING, timeRemaining)
            .putBoolean(KEY_IS_RUNNING, isRunning)
            .putLong(KEY_LAST_UPDATED_AT, System.currentTimeMillis())
            .apply()
    }

    fun updateWidget(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                IterioWidget().updateAll(context)
            } catch (e: Exception) {
                // Widget update failed, ignore
            }
        }
    }

    private var debounceJob: kotlinx.coroutines.Job? = null

    fun updateWidgetDebounced(context: Context, delayMs: Long = DEBOUNCE_DELAY_MS) {
        debounceJob?.cancel()
        debounceJob = CoroutineScope(Dispatchers.IO).launch {
            kotlinx.coroutines.delay(delayMs)
            try {
                IterioWidget().updateAll(context)
            } catch (e: Exception) {
                // Widget update failed, ignore
            }
        }
    }

    private data class TimerStateData(
        val phase: TimerPhase,
        val timeRemainingSeconds: Int,
        val isRunning: Boolean
    )

    private const val TIMER_PREFS_NAME = "iterio_widget_timer_prefs"
    private const val KEY_TIMER_PHASE = "timer_phase"
    private const val KEY_TIME_REMAINING = "time_remaining"
    private const val KEY_IS_RUNNING = "is_running"
    private const val KEY_LAST_UPDATED_AT = "last_updated_at"
    private const val STALENESS_THRESHOLD_MS = 60_000L
    private const val DEBOUNCE_DELAY_MS = 500L
    private const val MAX_WIDGET_TASKS = 5

    /**
     * Close the database when no longer needed (e.g., during app shutdown)
     */
    fun closeDatabase() {
        synchronized(this) {
            try {
                database?.close()
            } catch (e: Exception) {
                Timber.e(e, "Error closing database")
            } finally {
                database = null
            }
        }
    }
}
