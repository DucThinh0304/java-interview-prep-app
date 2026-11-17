package io.github.ducthinh0304.javainterviewprep.core.engine.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.ducthinh0304.javainterviewprep.core.engine.db.DailyActivityDao
import io.github.ducthinh0304.javainterviewprep.core.engine.db.DailyAnswerDao
import io.github.ducthinh0304.javainterviewprep.core.engine.db.EngineDatabase
import io.github.ducthinh0304.javainterviewprep.core.engine.db.NoteProgressDao
import io.github.ducthinh0304.javainterviewprep.core.engine.db.QuestionStatDao
import io.github.ducthinh0304.javainterviewprep.core.engine.db.UserStatsDao
import io.github.ducthinh0304.javainterviewprep.core.engine.gamification.LevelLadder
import io.github.ducthinh0304.javainterviewprep.core.engine.theme.JavaThemePack
import io.github.ducthinh0304.javainterviewprep.core.engine.theme.ThemePack
import kotlinx.serialization.json.Json
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideEngineDatabase(@ApplicationContext context: Context): EngineDatabase {
        return Room.databaseBuilder(
            context,
            EngineDatabase::class.java,
            "engine_database.db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true) // ponytail: pre-release, write real Migrations once users exist
            .build()
    }

    @Provides
    fun provideQuestionStatDao(database: EngineDatabase): QuestionStatDao {
        return database.questionStatDao()
    }

    @Provides
    fun provideNoteProgressDao(database: EngineDatabase): NoteProgressDao {
        return database.noteProgressDao()
    }

    @Provides
    fun provideDailyAnswerDao(database: EngineDatabase): DailyAnswerDao {
        return database.dailyAnswerDao()
    }

    @Provides
    fun provideDailyActivityDao(database: EngineDatabase): DailyActivityDao {
        return database.dailyActivityDao()
    }

    @Provides
    fun provideUserStatsDao(database: EngineDatabase): UserStatsDao {
        return database.userStatsDao()
    }

    @Provides
    @Singleton
    fun provideDefaultThemePack(): ThemePack = JavaThemePack

    @Provides
    fun provideLevelLadder(): LevelLadder = LevelLadder.DEFAULT
}
