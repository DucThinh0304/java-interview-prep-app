package io.github.ducthinh0304.javainterviewprep.core.engine.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.ducthinh0304.javainterviewprep.core.engine.db.EngineDatabase
import io.github.ducthinh0304.javainterviewprep.core.engine.testutil.MutableClock
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
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProgressRepositoryTest {

    private lateinit var database: EngineDatabase
    private lateinit var clock: MutableClock
    private lateinit var repository: ProgressRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EngineDatabase::class.java
        ).build()
        clock = MutableClock(Instant.parse("2026-11-15T00:00:00Z"))
        repository = ProgressRepository(
            questionStatDao = database.questionStatDao(),
            noteProgressDao = database.noteProgressDao(),
            dailyAnswerDao = database.dailyAnswerDao(),
            clock = clock
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun recordQuestionAttempt_correctAnswerAdvancesBox() = runTest {
        val first = repository.recordQuestionAttempt("q1", isCorrect = true)
        assertEquals(1, first.box)
        assertEquals(1, first.timesSeen)
        assertEquals(1, first.timesCorrect)
        assertEquals(Instant.parse("2026-11-16T00:00:00Z"), first.nextReviewAt)

        val second = repository.recordQuestionAttempt("q1", isCorrect = true)
        assertEquals(2, second.box)
        assertEquals(2, second.timesSeen)
        assertEquals(Instant.parse("2026-11-18T00:00:00Z"), second.nextReviewAt)
    }

    @Test
    fun recordQuestionAttempt_incorrectAnswerResetsBoxToOne() = runTest {
        repository.recordQuestionAttempt("q1", isCorrect = true)
        repository.recordQuestionAttempt("q1", isCorrect = true)
        val afterMiss = repository.recordQuestionAttempt("q1", isCorrect = false)

        assertEquals(1, afterMiss.box)
        assertEquals(3, afterMiss.timesSeen)
        assertEquals(2, afterMiss.timesCorrect)
        assertEquals(Instant.parse("2026-11-16T00:00:00Z"), afterMiss.nextReviewAt)
    }

    @Test
    fun dueQuestions_surfacesOnlyQuestionsAtOrPastNextReview() = runTest {
        repository.recordQuestionAttempt("dueSoon", isCorrect = false)

        val notYetDue = repository.dueQuestions(clock.instant()).first()
        assertTrue(notYetDue.none { it.questionId == "dueSoon" })

        clock.advanceByDays(1)
        val due = repository.dueQuestions(clock.instant()).first()
        assertTrue(due.any { it.questionId == "dueSoon" })
    }

    @Test
    fun recordNoteRead_thenRecordNoteQuizResult_preservesReadAt() = runTest {
        val read = repository.recordNoteRead("note1")
        assertEquals(false, read.quizPassed)

        val passed = repository.recordNoteQuizResult("note1", passed = true)
        assertEquals(true, passed.quizPassed)
        assertEquals(read.readAt, passed.readAt)
    }

    @Test
    fun recordDailyAnswer_isRetrievableByDate() = runTest {
        val date = LocalDate.of(2026, 11, 15)
        repository.recordDailyAnswer(date, "q1", selectedOptionIndex = 2, wasCorrect = true, xpEarned = 20)

        val stored = repository.dailyAnswer(date).first()
        assertEquals("q1", stored?.questionId)
        assertEquals(20, stored?.xpEarned)
        assertEquals(2, stored?.selectedOptionIndex)
    }

    @Test
    fun questionStat_returnsNullForUnseenQuestion() = runTest {
        assertNull(repository.questionStat("never-seen").first())
    }
}
