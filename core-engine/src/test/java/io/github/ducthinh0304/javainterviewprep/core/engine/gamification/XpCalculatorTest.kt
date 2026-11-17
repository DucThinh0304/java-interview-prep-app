package io.github.ducthinh0304.javainterviewprep.core.engine.gamification

import io.github.ducthinh0304.javainterviewprep.core.engine.gamification.XpCalculator.DAILY_BONUS
import io.github.ducthinh0304.javainterviewprep.core.engine.gamification.XpCalculator.earned
import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Test

class XpCalculatorTest {

    @Test
    fun baseScalesWithDifficulty() {
        assertEquals(10, earned(Difficulty.EASY, true, 1.0))
        assertEquals(20, earned(Difficulty.MEDIUM, true, 1.0))
        assertEquals(35, earned(Difficulty.HARD, true, 1.0))
    }

    @Test
    fun multiplierAppliesAndRounds() {
        assertEquals(15, earned(Difficulty.EASY, true, 1.5))
        assertEquals(53, earned(Difficulty.HARD, true, 1.5)) // 52.5 rounds half up
        assertEquals(105, earned(Difficulty.HARD, true, 3.0))
    }

    @Test
    fun dailyBonusIsAddedAfterTheMultiplier() {
        assertEquals(10 + DAILY_BONUS, earned(Difficulty.EASY, true, 1.0, isDaily = true))
        assertEquals(40 + DAILY_BONUS, earned(Difficulty.MEDIUM, true, 2.0, isDaily = true))
    }

    @Test
    fun checkInBonus_growsWithStreakAndCapsAt50() {
        assertEquals(10, XpCalculator.checkInBonus(1))
        assertEquals(15, XpCalculator.checkInBonus(2))
        assertEquals(40, XpCalculator.checkInBonus(7))
        assertEquals(50, XpCalculator.checkInBonus(9))
        assertEquals(50, XpCalculator.checkInBonus(365))
    }

    @Test(expected = IllegalArgumentException::class)
    fun checkInBonus_rejectsZeroStreak() {
        XpCalculator.checkInBonus(0)
    }

    @Test
    fun wrongAnswerEarnsNothing() {
        assertEquals(0, earned(Difficulty.HARD, false, 3.0, isDaily = true))
    }
}
