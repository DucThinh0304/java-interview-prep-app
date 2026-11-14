package io.github.ducthinh0304.javainterviewprep.core.engine.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "exercise_progress")
data class ExerciseProgressEntity(
    @PrimaryKey val exerciseId: String,
    val timesSeen: Int = 0,
    val timesCorrect: Int = 0,
    val lastAttemptEpochDay: Long = 0L,
    val inReviewQueue: Boolean = false,
    val nextReviewEpochDay: Long = 0L,
    val box: Int = 1,
    val intervalDays: Int = 1,
    val easeFactor: Double = 2.5,
    val repetitions: Int = 0,
    val lapses: Int = 0
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val xp: Int = 0,
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,
    val comboMultiplier: Double = 1.0,
    val consecutiveCorrectAnswers: Int = 0,
    val lastActiveEpochDay: Long? = null,
    val totalExercisesAnswered: Int = 0,
    val totalCorrectAnswers: Int = 0
)

@Dao
interface ExerciseProgressDao {
    @Query("SELECT * FROM exercise_progress WHERE exerciseId = :exerciseId")
    suspend fun getProgress(exerciseId: String): ExerciseProgressEntity?

    @Query("SELECT * FROM exercise_progress WHERE inReviewQueue = 1 AND nextReviewEpochDay <= :todayEpochDay ORDER BY nextReviewEpochDay ASC")
    fun getDueReviewItems(todayEpochDay: Long): Flow<List<ExerciseProgressEntity>>

    @Query("SELECT * FROM exercise_progress WHERE inReviewQueue = 1")
    fun getAllReviewItems(): Flow<List<ExerciseProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ExerciseProgressEntity)
}

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getStatsFlow(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getStats(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: UserStatsEntity)
}

@Database(
    entities = [ExerciseProgressEntity::class, UserStatsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EngineDatabase : RoomDatabase() {
    abstract fun exerciseProgressDao(): ExerciseProgressDao
    abstract fun userStatsDao(): UserStatsDao
}
