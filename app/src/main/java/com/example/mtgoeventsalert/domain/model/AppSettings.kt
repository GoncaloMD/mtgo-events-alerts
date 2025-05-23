package com.example.mtgoeventsalert.domain.model

data class AppSettings(
    val scrapingIntervalSeconds: Int = 30,
    val enableNotifications: Boolean = true,
    val notificationSoundEnabled: Boolean = true,
    val autoStartMonitoring: Boolean = false
)