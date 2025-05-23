package com.example.mtgoeventsalert.service

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.mtgoeventsalert.service.MonitoringManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MTGOEventsApplication : Application() {

    @Inject
    lateinit var monitoringManager: MonitoringManager

    override fun onCreate() {
        super.onCreate()
        
        // Initialize lifecycle-aware monitoring
        monitoringManager.initialize()
    }
}