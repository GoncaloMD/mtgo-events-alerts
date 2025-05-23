#!/bin/bash

# Android Project Structure Setup Script
# Run this from your project root directory (where app/ folder is located)

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Setting up Android project structure...${NC}"

# Base paths
BASE_PATH="app/src/main/java/com/example/mtgoeventsalert"
TEST_PATH="app/src/test/java/com/example/mtgoeventsalert"
ANDROID_TEST_PATH="app/src/androidTest/java/com/example/mtgoeventsalert"

# Create main source directories
echo -e "${GREEN}Creating main source directories...${NC}"
mkdir -p "$BASE_PATH/di"
mkdir -p "$BASE_PATH/data/local/database/dao"
mkdir -p "$BASE_PATH/data/local/database/entities"
mkdir -p "$BASE_PATH/data/local/preferences"
mkdir -p "$BASE_PATH/data/remote/scraping"
mkdir -p "$BASE_PATH/data/remote/api"
mkdir -p "$BASE_PATH/data/repository"
mkdir -p "$BASE_PATH/domain/model"
mkdir -p "$BASE_PATH/domain/repository"
mkdir -p "$BASE_PATH/domain/usecase"
mkdir -p "$BASE_PATH/presentation/ui/main"
mkdir -p "$BASE_PATH/presentation/ui/tournament"
mkdir -p "$BASE_PATH/presentation/ui/settings"
mkdir -p "$BASE_PATH/presentation/adapter"
mkdir -p "$BASE_PATH/service"
mkdir -p "$BASE_PATH/util"

# Create test directories
echo -e "${GREEN}Creating test directories...${NC}"
mkdir -p "$TEST_PATH/domain/usecase"
mkdir -p "$TEST_PATH/data/repository"
mkdir -p "$TEST_PATH/presentation/viewmodel"
mkdir -p "$ANDROID_TEST_PATH/database"
mkdir -p "$ANDROID_TEST_PATH/ui"

# Create DI module files
echo -e "${GREEN}Creating Dependency Injection files...${NC}"
cat > "$BASE_PATH/di/DatabaseModule.kt" << 'EOF'
package com.example.mtgoeventsalert.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // Database-related dependencies will go here
}
EOF

cat > "$BASE_PATH/di/NetworkModule.kt" << 'EOF'
package com.example.mtgoeventsalert.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // Network-related dependencies will go here
}
EOF

cat > "$BASE_PATH/di/RepositoryModule.kt" << 'EOF'
package com.example.mtgoeventsalert.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    // Repository bindings will go here
}
EOF

# Create Domain Model files
echo -e "${GREEN}Creating Domain Model files...${NC}"
cat > "$BASE_PATH/domain/model/Player.kt" << 'EOF'
package com.example.mtgoeventsalert.domain.model

data class Player(
    val username: String,
    val isActive: Boolean = true,
    val tournaments: List<String> = emptyList() // Tournament IDs player is tracking
)
EOF

cat > "$BASE_PATH/domain/model/Tournament.kt" << 'EOF'
package com.example.mtgoeventsalert.domain.model

data class Tournament(
    val id: String,              // Unique identifier
    val name: String,            // e.g., "Modern Challenge", "Legacy Showcase"
    val format: String,          // e.g., "Modern", "Legacy"
    val playerUsername: String,  // Which player this tournament belongs to
    val status: TournamentStatus,
    val isActive: Boolean = true
)
EOF

cat > "$BASE_PATH/domain/model/TournamentStatus.kt" << 'EOF'
package com.example.mtgoeventsalert.domain.model

data class TournamentStatus(
    val tournamentId: String,
    val record: String,          // e.g., "2-1", "3-0-1"
    val currentStatus: String,   // e.g., "Waiting for round to start"
    val roundNumber: Int? = null,
    val lastUpdated: Long,
    val isWaitingForRound: Boolean = false,
    val hasEnded: Boolean = false
)
EOF

cat > "$BASE_PATH/domain/model/NotificationEvent.kt" << 'EOF'
package com.example.mtgoeventsalert.domain.model

data class NotificationEvent(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType
)

enum class NotificationType {
    ROUND_STARTING,
    TOURNAMENT_ENDED,
    CONNECTION_ERROR
}
EOF

cat > "$BASE_PATH/domain/model/ScrapingResult.kt" << 'EOF'
package com.example.mtgoeventsalert.domain.model

data class MultiTournamentScrapingResult(
    val username: String,
    val tournaments: List<TournamentStatus>,
    val success: Boolean,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val cyclePosition: Int? = null  // Which tournament was being displayed when scraped
)

data class TournamentDetectionEvent(
    val username: String,
    val newTournaments: List<Tournament>,
    val endedTournaments: List<String>, // Tournament IDs that ended
    val timestamp: Long = System.currentTimeMillis()
)
EOF

cat > "$BASE_PATH/domain/model/AppSettings.kt" << 'EOF'
package com.example.mtgoeventsalert.domain.model

data class AppSettings(
    val scrapingIntervalSeconds: Int = 30,
    val enableNotifications: Boolean = true,
    val notificationSoundEnabled: Boolean = true,
    val autoStartMonitoring: Boolean = false
)
EOF

# Create Repository Interface files
echo -e "${GREEN}Creating Repository Interface files...${NC}"
cat > "$BASE_PATH/domain/repository/ITournamentRepository.kt" << 'EOF'
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
EOF

cat > "$BASE_PATH/domain/repository/IPlayerRepository.kt" << 'EOF'
package com.example.mtgoeventsalert.domain.repository

import com.example.mtgoeventsalert.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface IPlayerRepository {
    suspend fun getPlayer(username: String): Player?
    suspend fun savePlayer(player: Player)
    suspend fun getAllPlayers(): Flow<List<Player>>
    suspend fun deletePlayer(username: String)
}
EOF

# Create empty implementation files
echo -e "${GREEN}Creating empty implementation files...${NC}"

# Data layer files
touch "$BASE_PATH/data/local/database/AppDatabase.kt"
touch "$BASE_PATH/data/local/database/dao/PlayerDao.kt"
touch "$BASE_PATH/data/local/database/dao/TournamentDao.kt"
touch "$BASE_PATH/data/local/database/entities/PlayerEntity.kt"
touch "$BASE_PATH/data/local/database/entities/TournamentEntity.kt"
touch "$BASE_PATH/data/local/preferences/PreferencesManager.kt"
touch "$BASE_PATH/data/remote/scraping/WebScraper.kt"
touch "$BASE_PATH/data/remote/scraping/ScrapingService.kt"
touch "$BASE_PATH/data/repository/TournamentRepository.kt"
touch "$BASE_PATH/data/repository/PlayerRepository.kt"

# Use cases
touch "$BASE_PATH/domain/usecase/MonitorTournamentUseCase.kt"
touch "$BASE_PATH/domain/usecase/SendNotificationUseCase.kt"
touch "$BASE_PATH/domain/usecase/ManagePlayerUseCase.kt"

# Presentation layer files
touch "$BASE_PATH/presentation/ui/main/MainFragment.kt"
touch "$BASE_PATH/presentation/ui/main/MainViewModel.kt"
touch "$BASE_PATH/presentation/ui/tournament/TournamentFragment.kt"
touch "$BASE_PATH/presentation/ui/tournament/TournamentViewModel.kt"
touch "$BASE_PATH/presentation/ui/settings/SettingsFragment.kt"
touch "$BASE_PATH/presentation/ui/settings/SettingsViewModel.kt"
touch "$BASE_PATH/presentation/adapter/TournamentAdapter.kt"
touch "$BASE_PATH/presentation/MainActivity.kt"

# Service files
touch "$BASE_PATH/service/MonitoringService.kt"
touch "$BASE_PATH/service/NotificationService.kt"

# Utility files
cat > "$BASE_PATH/util/Constants.kt" << 'EOF'
package com.example.mtgoeventsalert.util

object Constants {
    const val DATABASE_NAME = "mtgo_events_db"
    const val NOTIFICATION_CHANNEL_ID = "mtgo_events_channel"
    const val DEFAULT_SCRAPING_INTERVAL = 30L // seconds
    const val MTGBOT_BASE_URL = "https://mtgbot.tv/overlay/compact.html"
}
EOF

touch "$BASE_PATH/util/Extensions.kt"
touch "$BASE_PATH/util/DateUtils.kt"

# Test files
touch "$TEST_PATH/domain/usecase/MonitorTournamentUseCaseTest.kt"
touch "$TEST_PATH/data/repository/TournamentRepositoryTest.kt"
touch "$TEST_PATH/presentation/viewmodel/MainViewModelTest.kt"
touch "$ANDROID_TEST_PATH/database/AppDatabaseTest.kt"
touch "$ANDROID_TEST_PATH/ui/MainFragmentTest.kt"

echo -e "${GREEN}✅ Project structure created successfully!${NC}"
echo -e "${BLUE}📁 Directory structure:${NC}"
find "$BASE_PATH" -type d | head -20
echo -e "${BLUE}📄 Created files:${NC}"
find "$BASE_PATH" -name "*.kt" | wc -l | xargs echo "Kotlin files created:"
echo -e "${GREEN}🚀 Ready to start development!${NC}"