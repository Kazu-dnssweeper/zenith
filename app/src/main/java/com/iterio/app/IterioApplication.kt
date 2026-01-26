package com.iterio.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.iterio.app.util.LocaleManager
import com.iterio.app.worker.ReviewReminderWorker
import com.iterio.app.worker.TrialExpirationWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class IterioApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var localeManager: LocaleManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize locale from saved settings
        localeManager.initializeLocale()

        // Schedule daily review reminder
        ReviewReminderWorker.scheduleDaily(this)

        // Schedule trial expiration check
        TrialExpirationWorker.scheduleDailyCheck(this)
    }
}
