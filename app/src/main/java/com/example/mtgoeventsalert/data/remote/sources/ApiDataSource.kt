package com.example.mtgoeventsalert.data.remote.sources

import com.example.mtgoeventsalert.domain.model.MultiTournamentScrapingResult
import com.example.mtgoeventsalert.domain.model.TournamentStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiDataSource @Inject constructor(
    // Future: HTTP client dependency
) : ITournamentDataSource {

    override suspend fun getTournamentData(username: String): MultiTournamentScrapingResult = withContext(Dispatchers.IO) {
        // TODO: Implement API calls when available
        MultiTournamentScrapingResult(
            username = username,
            tournaments = emptyList(),
            success = false,
            error = "API not yet implemented",
            timestamp = System.currentTimeMillis()
        )
    }

    override suspend fun isAvailable(): Boolean = false // Not implemented yet
    
    override fun getSourceType(): DataSourceType = DataSourceType.REST_API
    
    override fun getSourceName(): String = "MTGO Official API"
}