package com.shrikantbadwaik.publish.time.tracker.data.repo

import com.shrikantbadwaik.publish.time.tracker.data.api.MarketplaceApi
import com.shrikantbadwaik.publish.time.tracker.data.PluginStatus
import com.shrikantbadwaik.publish.time.tracker.data.VerificationStage
import kotlinx.coroutines.*
import com.intellij.util.concurrency.AppExecutorUtil

interface Repository {
    suspend fun fetchStatus(pluginId: String): PluginStatus
    fun fetchStatusSync(pluginId: String): PluginStatus
}

internal class RepositoryImpl(
    private val apiFactory: () -> MarketplaceApi
) : Repository {
    private val ioDispatcher = AppExecutorUtil.getAppExecutorService().asCoroutineDispatcher()
    
    override suspend fun fetchStatus(pluginId: String): PluginStatus {
        return withContext(ioDispatcher) {
            val api = apiFactory()
            try {
                val resp = api.fetchUpdates(pluginId)
                val latest = resp.firstOrNull()

                val verificationStage = VerificationStage.fromApiStatus(
                    status = null,
                    approve = latest?.approve, 
                    listed = latest?.listed
                )
                
                val status = verificationStage.name.lowercase()
                val version = latest?.version.orEmpty()
                val displayName = latest?.author?.name ?: pluginId
                val timestamp = latest?.getTimestamp()
                
                PluginStatus(
                    pluginId = pluginId,
                    displayName = displayName,
                    latestVersion = version,
                    status = status,
                    verificationStage = verificationStage,
                    lastCheckedAt = System.currentTimeMillis(),
                    lastUpdatedAtIso = timestamp
                )
            } catch (e: Exception) {
                print(e.message)
                PluginStatus(
                    pluginId = pluginId,
                    displayName = pluginId,
                    latestVersion = "",
                    status = "unknown",
                    verificationStage = VerificationStage.UNKNOWN,
                    lastCheckedAt = System.currentTimeMillis(),
                    lastUpdatedAtIso = null,
                    errorMessage = e.message
                )
            } finally {
                api.close()
            }
        }
    }
    
    override fun fetchStatusSync(pluginId: String): PluginStatus {
        return runBlocking(ioDispatcher) {
            fetchStatus(pluginId)
        }
    }
}