package com.example.mtgoeventsalert.service

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.mtgoeventsalert.domain.repository.IPlayerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonitoringManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playerRepository: IPlayerRepository
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentUsername: String? = null
    private var isAppInForeground = true

    fun initialize() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun startMonitoring(username: String) {
        currentUsername = username
        
        if (isAppInForeground) {
            // App is open - no need for foreground service
            stopBackgroundService()
        } else {
            // App is backgrounded - start foreground service
            startBackgroundService(username)
        }
    }

    fun stopMonitoring() {
        currentUsername = null
        stopBackgroundService()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // App came to foreground
        isAppInForeground = true
        
        // Stop foreground service since app is now visible
        stopBackgroundService()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // App went to background
        isAppInForeground = false
        
        // Start foreground service if we have an active user
        currentUsername?.let { username ->
            startBackgroundService(username)
        }
    }

    private fun startBackgroundService(username: String) {
        MonitoringService.startService(context, username)
    }

    private fun stopBackgroundService() {
        MonitoringService.stopService(context)
    }
}