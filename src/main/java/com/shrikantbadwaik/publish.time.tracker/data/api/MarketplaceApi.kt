package com.shrikantbadwaik.publish.time.tracker.data.api

import com.shrikantbadwaik.publish.time.tracker.data.MarketplaceUpdate
import com.shrikantbadwaik.publish.time.tracker.data.PluginUpdatesResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import kotlinx.serialization.json.Json

private const val BASE_URL = "https://plugins.jetbrains.com/"

interface MarketplaceApi {
    suspend fun fetchUpdates(pluginId: String): PluginUpdatesResponse
    fun close()
}

internal class MarketplaceApiImpl(
    private val client: HttpClient,
    private val apiToken: String?
) : MarketplaceApi {

    override suspend fun fetchUpdates(pluginId: String): PluginUpdatesResponse {
        val url = "${BASE_URL}api/plugins/$pluginId/updates"
        println("[MarketplaceApi] Fetching updates for plugin: $pluginId")
        println("[MarketplaceApi] URL: $url")
        println("[MarketplaceApi] Auth Token: ${if (apiToken != null) "Present" else "Not set"}")
        
        val httpResponse = client.get(urlString = url) {
            accept(ContentType.Application.Json)
            // Add authentication if token is available
            apiToken?.let {
                header("Authorization", "Bearer $it")
            }
        }
        
        // Get raw JSON text and manually parse to List
        val jsonText = httpResponse.bodyAsText()
        println("[MarketplaceApi] Response JSON length: ${jsonText.length}")
        
        val json = Json { 
            ignoreUnknownKeys = true
            isLenient = true
        }
        val response: List<MarketplaceUpdate> = json.decodeFromString(jsonText)
        
        println("[MarketplaceApi] Received ${response.size} updates")
        return response
    }

    override fun close() {
        client.close()
    }
}

class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)