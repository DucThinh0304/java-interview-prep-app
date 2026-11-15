package io.github.ducthinh0304.javainterviewprep.core.engine.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class QuestionStatDaoTest {

    private lateinit var database: EngineDatabase
    private lateinit var dao: QuestionStatDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EngineDatabase::class.java
        ).build()
        dao = database.questionStatDao()
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
        val entity = QuestionStatEntity(
            questionId = "q1",
            timesSeen = 3,
            timesCorrect = 2,
            lastSeenAt = Instant.ofEpochSecond(1_000),
            box = 2,
            nextReviewAt = Instant.ofEpochSecond(2_000)
        )
        dao.upsert(entity)

        assertEquals(entity, dao.get("q1"))
    }

    @Test
    fun upsert_replacesExistingRow() = runTest {
        dao.upsert(QuestionStatEntity(questionId = "q1", timesSeen = 1))
        dao.upsert(QuestionStatEntity(questionId = "q1", timesSeen = 5))

        assertEquals(5, dao.get("q1")?.timesSeen)
    }

    @Test
    fun observeDue_onlyReturnsRowsAtOrBeforeNow() = runTest {
        val now = Instant.ofEpochSecond(10_000)
        dao.upsert(QuestionStatEntity(questionId = "due", nextReviewAt = now.minusSeconds(1)))
        dao.upsert(QuestionStatEntity(questionId = "exactlyNow", nextReviewAt = now))
        dao.upsert(QuestionStatEntity(questionId = "future", nextReviewAt = now.plusSeconds(1)))
        dao.upsert(QuestionStatEntity(questionId = "neverSeen", nextReviewAt = null))

        val due = dao.observeDue(now).first()

        assertEquals(setOf("due", "exactlyNow"), due.map { it.questionId }.toSet())
        assertTrue(due.none { it.questionId == "future" || it.questionId == "neverSeen" })
    }
}
