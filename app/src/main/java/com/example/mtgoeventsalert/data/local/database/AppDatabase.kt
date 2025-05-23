package com.example.mtgoeventsalert.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.mtgoeventsalert.data.local.database.entities.PlayerEntity
import com.example.mtgoeventsalert.data.local.database.entities.TournamentEntity
import com.example.mtgoeventsalert.data.local.database.entities.TournamentStatusEntity
import com.example.mtgoeventsalert.data.local.database.entities.AppSettingsEntity
import com.example.mtgoeventsalert.data.local.database.dao.PlayerDao
import com.example.mtgoeventsalert.data.local.database.dao.TournamentDao
import com.example.mtgoeventsalert.data.local.database.dao.AppSettingsDao

@Database(
    entities = [
        PlayerEntity::class,
        TournamentEntity::class,
        TournamentStatusEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun playerDao(): PlayerDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun appSettingsDao(): AppSettingsDao
    
    companion object {
        const val DATABASE_NAME = "mtgo_events_db"
    }
}