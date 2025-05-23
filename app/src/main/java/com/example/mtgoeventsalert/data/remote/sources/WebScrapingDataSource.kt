package com.example.mtgoeventsalert.data.remote.sources

import com.example.mtgoeventsalert.domain.model.MultiTournamentScrapingResult
import com.example.mtgoeventsalert.data.remote.scraping.WebScraper
import com.example.mtgoeventsalert.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebScrapingDataSource @Inject constructor(
    private val webScraper: WebScraper
) : ITournamentDataSource {

    override suspend fun getTournamentData(username: String): MultiTournamentScrapingResult = withContext(Dispatchers.IO) {
        try {
            val url = "${Constants.MTGBOT_BASE_URL}?username=$username"
            val scrapingResult = webScraper.scrapeTournamentData(url)
            
            MultiTournamentScrapingResult(
                username = username,
                tournaments = scrapingResult.tournaments,
                success = scrapingResult.success,
                error = scrapingResult.error,
                timestamp = System.currentTimeMillis(),
                cyclePosition = scrapingResult.cyclePosition
            )
        } catch (e: Exception) {
            MultiTournamentScrapingResult(
                username = username,
                tournaments = emptyList(),
                success = false,
                error = "Scraping failed: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    override suspend fun isAvailable(): Boolean {
        return try {
            webScraper.isWebViewAvailable()
        } catch (e: Exception) {
            false
        }
    }

    override fun getSourceType(): DataSourceType = DataSourceType.WEB_SCRAPING
    
    override fun getSourceName(): String = "MTGBot Web Scraping"
}