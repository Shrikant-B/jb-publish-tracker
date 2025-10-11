package com.shrikantbadwaik.publish.time.tracker.data

import kotlinx.serialization.Serializable

@Serializable
data class MarketplaceUpdate(
    val id: Long? = null,
    val link: String? = null,
    val pluginId: Long? = null,
    val version: String? = null,
    val approve: Boolean? = null,
    val listed: Boolean? = null,
    val hidden: Boolean? = null,
    val author: Author? = null,
    val cdate: String? = null, // Timestamp as string in milliseconds
    val downloads: Long? = null,
    val size: Long? = null,
    val notes: String? = null,
    val channel: String? = null
) {
    /**
     * Converts cdate string to Long timestamp
     */
    fun getTimestamp(): Long? {
        return cdate?.toLongOrNull()
    }
}

@Serializable
data class Author(
    val id: String? = null,
    val name: String? = null,
    val link: String? = null,
    val hubLogin: String? = null,
    val isJetBrains: Boolean? = null
)

// Type alias for the API response which is a direct array
typealias PluginUpdatesResponse = List<MarketplaceUpdate>

enum class VerificationStage {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    PUBLISHED,
    UNKNOWN;
    
    companion object {
        /**
         * Infers verification stage from API response fields.
         * Note: JetBrains Marketplace API only returns approved/published plugins.
         * Plugins in review or rejected states are not returned by the API.
         */
        fun fromApiStatus(status: String?, approve: Boolean?, listed: Boolean?): VerificationStage {
            return when {
                // If we have data from the API, the plugin has been approved
                approve == true && listed == true -> PUBLISHED
                approve == true && listed == false -> APPROVED  // Approved but not yet visible
                approve == false -> REJECTED
                // If no approval info, assume published (most common case in API)
                approve == null && listed == true -> PUBLISHED
                approve == null && listed == false -> APPROVED
                // Explicit status checks (in case API adds these in future)
                status != null && status.contains("submitted", ignoreCase = true) -> SUBMITTED
                status != null && status.contains("review", ignoreCase = true) -> UNDER_REVIEW
                status != null && status.contains("rejected", ignoreCase = true) -> REJECTED
                else -> UNKNOWN
            }
        }
        
        fun fromString(status: String): VerificationStage {
            return when (status.lowercase()) {
                "submitted" -> SUBMITTED
                "under_review", "review" -> UNDER_REVIEW
                "approved" -> APPROVED
                "rejected" -> REJECTED
                "published" -> PUBLISHED
                else -> UNKNOWN
            }
        }
    }
}

data class PluginStatus(
    val pluginId: String = "",
    val displayName: String = "",
    val latestVersion: String = "",
    val status: String = "",
    val verificationStage: VerificationStage = VerificationStage.UNKNOWN,
    val lastCheckedAt: Long = System.currentTimeMillis(),
    val lastUpdatedAtIso: Long? = null,
    val processingTimeMs: Long? = null,
    val errorMessage: String? = null
)

data class PluginMetrics(
    val pluginId: String = "",
    val averageVerificationTimeMs: Long = 0L,
    val successRate: Double = 0.0,
    val totalSubmissions: Int = 0,
    val lastVerificationTime: Long = 0L,
    val fastestVerificationMs: Long = 0L,
    val slowestVerificationMs: Long = 0L
)

data class VerificationHistory(
    val pluginId: String = "",
    val version: String = "",
    val submittedAt: Long = 0L,
    val completedAt: Long? = null,
    val stage: VerificationStage = VerificationStage.UNKNOWN,
    val processingTimeMs: Long? = null,
    val notes: String? = null
)

/**
 * Tracks individual phase transitions with timestamps
 * This allows us to see: Upload → Verification → Approval → Published timeline
 */
data class PhaseTransition(
    val pluginId: String = "",
    val version: String = "",
    val fromStage: VerificationStage = VerificationStage.UNKNOWN,
    val toStage: VerificationStage = VerificationStage.UNKNOWN,
    val timestamp: Long = System.currentTimeMillis(),
    val durationInPreviousStageMs: Long? = null
)

/**
 * Complete timeline for a plugin version showing all phase transitions
 */
data class PluginVersionTimeline(
    val pluginId: String = "",
    val version: String = "",
    val transitions: List<PhaseTransition> = emptyList(),
    val uploadedAt: Long? = null,
    val verificationStartedAt: Long? = null,
    val approvedAt: Long? = null,
    val publishedAt: Long? = null,
    val currentStage: VerificationStage = VerificationStage.UNKNOWN,
    val totalDurationMs: Long? = null
) {
    fun getPhaseTimeline(): Map<String, Long?> {
        return mapOf(
            "Upload" to uploadedAt,
            "Verification" to verificationStartedAt,
            "Approval" to approvedAt,
            "Published" to publishedAt
        )
    }
    
    fun getPhaseDurations(): Map<String, Long?> {
        val durations = mutableMapOf<String, Long?>()
        
        if (uploadedAt != null && verificationStartedAt != null) {
            durations["Upload to Verification"] = verificationStartedAt - uploadedAt
        }
        
        if (verificationStartedAt != null && approvedAt != null) {
            durations["Verification to Approval"] = approvedAt - verificationStartedAt
        }
        
        if (approvedAt != null && publishedAt != null) {
            durations["Approval to Published"] = publishedAt - approvedAt
        }
        
        if (uploadedAt != null && publishedAt != null) {
            durations["Total (Upload to Published)"] = publishedAt - uploadedAt
        }
        
        return durations
    }
}