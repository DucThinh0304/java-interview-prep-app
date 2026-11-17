package io.github.ducthinh0304.javainterviewprep.core.engine.session

import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.AnswerChecker
import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.CheckResult
import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.UserAnswer
import io.github.ducthinh0304.javainterviewprep.core.engine.gamification.ComboTracker
import io.github.ducthinh0304.javainterviewprep.core.engine.gamification.XpCalculator
import io.github.ducthinh0304.javainterviewprep.core.model.Question

data class Feedback(val result: CheckResult, val xpEarned: Int)

/**
 * Immutable practice-session state. Hold it in a MutableStateFlow and `update { it.answer(a) }`:
 * Answering (feedback == null) -> answer() -> Showing feedback -> next() -> ... -> isFinished.
 * Calls that don't fit the current phase return the same state, so double-taps are no-ops.
 * Questions missed in the first pass are re-queued once at the end; misses in that re-run are not.
 */
data class PracticeSession(
    val queue: List<Question>,
    val correct: Int = 0,
    val incorrect: Int = 0,
    val combo: ComboTracker = ComboTracker(),
    val xpEarned: Int = 0,
    val missed: List<Question> = emptyList(),
    val isRerun: Boolean = false,
    val feedback: Feedback? = null
) {
    val current: Question? get() = queue.firstOrNull()
    val isFinished: Boolean get() = current == null

    fun answer(answer: UserAnswer): PracticeSession {
        val question = current ?: return this
        if (feedback != null) return this
        val result = AnswerChecker.check(question, answer)
        val nextCombo = combo.next(result.isCorrect)
        val xp = XpCalculator.earned(question.difficulty, result.isCorrect, nextCombo.multiplier)
        return copy(
            correct = correct + if (result.isCorrect) 1 else 0,
            incorrect = incorrect + if (result.isCorrect) 0 else 1,
            combo = nextCombo,
            xpEarned = xpEarned + xp,
            missed = if (result.isCorrect || isRerun) missed else missed + question,
            feedback = Feedback(result, xp)
        )
    }

    fun next(): PracticeSession {
        if (feedback == null) return this
        val rest = queue.drop(1)
        return if (rest.isEmpty() && !isRerun && missed.isNotEmpty()) {
            copy(queue = missed, missed = emptyList(), isRerun = true, feedback = null)
        } else {
            copy(queue = rest, feedback = null)
        }
    }
}
