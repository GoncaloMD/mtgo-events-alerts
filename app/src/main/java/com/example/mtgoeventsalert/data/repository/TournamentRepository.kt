package com.example.mtgoeventsalert.data.repository

import com.example.mtgoeventsalert.domain.model.Tournament
import com.example.mtgoeventsalert.domain.model.TournamentStatus
import com.example.mtgoeventsalert.domain.repository.ITournamentRepository
import com.example.mtgoeventsalert.data.remote.sources.ITournamentDataSource
import com.example.mtgoeventsalert.data.local.database.dao.TournamentDao
import com.example.mtgoeventsalert.data.local.mappers.*
import com.example.mtgoeventsalert.util.MTGOEventPatterns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TournamentRepository @Inject constructor(
    @Named("primary") private val primaryDataSource: ITournamentDataSource,
    @Named("fallback") private val fallbackDataSource: ITournamentDataSource?,
    private val tournamentDao: TournamentDao
) : ITournamentRepository {

    override suspend fun getTournaments(username: String): Flow<List<Tournament>> = flow {
        try {
            // Try to get fresh data from remote source
            val result = if (primaryDataSource.isAvailable()) {
                primaryDataSource.getTournamentData(username)
            } else if (fallbackDataSource?.isAvailable() == true) {
                fallbackDataSource.getTournamentData(username)
            } else {
                throw Exception("No data sources available")
            }

            if (result.success) {
                // Update database with fresh data
                result.tournaments.forEach { status ->
                    val tournament = Tournament(
                        id = status.tournamentId,
                        name = extractTournamentName(status.tournamentId),
                        format = extractFormat(status.tournamentId),
                        playerUsername = username,
                        status = status,
                        isActive = !status.hasEnded
                    )
                    
                    // Save tournament and status to database
                    tournamentDao.insertTournament(tournament.toEntity())
                    tournamentDao.insertTournamentStatus(status.toEntity())
                }
                
                // Convert to domain models and emit
                val tournaments = result.tournaments.map { status ->
                    Tournament(
                        id = status.tournamentId,
                        name = extractTournamentName(status.tournamentId),
                        format = extractFormat(status.tournamentId),
                        playerUsername = username,
                        status = status,
                        isActive = !status.hasEnded
                    )
                }
                emit(tournaments)
            } else {
                // Fall back to cached data
                emit(getCachedTournaments(username))
            }
        } catch (e: Exception) {
            // Return cached data on error
            emit(getCachedTournaments(username))
        }
    }
    override suspend fun getTournamentStatus(tournamentId: String): TournamentStatus? {
        return tournamentDao.getTournamentStatus(tournamentId)?.toDomain()
    }

    override suspend fun updateTournamentStatus(status: TournamentStatus) {
        tournamentDao.updateTournamentStatus(status.toEntity())
    }

    override suspend fun addTournament(tournament: Tournament) {
        tournamentDao.insertTournament(tournament.toEntity())
        tournamentDao.insertTournamentStatus(tournament.status.toEntity())
    }

    override suspend fun removeTournament(tournamentId: String) {
        tournamentDao.deleteTournamentById(tournamentId)
        tournamentDao.deleteTournamentStatus(tournamentId)
    }

    private suspend fun getCachedTournaments(username: String): List<Tournament> {
        return try {
            val tournamentEntities = tournamentDao.getActiveTournaments(username)
            // Convert Flow to a single emission for fallback case
            tournamentEntities.first().mapNotNull { tournamentEntity ->
                val status = tournamentDao.getTournamentStatus(tournamentEntity.id)
                if (status != null) {
                    tournamentEntity.toDomain(status.toDomain())
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun extractTournamentName(tournamentId: String): String {
        return tournamentId.split("_")
            .take(2)
            .joinToString(" ")
            .replaceFirstChar { it.uppercase() }
    }

    private fun extractFormat(tournamentId: String): String {
        return MTGOEventPatterns.detectFormat(tournamentId)
    }
}