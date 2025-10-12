package com.shrikantbadwaik.publish.time.tracker

import com.shrikantbadwaik.publish.time.tracker.data.MarketplaceUpdate
import com.shrikantbadwaik.publish.time.tracker.data.PhaseTransition
import com.shrikantbadwaik.publish.time.tracker.data.PluginStatus
import com.shrikantbadwaik.publish.time.tracker.data.VerificationStage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DataModelsTest {

    @Test
    fun `test MarketplaceUpdate timestamp conversion`() {
        val update = MarketplaceUpdate(cdate = "1234567890000")
        assertEquals(1234567890000L, update.getTimestamp())
    }

    @Test
    fun `test MarketplaceUpdate with invalid timestamp returns null`() {
        val update = MarketplaceUpdate(cdate = "invalid")
        assertNull(update.getTimestamp())
    }

    @Test
    fun `test VerificationStage fromApiStatus with published plugin`() {
        val stage = VerificationStage.fromApiStatus(
            status = null,
            approve = true,
            listed = true
        )
        assertEquals(VerificationStage.PUBLISHED, stage)
    }

    @Test
    fun `test VerificationStage fromApiStatus with approved but not listed`() {
        val stage = VerificationStage.fromApiStatus(
            status = null,
            approve = true,
            listed = false
        )
        assertEquals(VerificationStage.APPROVED, stage)
    }

    @Test
    fun `test VerificationStage fromApiStatus with rejected plugin`() {
        val stage = VerificationStage.fromApiStatus(
            status = null,
            approve = false,
            listed = false
        )
        assertEquals(VerificationStage.REJECTED, stage)
    }

    @Test
    fun `test VerificationStage fromString`() {
        assertEquals(VerificationStage.SUBMITTED, VerificationStage.fromString("submitted"))
        assertEquals(VerificationStage.UNDER_REVIEW, VerificationStage.fromString("review"))
        assertEquals(VerificationStage.APPROVED, VerificationStage.fromString("approved"))
        assertEquals(VerificationStage.REJECTED, VerificationStage.fromString("rejected"))
        assertEquals(VerificationStage.PUBLISHED, VerificationStage.fromString("published"))
        assertEquals(VerificationStage.UNKNOWN, VerificationStage.fromString("unknown_status"))
    }

    @Test
    fun `test PluginStatus creation with defaults`() {
        val status = PluginStatus()
        assertNotNull(status)
        assertEquals("", status.pluginId)
        assertEquals("", status.displayName)
        assertEquals(VerificationStage.UNKNOWN, status.verificationStage)
    }

    @Test
    fun `test PluginStatus with error message`() {
        val status = PluginStatus(
            pluginId = "123",
            displayName = "Test Plugin",
            errorMessage = "Network error"
        )
        assertEquals("123", status.pluginId)
        assertEquals("Test Plugin", status.displayName)
        assertEquals("Network error", status.errorMessage)
    }

    @Test
    fun `test PhaseTransition creation`() {
        val transition = PhaseTransition(
            pluginId = "123",
            version = "1.0.0",
            fromStage = VerificationStage.SUBMITTED,
            toStage = VerificationStage.UNDER_REVIEW,
            durationInPreviousStageMs = 5000L
        )
        assertEquals("123", transition.pluginId)
        assertEquals("1.0.0", transition.version)
        assertEquals(VerificationStage.SUBMITTED, transition.fromStage)
        assertEquals(VerificationStage.UNDER_REVIEW, transition.toStage)
        assertEquals(5000L, transition.durationInPreviousStageMs)
    }

    @Test
    fun `test PhaseTransition with default values`() {
        val transition = PhaseTransition()
        assertNotNull(transition)
        assertEquals("", transition.pluginId)
        assertEquals(VerificationStage.UNKNOWN, transition.fromStage)
        assertNull(transition.durationInPreviousStageMs)
    }
}
