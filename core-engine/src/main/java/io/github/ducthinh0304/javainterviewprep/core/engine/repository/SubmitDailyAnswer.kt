package io.github.ducthinh0304.javainterviewprep.core.engine.repository

import androidx.room.withTransaction
import io.github.ducthinh0304.javainterviewprep.core.engine.db.EngineDatabase
import io.github.ducthinh0304.javainterviewprep.core.model.Question
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Records a daily answer atomically: question stat, XP/streak/activity and the daily_answer row
 * either all land or none do (e.g. if the ViewModel scope is cancelled mid-save).
 * Returns the XP earned.
 */
class SubmitDailyAnswer @Inject constructor(
    private val database: EngineDatabase,
    private val progressRepository: ProgressRepository,
    private val statsRepository: StatsRepository,
    private val clock: Clock
) {
    // ponytail: StatsRepository's in-memory combo isn't rolled back with the DB; worst case a
    // failed save leaves the session combo one step ahead. Fine until combo becomes persisted.
    suspend operator fun invoke(question: Question, selectedOptionIndex: Int, isCorrect: Boolean): Int =
        database.withTransaction {
            progressRepository.recordQuestionAttempt(question.id, isCorrect)
            val (_, earnedXp) = statsRepository.recordAnswer(question.difficulty, isCorrect)
            progressRepository.recordDailyAnswer(
                LocalDate.now(clock), question.id, selectedOptionIndex, isCorrect, earnedXp
            )
            earnedXp
        }
}
