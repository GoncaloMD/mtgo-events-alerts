package com.example.mtgoeventsalert.presentation.ui.state

import com.example.mtgoeventsalert.domain.model.Tournament

sealed class TournamentUiState {
    object Loading : TournamentUiState()
    object Empty : TournamentUiState()
    data class Success(
        val tournaments: List<Tournament>,
        val isMonitoring: Boolean = false
    ) : TournamentUiState()
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : TournamentUiState()
}

sealed class MonitoringUiState {
    object Stopped : MonitoringUiState()
    object Starting : MonitoringUiState()
    data class Active(
        val username: String,
        val tournamentCount: Int,
        val lastUpdate: Long
    ) : MonitoringUiState()
    data class Error(
        val message: String
    ) : MonitoringUiState()
}