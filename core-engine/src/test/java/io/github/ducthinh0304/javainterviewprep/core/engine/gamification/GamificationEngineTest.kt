package io.github.ducthinh0304.javainterviewprep.core.engine.gamification

import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import io.github.ducthinh0304.javainterviewprep.core.model.UserStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GamificationEngineTest {

    private val engine = GamificationEngine()

    @Test
    fun baseXp_scalesWithDifficulty() {
        assertEquals(10, engine.calculateBaseXp(Difficulty.JUNIOR))
        assertEquals(20, engine.calculateBaseXp(Difficulty.MID))
        assertEquals(35, engine.calculateBaseXp(Difficulty.SENIOR))
    }

    @Test
    fun combo_increasesOnConsecutiveCorrectAnswers() {
        val (c1, m1) = engine.calculateNextCombo(currentConsecutive = 0, isCorrect = true)
        assertEquals(1, c1)
        assertEquals(1.1, m1, 0.001)

        val (c2, m2) = engine.calculateNextCombo(currentConsecutive = 1, isCorrect = true)
        assertEquals(2, c2)
        assertEquals(1.2, m2, 0.001)

        // Resets on incorrect answer
        val (cReset, mReset) = engine.calculateNextCombo(currentConsecutive = 5, isCorrect = false)
        assertEquals(0, cReset)
        assertEquals(1.0, mReset, 0.001)
    }

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
    fun applyAnswerResult_awardsCorrectXpAndUpdatesStats() {
        val initial = UserStats(xp = 0, consecutiveCorrectAnswers = 0, comboMultiplier = 1.0)
        val (updated, earnedXp) = engine.applyAnswerResult(
            currentStats = initial,
            difficulty = Difficulty.MID,
            isCorrect = true,
            todayEpochDay = 20000L
        )

        assertEquals(20, earnedXp)
        assertEquals(20, updated.xp)
        assertEquals(1, updated.consecutiveCorrectAnswers)
        assertEquals(1.1, updated.comboMultiplier, 0.001)
        assertEquals(1, updated.totalExercisesAnswered)
        assertEquals(1, updated.totalCorrectAnswers)
    }
}
