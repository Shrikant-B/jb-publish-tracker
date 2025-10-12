package com.shrikantbadwaik.publish.time.tracker.ui

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications.Bus
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.shrikantbadwaik.publish.time.tracker.data.repo.worker.StatusFetcher
import com.shrikantbadwaik.publish.time.tracker.data.repo.di.RepoModule
import java.awt.BorderLayout
import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.swing.JButton
import javax.swing.table.DefaultTableModel

class PluginStatusToolWindowFactory : ToolWindowFactory {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private val repository = RepoModule.getRepo()
    private var scheduledFuture: ScheduledFuture<*>? = null

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        val statusPanel = createStatusDashboard(project)
        val statusContent = ContentFactory.getInstance().createContent(statusPanel, "Status", false)
        toolWindow.contentManager.addContent(statusContent)

        // Create Analytics/Charts Tab
//        val chartsPanel = ChartsPanel()
//        val chartsContent = ContentFactory.getInstance().createContent(chartsPanel, "Analytics", false)
//        toolWindow.contentManager.addContent(chartsContent)

        // Create Timeline Tab
//        val timelinePanel = TimelinePanel()
//        val timelineContent = ContentFactory.getInstance().createContent(timelinePanel, "Timeline", false)
//        toolWindow.contentManager.addContent(timelineContent)
    }

    private fun createStatusDashboard(project: Project): JBPanel<JBPanel<*>> {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BorderLayout()
        val columns = arrayOf("Author", "Plugin ID", "Version", "Status", "Last Checked")
        val tableModel = DefaultTableModel(columns, 0)
        val table = JBTable(tableModel)
        table.fillsViewportHeight = true
        panel.add(JBScrollPane(table), BorderLayout.CENTER)
        val refreshBtn = JButton("Refresh Now")
        val startBtn = JButton("Start Auto Poll")
        val stopBtn = JButton("Stop Auto Poll")

        // Set initial button states
        stopBtn.isEnabled = false

        val topPanel = JBPanel<JBPanel<*>>()
        topPanel.add(refreshBtn)
        topPanel.add(startBtn)
        topPanel.add(stopBtn)
        panel.add(topPanel, BorderLayout.NORTH)

        return panel.also { setupStatusDashboardListeners(it, project, tableModel, refreshBtn, startBtn, stopBtn) }
    }

    private fun setupStatusDashboardListeners(
        panel: JBPanel<JBPanel<*>>,
        project: Project,
        tableModel: DefaultTableModel,
        refreshBtn: JButton,
        startBtn: JButton,
        stopBtn: JButton
    ) {
        val repo = StatusFetcher(project, repository)

        fun populate(rows: List<Array<Any>>) {
            tableModel.rowCount = 0
            rows.forEach { tableModel.addRow(it) }
        }

        refreshBtn.addActionListener {
            refreshBtn.isEnabled = false
            repo.fetchAllAsync {
                populate(
                    it.map { s ->
                    arrayOf(
                        s.displayName,
                        s.pluginId,
                        s.latestVersion,
                        s.status,
                        Date(s.lastCheckedAt)
                    )
                }
                )
                refreshBtn.isEnabled = true
            }
        }

        startBtn.addActionListener {
            if (scheduledFuture != null && !scheduledFuture!!.isCancelled) {
                Bus.notify(
                    Notification(
                        "PublishTracker",
                        "AutoPolling",
                        "Auto polling is already running",
                        NotificationType.WARNING
                    )
                )
                return@addActionListener
            }

            scheduledFuture = scheduler.scheduleAtFixedRate({
                println("[MarketplaceApi] Triggered at ${Date()}")
                repo.fetchAllAsync {
                    println("[MarketplaceApi] Received ${it.size} plugin statuses")
                    populate(
                        it.map { s ->
                        arrayOf(
                            s.displayName,
                            s.pluginId,
                            s.latestVersion,
                            s.status,
                            Date(s.lastCheckedAt)
                        )
                    }
                    )
                }
            }, 0, 10, TimeUnit.MINUTES)

            startBtn.isEnabled = false
            stopBtn.isEnabled = true

            Bus.notify(
                Notification(
                    "PublishTracker",
                    "AutoPolling",
                    "Started auto polling every 10 min",
                    NotificationType.INFORMATION
                )
            )
        }

        stopBtn.addActionListener {
            scheduledFuture?.cancel(false)
            scheduledFuture = null

            startBtn.isEnabled = true
            stopBtn.isEnabled = false

            Bus.notify(
                Notification(
                    "PublishTracker",
                    "AutoPolling",
                    "Stopped",
                    NotificationType.INFORMATION
                )
            )
        }
    }
}
