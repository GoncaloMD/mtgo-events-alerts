package com.example.mtgoeventsalert.domain.model

data class Tournament(
    val id: String,              // Unique identifier
    val name: String,            // e.g., "Modern Challenge", "Legacy Showcase"
    val format: String,          // e.g., "Modern", "Legacy"
    val playerUsername: String,  // Which player this tournament belongs to
    val status: TournamentStatus,
    val isActive: Boolean = true
)