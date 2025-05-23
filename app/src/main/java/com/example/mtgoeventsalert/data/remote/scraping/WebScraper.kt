package com.example.mtgoeventsalert.data.remote.scraping

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.mtgoeventsalert.domain.model.TournamentStatus
import com.example.mtgoeventsalert.util.MTGOEventPatterns
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebScraper @Inject constructor(
    private val context: Context
) {
    
    data class ScrapingResult(
        val tournaments: List<TournamentStatus>,
        val success: Boolean,
        val error: String? = null,
        val cyclePosition: Int? = null
    )

    suspend fun scrapeTournamentData(url: String): ScrapingResult = withContext(Dispatchers.Main) {
        val deferred = CompletableDeferred<ScrapingResult>()
        
        try {
            val webView = WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        
                        // MTGBot loads content dynamically, wait for it
                        Handler(Looper.getMainLooper()).postDelayed({
                            extractTournamentData(view, deferred)
                        }, 5000) // 5 second delay for dynamic content
                    }
                    
                    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        deferred.complete(ScrapingResult(
                            tournaments = emptyList(),
                            success = false,
                            error = "WebView error: $description"
                        ))
                    }
                }
            }
            
            webView.loadUrl(url)
            
            withTimeoutOrNull(30000) {
                deferred.await()
            } ?: ScrapingResult(
                tournaments = emptyList(),
                success = false,
                error = "Scraping timeout"
            )
            
        } catch (e: Exception) {
            ScrapingResult(
                tournaments = emptyList(),
                success = false,
                error = "Scraping error: ${e.message}"
            )
        }
    }
    private fun extractTournamentData(webView: WebView?, deferred: CompletableDeferred<ScrapingResult>) {
        webView ?: run {
            deferred.complete(ScrapingResult(
                tournaments = emptyList(),
                success = false,
                error = "WebView is null"
            ))
            return
        }

        // Optimized JavaScript for MTGBot structure - based on actual HTML analysis
        val jsCode = """
            (function() {
                try {
                    console.log('MTGBot scraper starting - checking elements...');
                    
                    // Direct element access using the correct IDs from HTML analysis
                    const statusElement = document.getElementById('statustext');
                    const tournElement = document.getElementById('tourntext');
                    const recordElement = document.getElementById('recordtext');
                    const deckElement = document.getElementById('decktext');
                    
                    // Extract text content with null safety
                    const statusText = statusElement ? statusElement.innerText.trim() : 'Not found';
                    const tournText = tournElement ? tournElement.innerText.trim() : 'Not found';
                    const recordText = recordElement ? recordElement.innerText.trim() : 'Not found';
                    const deckText = deckElement ? deckElement.innerText.trim() : 'Not found';
                    
                    // Debug logging
                    console.log('Status element found:', statusElement ? 'Yes' : 'No');
                    console.log('Tournament element found:', tournElement ? 'Yes' : 'No');
                    console.log('Record element found:', recordElement ? 'Yes' : 'No');
                    console.log('Deck element found:', deckElement ? 'Yes' : 'No');
                    
                    console.log('Extracted data:');
                    console.log('- Status:', statusText);
                    console.log('- Tournament:', tournText);
                    console.log('- Record:', recordText);
                    console.log('- Deck:', deckText);

                    return JSON.stringify({
                        status: statusText,
                        tournament: tournText,
                        record: recordText,
                        deck: deckText,
                        timestamp: Date.now(),
                        pageTitle: document.title,
                        url: window.location.href
                    });
                    
                } catch (error) {
                    console.error('JavaScript error in MTGBot scraper:', error);
                    return JSON.stringify({
                        error: error.message,
                        status: 'Error',
                        tournament: 'Error',
                        record: 'Error',
                        deck: 'Error'
                    });
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode) { result ->
            try {
                if (result.isNullOrEmpty() || result == "null") {
                    deferred.complete(ScrapingResult(
                        tournaments = emptyList(),
                        success = false,
                        error = "No data returned from JavaScript evaluation"
                    ))
                    return@evaluateJavascript
                }

                val cleanResult = result.replace("\\\"", "\"").trim('"')
                val jsonObject = JSONObject(cleanResult)
                
                if (jsonObject.has("error")) {
                    deferred.complete(ScrapingResult(
                        tournaments = emptyList(),
                        success = false,
                        error = "JavaScript error: ${jsonObject.getString("error")}"
                    ))
                    return@evaluateJavascript
                }
                val tournamentName = jsonObject.getString("tournament")
                val statusText = jsonObject.getString("status")
                val recordText = jsonObject.getString("record")
                val deckText = jsonObject.getString("deck")
                
                // Check if we actually found tournament data
                if (tournamentName == "Not found" || tournamentName.isBlank()) {
                    deferred.complete(ScrapingResult(
                        tournaments = emptyList(),
                        success = true,
                        error = "No active tournaments found for this player"
                    ))
                    return@evaluateJavascript
                }

                val tournamentStatus = TournamentStatus(
                    tournamentId = generateTournamentId(tournamentName),
                    record = if (recordText != "Not found") recordText else "0-0",
                    currentStatus = statusText,
                    roundNumber = extractRoundNumber(statusText),
                    lastUpdated = System.currentTimeMillis(),
                    isWaitingForRound = isWaitingForRound(statusText),
                    hasEnded = isEnded(statusText)
                )

                deferred.complete(ScrapingResult(
                    tournaments = listOf(tournamentStatus),
                    success = true,
                    cyclePosition = 0
                ))

            } catch (e: Exception) {
                deferred.complete(ScrapingResult(
                    tournaments = emptyList(),
                    success = false,
                    error = "Failed to parse tournament data: ${e.message}"
                ))
            }
        }
    }

    private fun generateTournamentId(tournamentName: String): String {
        val cleanName = tournamentName.replace(Regex("[^a-zA-Z0-9]"), "_").lowercase()
        val dayTimestamp = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
        return "${cleanName}_$dayTimestamp"
    }

    private fun extractRoundNumber(status: String): Int? {
        val roundRegex = Regex("""(?:round|r)\s*(\d+)""", RegexOption.IGNORE_CASE)
        return roundRegex.find(status)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun isWaitingForRound(status: String): Boolean {
        return MTGOEventPatterns.isWaitingForRound(status)
    }

    private fun isEnded(status: String): Boolean {
        return MTGOEventPatterns.hasEnded(status)
    }

    fun isWebViewAvailable(): Boolean {
        return try {
            WebView(context)
            true
        } catch (e: Exception) {
            false
        }
    }
}