package com.example.mtgoeventsalert.di

import android.content.Context
import com.example.mtgoeventsalert.data.remote.sources.ITournamentDataSource
import com.example.mtgoeventsalert.data.remote.sources.WebScrapingDataSource
import com.example.mtgoeventsalert.data.remote.sources.ApiDataSource
import com.example.mtgoeventsalert.data.remote.sources.MockDataSource
import com.example.mtgoeventsalert.data.local.preferences.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    @Named("primary")
    fun providePrimaryDataSource(
        webScrapingDataSource: WebScrapingDataSource
    ): ITournamentDataSource {
        return webScrapingDataSource
    }
    
    @Provides
    @Singleton
    @Named("fallback")
    fun provideFallbackDataSource(
        mockDataSource: MockDataSource
    ): ITournamentDataSource {
        return mockDataSource
    }
    
    @Provides
    @Singleton
    @Named("api")
    fun provideApiDataSource(
        apiDataSource: ApiDataSource
    ): ITournamentDataSource {
        return apiDataSource
    }
    
    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }
}