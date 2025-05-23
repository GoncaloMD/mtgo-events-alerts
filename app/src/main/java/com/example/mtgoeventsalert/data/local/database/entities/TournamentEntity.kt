package com.example.mtgoeventsalert.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "tournaments",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["username"],
            childColumns = ["playerUsername"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playerUsername"])]
)
data class TournamentEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val format: String,
    val playerUsername: String,
    val isActive: Boolean,
    val createdAt: Long,
    val lastUpdated: Long
)