package com.example.mtgoeventsalert.data.local.database.dao

import androidx.room.*
import com.example.mtgoeventsalert.data.local.database.entities.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettingsEntity?>
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsOnce(): AppSettingsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettingsEntity)
    
    @Update
    suspend fun updateSettings(settings: AppSettingsEntity)
    
    @Query("UPDATE app_settings SET scrapingIntervalSeconds = :interval WHERE id = 1")
    suspend fun updateScrapingInterval(interval: Int)
    
    @Query("UPDATE app_settings SET enableNotifications = :enabled WHERE id = 1")
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    
    @Query("UPDATE app_settings SET notificationSoundEnabled = :enabled WHERE id = 1")
    suspend fun updateNotificationSound(enabled: Boolean)
    
    @Query("UPDATE app_settings SET autoStartMonitoring = :enabled WHERE id = 1")
    suspend fun updateAutoStart(enabled: Boolean)
}