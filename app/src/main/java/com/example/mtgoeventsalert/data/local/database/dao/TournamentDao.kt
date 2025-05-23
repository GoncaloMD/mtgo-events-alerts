package com.example.mtgoeventsalert.data.local.database.dao

import androidx.room.*
import com.example.mtgoeventsalert.data.local.database.entities.TournamentEntity
import com.example.mtgoeventsalert.data.local.database.entities.TournamentStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {
    
    @Query("SELECT * FROM tournaments WHERE playerUsername = :username AND isActive = 1")
    fun getActiveTournaments(username: String): Flow<List<TournamentEntity>>
    
    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    suspend fun getTournament(tournamentId: String): TournamentEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity)
    
    @Update
    suspend fun updateTournament(tournament: TournamentEntity)
    
    @Delete
    suspend fun deleteTournament(tournament: TournamentEntity)
    
    @Query("DELETE FROM tournaments WHERE id = :tournamentId")
    suspend fun deleteTournamentById(tournamentId: String)
    
    @Query("UPDATE tournaments SET isActive = 0 WHERE id = :tournamentId")
    suspend fun deactivateTournament(tournamentId: String)
    
    // Tournament Status methods
    @Query("SELECT * FROM tournament_status WHERE tournamentId = :tournamentId")
    suspend fun getTournamentStatus(tournamentId: String): TournamentStatusEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournamentStatus(status: TournamentStatusEntity)
    
    @Update
    suspend fun updateTournamentStatus(status: TournamentStatusEntity)
    
    @Query("DELETE FROM tournament_status WHERE tournamentId = :tournamentId")
    suspend fun deleteTournamentStatus(tournamentId: String)
    
    // Combined queries
    @Query("""
        SELECT t.*, ts.* FROM tournaments t 
        LEFT JOIN tournament_status ts ON t.id = ts.tournamentId 
        WHERE t.playerUsername = :username AND t.isActive = 1
    """)
    fun getTournamentsWithStatus(username: String): Flow<Map<TournamentEntity, TournamentStatusEntity?>>
}