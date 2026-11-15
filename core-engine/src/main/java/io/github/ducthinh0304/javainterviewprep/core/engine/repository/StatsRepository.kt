package io.github.ducthinh0304.javainterviewprep.core.engine.repository

import androidx.room.withTransaction
import io.github.ducthinh0304.javainterviewprep.core.engine.db.DailyActivityDao
import io.github.ducthinh0304.javainterviewprep.core.engine.db.DailyActivityEntity
import io.github.ducthinh0304.javainterviewprep.core.engine.db.EngineDatabase
import io.github.ducthinh0304.javainterviewprep.core.engine.db.UserStatsDao
import io.github.ducthinh0304.javainterviewprep.core.engine.db.UserStatsEntity
import io.github.ducthinh0304.javainterviewprep.core.engine.gamification.GamificationEngine
import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import io.github.ducthinh0304.javainterviewprep.core.model.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Only totalXp/streak/lastActiveDate/bestCombo survive a restart (per UserStatsEntity).
 * The combo multiplier and consecutive-correct counter are session-scoped and reset on launch.
 */
private data class SessionStats(
    val comboMultiplier: Double = 1.0,
    val consecutiveCorrectAnswers: Int = 0,
    val exercisesAnswered: Int = 0,
    val correctAnswers: Int = 0
)

@Singleton
class StatsRepository @Inject constructor(
    private val database: EngineDatabase,
    private val userStatsDao: UserStatsDao,
    private val dailyActivityDao: DailyActivityDao,
    private val gamificationEngine: GamificationEngine,
    private val clock: Clock
) {
    private val sessionStats = MutableStateFlow(SessionStats())

    val userStatsFlow: Flow<UserStats> = combine(
        userStatsDao.observe().map { it ?: UserStatsEntity() },
        sessionStats
    ) { persisted, session -> persisted.toDomain(session) }

    fun dailyActivity(date: LocalDate): Flow<DailyActivityEntity?> = dailyActivityDao.observe(date)

    suspend fun recordAnswer(difficulty: Difficulty, isCorrect: Boolean): Pair<UserStats, Int> {
        val today = LocalDate.now(clock)
        val persisted = userStatsDao.get() ?: UserStatsEntity()

        val (updated, earnedXp) = gamificationEngine.applyAnswerResult(
            currentStats = persisted.toDomain(sessionStats.value),
            difficulty = difficulty,
            isCorrect = isCorrect
        )

        // Answering alone is not a check-in; the streak only moves in recordQuizCompleted().
        userStatsDao.upsert(
            persisted.copy(
                totalXp = updated.xp,
                bestCombo = maxOf(persisted.bestCombo, updated.comboMultiplier)
            )
        )

        recordDailyActivity(today, earnedXp)

        // Only advance in-memory session stats once the DB writes have succeeded.
        sessionStats.value = SessionStats(
            comboMultiplier = updated.comboMultiplier,
            consecutiveCorrectAnswers = updated.consecutiveCorrectAnswers,
            exercisesAnswered = updated.totalExercisesAnswered,
            correctAnswers = updated.totalCorrectAnswers
        )

        return Pair(updated, earnedXp)
    }

    /**
     * Quizzes are unlimited per day. The first one completed on a day is that day's check-in
     * (quizzesCompleted goes 0 -> 1) and is the only thing that moves the streak.
     */
    suspend fun recordQuizCompleted() = database.withTransaction {
        val today = LocalDate.now(clock)
        val activity = dailyActivityDao.get(today) ?: DailyActivityEntity(date = today)
        dailyActivityDao.upsert(activity.copy(quizzesCompleted = activity.quizzesCompleted + 1))
        if (activity.quizzesCompleted >= 1) return@withTransaction // already checked in today

        val persisted = userStatsDao.get() ?: UserStatsEntity()
        val (streak, longest) = gamificationEngine.calculateStreak(
            currentStreak = persisted.currentStreak,
            highestStreak = persisted.longestStreak,
            lastActiveEpochDay = persisted.lastActiveDate?.toEpochDay(),
            todayEpochDay = today.toEpochDay()
        )
        userStatsDao.upsert(persisted.copy(currentStreak = streak, longestStreak = longest, lastActiveDate = today))
    }

    private suspend fun recordDailyActivity(date: LocalDate, xpEarned: Int) {
        val existing = dailyActivityDao.get(date)
        dailyActivityDao.upsert(
            (existing ?: DailyActivityEntity(date = date)).copy(
                xpEarned = (existing?.xpEarned ?: 0) + xpEarned,
                questionsAnswered = (existing?.questionsAnswered ?: 0) + 1
            )
        )
    }

    /** Alive while the last check-in was today or yesterday; after a missed day it shows 0 right away. */
    private fun UserStatsEntity.isStreakAlive(): Boolean =
        lastActiveDate?.isBefore(LocalDate.now(clock).minusDays(1)) == false

    private fun UserStatsEntity.toDomain(session: SessionStats): UserStats = UserStats(
        xp = totalXp,
        currentStreak = if (isStreakAlive()) currentStreak else 0,
        highestStreak = longestStreak,
        comboMultiplier = session.comboMultiplier,
        consecutiveCorrectAnswers = session.consecutiveCorrectAnswers,
        lastActiveEpochDay = lastActiveDate?.toEpochDay(),
        totalExercisesAnswered = session.exercisesAnswered,
        totalCorrectAnswers = session.correctAnswers
    )
}
