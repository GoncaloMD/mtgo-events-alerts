package com.example.mtgoeventsalert.di

import com.example.mtgoeventsalert.domain.repository.ITournamentRepository
import com.example.mtgoeventsalert.domain.repository.IPlayerRepository
import com.example.mtgoeventsalert.data.repository.TournamentRepository
import com.example.mtgoeventsalert.data.repository.PlayerRepository
import com.example.mtgoeventsalert.data.remote.sources.ITournamentDataSource
import com.example.mtgoeventsalert.data.local.database.dao.TournamentDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindTournamentRepository(
        tournamentRepository: TournamentRepository
    ): ITournamentRepository
    
    @Binds  
    @Singleton
    abstract fun bindPlayerRepository(
        playerRepository: PlayerRepository
    ): IPlayerRepository
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryImplModule {
    
    @Provides
    @Singleton
    fun provideTournamentRepository(
        @Named("primary") primaryDataSource: ITournamentDataSource,
        @Named("fallback") fallbackDataSource: ITournamentDataSource,
        tournamentDao: TournamentDao
    ): TournamentRepository {
        return TournamentRepository(primaryDataSource, fallbackDataSource, tournamentDao)
    }
}