package io.github.ducthinh0304.javainterviewprep.core.engine.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.ducthinh0304.javainterviewprep.core.engine.db.EngineDatabase
import io.github.ducthinh0304.javainterviewprep.core.engine.gamification.GamificationEngine
import io.github.ducthinh0304.javainterviewprep.core.engine.gamification.XpCalculator
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
import java.time.LocalDate

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
    fun severalQuizzesInOneDay_countAsASingleCheckIn() = runTest {
        repeat(3) { repository.recordQuizCompleted() }

        assertEquals(1, database.userStatsDao().get()?.currentStreak)
        assertEquals(3, repository.dailyActivity(LocalDate.now(clock)).first()?.quizzesCompleted)
    }

    @Test
    fun checkInsOnConsecutiveDays_extendTheStreak() = runTest {
        repeat(3) { day ->
            if (day > 0) clock.advanceByDays(1)
            repository.recordQuizCompleted()
            repository.recordQuizCompleted() // extra quizzes never double count
        }

        val stats = database.userStatsDao().get()!!
        assertEquals(3, stats.currentStreak)
        assertEquals(3, stats.longestStreak)
    }

    @Test
    fun checkIn_paysAStreakScaledBonusOncePerDay() = runTest {
        val paid = (1..10).map { day ->
            if (day > 1) clock.advanceByDays(1)
            repository.recordQuizCompleted().also {
                assertEquals(0, repository.recordQuizCompleted()) // second quiz that day pays nothing
            }
        }

        assertEquals(listOf(10, 15, 20, 25, 30, 35, 40, 45, 50, 50), paid)
        assertEquals(paid.sum(), database.userStatsDao().get()?.totalXp)
        assertEquals(50, repository.dailyActivity(LocalDate.now(clock)).first()?.xpEarned)
    }

    @Test
    fun checkIn_bonusDropsBackAfterAMissedDay() = runTest {
        repository.recordQuizCompleted()
        clock.advanceByDays(1)
        assertEquals(15, repository.recordQuizCompleted())
        clock.advanceByDays(2)
        assertEquals(10, repository.recordQuizCompleted())
    }

    @Test
    fun aDayWithoutACheckIn_resetsTheStreak() = runTest {
        repository.recordQuizCompleted()
        clock.advanceByDays(1)
        repository.recordQuizCompleted()

        clock.advanceByDays(1)
        repository.recordAnswer(Difficulty.EASY, isCorrect = true) // answered, but no quiz completed
        clock.advanceByDays(1)
        repository.recordQuizCompleted()

        val stats = database.userStatsDao().get()!!
        assertEquals(1, stats.currentStreak)
        assertEquals(2, stats.longestStreak) // longest streak is preserved
    }

    @Test
    fun shownStreak_staysAliveUntilAFullDayIsMissed() = runTest {
        repository.recordQuizCompleted()
        clock.advanceByDays(1)
        repository.recordQuizCompleted()

        clock.advanceByDays(1) // not checked in yet today: still alive
        assertEquals(2, repository.userStatsFlow.first().currentStreak)

        clock.advanceByDays(1) // yesterday was missed: broken, before any new quiz
        assertEquals(0, repository.userStatsFlow.first().currentStreak)
        assertEquals(2, repository.userStatsFlow.first().highestStreak)
    }

    @Test
    fun answeringWithoutCompletingAQuiz_doesNotCheckIn() = runTest {
        repository.recordAnswer(Difficulty.EASY, isCorrect = true)

        assertEquals(0, database.userStatsDao().get()?.currentStreak)
        assertEquals(0, repository.dailyActivity(LocalDate.now(clock)).first()?.quizzesCompleted)
    }

    @Test
    fun recordAnswer_accumulatesTotalXpAndTracksBestCombo() = runTest {
        repository.recordAnswer(Difficulty.EASY, isCorrect = true) // streak 1, x1 -> 10
        repository.recordAnswer(Difficulty.EASY, isCorrect = true) // streak 2, x1 -> 10
        val (stats, xp) = repository.recordAnswer(Difficulty.EASY, isCorrect = true) // streak 3, x1.5 -> 15

        assertEquals(15, xp)
        assertEquals(35, stats.xp)
        repository.recordAnswer(Difficulty.EASY, isCorrect = false) // resets combo, best is kept

        val persisted = database.userStatsDao().get()!!
        assertEquals(35, persisted.totalXp)
        assertEquals(1.5, persisted.bestCombo, 0.0001)
    }

    @Test
    fun recordAnswer_dailyAddsBonus() = runTest {
        val (_, xp) = repository.recordAnswer(Difficulty.EASY, isCorrect = true, isDaily = true)
        assertEquals(10 + XpCalculator.DAILY_BONUS, xp)
    }

    @Test
    fun recordAnswer_writesDailyActivityForTheDay() = runTest {
        repository.recordAnswer(Difficulty.MEDIUM, isCorrect = true)
        repository.recordAnswer(Difficulty.EASY, isCorrect = false)

        val today = LocalDate.now(clock)
        val activity = repository.dailyActivity(today).first()

        assertEquals(2, activity?.questionsAnswered)
        assertEquals(20, activity?.xpEarned)
    }

    @Test
    fun userStatsFlow_reflectsPersistedAndSessionState() = runTest {
        repository.recordAnswer(Difficulty.EASY, isCorrect = true)

        val emitted = repository.userStatsFlow.first()
        assertEquals(10, emitted.xp)
        assertEquals(1, emitted.consecutiveCorrectAnswers)
    }
}
