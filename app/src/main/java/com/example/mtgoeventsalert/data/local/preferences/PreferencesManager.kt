package com.example.mtgoeventsalert.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private val SCRAPING_INTERVAL = intPreferencesKey("scraping_interval")
        private val ENABLE_NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
        private val NOTIFICATION_SOUND = booleanPreferencesKey("notification_sound")
        private val AUTO_START_MONITORING = booleanPreferencesKey("auto_start_monitoring")
        private val PUSH_NOTIFICATIONS_ENABLED = booleanPreferencesKey("push_notifications_enabled")
        private val CURRENT_USERNAME = stringPreferencesKey("current_username")
    }

    val scrapingInterval: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SCRAPING_INTERVAL] ?: 30
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_NOTIFICATIONS] ?: true
    }

    val notificationSoundEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_SOUND] ?: true
    }

    val autoStartMonitoring: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_START_MONITORING] ?: false
    }

    val pushNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PUSH_NOTIFICATIONS_ENABLED] ?: false // Future feature
    }

    val currentUsername: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_USERNAME]
    }

    suspend fun updateScrapingInterval(interval: Int) {
        context.dataStore.edit { preferences ->
            preferences[SCRAPING_INTERVAL] = interval
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_NOTIFICATIONS] = enabled
        }
    }

    suspend fun updateCurrentUsername(username: String?) {
        context.dataStore.edit { preferences ->
            if (username != null) {
                preferences[CURRENT_USERNAME] = username
            } else {
                preferences.remove(CURRENT_USERNAME)
            }
        }
    }

    // Future: Push notification settings
    suspend fun updatePushNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PUSH_NOTIFICATIONS_ENABLED] = enabled
        }
    }
}