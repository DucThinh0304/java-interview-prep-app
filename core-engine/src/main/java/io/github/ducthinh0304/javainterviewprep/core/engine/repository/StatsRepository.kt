package io.github.ducthinh0304.javainterviewprep.core.engine.repository

import io.github.ducthinh0304.javainterviewprep.core.engine.db.DailyActivityDao
import io.github.ducthinh0304.javainterviewprep.core.engine.db.DailyActivityEntity
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
            isCorrect = isCorrect,
            todayEpochDay = today.toEpochDay()
        )

        userStatsDao.upsert(
            UserStatsEntity(
                totalXp = updated.xp,
                currentStreak = updated.currentStreak,
                longestStreak = updated.highestStreak,
                lastActiveDate = today,
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

    private suspend fun recordDailyActivity(date: LocalDate, xpEarned: Int) {
        val existing = dailyActivityDao.get(date)
        dailyActivityDao.upsert(
            (existing ?: DailyActivityEntity(date = date)).copy(
                xpEarned = (existing?.xpEarned ?: 0) + xpEarned,
                questionsAnswered = (existing?.questionsAnswered ?: 0) + 1
            )
        )
    }

    private fun UserStatsEntity.toDomain(session: SessionStats): UserStats = UserStats(
        xp = totalXp,
        currentStreak = currentStreak,
        highestStreak = longestStreak,
        comboMultiplier = session.comboMultiplier,
        consecutiveCorrectAnswers = session.consecutiveCorrectAnswers,
        lastActiveEpochDay = lastActiveDate?.toEpochDay(),
        totalExercisesAnswered = session.exercisesAnswered,
        totalCorrectAnswers = session.correctAnswers
    )
}
