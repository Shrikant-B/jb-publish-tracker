package com.shrikantbadwaik.publish.time.tracker.domain

import com.shrikantbadwaik.publish.time.tracker.data.PluginHistoryEntry
import com.shrikantbadwaik.publish.time.tracker.data.PluginMetrics
import com.shrikantbadwaik.publish.time.tracker.data.PluginStatus
import com.shrikantbadwaik.publish.time.tracker.data.VerificationHistory
import com.shrikantbadwaik.publish.time.tracker.data.VerificationStage
import java.time.Instant
import java.time.temporal.ChronoUnit

class AnalyticsCalculator {

    fun calculatePluginMetrics(
        pluginId: String,
        history: List<PluginHistoryEntry>
    ): PluginMetrics {
        if (history.isEmpty()) {
            return PluginMetrics(
                pluginId = pluginId,
                averageVerificationTimeMs = 0,
                successRate = 0.0,
                totalSubmissions = 0,
                lastVerificationTime = 0,
                fastestVerificationMs = 0,
                slowestVerificationMs = 0
            )
        }

        val validDurations = history.filter { it.durationMs > 0 }.map { it.durationMs }
        val averageTime = if (validDurations.isNotEmpty()) {
            validDurations.average().toLong()
        } else {
            0L
        }

        val successRate = history.count { it.durationMs > 0 }.toDouble() / history.size

        return PluginMetrics(
            pluginId = pluginId,
            averageVerificationTimeMs = averageTime,
            successRate = successRate,
            totalSubmissions = history.size,
            lastVerificationTime = history.maxOfOrNull { it.timestamp } ?: 0,
            fastestVerificationMs = validDurations.minOrNull() ?: 0,
            slowestVerificationMs = validDurations.maxOrNull() ?: 0
        )
    }

    fun calculateOverallMetrics(histories: List<PluginHistoryEntry>): Map<String, Any> {
        val totalSubmissions = histories.size
        val successfulSubmissions = histories.count { it.durationMs > 0 }
        val averageProcessingTime = if (successfulSubmissions > 0) {
            histories.filter { it.durationMs > 0 }.map { it.durationMs }.average()
        } else {
            0.0
        }

        val successRate = if (totalSubmissions > 0) {
            successfulSubmissions.toDouble() / totalSubmissions
        } else {
            0.0
        }

        val submissionsByDay = histories.groupBy {
            Instant.ofEpochMilli(it.timestamp).truncatedTo(ChronoUnit.DAYS)
        }.mapValues { it.value.size }

        return mapOf(
            "totalSubmissions" to totalSubmissions,
            "successfulSubmissions" to successfulSubmissions,
            "successRate" to successRate,
            "averageProcessingTimeMs" to averageProcessingTime,
            "submissionsByDay" to submissionsByDay
        )
    }

    fun getTrendData(histories: List<PluginHistoryEntry>, days: Int = 30): List<Pair<Long, Int>> {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val recentHistories = histories.filter { it.timestamp >= cutoffTime }

        return recentHistories
            .groupBy { it.timestamp / (24 * 60 * 60 * 1000L) } // Group by day
            .map { (day, entries) -> day * (24 * 60 * 60 * 1000L) to entries.size }
            .sortedBy { it.first }
    }

    fun getStatusDistribution(histories: List<VerificationHistory>): Map<VerificationStage, Int> {
        return histories.groupingBy { it.stage }.eachCount()
    }

    fun getStatusDistributionFromPluginStatus(pluginStatuses: List<PluginStatus>): Map<VerificationStage, Int> {
        return pluginStatuses.groupingBy { it.verificationStage }.eachCount()
    }

    fun predictCompletionTime(pluginId: String, histories: List<PluginHistoryEntry>): Long? {
        val pluginHistories = histories.filter { it.pluginId == pluginId && it.durationMs > 0 }
        if (pluginHistories.size < 3) return null // Need at least 3 data points

        // Simple moving average of last 5 submissions
        val recentDurations = pluginHistories
            .sortedByDescending { it.timestamp }
            .take(5)
            .map { it.durationMs }

        return recentDurations.average().toLong()
    }
}
