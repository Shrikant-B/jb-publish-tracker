package com.shrikantbadwaik.publish.time.tracker.ui

import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBLabel
import com.intellij.ui.table.JBTable
import com.shrikantbadwaik.publish.time.tracker.data.PublishTrackerStorage
import com.shrikantbadwaik.publish.time.tracker.domain.AnalyticsCalculator
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.time.Day
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

class ChartsPanel : JBPanel<JBPanel<*>>() {
    
    private val storage = PublishTrackerStorage.getInstance()
    private val analyticsCalculator = AnalyticsCalculator()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm")
    
    init {
        layout = BorderLayout()
        createAnalyticsPanel()
    }
    
    private fun createAnalyticsPanel() {
        val mainPanel = JBPanel<JBPanel<*>>()
        mainPanel.layout = GridLayout(2, 2, 10, 10)
        
        // Overall Metrics Panel
        val overallMetricsPanel = createOverallMetricsPanel()
        mainPanel.add(overallMetricsPanel)
        
        // Plugin Performance Panel
        val performancePanel = createPerformancePanel()
        mainPanel.add(performancePanel)
        
        // Trend Analysis Panel
        val trendPanel = createTrendPanel()
        mainPanel.add(trendPanel)
        
        // Plugin Details Panel
        val detailsPanel = createPluginDetailsPanel()
        mainPanel.add(detailsPanel)
        
        add(mainPanel, BorderLayout.CENTER)
        
        // Refresh button
        val refreshButton = JButton("Refresh Analytics")
        refreshButton.addActionListener { refreshAnalytics() }
        add(refreshButton, BorderLayout.SOUTH)
    }
    
    private fun createOverallMetricsPanel(): JPanel {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BorderLayout()
        
        val title = JBLabel("Average Phase Durations")
        title.font = title.font.deriveFont(16f)
        panel.add(title, BorderLayout.NORTH)
        
        // Get average phase durations
        val phaseDurations = storage.getAveragePhaseDurations()
        
        if (phaseDurations.isEmpty()) {
            val noDataLabel = JBLabel("No phase data available yet")
            panel.add(noDataLabel, BorderLayout.CENTER)
        } else {
            // Create bar chart for phase durations
            val dataset = DefaultCategoryDataset()
            phaseDurations.forEach { (phase, durationMs) ->
                val hours = durationMs / (1000.0 * 60 * 60)
                dataset.addValue(hours, "Duration", phase)
            }
            
            val chart: JFreeChart = ChartFactory.createBarChart(
                "",  // No title (we have label above)
                "Phase",
                "Duration (hours)",
                dataset,
                PlotOrientation.VERTICAL,
                false, // legend
                true,  // tooltips
                false  // urls
            )
            
            val chartPanel = ChartPanel(chart)
            chartPanel.preferredSize = Dimension(400, 300)
            panel.add(chartPanel, BorderLayout.CENTER)
        }
        
        return panel
    }
    
    private fun createPerformancePanel(): JPanel {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BorderLayout()
        
        val title = JBLabel("Plugin Performance")
        title.font = title.font.deriveFont(16f)
        panel.add(title, BorderLayout.NORTH)
        
        val pluginIds = storage.getState().trackedPluginIds
        val performanceData = pluginIds.map { pluginId ->
            val pluginHistory = storage.getState().history.filter { it.pluginId == pluginId }
            analyticsCalculator.calculatePluginMetrics(pluginId, pluginHistory)
        }
        
        val columns = arrayOf("Plugin ID", "Avg Time", "Success Rate", "Submissions")
        val tableModel = DefaultTableModel(columns, 0)
        
        performanceData.forEach { metrics ->
            tableModel.addRow(arrayOf(
                metrics.pluginId,
                formatDuration(metrics.averageVerificationTimeMs),
                "${String.format("%.1f", metrics.successRate * 100)}%",
                metrics.totalSubmissions
            ))
        }
        
        val table = JBTable(tableModel)
        table.fillsViewportHeight = true
        panel.add(JBScrollPane(table), BorderLayout.CENTER)
        
        return panel
    }
    
    private fun createTrendPanel(): JPanel {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BorderLayout()
        
        val title = JBLabel("Submission Trends (Last 30 Days)")
        title.font = title.font.deriveFont(16f)
        panel.add(title, BorderLayout.NORTH)
        
        // Create time series chart
        val trendData = analyticsCalculator.getTrendData(storage.getState().history, 30)
        
        if (trendData.isEmpty()) {
            val noDataLabel = JBLabel("No recent data available")
            panel.add(noDataLabel, BorderLayout.CENTER)
        } else {
            val series = TimeSeries("Submissions")
            trendData.forEach { (timestamp, count) ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = timestamp
                series.add(Day(calendar.time), count)
            }
            
            val dataset = TimeSeriesCollection(series)
            val chart: JFreeChart = ChartFactory.createTimeSeriesChart(
                "",  // No title (we have label above)
                "Date",
                "Submissions",
                dataset,
                false, // legend
                true,  // tooltips
                false  // urls
            )
            
            val chartPanel = ChartPanel(chart)
            chartPanel.preferredSize = Dimension(400, 300)
            panel.add(chartPanel, BorderLayout.CENTER)
        }
        
        return panel
    }
    
    private fun createPluginDetailsPanel(): JPanel {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BorderLayout()
        
        val title = JBLabel("Recent Activity")
        title.font = title.font.deriveFont(16f)
        panel.add(title, BorderLayout.NORTH)
        
        val recentHistory = storage.getState().history
            .sortedByDescending { it.timestamp }
            .take(10)
        
        val columns = arrayOf("Plugin ID", "Duration", "Time")
        val tableModel = DefaultTableModel(columns, 0)
        
        recentHistory.forEach { entry ->
            tableModel.addRow(arrayOf(
                entry.pluginId,
                if (entry.durationMs > 0) formatDuration(entry.durationMs) else "N/A",
                dateFormat.format(Date(entry.timestamp))
            ))
        }
        
        val table = JBTable(tableModel)
        table.fillsViewportHeight = true
        panel.add(JBScrollPane(table), BorderLayout.CENTER)
        
        return panel
    }
    
    private fun refreshAnalytics() {
        // Remove all components and recreate
        removeAll()
        createAnalyticsPanel()
        revalidate()
        repaint()
    }
    
    private fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
}
