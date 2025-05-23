package com.example.mtgoeventsalert.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "tournament_status",
    foreignKeys = [
        ForeignKey(
            entity = TournamentEntity::class,
            parentColumns = ["id"],
            childColumns = ["tournamentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tournamentId"])]
)
data class TournamentStatusEntity(
    @PrimaryKey
    val tournamentId: String,
    val record: String,
    val currentStatus: String,
    val roundNumber: Int?,
    val lastUpdated: Long,
    val isWaitingForRound: Boolean,
    val hasEnded: Boolean
)