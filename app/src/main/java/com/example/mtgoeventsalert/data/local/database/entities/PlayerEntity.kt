package com.example.mtgoeventsalert.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey
    val username: String,
    val isActive: Boolean,
    val createdAt: Long,
    val lastUpdated: Long
)