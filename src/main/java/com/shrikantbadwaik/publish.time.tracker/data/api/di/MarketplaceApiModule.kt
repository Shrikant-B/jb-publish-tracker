package com.shrikantbadwaik.publish.time.tracker.data.api.di

import com.shrikantbadwaik.publish.time.tracker.data.api.MarketplaceApi
import com.shrikantbadwaik.publish.time.tracker.data.api.MarketplaceApiImpl
import com.shrikantbadwaik.publish.time.tracker.data.PublishTrackerStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object MarketplaceApiModule {
    private fun createClient() = HttpClient(engineFactory = OkHttp) {
        install(plugin = ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    fun getMarketplaceApi(): MarketplaceApi {
        val storage = PublishTrackerStorage.getInstance()
        val apiToken = storage.getState().apiToken
        return MarketplaceApiImpl(createClient(), apiToken)
    }
}
