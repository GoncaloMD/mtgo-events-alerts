package com.example.mtgoeventsalert.util

enum class MonitoringState {
    STOPPED,           // No monitoring active
    FOREGROUND,        // App is open, direct monitoring
    BACKGROUND_SERVICE // App backgrounded, foreground service running
}

data class MonitoringStatus(
    val state: MonitoringState,
    val username: String?,
    val activeTournaments: Int = 0,
    val lastUpdate: Long = System.currentTimeMillis(),
    val error: String? = null
)