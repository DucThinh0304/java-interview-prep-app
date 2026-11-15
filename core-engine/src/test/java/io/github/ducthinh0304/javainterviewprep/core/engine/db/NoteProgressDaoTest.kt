package io.github.ducthinh0304.javainterviewprep.core.engine.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NoteProgressDaoTest {

    private lateinit var database: EngineDatabase
    private lateinit var dao: NoteProgressDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EngineDatabase::class.java
        ).build()
        dao = database.noteProgressDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun get_returnsNullWhenMissing() = runTest {
        assertNull(dao.get("unknown"))
    }

    @Test
    fun upsertThenGet_returnsStoredEntity() = runTest {
        val entity = NoteProgressEntity(
            noteId = "note1",
            readAt = Instant.ofEpochSecond(1_000),
            quizPassed = true
        )
        dao.upsert(entity)

        assertEquals(entity, dao.get("note1"))
    }

    @Test
    fun upsert_canMarkQuizPassedAfterInitialRead() = runTest {
        dao.upsert(NoteProgressEntity(noteId = "note1", readAt = Instant.ofEpochSecond(1_000)))
        val existing = dao.get("note1")!!
        dao.upsert(existing.copy(quizPassed = true))

        val updated = dao.get("note1")
        assertEquals(true, updated?.quizPassed)
        assertEquals(Instant.ofEpochSecond(1_000), updated?.readAt)
    }
}
