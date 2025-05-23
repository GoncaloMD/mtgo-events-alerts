package com.example.mtgoeventsalert.data.repository

// Player repository implementation will go here
    override suspend fun getPlayer(username: String): Player? {
        val playerEntity = playerDao.getPlayer(username) ?: return null
        val tournaments = tournamentDao.getActiveTournaments(username)
            .map { tournamentList -> tournamentList.map { it.id } }
        
        // Note: This is a simplified approach. In a real app, you'd want to handle this better
        return playerEntity.toDomain(emptyList()) // We'll get tournaments separately
    }

    override suspend fun savePlayer(player: Player) {
        playerDao.insertPlayer(player.toEntity())
    }

    override suspend fun getAllPlayers(): Flow<List<Player>> {
        return playerDao.getAllActivePlayers().map { entities ->
            entities.map { entity ->
                entity.toDomain()
            }
        }
    }

    override suspend fun deletePlayer(username: String) {
        playerDao.deletePlayerByUsername(username)
    }
}