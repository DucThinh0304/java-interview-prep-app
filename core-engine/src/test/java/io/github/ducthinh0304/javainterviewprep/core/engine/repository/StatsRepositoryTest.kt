package io.github.ducthinh0304.javainterviewprep.core.engine.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.ducthinh0304.javainterviewprep.core.engine.db.EngineDatabase
import io.github.ducthinh0304.javainterviewprep.core.engine.gamification.GamificationEngine
import io.github.ducthinh0304.javainterviewprep.core.engine.testutil.MutableClock
import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class StatsRepositoryTest {

    private lateinit var database: EngineDatabase
    private lateinit var clock: MutableClock
    private lateinit var repository: StatsRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EngineDatabase::class.java
        ).build()
        clock = MutableClock(Instant.parse("2026-11-15T00:00:00Z"))
        repository = StatsRepository(
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
    fun streak_incrementsOnConsecutiveDays() = runTest {
        val day1 = repository.recordAnswer(Difficulty.EASY, isCorrect = true).first
        assertEquals(1, day1.currentStreak)

        clock.advanceByDays(1)
        val day2 = repository.recordAnswer(Difficulty.EASY, isCorrect = true).first
        assertEquals(2, day2.currentStreak)

        clock.advanceByDays(1)
        val day3 = repository.recordAnswer(Difficulty.EASY, isCorrect = true).first
        assertEquals(3, day3.currentStreak)
        assertEquals(3, day3.highestStreak)
    }

    @Test
    fun streak_resetsAfterAGapDay() = runTest {
        repository.recordAnswer(Difficulty.EASY, isCorrect = true)
        clock.advanceByDays(1)
        repository.recordAnswer(Difficulty.EASY, isCorrect = true)
        clock.advanceByDays(1)
        val beforeGap = repository.recordAnswer(Difficulty.EASY, isCorrect = true).first
        assertEquals(3, beforeGap.currentStreak)

        clock.advanceByDays(2) // skip a day entirely
        val afterGap = repository.recordAnswer(Difficulty.EASY, isCorrect = true).first

        assertEquals(1, afterGap.currentStreak)
        assertEquals(3, afterGap.highestStreak) // longest streak is preserved
    }

    @Test
    fun streak_doesNotDoubleIncrementWithinSameDay() = runTest {
        repository.recordAnswer(Difficulty.EASY, isCorrect = true)
        val secondAnswerSameDay = repository.recordAnswer(Difficulty.EASY, isCorrect = false).first

        assertEquals(1, secondAnswerSameDay.currentStreak)
    }

    @Test
    fun recordAnswer_accumulatesTotalXpAndTracksBestCombo() = runTest {
        repository.recordAnswer(Difficulty.EASY, isCorrect = true) // combo x1.0, +10xp
        clock.advanceByDays(1)
        val (stats, _) = repository.recordAnswer(Difficulty.EASY, isCorrect = true) // combo x1.1, +11xp

        assertEquals(21, stats.xp)

        val persisted = database.userStatsDao().get()!!
        assertEquals(21, persisted.totalXp)
        assertEquals(1.2, persisted.bestCombo, 0.0001)
    }

    @Test
    fun recordAnswer_writesDailyActivityForTheDay() = runTest {
        repository.recordAnswer(Difficulty.MEDIUM, isCorrect = true)
        repository.recordAnswer(Difficulty.EASY, isCorrect = false)

        val today = java.time.LocalDate.now(clock)
        val activity = repository.dailyActivity(today).first()

        assertEquals(2, activity?.questionsAnswered)
        assertEquals(20, activity?.xpEarned)
    }

    @Test
    fun userStatsFlow_reflectsPersistedAndSessionState() = runTest {
        repository.recordAnswer(Difficulty.EASY, isCorrect = true)

        val emitted = repository.userStatsFlow.first()
        assertEquals(10, emitted.xp)
        assertEquals(1, emitted.currentStreak)
        assertEquals(1, emitted.consecutiveCorrectAnswers)
    }
}
