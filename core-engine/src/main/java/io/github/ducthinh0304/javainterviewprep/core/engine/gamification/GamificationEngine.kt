package io.github.ducthinh0304.javainterviewprep.core.engine.gamification

import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import io.github.ducthinh0304.javainterviewprep.core.model.UserStats
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Singleton
class GamificationEngine @Inject constructor() {

    fun calculateBaseXp(difficulty: Difficulty): Int {
        return when (difficulty) {
            Difficulty.JUNIOR -> 10
            Difficulty.MID -> 20
            Difficulty.SENIOR -> 35
        }
    }

    fun calculateEarnedXp(difficulty: Difficulty, comboMultiplier: Double): Int {
        val base = calculateBaseXp(difficulty)
        return (base * comboMultiplier).roundToInt()
    }

    fun calculateNextCombo(currentConsecutive: Int, isCorrect: Boolean): Pair<Int, Double> {
        return if (isCorrect) {
            val nextConsecutive = currentConsecutive + 1
            // Combo multiplier scales by +0.1 for every consecutive correct, capped at 2.5x
            val multiplier = min(2.5, 1.0 + (nextConsecutive * 0.1))
            // Round to 1 decimal place
            val roundedMultiplier = (multiplier * 10).roundToInt() / 10.0
            Pair(nextConsecutive, roundedMultiplier)
        } else {
            Pair(0, 1.0)
        }
    }

    fun calculateStreak(
        currentStreak: Int,
        highestStreak: Int,
        lastActiveEpochDay: Long?,
        todayEpochDay: Long
    ): Pair<Int, Int> {
        if (lastActiveEpochDay == null) {
            return Pair(1, max(1, highestStreak))
        }

        val dayDiff = todayEpochDay - lastActiveEpochDay
        return when {
            dayDiff == 0L -> Pair(currentStreak, highestStreak) // Already logged today
            dayDiff == 1L -> {
                val newStreak = currentStreak + 1
                Pair(newStreak, max(newStreak, highestStreak))
            }
            else -> Pair(1, max(1, highestStreak)) // Streak broken
        }
    }

    fun applyAnswerResult(
        currentStats: UserStats,
        difficulty: Difficulty,
        isCorrect: Boolean,
        todayEpochDay: Long
    ): Pair<UserStats, Int> {
        val (newConsecutive, newCombo) = calculateNextCombo(
            currentConsecutive = currentStats.consecutiveCorrectAnswers,
            isCorrect = isCorrect
        )

        val earnedXp = if (isCorrect) {
            calculateEarnedXp(difficulty, currentStats.comboMultiplier)
        } else {
            0
        }

        val (newStreak, newHighest) = calculateStreak(
            currentStreak = currentStats.currentStreak,
            highestStreak = currentStats.highestStreak,
            lastActiveEpochDay = currentStats.lastActiveEpochDay,
            todayEpochDay = todayEpochDay
        )

        val updatedStats = currentStats.copy(
            xp = currentStats.xp + earnedXp,
            currentStreak = newStreak,
            highestStreak = newHighest,
            comboMultiplier = newCombo,
            consecutiveCorrectAnswers = newConsecutive,
            lastActiveEpochDay = todayEpochDay,
            totalExercisesAnswered = currentStats.totalExercisesAnswered + 1,
            totalCorrectAnswers = currentStats.totalCorrectAnswers + if (isCorrect) 1 else 0
        )

        return Pair(updatedStats, earnedXp)
    }
}
