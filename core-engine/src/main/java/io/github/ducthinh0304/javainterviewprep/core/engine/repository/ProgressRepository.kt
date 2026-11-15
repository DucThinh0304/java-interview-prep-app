package io.github.ducthinh0304.javainterviewprep.core.engine.repository

import io.github.ducthinh0304.javainterviewprep.core.engine.db.DailyAnswerDao
import io.github.ducthinh0304.javainterviewprep.core.engine.db.DailyAnswerEntity
import io.github.ducthinh0304.javainterviewprep.core.engine.db.NoteProgressDao
import io.github.ducthinh0304.javainterviewprep.core.engine.db.NoteProgressEntity
import io.github.ducthinh0304.javainterviewprep.core.engine.db.QuestionStatDao
import io.github.ducthinh0304.javainterviewprep.core.engine.db.QuestionStatEntity
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Leitner-style box intervals: box `n` is re-shown after `BOX_INTERVAL_DAYS[n]` days.
 * A miss always drops a question back to box 1 (due again tomorrow).
 */
private val BOX_INTERVAL_DAYS = longArrayOf(0, 1, 3, 7, 14, 30)

@Singleton
class ProgressRepository @Inject constructor(
    private val questionStatDao: QuestionStatDao,
    private val noteProgressDao: NoteProgressDao,
    private val dailyAnswerDao: DailyAnswerDao,
    private val clock: Clock
) {
    fun questionStat(questionId: String): Flow<QuestionStatEntity?> = questionStatDao.observe(questionId)

    fun dueQuestions(now: Instant = Instant.now(clock)): Flow<List<QuestionStatEntity>> =
        questionStatDao.observeDue(now)

    suspend fun recordQuestionAttempt(questionId: String, isCorrect: Boolean): QuestionStatEntity {
        val now = Instant.now(clock)
        val existing = questionStatDao.get(questionId) ?: QuestionStatEntity(questionId = questionId)
        val newBox = if (isCorrect) {
            (existing.box + 1).coerceAtMost(BOX_INTERVAL_DAYS.lastIndex)
        } else {
            1
        }
        val updated = existing.copy(
            timesSeen = existing.timesSeen + 1,
            timesCorrect = existing.timesCorrect + if (isCorrect) 1 else 0,
            lastSeenAt = now,
            box = newBox,
            nextReviewAt = now.plus(BOX_INTERVAL_DAYS[newBox], ChronoUnit.DAYS)
        )
        questionStatDao.upsert(updated)
        return updated
    }

    fun noteProgress(noteId: String): Flow<NoteProgressEntity?> = noteProgressDao.observe(noteId)

    suspend fun recordNoteRead(noteId: String): NoteProgressEntity {
        val existing = noteProgressDao.get(noteId) ?: NoteProgressEntity(noteId = noteId)
        val updated = existing.copy(readAt = Instant.now(clock))
        noteProgressDao.upsert(updated)
        return updated
    }

    suspend fun recordNoteQuizResult(noteId: String, passed: Boolean): NoteProgressEntity {
        val existing = noteProgressDao.get(noteId)
            ?: NoteProgressEntity(noteId = noteId, readAt = Instant.now(clock))
        val updated = existing.copy(quizPassed = passed)
        noteProgressDao.upsert(updated)
        return updated
    }

    fun dailyAnswer(date: LocalDate): Flow<DailyAnswerEntity?> = dailyAnswerDao.observe(date)

    suspend fun recordDailyAnswer(
        date: LocalDate,
        questionId: String,
        selectedOptionIndex: Int,
        wasCorrect: Boolean,
        xpEarned: Int
    ) {
        dailyAnswerDao.upsert(
            DailyAnswerEntity(
                date = date,
                questionId = questionId,
                selectedOptionIndex = selectedOptionIndex,
                wasCorrect = wasCorrect,
                xpEarned = xpEarned
            )
        )
    }
}
