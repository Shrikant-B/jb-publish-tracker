package com.shrikantbadwaik.publish.time.tracker.data

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

data class PluginHistoryEntry(
    val pluginId: String = "",
    val timestamp: Long = 0L,
    val durationMs: Long = 0L
)

data class PublishTrackerState(
    var apiToken: String? = null,
    var trackedPluginIds: MutableList<String> = ArrayList(),
    var history: MutableList<PluginHistoryEntry> = ArrayList(),
    var verificationHistory: MutableList<VerificationHistory> = ArrayList(),
    var phaseTransitions: MutableList<PhaseTransition> = ArrayList(),
    var lastKnownStatus: MutableMap<String, PluginStatus> = HashMap(),
    var settings: PluginSettings = PluginSettings()
)

data class PluginSettings(
    var pollingIntervalMinutes: Int = 10,
    var notificationType: String = "BALLOON",
    var enableNotifications: Boolean = true,
    var enableAutoPoll: Boolean = false,
    var enableEmailNotifications: Boolean = false,
    var emailAddress: String? = null,
    var dataRetentionDays: Int = 30,
    var maxHistoryEntries: Int = 1000
)

@Service
@State(name = "PublishTrackerState", storages = [Storage("publish-tracker.xml")])
class PublishTrackerStorage : PersistentStateComponent<PublishTrackerState> {
    private var state = PublishTrackerState()
    override fun getState(): PublishTrackerState = state
    override fun loadState(state: PublishTrackerState) {
        this.state = state
    }

    // Data management methods
    fun addHistoryEntry(entry: PluginHistoryEntry) {
        state.history.add(entry)
        cleanupOldHistory()
    }
    
    fun addVerificationHistory(entry: VerificationHistory) {
        state.verificationHistory.add(entry)
        cleanupOldVerificationHistory()
    }
    
    fun cleanupOldHistory() {
        val cutoffTime = System.currentTimeMillis() - (state.settings.dataRetentionDays * 24 * 60 * 60 * 1000L)
        state.history.removeAll { it.timestamp < cutoffTime }
        
        // Also limit by max entries
        if (state.history.size > state.settings.maxHistoryEntries) {
            val sortedHistory = state.history.sortedBy { it.timestamp }
            state.history.clear()
            state.history.addAll(sortedHistory.takeLast(state.settings.maxHistoryEntries))
        }
    }
    
    fun cleanupOldVerificationHistory() {
        val cutoffTime = System.currentTimeMillis() - (state.settings.dataRetentionDays * 24 * 60 * 60 * 1000L)
        state.verificationHistory.removeAll { it.submittedAt < cutoffTime }
    }
    
    fun exportHistory(): String {
        val historyJson = buildString {
            appendLine("{")
            appendLine("  \"exportedAt\": ${System.currentTimeMillis()},")
            appendLine("  \"pluginHistory\": [")
            state.history.forEachIndexed { index, entry ->
                appendLine("    {")
                appendLine("      \"pluginId\": \"${entry.pluginId}\",")
                appendLine("      \"timestamp\": ${entry.timestamp},")
                appendLine("      \"durationMs\": ${entry.durationMs}")
                appendLine("    }${if (index < state.history.size - 1) "," else ""}")
            }
            appendLine("  ],")
            appendLine("  \"verificationHistory\": [")
            state.verificationHistory.forEachIndexed { index, entry ->
                appendLine("    {")
                appendLine("      \"pluginId\": \"${entry.pluginId}\",")
                appendLine("      \"version\": \"${entry.version}\",")
                appendLine("      \"submittedAt\": ${entry.submittedAt},")
                appendLine("      \"completedAt\": ${entry.completedAt ?: "null"},")
                appendLine("      \"stage\": \"${entry.stage}\",")
                appendLine("      \"processingTimeMs\": ${entry.processingTimeMs ?: "null"}")
                appendLine("    }${if (index < state.verificationHistory.size - 1) "," else ""}")
            }
            appendLine("  ]")
            appendLine("}")
        }
        return historyJson
    }
    
    fun clearAllHistory() {
        state.history.clear()
        state.verificationHistory.clear()
    }
    
    fun getHistoryForPlugin(pluginId: String): List<PluginHistoryEntry> {
        return state.history.filter { it.pluginId == pluginId }
    }
    
    fun getVerificationHistoryForPlugin(pluginId: String): List<VerificationHistory> {
        return state.verificationHistory.filter { it.pluginId == pluginId }
    }
    
    // Phase transition tracking methods
    fun addPhaseTransition(transition: PhaseTransition) {
        state.phaseTransitions.add(transition)
        cleanupOldPhaseTransitions()
    }
    
    fun getPhaseTransitionsForPlugin(pluginId: String, version: String? = null): List<PhaseTransition> {
        return if (version != null) {
            state.phaseTransitions.filter { it.pluginId == pluginId && it.version == version }
        } else {
            state.phaseTransitions.filter { it.pluginId == pluginId }
        }
    }
    
    fun buildTimeline(pluginId: String, version: String): PluginVersionTimeline? {
        val transitions = getPhaseTransitionsForPlugin(pluginId, version).sortedBy { it.timestamp }
        if (transitions.isEmpty()) return null
        
        var uploadedAt: Long? = null
        var verificationStartedAt: Long? = null
        var approvedAt: Long? = null
        var publishedAt: Long? = null
        
        for (transition in transitions) {
            when (transition.toStage) {
                VerificationStage.SUBMITTED -> uploadedAt = transition.timestamp
                VerificationStage.UNDER_REVIEW -> verificationStartedAt = transition.timestamp
                VerificationStage.APPROVED -> approvedAt = transition.timestamp
                VerificationStage.PUBLISHED -> publishedAt = transition.timestamp
                else -> {}
            }
        }
        
        val currentStage = transitions.lastOrNull()?.toStage ?: VerificationStage.UNKNOWN
        val firstTimestamp = transitions.firstOrNull()?.timestamp
        val lastTimestamp = transitions.lastOrNull()?.timestamp
        val totalDuration = if (firstTimestamp != null && lastTimestamp != null) {
            lastTimestamp - firstTimestamp
        } else null
        
        return PluginVersionTimeline(
            pluginId = pluginId,
            version = version,
            transitions = transitions,
            uploadedAt = uploadedAt,
            verificationStartedAt = verificationStartedAt,
            approvedAt = approvedAt,
            publishedAt = publishedAt,
            currentStage = currentStage,
            totalDurationMs = totalDuration
        )
    }
    
    fun getAveragePhaseDurations(pluginId: String? = null): Map<String, Long> {
        val relevantTransitions = if (pluginId != null) {
            state.phaseTransitions.filter { it.pluginId == pluginId }
        } else {
            state.phaseTransitions
        }
        
        // Group by version to get complete timelines
        val byVersion = relevantTransitions.groupBy { "${it.pluginId}:${it.version}" }
        
        val allDurations = mutableMapOf<String, MutableList<Long>>()
        
        byVersion.values.forEach { versionTransitions ->
            val sorted = versionTransitions.sortedBy { it.timestamp }
            var uploadTime: Long? = null
            var verificationTime: Long? = null
            var approvalTime: Long? = null
            
            sorted.forEach { transition ->
                when (transition.toStage) {
                    VerificationStage.SUBMITTED -> uploadTime = transition.timestamp
                    VerificationStage.UNDER_REVIEW -> {
                        verificationTime = transition.timestamp
                        uploadTime?.let {
                            allDurations.getOrPut("Upload to Verification") { mutableListOf() }
                                .add(verificationTime!! - it)
                        }
                    }
                    VerificationStage.APPROVED -> {
                        approvalTime = transition.timestamp
                        verificationTime?.let {
                            allDurations.getOrPut("Verification to Approval") { mutableListOf() }
                                .add(approvalTime!! - it)
                        }
                    }
                    VerificationStage.PUBLISHED -> {
                        val publishTime = transition.timestamp
                        approvalTime?.let {
                            allDurations.getOrPut("Approval to Published") { mutableListOf() }
                                .add(publishTime - it)
                        }
                        uploadTime?.let {
                            allDurations.getOrPut("Total (Upload to Published)") { mutableListOf() }
                                .add(publishTime - it)
                        }
                    }
                    else -> {}
                }
            }
        }
        
        return allDurations.mapValues { (_, durations) ->
            if (durations.isEmpty()) 0L else durations.average().toLong()
        }
    }
    
    private fun cleanupOldPhaseTransitions() {
        val cutoffTime = System.currentTimeMillis() - (state.settings.dataRetentionDays * 24 * 60 * 60 * 1000L)
        state.phaseTransitions.removeAll { it.timestamp < cutoffTime }
    }

    companion object {
        fun getInstance(): PublishTrackerStorage = ApplicationManager.getApplication().getService(PublishTrackerStorage::class.java)
    }
}