package com.example.mtgoeventsalert.domain.repository

import com.example.mtgoeventsalert.domain.model.Tournament
import com.example.mtgoeventsalert.domain.model.TournamentStatus
import kotlinx.coroutines.flow.Flow

interface ITournamentRepository {
    suspend fun getTournaments(username: String): Flow<List<Tournament>>
    suspend fun getTournamentStatus(tournamentId: String): TournamentStatus?
    suspend fun updateTournamentStatus(status: TournamentStatus)
    suspend fun addTournament(tournament: Tournament)
    suspend fun removeTournament(tournamentId: String)
}