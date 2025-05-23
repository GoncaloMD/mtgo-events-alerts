package com.example.mtgoeventsalert.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1, // Single row table
    val scrapingIntervalSeconds: Int,
    val enableNotifications: Boolean,
    val notificationSoundEnabled: Boolean,
    val autoStartMonitoring: Boolean,
    val lastUpdated: Long
)