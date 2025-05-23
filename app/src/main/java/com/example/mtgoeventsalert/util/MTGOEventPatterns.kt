package com.example.mtgoeventsalert.util

object MTGOEventPatterns {
    
    // Tournament types and their patterns
    enum class EventType(val patterns: List<String>) {
        QUALIFIER(listOf("qualifier", "ptq", "rptq", "mptq")),
        CHALLENGE(listOf("challenge", "weekly", "daily")),
        LEAGUE(listOf("league", "friendly")),
        PREMIER(listOf("premier", "showcase", "championship")),
        DRAFT(listOf("draft", "sealed", "limited")),
        CONSTRUCTED(listOf("modern", "legacy", "vintage", "pioneer", "standard", "pauper")),
        SPECIAL(listOf("arena", "championship", "invitational"))
    }
    
    // Status patterns that indicate waiting for next round (the critical 2-minute window)
    val WAITING_PATTERNS = listOf(
        "waiting for round",
        "about to start", 
        "starting soon",
        "round will begin",
        "next round in",
        "waiting for",
        "round starting",
        "preparing round"
    )
    
    // Status patterns that indicate actively in a match
    val IN_MATCH_PATTERNS = listOf(
        "in match",
        "playing",
        "game in progress", 
        "match in progress",
        "game",
        "vs ",
        "opponent"
    )
    
    // Status patterns that indicate tournament ended
    val ENDED_PATTERNS = listOf(
        "ended",
        "finished", 
        "eliminated",
        "dropped",
        "complete",
        "tournament over",
        "final standings",
        "qualified",
        "did not qualify"
    )
    
    // Formats with their detection patterns
    val FORMAT_PATTERNS = mapOf(
        "Modern" to listOf("modern"),
        "Legacy" to listOf("legacy"),
        "Vintage" to listOf("vintage"),
        "Pioneer" to listOf("pioneer"),
        "Standard" to listOf("standard"),
        "Pauper" to listOf("pauper"),
        "Limited" to listOf("draft", "sealed", "limited"),
        "Commander" to listOf("commander", "edh", "cmdr")
    )
    
    fun detectEventType(tournamentName: String): EventType {
        val nameLower = tournamentName.lowercase()
        return EventType.values().firstOrNull { eventType ->
            eventType.patterns.any { pattern -> nameLower.contains(pattern) }
        } ?: EventType.CONSTRUCTED
    }
    
    fun detectFormat(tournamentName: String): String {
        val nameLower = tournamentName.lowercase()
        return FORMAT_PATTERNS.entries.firstOrNull { (format, patterns) ->
            patterns.any { pattern -> nameLower.contains(pattern) }
        }?.key ?: "Unknown"
    }
    
    // Enhanced status detection for better accuracy across event types
    fun isWaitingForRound(status: String): Boolean {
        val statusLower = status.lowercase()
        val hasWaitingPattern = WAITING_PATTERNS.any { pattern -> 
            statusLower.contains(pattern) 
        }
        val hasMatchPattern = IN_MATCH_PATTERNS.any { pattern -> 
            statusLower.contains(pattern) 
        }
        
        // Only waiting if we have waiting indicators but no match indicators
        return hasWaitingPattern && !hasMatchPattern
    }
    
    fun isInMatch(status: String): Boolean {
        val statusLower = status.lowercase()
        return IN_MATCH_PATTERNS.any { pattern -> statusLower.contains(pattern) }
    }
    
    fun hasEnded(status: String): Boolean {
        val statusLower = status.lowercase()
        return ENDED_PATTERNS.any { pattern -> statusLower.contains(pattern) }
    }
}