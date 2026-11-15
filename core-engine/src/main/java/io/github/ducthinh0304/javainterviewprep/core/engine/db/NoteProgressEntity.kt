package io.github.ducthinh0304.javainterviewprep.core.engine.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Entity(tableName = "note_progress")
data class NoteProgressEntity(
    @PrimaryKey val noteId: String,
    val readAt: Instant? = null,
    val quizPassed: Boolean = false
)

@Dao
interface NoteProgressDao {
    @Query("SELECT * FROM note_progress WHERE noteId = :noteId")
    suspend fun get(noteId: String): NoteProgressEntity?

    @Query("SELECT * FROM note_progress WHERE noteId = :noteId")
    fun observe(noteId: String): Flow<NoteProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NoteProgressEntity)
}
