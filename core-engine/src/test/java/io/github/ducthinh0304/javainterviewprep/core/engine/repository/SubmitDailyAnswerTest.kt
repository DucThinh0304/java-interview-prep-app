package io.github.ducthinh0304.javainterviewprep.core.engine.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.ducthinh0304.javainterviewprep.core.engine.db.EngineDatabase
import io.github.ducthinh0304.javainterviewprep.core.engine.gamification.GamificationEngine
import io.github.ducthinh0304.javainterviewprep.core.engine.testutil.MutableClock
import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import io.github.ducthinh0304.javainterviewprep.core.model.Question
import io.mockk.coEvery
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SubmitDailyAnswerTest {

    private lateinit var database: EngineDatabase
    private lateinit var clock: MutableClock
    private lateinit var progressRepository: ProgressRepository
    private lateinit var statsRepository: StatsRepository

    private val today = LocalDate.of(2026, 11, 15)
    private val question = Question.SingleChoice(
        id = "q1",
        unitId = "u1",
        difficulty = Difficulty.EASY,
        prompt = "p",
        explanation = "e",
        options = listOf("a", "b"),
        correctIndex = 0
    )

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EngineDatabase::class.java
        ).build()
        clock = MutableClock(Instant.parse("2026-11-15T09:00:00Z"))
        progressRepository = ProgressRepository(
            questionStatDao = database.questionStatDao(),
            noteProgressDao = database.noteProgressDao(),
            dailyAnswerDao = database.dailyAnswerDao(),
            clock = clock
        )
        statsRepository = StatsRepository(
            database = database,
            userStatsDao = database.userStatsDao(),
            dailyActivityDao = database.dailyActivityDao(),
            gamificationEngine = GamificationEngine(),
            clock = clock
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun submit_writesStatXpAndDailyAnswer() = runTest {
        val submit = SubmitDailyAnswer(database, progressRepository, statsRepository, clock)

        val xp = submit(question, selectedOptionIndex = 0, isCorrect = true)

        assertEquals(10, xp)
        assertEquals(1, database.questionStatDao().get("q1")?.timesSeen)
        assertEquals(10, database.userStatsDao().get()?.totalXp)
        val daily = database.dailyAnswerDao().get(today)
        assertEquals(0, daily?.selectedOptionIndex)
        assertEquals(10, daily?.xpEarned)
        assertEquals(1, database.dailyActivityDao().get(today)?.quizzesCompleted)
        assertEquals(1, database.userStatsDao().get()?.currentStreak)
    }

    @Test
    fun submit_rollsBackEverythingWhenALaterWriteFails() = runTest {
        val failingProgress = spyk(progressRepository)
        coEvery { failingProgress.recordDailyAnswer(any(), any(), any(), any(), any()) } throws IllegalStateException("boom")
        val submit = SubmitDailyAnswer(database, failingProgress, statsRepository, clock)

        try {
            submit(question, selectedOptionIndex = 0, isCorrect = true)
            fail("expected failure")
        } catch (_: IllegalStateException) {
        }

        assertNull(database.questionStatDao().get("q1"))
        assertNull(database.userStatsDao().get())
        assertNull(database.dailyActivityDao().get(today))
        assertNull(database.dailyAnswerDao().get(today))
    }
}
