package com.iterio.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.iterio.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages application locale/language settings.
 *
 * Responsibilities:
 * - Load language setting from repository
 * - Apply locale changes using AppCompatDelegate
 * - Persist language preference
 *
 * Note: Uses SharedPreferences for synchronous reading (required for attachBaseContext)
 * and Room database for persistent storage.
 */
@Singleton
class LocaleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Supported language codes
     */
    companion object {
        const val LANGUAGE_JAPANESE = "ja"
        const val LANGUAGE_ENGLISH = "en"
        const val DEFAULT_LANGUAGE = LANGUAGE_JAPANESE

        val SUPPORTED_LANGUAGES = listOf(LANGUAGE_JAPANESE, LANGUAGE_ENGLISH)

        // SharedPreferences constants
        private const val PREFS_NAME = "iterio_locale_prefs"
        private const val KEY_LANGUAGE = "language"
    }

    private val _currentLanguage = MutableStateFlow(getLanguageSync())
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    /**
     * Initialize locale on app startup.
     * Should be called from Application.onCreate()
     */
    fun initializeLocale() {
        // Apply locale synchronously from SharedPreferences first
        // This ensures the locale is set before any Activity is created
        val cachedLanguage = getLanguageSync()
        _currentLanguage.value = cachedLanguage
        applyLocale(cachedLanguage)
        Timber.d("Applied cached locale: $cachedLanguage")

        // Then sync with Room database asynchronously
        scope.launch {
            val savedLanguage = settingsRepository.getLanguage()
            if (savedLanguage != cachedLanguage) {
                // Room has different value, update SharedPreferences and apply
                sharedPrefs.edit().putString(KEY_LANGUAGE, savedLanguage).apply()
                _currentLanguage.value = savedLanguage
                applyLocale(savedLanguage)
                Timber.d("Synced locale from database: $savedLanguage")
            }
        }
    }

    /**
     * Get the current language code synchronously from SharedPreferences.
     * This is safe to call from attachBaseContext().
     */
    fun getLanguageSync(): String {
        return sharedPrefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    /**
     * Get the current language code synchronously.
     * Returns cached value, may not reflect the latest persisted value.
     */
    fun getCurrentLanguage(): String = _currentLanguage.value

    /**
     * Update the app language.
     * Persists the setting and applies the new locale.
     */
    suspend fun setLanguage(languageCode: String) {
        if (languageCode !in SUPPORTED_LANGUAGES) {
            Timber.w("Unsupported language code: $languageCode")
            return
        }

        // Save to SharedPreferences synchronously (for next attachBaseContext call)
        sharedPrefs.edit().putString(KEY_LANGUAGE, languageCode).commit()

        // Save to Room database asynchronously
        settingsRepository.setLanguage(languageCode)
        _currentLanguage.value = languageCode
        applyLocale(languageCode)
        Timber.d("Language changed to: $languageCode")
    }

    /**
     * Apply locale using AppCompatDelegate.
     * This will trigger activity recreation if needed.
     */
    private fun applyLocale(languageCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    /**
     * Check if the given language is currently active
     */
    fun isLanguageActive(languageCode: String): Boolean {
        return _currentLanguage.value == languageCode
    }

    /**
     * Static method to get language synchronously without LocaleManager instance.
     * Used in attachBaseContext() before Hilt is initialized.
     */
    object Sync {
        fun getLanguage(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        }
    }
}
