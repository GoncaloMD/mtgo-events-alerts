package com.example.mtgoeventsalert.domain.model

data class TournamentStatus(
    val tournamentId: String,
    val record: String,          // e.g., "2-1", "3-0-1"
    val currentStatus: String,   // e.g., "Waiting for round to start"
    val roundNumber: Int? = null,
    val lastUpdated: Long,
    val isWaitingForRound: Boolean = false,
    val hasEnded: Boolean = false
)