package io.github.ducthinh0304.javainterviewprep.core.engine.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Entity(tableName = "daily_answer")
data class DailyAnswerEntity(
    @PrimaryKey val date: LocalDate,
    val questionId: String,
    val selectedOptionIndex: Int,
    val wasCorrect: Boolean,
    val xpEarned: Int
)

@Dao
interface DailyAnswerDao {
    @Query("SELECT * FROM daily_answer WHERE date = :date")
    suspend fun get(date: LocalDate): DailyAnswerEntity?

    @Query("SELECT * FROM daily_answer WHERE date = :date")
    fun observe(date: LocalDate): Flow<DailyAnswerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyAnswerEntity)
}
