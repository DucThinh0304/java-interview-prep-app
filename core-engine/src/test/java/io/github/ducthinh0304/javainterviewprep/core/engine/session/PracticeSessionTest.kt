package io.github.ducthinh0304.javainterviewprep.core.engine.session

import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.UserAnswer
import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import io.github.ducthinh0304.javainterviewprep.core.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeSessionTest {

    private fun q(id: String) = Question.SingleChoice(
        id, "u", Difficulty.EASY, explanation = "e", prompt = "p", options = listOf("right", "wrong"), correctIndex = 0
    )

    private val right = UserAnswer.Choice(0)
    private val wrong = UserAnswer.Choice(1)

    private fun PracticeSession.step(a: UserAnswer) = answer(a).next()

    @Test
    fun answer_showsFeedbackThenNextAdvances() {
        val s = PracticeSession(listOf(q("a"), q("b"))).answer(right)
        assertEquals("a", s.current?.id)
        assertTrue(s.feedback!!.result.isCorrect)
        assertEquals(10, s.feedback!!.xpEarned)

        val n = s.next()
        assertEquals("b", n.current?.id)
        assertNull(n.feedback)
    }

    @Test
    fun doubleAnswer_andPrematureNext_areNoOps() {
        val start = PracticeSession(listOf(q("a")))
        assertSame(start, start.next())
        val answered = start.answer(right)
        assertSame(answered, answered.answer(wrong))
    }

    @Test
    fun countsComboAndXpAccumulate() {
        val s = PracticeSession(List(4) { q("$it") })
            .step(right).step(right).step(right) // 10 + 10 + 15 (x1.5 on the 3rd)
            .answer(wrong)
        assertEquals(3, s.correct)
        assertEquals(1, s.incorrect)
        assertEquals(35, s.xpEarned)
        assertEquals(0, s.combo.streak)
        assertEquals(0, s.feedback!!.xpEarned)
    }

    @Test
    fun missedQuestionsAreRequeuedOnceAtTheEnd() {
        var s = PracticeSession(listOf(q("a"), q("b"), q("c")))
            .step(wrong).step(right).step(wrong)

        assertTrue(s.isRerun)
        assertEquals(listOf("a", "c"), s.queue.map { it.id })
        assertFalse(s.isFinished)

        s = s.step(wrong).step(right) // missing again in the re-run does not re-queue
        assertTrue(s.isFinished)
        assertNull(s.current)
        assertEquals(2, s.correct)
        assertEquals(3, s.incorrect)
        assertTrue(s.missed.isEmpty())
    }

    @Test
    fun noMisses_finishesWithoutRerun() {
        val s = PracticeSession(listOf(q("a"))).step(right)
        assertTrue(s.isFinished)
        assertFalse(s.isRerun)
    }

    @Test
    fun finishedSession_ignoresInput() {
        val done = PracticeSession(emptyList())
        assertTrue(done.isFinished)
        assertSame(done, done.answer(right))
        assertSame(done, done.next())
    }
}
