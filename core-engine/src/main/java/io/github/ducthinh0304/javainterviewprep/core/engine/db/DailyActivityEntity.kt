package io.github.ducthinh0304.javainterviewprep.core.engine.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Entity(tableName = "daily_activity")
data class DailyActivityEntity(
    @PrimaryKey val date: LocalDate,
    val xpEarned: Int = 0,
    val questionsAnswered: Int = 0,
    /** A day counts as checked in for the streak once this is >= 1. */
    val quizzesCompleted: Int = 0
)

@Dao
interface DailyActivityDao {
    @Query("SELECT * FROM daily_activity WHERE date = :date")
    suspend fun get(date: LocalDate): DailyActivityEntity?

    @Query("SELECT * FROM daily_activity WHERE date = :date")
    fun observe(date: LocalDate): Flow<DailyActivityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyActivityEntity)
}
