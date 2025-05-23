package com.example.mtgoeventsalert.di

import android.content.Context
import androidx.room.Room
import com.example.mtgoeventsalert.data.local.database.AppDatabase
import com.example.mtgoeventsalert.data.local.database.dao.PlayerDao
import com.example.mtgoeventsalert.data.local.database.dao.TournamentDao
import com.example.mtgoeventsalert.data.local.database.dao.AppSettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration() // Remove in production
        .build()
    }
    
    @Provides
    fun providePlayerDao(database: AppDatabase): PlayerDao {
        return database.playerDao()
    }
    
    @Provides
    fun provideTournamentDao(database: AppDatabase): TournamentDao {
        return database.tournamentDao()
    }
    
    @Provides
    fun provideAppSettingsDao(database: AppDatabase): AppSettingsDao {
        return database.appSettingsDao()
    }
}