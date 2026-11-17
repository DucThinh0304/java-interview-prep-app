package io.github.ducthinh0304.javainterviewprep.core.engine.gamification

import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import io.github.ducthinh0304.javainterviewprep.core.model.UserStats
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class GamificationEngine @Inject constructor() {

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

    /** The answer that reaches a combo tier already earns at that tier's multiplier. */
    fun applyAnswerResult(
        currentStats: UserStats,
        difficulty: Difficulty,
        isCorrect: Boolean,
        isDaily: Boolean = false
    ): Pair<UserStats, Int> {
        val combo = ComboTracker(currentStats.consecutiveCorrectAnswers).next(isCorrect)
        val earnedXp = XpCalculator.earned(difficulty, isCorrect, combo.multiplier, isDaily)

        val updatedStats = currentStats.copy(
            xp = currentStats.xp + earnedXp,
            comboMultiplier = combo.multiplier,
            consecutiveCorrectAnswers = combo.streak,
            totalExercisesAnswered = currentStats.totalExercisesAnswered + 1,
            totalCorrectAnswers = currentStats.totalCorrectAnswers + if (isCorrect) 1 else 0
        )

        return Pair(updatedStats, earnedXp)
    }
}
