package com.example.mtgoeventsalert.data.remote.sources

import com.example.mtgoeventsalert.domain.model.MultiTournamentScrapingResult
import com.example.mtgoeventsalert.domain.model.TournamentStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockDataSource @Inject constructor() : ITournamentDataSource {

    override suspend fun getTournamentData(username: String): MultiTournamentScrapingResult {
        // Mock data for testing
        val mockTournaments = listOf(
            TournamentStatus(
                tournamentId = "mock_modern_challenge_1",
                record = "2-1",
                currentStatus = "Waiting for round to start",
                roundNumber = 4,
                lastUpdated = System.currentTimeMillis(),
                isWaitingForRound = true,
                hasEnded = false
            ),
            TournamentStatus(
                tournamentId = "mock_legacy_showcase_1", 
                record = "1-0-1",
                currentStatus = "In match",
                roundNumber = 2,
                lastUpdated = System.currentTimeMillis(),
                isWaitingForRound = false,
                hasEnded = false
            )
        )

        return MultiTournamentScrapingResult(
            username = username,
            tournaments = mockTournaments,
            success = true,
            error = null,
            timestamp = System.currentTimeMillis(),
            cyclePosition = 0
        )
    }

    override suspend fun isAvailable(): Boolean = true
    
    override fun getSourceType(): DataSourceType = DataSourceType.MOCK_DATA
    
    override fun getSourceName(): String = "Mock Data Source"
}