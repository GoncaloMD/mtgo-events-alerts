package com.example.mtgoeventsalert.data.local.database.dao

import androidx.room.*
import com.example.mtgoeventsalert.data.local.database.entities.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    
    @Query("SELECT * FROM players WHERE isActive = 1")
    fun getAllActivePlayers(): Flow<List<PlayerEntity>>
    
    @Query("SELECT * FROM players WHERE username = :username")
    suspend fun getPlayer(username: String): PlayerEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity)
    
    @Update
    suspend fun updatePlayer(player: PlayerEntity)
    
    @Delete
    suspend fun deletePlayer(player: PlayerEntity)
    
    @Query("DELETE FROM players WHERE username = :username")
    suspend fun deletePlayerByUsername(username: String)
    
    @Query("UPDATE players SET isActive = :isActive WHERE username = :username")
    suspend fun updatePlayerActiveStatus(username: String, isActive: Boolean)
}