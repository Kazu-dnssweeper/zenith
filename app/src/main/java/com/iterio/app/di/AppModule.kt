package com.iterio.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.iterio.app.data.encryption.EncryptionManager
import com.iterio.app.data.local.IterioDatabase
import com.iterio.app.data.local.dao.*
import com.iterio.app.data.mapper.*
import com.iterio.app.data.repository.*
import com.iterio.app.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.premiumDataStore: DataStore<Preferences> by preferencesDataStore(name = "premium_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    @Singleton
    fun provideSubjectGroupRepository(
        subjectGroupDao: SubjectGroupDao,
        mapper: SubjectGroupMapper
    ): SubjectGroupRepository {
        return SubjectGroupRepositoryImpl(subjectGroupDao, mapper)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao,
        mapper: TaskMapper
    ): TaskRepository {
        return TaskRepositoryImpl(taskDao, mapper)
    }

    @Provides
    @Singleton
    fun provideStudySessionRepository(
        studySessionDao: StudySessionDao,
        mapper: StudySessionMapper
    ): StudySessionRepository {
        return StudySessionRepositoryImpl(studySessionDao, mapper)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsDao: SettingsDao,
        database: IterioDatabase
    ): SettingsRepository {
        return SettingsRepositoryImpl(settingsDao, database)
    }

    @Provides
    @Singleton
    fun provideDailyStatsRepository(
        dailyStatsDao: DailyStatsDao,
        mapper: DailyStatsMapper
    ): DailyStatsRepository {
        return DailyStatsRepositoryImpl(dailyStatsDao, mapper)
    }

    @Provides
    @Singleton
    fun provideReviewTaskRepository(
        reviewTaskDao: ReviewTaskDao,
        mapper: ReviewTaskMapper
    ): ReviewTaskRepository {
        return ReviewTaskRepositoryImpl(reviewTaskDao, mapper)
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.premiumDataStore
    }

    @Provides
    @Singleton
    fun providePremiumRepository(dataStore: DataStore<Preferences>): PremiumRepository {
        return PremiumRepositoryImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideBackupRepository(
        @ApplicationContext context: Context,
        database: IterioDatabase,
        subjectGroupDao: SubjectGroupDao,
        taskDao: TaskDao,
        studySessionDao: StudySessionDao,
        reviewTaskDao: ReviewTaskDao,
        settingsDao: SettingsDao,
        dailyStatsDao: DailyStatsDao,
        gson: Gson,
        encryptionManager: EncryptionManager
    ): BackupRepository {
        return BackupRepositoryImpl(
            context = context,
            database = database,
            subjectGroupDao = subjectGroupDao,
            taskDao = taskDao,
            studySessionDao = studySessionDao,
            reviewTaskDao = reviewTaskDao,
            settingsDao = settingsDao,
            dailyStatsDao = dailyStatsDao,
            gson = gson,
            encryptionManager = encryptionManager
        )
    }
}
