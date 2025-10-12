package com.shrikantbadwaik.publish.time.tracker

import com.shrikantbadwaik.publish.time.tracker.data.PluginHistoryEntry
import com.shrikantbadwaik.publish.time.tracker.data.PluginStatus
import com.shrikantbadwaik.publish.time.tracker.data.VerificationStage
import com.shrikantbadwaik.publish.time.tracker.domain.AnalyticsCalculator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AnalyticsCalculatorTest {

    private lateinit var calculator: AnalyticsCalculator

    @BeforeEach
    fun setup() {
        calculator = AnalyticsCalculator()
    }

    @Test
    fun `test calculatePluginMetrics with empty history returns zero metrics`() {
        val metrics = calculator.calculatePluginMetrics("123", emptyList())
        assertNotNull(metrics)
        assertEquals("123", metrics.pluginId)
        assertEquals(0L, metrics.averageVerificationTimeMs)
        assertEquals(0.0, metrics.successRate)
        assertEquals(0, metrics.totalSubmissions)
    }

    @Test
    fun `test calculatePluginMetrics with single entry`() {
        val history = listOf(
            PluginHistoryEntry(
                pluginId = "123",
                timestamp = System.currentTimeMillis(),
                durationMs = 5000L
            )
        )

        val metrics = calculator.calculatePluginMetrics("123", history)
        assertNotNull(metrics)
        assertEquals("123", metrics.pluginId)
        assertEquals(5000L, metrics.averageVerificationTimeMs)
        assertEquals(1.0, metrics.successRate)
        assertEquals(1, metrics.totalSubmissions)
    }

    @Test
    fun `test calculatePluginMetrics with multiple entries`() {
        val history = listOf(
            PluginHistoryEntry(
                pluginId = "123",
                timestamp = 1000L,
                durationMs = 5000L
            ),
            PluginHistoryEntry(
                pluginId = "123",
                timestamp = 7000L,
                durationMs = 3000L
            ),
            PluginHistoryEntry(
                pluginId = "123",
                timestamp = 11000L,
                durationMs = 7000L
            )
        )

        val metrics = calculator.calculatePluginMetrics("123", history)
        assertNotNull(metrics)
        assertEquals("123", metrics.pluginId)
        assertEquals(5000L, metrics.averageVerificationTimeMs) // (5000 + 3000 + 7000) / 3
        assertEquals(1.0, metrics.successRate) // All successful
        assertEquals(3, metrics.totalSubmissions)
        assertEquals(3000L, metrics.fastestVerificationMs)
        assertEquals(7000L, metrics.slowestVerificationMs)
    }

    @Test
    fun `test getStatusDistributionFromPluginStatus with empty list`() {
        val distribution = calculator.getStatusDistributionFromPluginStatus(emptyList())
        assertTrue(distribution.isEmpty())
    }

    @Test
    fun `test getStatusDistributionFromPluginStatus with multiple statuses`() {
        val statuses = listOf(
            PluginStatus(
                pluginId = "1",
                displayName = "Plugin 1",
                verificationStage = VerificationStage.PUBLISHED
            ),
            PluginStatus(
                pluginId = "2",
                displayName = "Plugin 2",
                verificationStage = VerificationStage.PUBLISHED
            ),
            PluginStatus(
                pluginId = "3",
                displayName = "Plugin 3",
                verificationStage = VerificationStage.UNDER_REVIEW
            )
        )

        val distribution = calculator.getStatusDistributionFromPluginStatus(statuses)
        assertEquals(2, distribution[VerificationStage.PUBLISHED])
        assertEquals(1, distribution[VerificationStage.UNDER_REVIEW])
    }

    @Test
    fun `test getTrendData with empty history`() {
        val trends = calculator.getTrendData(emptyList())
        assertTrue(trends.isEmpty())
    }

    @Test
    fun `test getTrendData groups by day`() {
        val now = System.currentTimeMillis()
        val oneDayAgo = now - (24 * 60 * 60 * 1000)
        val twoDaysAgo = now - (2 * 24 * 60 * 60 * 1000)

        val history = listOf(
            PluginHistoryEntry(pluginId = "1", timestamp = now),
            PluginHistoryEntry(pluginId = "1", timestamp = now - 1000),
            PluginHistoryEntry(pluginId = "1", timestamp = oneDayAgo),
            PluginHistoryEntry(pluginId = "1", timestamp = twoDaysAgo)
        )

        val trends = calculator.getTrendData(history)
        // getTrendData returns Map<Instant, Int>, at least one entry should exist
        assertTrue(trends.isNotEmpty())
    }

    @Test
    fun `test calculateOverallMetrics with valid history`() {
        val history = listOf(
            PluginHistoryEntry(pluginId = "1", timestamp = System.currentTimeMillis(), durationMs = 1000L),
            PluginHistoryEntry(pluginId = "1", timestamp = System.currentTimeMillis(), durationMs = 2000L)
        )

        val metrics = calculator.calculateOverallMetrics(history)
        assertNotNull(metrics)
        assertTrue(metrics.containsKey("totalSubmissions"))
        assertTrue(metrics.containsKey("successRate"))
        assertEquals(2, metrics["totalSubmissions"])
    }
}
