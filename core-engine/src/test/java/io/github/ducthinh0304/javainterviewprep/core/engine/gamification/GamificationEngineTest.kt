package io.github.ducthinh0304.javainterviewprep.core.engine.gamification

import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import io.github.ducthinh0304.javainterviewprep.core.model.UserStats
import org.junit.Assert.assertEquals
import org.junit.Test

class GamificationEngineTest {

    private val engine = GamificationEngine()

    @Test
    fun streak_incrementsOnConsecutiveDays() {
        val today = 20000L
        val yesterday = 19999L

        val (s1, h1) = engine.calculateStreak(
            currentStreak = 3,
            highestStreak = 5,
            lastActiveEpochDay = yesterday,
            todayEpochDay = today
        )
        assertEquals(4, s1)
        assertEquals(5, h1)

        // Same day does not increment
        val (sSame, _) = engine.calculateStreak(
            currentStreak = 4,
            highestStreak = 5,
            lastActiveEpochDay = today,
            todayEpochDay = today
        )
        assertEquals(4, sSame)

        // Missed day resets to 1
        val (sBroken, _) = engine.calculateStreak(
            currentStreak = 4,
            highestStreak = 5,
            lastActiveEpochDay = 19990L,
            todayEpochDay = today
        )
        assertEquals(1, sBroken)
    }

    @Test
    fun applyAnswerResult_awardsXpAndAdvancesCombo() {
        val initial = UserStats(xp = 0, consecutiveCorrectAnswers = 2)
        val (updated, earnedXp) = engine.applyAnswerResult(initial, Difficulty.MEDIUM, isCorrect = true)

        assertEquals(30, earnedXp) // 3rd in a row reaches x1.5
        assertEquals(30, updated.xp)
        assertEquals(3, updated.consecutiveCorrectAnswers)
        assertEquals(1.5, updated.comboMultiplier, 0.001)
        assertEquals(1, updated.totalExercisesAnswered)
        assertEquals(1, updated.totalCorrectAnswers)
    }

    @Test
    fun applyAnswerResult_wrongAnswerResetsComboAndEarnsNothing() {
        val initial = UserStats(xp = 50, consecutiveCorrectAnswers = 7, comboMultiplier = 2.0)
        val (updated, earnedXp) = engine.applyAnswerResult(initial, Difficulty.HARD, isCorrect = false, isDaily = true)

        assertEquals(0, earnedXp)
        assertEquals(50, updated.xp)
        assertEquals(0, updated.consecutiveCorrectAnswers)
        assertEquals(1.0, updated.comboMultiplier, 0.001)
        assertEquals(0, updated.totalCorrectAnswers)
    }
}
