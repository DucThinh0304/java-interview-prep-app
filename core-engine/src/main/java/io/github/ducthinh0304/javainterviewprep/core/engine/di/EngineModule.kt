package io.github.ducthinh0304.javainterviewprep.core.engine.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.ducthinh0304.javainterviewprep.core.engine.db.EngineDatabase
import io.github.ducthinh0304.javainterviewprep.core.engine.db.ExerciseProgressDao
import io.github.ducthinh0304.javainterviewprep.core.engine.db.UserStatsDao
import io.github.ducthinh0304.javainterviewprep.core.engine.theme.JavaThemePack
import io.github.ducthinh0304.javainterviewprep.core.engine.theme.ThemePack
import kotlinx.serialization.json.Json
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
    fun provideEngineDatabase(@ApplicationContext context: Context): EngineDatabase {
        return Room.databaseBuilder(
            context,
            EngineDatabase::class.java,
            "engine_database.db"
        ).build()
    }

    @Provides
    fun provideExerciseProgressDao(database: EngineDatabase): ExerciseProgressDao {
        return database.exerciseProgressDao()
    }

    @Provides
    fun provideUserStatsDao(database: EngineDatabase): UserStatsDao {
        return database.userStatsDao()
    }

    @Provides
    @Singleton
    fun provideDefaultThemePack(): ThemePack = JavaThemePack
}
