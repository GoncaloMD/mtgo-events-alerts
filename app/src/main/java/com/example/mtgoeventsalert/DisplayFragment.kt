package com.example.mtgoeventsalert

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class DisplayFragment : Fragment() {

    private var isNotificationPermissionGranted = false

    private val args: DisplayFragmentArgs by navArgs()
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val updateInterval: Long = 1000 // 5 seconds, adjust as needed

    private lateinit var viewModel: MainViewModel
    private lateinit var nameTextView: TextView
    private lateinit var resultTextView: TextView
    private lateinit var webView: WebView
    private lateinit var returnButton: Button

    private lateinit var playerInfo: JSONObject

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //Creates UI elements from the xml file
        val view = inflater.inflate(R.layout.fragment_display, container, false)

        //Setting objects to their default values
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        nameTextView = view.findViewById(R.id.nameTextView)
        resultTextView = view.findViewById(R.id.resultTextView)
        webView = view.findViewById(R.id.webview)
        returnButton = view.findViewById(R.id.returnButton)

        playerInfo = JSONObject()
        playerInfo.put("tournament", "")
        playerInfo.put("record", "")
        playerInfo.put("status", "")

        resultTextView.text = jsonToView(playerInfo)

        // Observe the name saved in the ViewModel
        viewModel.name.observe(viewLifecycleOwner) { name ->
            nameTextView.text = "Tournament status: $name"

            // Load the webpage and scrape the info after the page has loaded
            loadMTGOResult(name)
        }

        var intent = Intent(requireContext(), ScrapingService::class.java).apply {
            putExtra("USER_NAME", nameTextView.text)
        }
        requireContext().startService(intent)

        // Return to the previous fragment
        returnButton.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start polling for updates
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                checkForUpdates()
                handler.postDelayed(this, updateInterval)
            }
        }
        handler.post(runnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable) // Stop the handler when the view is destroyed
    }

    private fun checkForUpdates() {
        // Assume webView is already initialized and loaded with the URL
        webView.evaluateJavascript(
            """
                        (function() {
                            const statusText = document.querySelector('.text#statustext')?.innerText || 'Not found';
                            const deckText = document.getElementById('decktext')?.innerText || 'Not found';
                            const tournText = document.getElementById('tourntext')?.innerText || 'Not found';
                            const recordText = document.getElementById('recordtext')?.innerText || 'Not found';

                            return JSON.stringify({
                                status: statusText,
                                deck: deckText,
                                tournament: tournText,
                                record: recordText
                            });
                        })();
                    """
        ) { result ->
            try {
                //Exits if there is no result to avoid throwing unnecessary errors
                if(result.isEmpty()) return@evaluateJavascript

                //Clean up result for Kotlin use
                var cleanResult = result.replace("\\", "")
                cleanResult = cleanResult.trim('"')
                val json = JSONObject(cleanResult)

                //Setting playerInfo
                playerInfo.put("tournament", json.getString("tournament"))
                playerInfo.put("record", json.getString("record"))
                playerInfo.put("status", json.getString("status"))

                //DEBUG testing notifications while app is on
                //TODO create a method to check if tournament is between rounds
                //sendNotification("test", "test test")

                /*val displayText = """
                    Tournament: ${playerInfo.getString("tournament")}
                    Record: ${playerInfo.getString("record")}
                    Status: ${playerInfo.getString("status")}
                """.trimIndent()*/

                // Update the UI with the scraped data
                resultTextView.text = jsonToView(playerInfo)
            } catch (e: Exception) {
                // Handle potential parsing errors
                println(e)
                resultTextView.text = "Error fetching data..."
            }
        }
    }

    private fun loadMTGOResult(name: String){

        webView.settings.javaScriptEnabled = true;

        //This override can potentially be deleted as it does not really fetch anything relevant
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Introduce a delay before scraping
                Handler(Looper.getMainLooper()).postDelayed({
                    val jsCode = """
                        (function() {
                            const statusText = document.querySelector('.text#statustext')?.innerText || 'Not found';
                            const deckText = document.getElementById('decktext')?.innerText || 'Not found';
                            const tournText = document.getElementById('tourntext')?.innerText || 'Not found';
                            const recordText = document.getElementById('recordtext')?.innerText || 'Not found';
        
                            return JSON.stringify({
                                status: statusText,
                                deck: deckText,
                                tournament: tournText,
                                record: recordText
                            });
                        })();
                    """

                    webView.evaluateJavascript(jsCode) { result ->
                        try {
                            //Currently not doing anything here, as the page takes some time to load
                        } catch (e: Exception) {
                            //Currently not doing anything here, as the page takes some time to load
                        }
                    }
                }, 0) // Delay set to 0 as default
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        }

        // Load the URL (you can modify this to include the name as a parameter)
        val url = "https://mtgbot.tv/overlay/compact.html?username=$name"
        webView.loadUrl(url)
    }

    fun sendNotification(title: String, message: String) {

        // Check for notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

                // Request permission
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101
                )
                return // Exit until permission is granted
            }
        }

        // If permission is granted or not required, show the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "mtgo_events_alert"
            val channelName = "MTGO Events Alert"
            val channelDescription = "MTGO Events Alert pings you whenever a tournament you are participating in is about to get a new round."
            val channelImportance = NotificationManager.IMPORTANCE_DEFAULT

            val notificationChannel = NotificationChannel(
                channelId
                , channelName
                , channelImportance
            ).apply {
                description = channelDescription
            }

            val notificationManager = requireContext().getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(notificationChannel)
        }

        val notificationManager = NotificationManagerCompat.from(requireContext())
        val notificationId = 1

        val builder = NotificationCompat.Builder(requireContext(), "mtgo_events_alert")
            .setSmallIcon(R.drawable.ic_launcher_background) // Replace with your icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(notificationId, builder.build())
    }

    private fun jsonToView(json: JSONObject): String {

        val stringBuilder = StringBuilder()

        // Iterate through the keys of the JSONObject
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.optString(key, "Not found")
            stringBuilder.append("$key: $value\n")
        }

        return stringBuilder.toString().trim()
    }
}
