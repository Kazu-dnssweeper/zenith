package com.iterio.app.di

import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.model.Task
import com.iterio.app.domain.repository.*
import com.iterio.app.ui.premium.PremiumManager
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import javax.inject.Singleton

/**
 * E2Eテスト用のテストモジュール
 * 本番のAppModuleをこのモジュールで置換
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    @Provides
    @Singleton
    fun provideTaskRepository(): TaskRepository = mockk(relaxed = true) {
        val testTask = Task(
            id = 1L,
            groupId = 1L,
            name = "テストタスク",
            workDurationMinutes = 25
        )
        coEvery { getTaskById(any()) } returns testTask
        every { getTasksByGroup(any()) } returns flowOf(listOf(testTask))
        coEvery { getTasksForDate(any()) } returns listOf(testTask)
    }

    @Provides
    @Singleton
    fun provideSubjectGroupRepository(): SubjectGroupRepository = mockk(relaxed = true) {
        every { getAllGroups() } returns flowOf(emptyList())
    }

    @Provides
    @Singleton
    fun provideStudySessionRepository(): StudySessionRepository = mockk(relaxed = true) {
        coEvery { insertSession(any()) } returns 1L
        every { getSessionsForDay(any()) } returns flowOf(emptyList())
        coEvery { getTotalMinutesForDay(any()) } returns 0
        coEvery { getTotalCyclesForDay(any()) } returns 0
    }

    @Provides
    @Singleton
    fun provideReviewTaskRepository(): ReviewTaskRepository = mockk(relaxed = true) {
        every { getPendingTasksForDate(any()) } returns flowOf(emptyList())
        every { getOverdueAndTodayTasks(any()) } returns flowOf(emptyList())
        coEvery { getPendingTaskCountForDate(any()) } returns 0
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(): SettingsRepository = mockk(relaxed = true) {
        coEvery { getPomodoroSettings() } returns PomodoroSettings()
        every { getPomodoroSettingsFlow() } returns flowOf(PomodoroSettings())
        coEvery { getAllowedApps() } returns emptyList()
        every { getAllowedAppsFlow() } returns flowOf(emptyList())
    }

    @Provides
    @Singleton
    fun provideDailyStatsRepository(): DailyStatsRepository = mockk(relaxed = true) {
        coEvery { getByDate(any()) } returns null
        every { getByDateFlow(any()) } returns flowOf(null)
        coEvery { getCurrentStreak() } returns 0
        coEvery { getMaxStreak() } returns 0
    }

    @Provides
    @Singleton
    fun providePremiumRepository(): PremiumRepository = mockk(relaxed = true) {
        val subscriptionFlow = MutableStateFlow(SubscriptionStatus())
        every { subscriptionStatus } returns subscriptionFlow
        coEvery { getSubscriptionStatus() } returns SubscriptionStatus()
        coEvery { canAccessFeature(any()) } returns false
    }

    @Provides
    @Singleton
    fun providePremiumManager(
        premiumRepository: PremiumRepository
    ): PremiumManager = PremiumManager(premiumRepository)
}
