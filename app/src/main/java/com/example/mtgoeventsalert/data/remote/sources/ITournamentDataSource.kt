package com.example.mtgoeventsalert.data.remote.sources

import com.example.mtgoeventsalert.domain.model.MultiTournamentScrapingResult

interface ITournamentDataSource {
    suspend fun getTournamentData(username: String): MultiTournamentScrapingResult
    suspend fun isAvailable(): Boolean
    fun getSourceType(): DataSourceType
    fun getSourceName(): String
}

enum class DataSourceType {
    WEB_SCRAPING,
    REST_API,
    WEBSOCKET,
    MOCK_DATA
}