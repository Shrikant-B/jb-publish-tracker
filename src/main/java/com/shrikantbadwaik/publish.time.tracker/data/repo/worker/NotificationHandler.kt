package com.shrikantbadwaik.publish.time.tracker.data.repo.worker

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.shrikantbadwaik.publish.time.tracker.data.PluginStatus
import com.shrikantbadwaik.publish.time.tracker.data.PublishTrackerStorage
import com.shrikantbadwaik.publish.time.tracker.data.VerificationStage

class NotificationHandler {

    private val storage = PublishTrackerStorage.getInstance()
    private val notificationGroup = "PublishTracker"

    enum class NotificationPreference {
        BALLOON,
        STICKY_BALLOON,
        NONE,
        EMAIL
    }

    fun notifyPluginStatusChange(pluginStatus: PluginStatus, previousStatus: String? = null) {
        if (!shouldNotify(pluginStatus, previousStatus)) {
            return
        }

        val notificationType = getNotificationType(pluginStatus)
        val title = getNotificationTitle(pluginStatus)
        val content = getNotificationContent(pluginStatus)

        val notification = Notification(
            notificationGroup,
            title,
            content,
            notificationType
        )

        // Add action buttons
        // Note: Action buttons would be added here in a full implementation

        Notifications.Bus.notify(notification)
    }

    fun notifyError(pluginId: String, error: String) {
        val notification = Notification(
            notificationGroup,
            "Plugin Fetch Error",
            "Failed to fetch status for plugin $pluginId: $error",
            NotificationType.ERROR
        )

        // Note: Retry action would be added here in a full implementation

        Notifications.Bus.notify(notification)
    }

    fun notifyConfigurationWarning(message: String) {
        val notification = Notification(
            notificationGroup,
            "Configuration Warning",
            message,
            NotificationType.WARNING
        )

        // Note: Settings action would be added here in a full implementation

        Notifications.Bus.notify(notification)
    }

    fun notifyAutoPollingStarted(intervalMinutes: Int) {
        val notification = Notification(
            notificationGroup,
            "Auto Polling Started",
            "Started monitoring plugins every $intervalMinutes minutes",
            NotificationType.INFORMATION
        )

        Notifications.Bus.notify(notification)
    }

    fun notifyAutoPollingStopped() {
        val notification = Notification(
            notificationGroup,
            "Auto Polling Stopped",
            "Stopped automatic plugin monitoring",
            NotificationType.INFORMATION
        )

        Notifications.Bus.notify(notification)
    }

    fun notifyBatchUpdateComplete(successCount: Int, errorCount: Int) {
        val notification = Notification(
            notificationGroup,
            "Batch Update Complete",
            "Updated $successCount plugins successfully${if (errorCount > 0) ", $errorCount errors" else ""}",
            if (errorCount > 0) NotificationType.WARNING else NotificationType.INFORMATION
        )

        Notifications.Bus.notify(notification)
    }

    private fun shouldNotify(pluginStatus: PluginStatus, previousStatus: String?): Boolean {
        // Only notify on status changes
        if (previousStatus == null || previousStatus == pluginStatus.status) {
            return false
        }

        // Check user preferences
        val preferences = getUserNotificationPreferences()
        if (preferences == NotificationPreference.NONE) {
            return false
        }

        // Notify for important status changes
        return isImportantStatusChange(pluginStatus.status, previousStatus)
    }

    private fun isImportantStatusChange(newStatus: String, previousStatus: String): Boolean {
        val newStage = VerificationStage.fromString(newStatus)
        val previousStage = VerificationStage.fromString(previousStatus)

        val importantStages = setOf(
            VerificationStage.APPROVED,
            VerificationStage.PUBLISHED,
            VerificationStage.REJECTED
        )

        return importantStages.contains(newStage) || importantStages.contains(previousStage)
    }

    private fun getNotificationType(pluginStatus: PluginStatus): NotificationType {
        return when (pluginStatus.verificationStage) {
            VerificationStage.REJECTED -> NotificationType.WARNING
            VerificationStage.APPROVED, VerificationStage.PUBLISHED -> NotificationType.INFORMATION
            VerificationStage.UNKNOWN -> if (pluginStatus.errorMessage != null) NotificationType.ERROR else NotificationType.INFORMATION
            else -> NotificationType.INFORMATION
        }
    }

    private fun getNotificationTitle(pluginStatus: PluginStatus): String {
        return when (pluginStatus.verificationStage) {
            VerificationStage.APPROVED -> "Plugin Approved"
            VerificationStage.PUBLISHED -> "Plugin Published"
            VerificationStage.REJECTED -> "Plugin Rejected"
            VerificationStage.SUBMITTED -> "Plugin Submitted"
            VerificationStage.UNDER_REVIEW -> "Plugin Under Review"
            VerificationStage.UNKNOWN -> if (pluginStatus.errorMessage != null) "Plugin Error" else "Plugin Status Update"
        }
    }

    private fun getNotificationContent(pluginStatus: PluginStatus): String {
        return "${pluginStatus.pluginId}: ${pluginStatus.status}"
    }

    private fun getUserNotificationPreferences(): NotificationPreference {
        // In a real implementation, this would read from user settings
        // For now, default to BALLOON
        return NotificationPreference.BALLOON
    }

    // Notification history management
    fun getNotificationHistory(): List<NotificationRecord> {
        // In a real implementation, this would return stored notification history
        return emptyList()
    }

    fun clearNotificationHistory() {
        // In a real implementation, this would clear stored notification history
    }

    data class NotificationRecord(
        val timestamp: Long,
        val title: String,
        val content: String,
        val type: NotificationType,
        val pluginId: String?
    )
}
