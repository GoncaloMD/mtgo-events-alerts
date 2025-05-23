package com.example.mtgoeventsalert.presentation.ui.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtgoeventsalert.domain.repository.ITournamentRepository
import com.example.mtgoeventsalert.presentation.ui.state.TournamentUiState
import com.example.mtgoeventsalert.presentation.ui.state.MonitoringUiState
import com.example.mtgoeventsalert.service.MonitoringManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TournamentViewModel @Inject constructor(
    private val tournamentRepository: ITournamentRepository,
    private val monitoringManager: MonitoringManager
) : ViewModel() {

    private val _tournamentState = MutableStateFlow<TournamentUiState>(TournamentUiState.Loading)
    val tournamentState: StateFlow<TournamentUiState> = _tournamentState.asStateFlow()

    private val _monitoringState = MutableStateFlow<MonitoringUiState>(MonitoringUiState.Stopped)
    val monitoringState: StateFlow<MonitoringUiState> = _monitoringState.asStateFlow()

    private var currentUsername: String? = null

    fun loadTournaments(username: String) {
        currentUsername = username
        _tournamentState.value = TournamentUiState.Loading

        viewModelScope.launch {
            try {
                tournamentRepository.getTournaments(username)
                    .catch { e ->
                        _tournamentState.value = TournamentUiState.Error(
                            message = "Failed to load tournaments: ${e.message}",
                            canRetry = true
                        )
                    }
                    .collect { tournaments ->
                        if (tournaments.isEmpty()) {
                            _tournamentState.value = TournamentUiState.Empty
                        } else {
                            _tournamentState.value = TournamentUiState.Success(
                                tournaments = tournaments,
                                isMonitoring = _monitoringState.value is MonitoringUiState.Active
                            )
                        }
                        
                        // Update monitoring state
                        if (tournaments.isNotEmpty()) {
                            _monitoringState.value = MonitoringUiState.Active(
                                username = username,
                                tournamentCount = tournaments.size,
                                lastUpdate = System.currentTimeMillis()
                            )
                        }
                    }
            } catch (e: Exception) {
                _tournamentState.value = TournamentUiState.Error(
                    message = "Failed to load tournaments: ${e.message}",
                    canRetry = true
                )
            }
        }
    }
    fun retryLoading() {
        currentUsername?.let { username ->
            loadTournaments(username)
        }
    }

    fun stopMonitoring() {
        viewModelScope.launch {
            try {
                monitoringManager.stopMonitoring()
                _monitoringState.value = MonitoringUiState.Stopped
                
                // Update tournament state to reflect monitoring stopped
                val currentState = _tournamentState.value
                if (currentState is TournamentUiState.Success) {
                    _tournamentState.value = currentState.copy(isMonitoring = false)
                }
            } catch (e: Exception) {
                _monitoringState.value = MonitoringUiState.Error(
                    message = "Failed to stop monitoring: ${e.message}"
                )
            }
        }
    }

    fun startMonitoring() {
        currentUsername?.let { username ->
            viewModelScope.launch {
                try {
                    _monitoringState.value = MonitoringUiState.Starting
                    monitoringManager.startMonitoring(username)
                    
                    // Refresh tournaments to get latest data
                    loadTournaments(username)
                } catch (e: Exception) {
                    _monitoringState.value = MonitoringUiState.Error(
                        message = "Failed to start monitoring: ${e.message}"
                    )
                }
            }
        }
    }

    fun refreshTournaments() {
        currentUsername?.let { username ->
            loadTournaments(username)
        }
    }
}