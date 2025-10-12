package com.shrikantbadwaik.publish.time.tracker.data.repo.di

import com.shrikantbadwaik.publish.time.tracker.data.api.di.MarketplaceApiModule
import com.shrikantbadwaik.publish.time.tracker.data.repo.Repository
import com.shrikantbadwaik.publish.time.tracker.data.repo.RepositoryImpl

object RepoModule {
    private val apiFactory = { MarketplaceApiModule.getMarketplaceApi() }

    fun getRepo(): Repository = RepositoryImpl(apiFactory)
}
