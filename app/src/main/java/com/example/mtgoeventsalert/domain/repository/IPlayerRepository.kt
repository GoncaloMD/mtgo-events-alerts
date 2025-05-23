package com.example.mtgoeventsalert.domain.repository

import com.example.mtgoeventsalert.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface IPlayerRepository {
    suspend fun getPlayer(username: String): Player?
    suspend fun savePlayer(player: Player)
    suspend fun getAllPlayers(): Flow<List<Player>>
    suspend fun deletePlayer(username: String)
}