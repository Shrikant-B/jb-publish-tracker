package com.shrikantbadwaik.publish.time.tracker.ui

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.shrikantbadwaik.publish.time.tracker.data.PhaseTransition
import com.shrikantbadwaik.publish.time.tracker.data.PluginVersionTimeline
import com.shrikantbadwaik.publish.time.tracker.data.PublishTrackerStorage
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel

/**
 * Panel showing detailed timeline of phase transitions for plugins
 */
class TimelinePanel : JBPanel<JBPanel<*>>() {
    
    private val storage = PublishTrackerStorage.getInstance()
    private val dateFormat = SimpleDateFormat("MMM dd, HH:mm")
    
    init {
        layout = BorderLayout()
        createTimelineView()
    }
    
    private fun createTimelineView() {
        val mainPanel = JBPanel<JBPanel<*>>()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        
        val title = JBLabel("Phase Timeline & Durations")
        title.font = title.font.deriveFont(Font.BOLD, 16f)
        title.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        mainPanel.add(title)
        
        // Get all plugins and their timelines
        val trackedPlugins = storage.getState().trackedPluginIds
        
        if (trackedPlugins.isEmpty()) {
            val noDataLabel = JBLabel("No plugins configured. Add plugins in Settings.")
            noDataLabel.border = BorderFactory.createEmptyBorder(20, 20, 20, 20)
            mainPanel.add(noDataLabel)
        } else {
            trackedPlugins.forEach { pluginId ->
                val pluginPanel = createPluginTimelinePanel(pluginId)
                mainPanel.add(pluginPanel)
            }
        }
        
        add(JBScrollPane(mainPanel), BorderLayout.CENTER)
    }
    
    private fun createPluginTimelinePanel(pluginId: String): JPanel {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BorderLayout()
        panel.border = BorderFactory.createTitledBorder("Plugin: $pluginId")
        
        // Get all transitions for this plugin
        val transitions = storage.getPhaseTransitionsForPlugin(pluginId)
        
        if (transitions.isEmpty()) {
            val noDataLabel = JBLabel("No timeline data available yet")
            noDataLabel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
            panel.add(noDataLabel, BorderLayout.CENTER)
            return panel
        }
        
        // Group by version
        val byVersion = transitions.groupBy { it.version }.toSortedMap(compareByDescending { it })
        
        // Show latest version's detailed timeline
        val latestVersion = byVersion.keys.firstOrNull()
        if (latestVersion != null && latestVersion.isNotEmpty()) {
            val timeline = storage.buildTimeline(pluginId, latestVersion)
            if (timeline != null) {
                val detailPanel = createDetailedTimelinePanel(timeline)
                panel.add(detailPanel, BorderLayout.CENTER)
            }
        }
        
        // Show history table
        val historyPanel = createTransitionHistoryPanel(transitions)
        panel.add(historyPanel, BorderLayout.SOUTH)
        
        return panel
    }
    
    private fun createDetailedTimelinePanel(timeline: PluginVersionTimeline): JPanel {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BorderLayout()
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        
        val versionLabel = JBLabel("Version: ${timeline.version} - Current Stage: ${timeline.currentStage.name}")
        versionLabel.font = versionLabel.font.deriveFont(Font.BOLD)
        panel.add(versionLabel, BorderLayout.NORTH)
        
        // Create visual timeline
        val timelineVisual = createTimelineVisual(timeline)
        panel.add(timelineVisual, BorderLayout.CENTER)
        
        // Show phase durations
        val durationsPanel = JBPanel<JBPanel<*>>()
        durationsPanel.layout = BoxLayout(durationsPanel, BoxLayout.Y_AXIS)
        durationsPanel.border = BorderFactory.createTitledBorder("Phase Durations")
        
        val phaseDurations = timeline.getPhaseDurations()
        if (phaseDurations.isEmpty()) {
            durationsPanel.add(JBLabel("Timeline in progress..."))
        } else {
            phaseDurations.forEach { (phase, durationMs) ->
                val durationText = if (durationMs != null) {
                    formatDuration(durationMs)
                } else {
                    "N/A"
                }
                val label = JBLabel("$phase: $durationText")
                label.border = BorderFactory.createEmptyBorder(2, 5, 2, 5)
                durationsPanel.add(label)
            }
        }
        
        panel.add(durationsPanel, BorderLayout.SOUTH)
        
        return panel
    }
    
    private fun createTimelineVisual(timeline: PluginVersionTimeline): JPanel {
        return object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                val g2d = g as Graphics2D
                
                val phases = listOf(
                    "Upload" to timeline.uploadedAt,
                    "Verification" to timeline.verificationStartedAt,
                    "Approval" to timeline.approvedAt,
                    "Published" to timeline.publishedAt
                )
                
                val width = width
                val height = height
                val phaseHeight = 40
                val startX = 50
                val lineWidth = width - 100
                
                var y = 30
                
                // Draw timeline line
                g2d.color = Color.LIGHT_GRAY
                g2d.drawLine(startX, y + phaseHeight / 2, startX + lineWidth, y + phaseHeight / 2)
                
                phases.forEachIndexed { index, (phaseName, timestamp) ->
                    val x = startX + (lineWidth * index / (phases.size - 1))
                    
                    // Draw phase marker
                    if (timestamp != null) {
                        g2d.color = Color(76, 175, 80) // Green for completed
                        g2d.fillOval(x - 8, y + phaseHeight / 2 - 8, 16, 16)
                        g2d.color = Color.BLACK
                        g2d.drawOval(x - 8, y + phaseHeight / 2 - 8, 16, 16)
                    } else {
                        g2d.color = Color.LIGHT_GRAY
                        g2d.fillOval(x - 6, y + phaseHeight / 2 - 6, 12, 12)
                    }
                    
                    // Draw phase name
                    g2d.color = Color.BLACK
                    val nameWidth = g2d.fontMetrics.stringWidth(phaseName)
                    g2d.drawString(phaseName, x - nameWidth / 2, y - 5)
                    
                    // Draw timestamp
                    if (timestamp != null) {
                        val timeStr = dateFormat.format(Date(timestamp))
                        val timeWidth = g2d.fontMetrics.stringWidth(timeStr)
                        g2d.drawString(timeStr, x - timeWidth / 2, y + phaseHeight + 15)
                    }
                }
            }
            
            override fun getPreferredSize(): Dimension {
                return Dimension(600, 100)
            }
        }
    }
    
    private fun createTransitionHistoryPanel(transitions: List<PhaseTransition>): JPanel {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BorderLayout()
        panel.border = BorderFactory.createTitledBorder("Transition History")
        
        val columns = arrayOf("Version", "From", "To", "Timestamp", "Duration in Stage")
        val tableModel = DefaultTableModel(columns, 0)
        
        // Sort by timestamp descending
        transitions.sortedByDescending { it.timestamp }.take(10).forEach { transition ->
            tableModel.addRow(arrayOf(
                transition.version,
                transition.fromStage.name,
                transition.toStage.name,
                dateFormat.format(Date(transition.timestamp)),
                transition.durationInPreviousStageMs?.let { formatDuration(it) } ?: "N/A"
            ))
        }
        
        val table = JBTable(tableModel)
        table.fillsViewportHeight = true
        
        // Color code the "To" column based on stage
        table.columnModel.getColumn(2).cellRenderer = object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(
                table: javax.swing.JTable?,
                value: Any?,
                isSelected: Boolean,
                hasFocus: Boolean,
                row: Int,
                column: Int
            ): Component {
                val cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                if (!isSelected) {
                    when (value.toString()) {
                        "PUBLISHED" -> cell.foreground = Color(76, 175, 80) // Green
                        "APPROVED" -> cell.foreground = Color(33, 150, 243) // Blue
                        "REJECTED" -> cell.foreground = Color(244, 67, 54) // Red
                        "UNDER_REVIEW" -> cell.foreground = Color(255, 152, 0) // Orange
                        else -> cell.foreground = Color.BLACK
                    }
                }
                return cell
            }
        }
        
        panel.add(JBScrollPane(table), BorderLayout.CENTER)
        panel.preferredSize = Dimension(600, 150)
        
        return panel
    }
    
    fun refresh() {
        removeAll()
        createTimelineView()
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

