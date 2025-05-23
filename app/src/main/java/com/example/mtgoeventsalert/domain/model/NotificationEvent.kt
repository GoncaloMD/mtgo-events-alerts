package com.example.mtgoeventsalert.domain.model

data class NotificationEvent(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType
)

enum class NotificationType {
    ROUND_STARTING,
    TOURNAMENT_ENDED,
    CONNECTION_ERROR
}