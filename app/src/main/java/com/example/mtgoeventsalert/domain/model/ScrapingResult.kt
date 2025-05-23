package com.example.mtgoeventsalert.domain.model

data class MultiTournamentScrapingResult(
    val username: String,
    val tournaments: List<TournamentStatus>,
    val success: Boolean,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val cyclePosition: Int? = null  // Which tournament was being displayed when scraped
)

data class TournamentDetectionEvent(
    val username: String,
    val newTournaments: List<Tournament>,
    val endedTournaments: List<String>, // Tournament IDs that ended
    val timestamp: Long = System.currentTimeMillis()
)