package com.example.mtgoeventsalert.domain.model

data class Player(
    val username: String,
    val isActive: Boolean = true,
    val tournaments: List<String> = emptyList() // Tournament IDs player is tracking
)