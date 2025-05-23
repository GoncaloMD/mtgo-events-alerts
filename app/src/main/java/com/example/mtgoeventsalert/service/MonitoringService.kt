package com.example.mtgoeventsalert.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.mtgoeventsalert.R
import com.example.mtgoeventsalert.domain.repository.ITournamentRepository
import com.example.mtgoeventsalert.domain.model.NotificationType
import com.example.mtgoeventsalert.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class MonitoringService : Service() {

    @Inject
    lateinit var tournamentRepository: ITournamentRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitoringJob: Job? = null
    private var currentUsername: String? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val ALERT_NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "monitoring_service"
        private const val ALERT_CHANNEL_ID = "tournament_alerts"
        private const val ACTION_STOP_SERVICE = "STOP_SERVICE"
        
        fun startService(context: Context, username: String) {
            val intent = Intent(context, MonitoringService::class.java).apply {
                putExtra("username", username)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, MonitoringService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }
        
        val username = intent?.getStringExtra("username")
        if (username == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        currentUsername = username
        startForeground(NOTIFICATION_ID, createForegroundNotification(username))
        startMonitoring(username)
        
        return START_REDELIVER_INTENT
    }
    private fun startMonitoring(username: String) {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    // Check tournaments
                    tournamentRepository.getTournaments(username)
                        .catch { e -> 
                            // Log error, continue monitoring
                            updateForegroundNotification("Error: ${e.message}")
                        }
                        .collect { tournaments ->
                            val waitingTournaments = tournaments.filter { 
                                it.status.isWaitingForRound && it.isActive 
                            }
                            
                            if (waitingTournaments.isNotEmpty()) {
                                waitingTournaments.forEach { tournament ->
                                    sendTournamentAlert(tournament.name, tournament.status.record)
                                }
                                updateForegroundNotification("Monitoring: ${tournaments.size} active tournaments")
                            } else {
                                updateForegroundNotification("Monitoring: ${tournaments.size} tournaments")
                            }
                        }
                } catch (e: Exception) {
                    updateForegroundNotification("Monitoring error - retrying...")
                }
                
                // Wait 30 seconds before next check
                delay(30_000)
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Tournament Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when app is monitoring tournaments in background"
                setShowBadge(false)
            }
            
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Tournament Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when tournament rounds are starting"
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }
    private fun createForegroundNotification(username: String): Notification {
        val stopIntent = Intent(this, MonitoringService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MTGO Tournament Monitoring")
            .setContentText("Monitoring tournaments for $username")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .build()
    }
    
    private fun updateForegroundNotification(message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MTGO Tournament Monitoring")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
            
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun sendTournamentAlert(tournamentName: String, record: String) {
        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle("Round Starting Soon!")
            .setContentText("$tournamentName - Your record: $record")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(ALERT_NOTIFICATION_ID + tournamentName.hashCode(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        monitoringJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}