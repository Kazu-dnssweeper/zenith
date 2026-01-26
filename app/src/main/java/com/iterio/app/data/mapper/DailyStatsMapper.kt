package com.iterio.app.data.mapper

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.iterio.app.data.local.entity.DailyStatsEntity
import com.iterio.app.domain.model.DailyStats
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DailyStatsEntity と DailyStats 間の変換を行うマッパー
 *
 * subjectBreakdown の JSON シリアライズ/デシリアライズを担当
 */
@Singleton
class DailyStatsMapper @Inject constructor(
    private val gson: Gson
) : Mapper<DailyStatsEntity, DailyStats> {

    override fun toDomain(entity: DailyStatsEntity): DailyStats {
        return DailyStats(
            date = entity.date,
            totalStudyMinutes = entity.totalStudyMinutes,
            sessionCount = entity.sessionCount,
            subjectBreakdown = parseBreakdown(entity.subjectBreakdownJson)
        )
    }

    override fun toEntity(domain: DailyStats): DailyStatsEntity {
        return DailyStatsEntity(
            date = domain.date,
            totalStudyMinutes = domain.totalStudyMinutes,
            sessionCount = domain.sessionCount,
            subjectBreakdownJson = serializeBreakdown(domain.subjectBreakdown)
        )
    }

    /**
     * JSON 文字列を Map<String, Int> に変換
     */
    private fun parseBreakdown(json: String): Map<String, Int> {
        if (json.isBlank()) return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: JsonSyntaxException) {
            Timber.w(e, "Invalid JSON format in breakdown: %s", json)
            emptyMap()
        } catch (e: Exception) {
            Timber.w(e, "Failed to parse breakdown JSON")
            emptyMap()
        }
    }

    /**
     * Map<String, Int> を JSON 文字列に変換
     */
    private fun serializeBreakdown(breakdown: Map<String, Int>): String {
        return gson.toJson(breakdown)
    }
}
