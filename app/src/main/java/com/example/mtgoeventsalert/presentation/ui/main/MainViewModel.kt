package com.example.mtgoeventsalert.presentation.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtgoeventsalert.domain.repository.IPlayerRepository
import com.example.mtgoeventsalert.domain.model.Player
import com.example.mtgoeventsalert.data.local.preferences.PreferencesManager
import com.example.mtgoeventsalert.service.MonitoringManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val playerRepository: IPlayerRepository,
    private val preferencesManager: PreferencesManager,
    private val monitoringManager: MonitoringManager
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        // Load the last used username
        viewModelScope.launch {
            preferencesManager.currentUsername.collect { savedUsername ->
                if (!savedUsername.isNullOrBlank()) {
                    _username.value = savedUsername
                }
            }
        }
    }

    fun updateUsername(newUsername: String) {
        _username.value = newUsername.trim()
        _errorMessage.value = null
    }

    fun onSearchClick() {
        val currentUsername = _username.value.trim()
        
        if (currentUsername.isBlank()) {
            _errorMessage.value = "Please enter a valid MTGO username"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Save the player
                val player = Player(
                    username = currentUsername,
                    isActive = true
                )
                playerRepository.savePlayer(player)

                // Save username to preferences
                preferencesManager.updateCurrentUsername(currentUsername)

                // Start monitoring
                monitoringManager.startMonitoring(currentUsername)

                // Navigate to tournament view
                _navigationEvent.emit(NavigationEvent.NavigateToTournaments(currentUsername))

            } catch (e: Exception) {
                _errorMessage.value = "Failed to start monitoring: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    sealed class NavigationEvent {
        data class NavigateToTournaments(val username: String) : NavigationEvent()
    }
}