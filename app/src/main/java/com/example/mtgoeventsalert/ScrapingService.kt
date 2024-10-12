package com.example.mtgoeventsalert

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import org.json.JSONObject

class ScrapingService : Service() {

    private val channelId: String = "scraping_service_channel"
    private val scrapeInterval: Long = 30000 // 30 seconds in milliseconds

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var userName: String

    private lateinit var webView: WebView

    private val scrapeRunnable = object : Runnable {
        override fun run() {
            val sharedPref = getSharedPreferences("MTGOEventsAlert", Context.MODE_PRIVATE)
            userName = sharedPref.getString("mtgo-username","").toString()
            val url = "https://mtgbot.tv/overlay/compact.html?username=$userName"
            webView.loadUrl(url)
            handler.postDelayed(this, scrapeInterval) //1 minute
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        startForegroundService()
        setupWebView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Retrieve the user's name from the intent
        userName = intent?.getStringExtra("USER_NAME") ?: return START_NOT_STICKY
        // You can now use userName to construct your URL or for other purposes

        handler.post(scrapeRunnable)

        return START_REDELIVER_INTENT
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Checking for new Round")
            .setContentText("Checking MTGO to see if your tournament is about to get a new Round")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Scraping Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun sendNotification(title: String, message: String) {
        var resultNotification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, resultNotification)
    }

    private fun setupWebView() {
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                handler.postDelayed({
                    view?.evaluateJavascript(
                        """(function() {
                            const statusText = document.querySelector('.text#statustext')?.innerText || 'Not found';

                            return JSON.stringify({
                                status: statusText,
                            });
                        })();"""

                    ) {result ->
                        try {
                            //Exits if there is no result to avoid throwing unnecessary errors
                            if(result.isEmpty()) return@evaluateJavascript

                            //Clean up result for Kotlin use
                            var cleanResult = result.replace("\\", "")
                            cleanResult = cleanResult.trim('"')
                            val statusText = JSONObject(cleanResult).getString("status")

                            //sendNotification("A minute has passed", "A minute has passed since last check")
                            println(statusText)

                            if(statusText.contains("Waiting") && !statusText.contains("Match")) {
                                sendNotification("Round about to start", "Your next round is starting in less than 2 minutes!");
                            }

                        } catch (e: Exception) {

                        }
                    }
                }, 5000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(scrapeRunnable)  // Stop the scraping cycle when the service is destroyed
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}