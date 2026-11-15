package io.github.ducthinh0304.javainterviewprep.core.engine.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Entity(tableName = "question_stat")
data class QuestionStatEntity(
    @PrimaryKey val questionId: String,
    val timesSeen: Int = 0,
    val timesCorrect: Int = 0,
    val lastSeenAt: Instant? = null,
    val box: Int = 0,
    val nextReviewAt: Instant? = null
)

@Dao
interface QuestionStatDao {
    @Query("SELECT * FROM question_stat WHERE questionId = :questionId")
    suspend fun get(questionId: String): QuestionStatEntity?

    @Query("SELECT * FROM question_stat WHERE questionId = :questionId")
    fun observe(questionId: String): Flow<QuestionStatEntity?>

    @Query("SELECT * FROM question_stat WHERE nextReviewAt IS NOT NULL AND nextReviewAt <= :now ORDER BY nextReviewAt ASC")
    fun observeDue(now: Instant): Flow<List<QuestionStatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: QuestionStatEntity)
}
