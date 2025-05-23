package com.example.mtgoeventsalert.data.local.mappers

import com.example.mtgoeventsalert.domain.model.Player
import com.example.mtgoeventsalert.domain.model.Tournament
import com.example.mtgoeventsalert.domain.model.TournamentStatus
import com.example.mtgoeventsalert.domain.model.AppSettings
import com.example.mtgoeventsalert.data.local.database.entities.PlayerEntity
import com.example.mtgoeventsalert.data.local.database.entities.TournamentEntity
import com.example.mtgoeventsalert.data.local.database.entities.TournamentStatusEntity
import com.example.mtgoeventsalert.data.local.database.entities.AppSettingsEntity

// Player mappings
fun Player.toEntity(): PlayerEntity {
    return PlayerEntity(
        username = username,
        isActive = isActive,
        createdAt = System.currentTimeMillis(),
        lastUpdated = System.currentTimeMillis()
    )
}

fun PlayerEntity.toDomain(tournaments: List<String> = emptyList()): Player {
    return Player(
        username = username,
        isActive = isActive,
        tournaments = tournaments
    )
}

// Tournament mappings
fun Tournament.toEntity(): TournamentEntity {
    return TournamentEntity(
        id = id,
        name = name,
        format = format,
        playerUsername = playerUsername,
        isActive = isActive,
        createdAt = System.currentTimeMillis(),
        lastUpdated = System.currentTimeMillis()
    )
}

fun TournamentEntity.toDomain(status: TournamentStatus): Tournament {
    return Tournament(
        id = id,
        name = name,
        format = format,
        playerUsername = playerUsername,
        status = status,
        isActive = isActive
    )
}
// Tournament Status mappings
fun TournamentStatus.toEntity(): TournamentStatusEntity {
    return TournamentStatusEntity(
        tournamentId = tournamentId,
        record = record,
        currentStatus = currentStatus,
        roundNumber = roundNumber,
        lastUpdated = lastUpdated,
        isWaitingForRound = isWaitingForRound,
        hasEnded = hasEnded
    )
}

fun TournamentStatusEntity.toDomain(): TournamentStatus {
    return TournamentStatus(
        tournamentId = tournamentId,
        record = record,
        currentStatus = currentStatus,
        roundNumber = roundNumber,
        lastUpdated = lastUpdated,
        isWaitingForRound = isWaitingForRound,
        hasEnded = hasEnded
    )
}

// App Settings mappings
fun AppSettings.toEntity(): AppSettingsEntity {
    return AppSettingsEntity(
        scrapingIntervalSeconds = scrapingIntervalSeconds,
        enableNotifications = enableNotifications,
        notificationSoundEnabled = notificationSoundEnabled,
        autoStartMonitoring = autoStartMonitoring,
        lastUpdated = System.currentTimeMillis()
    )
}

fun AppSettingsEntity.toDomain(): AppSettings {
    return AppSettings(
        scrapingIntervalSeconds = scrapingIntervalSeconds,
        enableNotifications = enableNotifications,
        notificationSoundEnabled = notificationSoundEnabled,
        autoStartMonitoring = autoStartMonitoring
    )
}