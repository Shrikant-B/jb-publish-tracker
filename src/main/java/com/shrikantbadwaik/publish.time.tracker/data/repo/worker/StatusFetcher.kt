package com.shrikantbadwaik.publish.time.tracker.data.repo.worker

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import com.shrikantbadwaik.publish.time.tracker.data.PhaseTransition
import com.shrikantbadwaik.publish.time.tracker.data.PluginHistoryEntry
import com.shrikantbadwaik.publish.time.tracker.data.PluginStatus
import com.shrikantbadwaik.publish.time.tracker.data.PublishTrackerStorage
import com.shrikantbadwaik.publish.time.tracker.data.VerificationStage
import com.shrikantbadwaik.publish.time.tracker.data.repo.Repository
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class StatusFetcher(
    private val project: Project,
    private val repo: Repository
) {
    private val storage = PublishTrackerStorage.getInstance()
    private val notificationHandler = NotificationHandler()
    private val backgroundDispatcher = AppExecutorUtil.getAppExecutorService().asCoroutineDispatcher()

    fun fetchAll(callback: (List<PluginStatus>) -> Unit) {
        val pluginIds = storage.getState().trackedPluginIds.toList()
        if (pluginIds.isEmpty()) {
            notificationHandler.notifyConfigurationWarning("No plugins configured. Add plugin IDs in Settings")
            callback(emptyList())
            return
        }

        // Create a backgroundable task
        val task = object : Task.Backgroundable(project, "Fetching Plugin Status", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Fetching plugin statuses..."
                indicator.fraction = 0.0

                val results = mutableListOf<PluginStatus>()
                val totalPlugins = pluginIds.size

                for ((index, pid) in pluginIds.withIndex()) {
                    if (indicator.isCanceled) {
                        return
                    }

                    indicator.text = "Checking plugin: $pid"
                    indicator.fraction = index.toDouble() / totalPlugins

                    val duration = measureTimeMillis {
                        try {
                            // Now using real API call with synchronous method
                            val s = repo.fetchStatusSync(pid)
                            results.add(s)

                            // Detect and record state changes
                            detectAndRecordStateChange(s)
                        } catch (e: Exception) {
                            // Handle API errors gracefully
                            val errorStatus = PluginStatus(
                                pluginId = pid,
                                displayName = "Error",
                                status = "Error: ${e.message}",
                                errorMessage = e.message
                            )
                            results.add(errorStatus)
                        }
                    }

                    // Save history entry with duration
                    storage.getState().history.add(
                        PluginHistoryEntry(
                            pluginId = pid,
                            timestamp = System.currentTimeMillis(),
                            durationMs = duration
                        )
                    )
                }

                indicator.fraction = 1.0
                indicator.text = "Completed"

                // Post notifications for interesting status changes
                for (s in results) {
                    if (s.errorMessage != null) {
                        notificationHandler.notifyError(s.pluginId, s.errorMessage)
                    } else {
                        // Get previous status to detect changes
                        val previousStatus = storage.getState().lastKnownStatus[s.pluginId]
                        notificationHandler.notifyPluginStatusChange(s, previousStatus?.status)
                    }
                }

                callback(results)
            }
        }

        ProgressManager.getInstance().run(task)
    }

    fun fetchAllAsync(callback: (List<PluginStatus>) -> Unit) {
        val pluginIds = storage.getState().trackedPluginIds.toList()
        if (pluginIds.isEmpty()) {
            notificationHandler.notifyConfigurationWarning("No plugins configured. Add plugin IDs in Settings")
            callback(emptyList())
            return
        }

        CoroutineScope(backgroundDispatcher).launch {
            val results = mutableListOf<PluginStatus>()
            for (pid in pluginIds) {
                    val duration = measureTimeMillis {
                    try {
                        val s = repo.fetchStatus(pid)
                        results.add(s)
                        detectAndRecordStateChange(s)
                    } catch (e: Exception) {
                        val errorStatus = PluginStatus(
                            pluginId = pid,
                            displayName = "Error",
                            status = "Error: ${e.message}",
                            errorMessage = e.message
                        )
                        results.add(errorStatus)
                    }
                }

                storage.getState().history.add(
                    PluginHistoryEntry(
                        pluginId = pid,
                        timestamp = System.currentTimeMillis(),
                        durationMs = duration
                    )
                )
            }
            ApplicationManager.getApplication().invokeLater {
                for (s in results) {
                    if (s.errorMessage != null) {
                        notificationHandler.notifyError(s.pluginId, s.errorMessage)
                    } else {
                        val previousStatus = storage.getState().lastKnownStatus[s.pluginId]
                        notificationHandler.notifyPluginStatusChange(s, previousStatus?.status)
                    }
                }
                callback(results)
            }
        }
    }

    /**
     * Detects if a plugin status has changed and records the phase transition
     */
    private fun detectAndRecordStateChange(newStatus: PluginStatus) {
        val state = storage.getState()
        val lastKnownStatus = state.lastKnownStatus[newStatus.pluginId]

        if (lastKnownStatus == null || lastKnownStatus.verificationStage != newStatus.verificationStage) {
            val previousStage = lastKnownStatus?.verificationStage ?: VerificationStage.UNKNOWN
            val previousTimestamp = lastKnownStatus?.lastCheckedAt
            val durationInPreviousStage = if (previousTimestamp != null) {
                System.currentTimeMillis() - previousTimestamp
            } else {
                null
            }

            val transition = PhaseTransition(
                pluginId = newStatus.pluginId,
                version = newStatus.latestVersion,
                fromStage = previousStage,
                toStage = newStatus.verificationStage,
                timestamp = System.currentTimeMillis(),
                durationInPreviousStageMs = durationInPreviousStage
            )

            storage.addPhaseTransition(transition)
            state.lastKnownStatus[newStatus.pluginId] = newStatus
        } else {
            state.lastKnownStatus[newStatus.pluginId] = newStatus
        }
    }
}
