package io.github.ducthinh0304.javainterviewprep.core.engine.repository

import io.github.ducthinh0304.javainterviewprep.core.engine.db.EngineDatabase
import io.github.ducthinh0304.javainterviewprep.core.engine.db.ExerciseProgressEntity
import io.github.ducthinh0304.javainterviewprep.core.engine.db.UserStatsEntity
import io.github.ducthinh0304.javainterviewprep.core.engine.gamification.GamificationEngine
import io.github.ducthinh0304.javainterviewprep.core.engine.srs.SpacedRepetitionEngine
import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import io.github.ducthinh0304.javainterviewprep.core.model.ReviewItem
import io.github.ducthinh0304.javainterviewprep.core.model.ReviewResult
import io.github.ducthinh0304.javainterviewprep.core.model.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProgressRepository @Inject constructor(
    private val database: EngineDatabase,
    private val gamificationEngine: GamificationEngine,
    private val srsEngine: SpacedRepetitionEngine
) {
    private val progressDao = database.exerciseProgressDao()
    private val statsDao = database.userStatsDao()

    val userStatsFlow: Flow<UserStats> = statsDao.getStatsFlow().map { entity ->
        entity?.toModel() ?: UserStats()
    }

    val reviewQueueFlow: Flow<List<ExerciseProgressEntity>> = progressDao.getAllReviewItems()

    suspend fun getExerciseProgress(exerciseId: String): ExerciseProgressEntity? {
        return progressDao.getProgress(exerciseId)
    }

    suspend fun recordAttempt(
        exerciseId: String,
        difficulty: Difficulty,
        isCorrect: Boolean,
        todayEpochDay: Long
    ): Pair<UserStats, Int> {
        val currentStats = (statsDao.getStats()?.toModel()) ?: UserStats()
        val (updatedStats, earnedXp) = gamificationEngine.applyAnswerResult(
            currentStats = currentStats,
            difficulty = difficulty,
            isCorrect = isCorrect,
            todayEpochDay = todayEpochDay
        )

        // Save updated user stats
        statsDao.upsert(updatedStats.toEntity())

        // Update exercise progress & spaced repetition queue
        val existingProgress = progressDao.getProgress(exerciseId) ?: ExerciseProgressEntity(
            exerciseId = exerciseId
        )

        val reviewItem = ReviewItem(
            exerciseId = exerciseId,
            box = existingProgress.box,
            nextReviewEpochDay = existingProgress.nextReviewEpochDay,
            intervalDays = existingProgress.intervalDays,
            easeFactor = existingProgress.easeFactor,
            repetitions = existingProgress.repetitions,
            lapses = existingProgress.lapses
        )

        val reviewResult = if (isCorrect) ReviewResult.GOOD else ReviewResult.AGAIN
        val updatedReviewItem = srsEngine.calculateNextReview(reviewItem, reviewResult, todayEpochDay)

        val updatedProgress = existingProgress.copy(
            timesSeen = existingProgress.timesSeen + 1,
            timesCorrect = existingProgress.timesCorrect + if (isCorrect) 1 else 0,
            lastAttemptEpochDay = todayEpochDay,
            // If missed, add to review queue ("Re-run"). If answered correctly and interval > 7 days, remove from active re-run queue.
            inReviewQueue = !isCorrect || (existingProgress.inReviewQueue && updatedReviewItem.intervalDays < 7),
            nextReviewEpochDay = updatedReviewItem.nextReviewEpochDay,
            box = updatedReviewItem.box,
            intervalDays = updatedReviewItem.intervalDays,
            easeFactor = updatedReviewItem.easeFactor,
            repetitions = updatedReviewItem.repetitions,
            lapses = updatedReviewItem.lapses
        )

        progressDao.upsert(updatedProgress)

        return Pair(updatedStats, earnedXp)
    }

    private fun UserStatsEntity.toModel(): UserStats = UserStats(
        xp = xp,
        currentStreak = currentStreak,
        highestStreak = highestStreak,
        comboMultiplier = comboMultiplier,
        consecutiveCorrectAnswers = consecutiveCorrectAnswers,
        lastActiveEpochDay = lastActiveEpochDay,
        totalExercisesAnswered = totalExercisesAnswered,
        totalCorrectAnswers = totalCorrectAnswers
    )

    private fun UserStats.toEntity(): UserStatsEntity = UserStatsEntity(
        id = 1,
        xp = xp,
        currentStreak = currentStreak,
        highestStreak = highestStreak,
        comboMultiplier = comboMultiplier,
        consecutiveCorrectAnswers = consecutiveCorrectAnswers,
        lastActiveEpochDay = lastActiveEpochDay,
        totalExercisesAnswered = totalExercisesAnswered,
        totalCorrectAnswers = totalCorrectAnswers
    )
}
