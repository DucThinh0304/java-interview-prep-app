package io.github.ducthinh0304.javainterviewprep.core.engine.gamification

import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import kotlin.math.min
import kotlin.math.roundToInt

object XpCalculator {

    const val DAILY_BONUS = 10

    fun baseXp(difficulty: Difficulty): Int = when (difficulty) {
        Difficulty.EASY -> 10
        Difficulty.MEDIUM -> 20
        Difficulty.HARD -> 35
    }

    /** Wrong answers earn nothing, daily bonus included. The bonus is not multiplied. */
    fun earned(difficulty: Difficulty, isCorrect: Boolean, multiplier: Double, isDaily: Boolean = false): Int {
        if (!isCorrect) return 0
        return (baseXp(difficulty) * multiplier).roundToInt() + if (isDaily) DAILY_BONUS else 0
    }

    /** Paid once per day for checking in, whatever the score: 10 on day 1, +5 per streak day, capped at 50. */
    fun checkInBonus(streak: Int): Int {
        require(streak >= 1) { "A check-in always has a streak of at least 1" }
        return min(10 + 5 * (streak - 1), 50)
    }
}
