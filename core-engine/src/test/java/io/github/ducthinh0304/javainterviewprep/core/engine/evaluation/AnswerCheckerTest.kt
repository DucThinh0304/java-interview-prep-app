package io.github.ducthinh0304.javainterviewprep.core.engine.evaluation

import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.AnswerChecker.check
import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.AnswerChecker.normalizeBlank
import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty.EASY
import io.github.ducthinh0304.javainterviewprep.core.model.MatchingPair
import io.github.ducthinh0304.javainterviewprep.core.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerCheckerTest {

    private fun fillBlank(vararg accepted: String) =
        Question.FillBlank("fb", "u", EASY, explanation = "why", prompt = "p", acceptedAnswers = accepted.toList())

    private fun blank(q: Question.FillBlank, text: String) = check(q, UserAnswer.Text(text)).isCorrect

    // FillBlank normalization

    @Test
    fun normalizeBlank_cases() {
        assertEquals("volatile", normalizeBlank("volatile"))
        assertEquals("volatile", normalizeBlank("  VOLATILE \n"))
        assertEquals("volatile", normalizeBlank("Volatile;"))
        assertEquals("volatile", normalizeBlank("volatile ;; ; "))
        assertEquals("final int x", normalizeBlank("final\tint   x"))
        assertEquals("return x", normalizeBlank("return\n x ;"))
        assertEquals("a;b", normalizeBlank("a;b;")) // only trailing semicolons go
        assertEquals("", normalizeBlank(" ;; "))
    }

    @Test
    fun fillBlank_acceptsCaseWhitespaceAndSemicolonVariants() {
        val q = fillBlank("volatile")
        listOf("volatile", "VOLATILE", " Volatile ", "volatile;", "volatile ; ;", "\tvolatile\n").forEach {
            assertTrue("'$it' should be accepted", blank(q, it))
        }
    }

    @Test
    fun fillBlank_rejectsWrongOrPartialAnswers() {
        val q = fillBlank("volatile")
        listOf("synchronized", "volatil", "vola tile", "volatile x", "").forEach {
            assertFalse("'$it' should be rejected", blank(q, it))
        }
    }

    @Test
    fun fillBlank_normalizesTheAcceptedAnswersToo() {
        val q = fillBlank("  System.out.println(x);  ")
        assertTrue(blank(q, "system.out.println(x)"))
        assertTrue(blank(q, "SYSTEM.OUT.PRINTLN(x);"))
        assertFalse(blank(q, "System.out.println( x )")) // inner spacing is content, not formatting
    }

    @Test
    fun fillBlank_matchesAnyAcceptedAnswer() {
        val q = fillBlank("ArrayList", "java.util.ArrayList")
        assertTrue(blank(q, "arraylist"))
        assertTrue(blank(q, "java.util.arraylist;"))
        assertFalse(blank(q, "LinkedList"))
    }

    @Test
    fun fillBlank_emptyInputIsNeverCorrect() {
        assertFalse(blank(fillBlank(";"), ""))
        assertFalse(blank(fillBlank(";"), " ; "))
    }

    // Other kinds

    @Test
    fun singleChoice_and_predictOutput_compareIndex() {
        val sc = Question.SingleChoice("q", "u", EASY, explanation = "e", prompt = "p", options = listOf("a", "b"), correctIndex = 1)
        assertTrue(check(sc, UserAnswer.Choice(1)).isCorrect)
        assertFalse(check(sc, UserAnswer.Choice(0)).isCorrect)

        val po = Question.PredictOutput("q", "u", EASY, explanation = "e", code = "c", options = listOf("1", "2"), correctIndex = 0)
        assertTrue(check(po, UserAnswer.Choice(0)).isCorrect)
        assertFalse(check(po, UserAnswer.Choice(1)).isCorrect)
    }

    @Test
    fun multiSelect_isAllOrNothing() {
        val q = Question.MultiSelect("q", "u", EASY, explanation = "e", prompt = "p", options = listOf("a", "b", "c", "d"), correctIndices = listOf(0, 2))
        assertTrue(check(q, UserAnswer.Choices(setOf(2, 0))).isCorrect)
        assertFalse(check(q, UserAnswer.Choices(setOf(0))).isCorrect) // missing one
        assertFalse(check(q, UserAnswer.Choices(setOf(0, 1, 2))).isCorrect) // one extra
        assertFalse(check(q, UserAnswer.Choices(emptySet())).isCorrect)
    }

    @Test
    fun spotTheBug_needsExactlyTheBuggyLines() {
        val q = Question.SpotTheBug("q", "u", EASY, explanation = "e", codeLines = listOf("a", "b", "c"), buggyLineIndices = listOf(1))
        assertTrue(check(q, UserAnswer.Choices(setOf(1))).isCorrect)
        assertFalse(check(q, UserAnswer.Choices(setOf(1, 2))).isCorrect)
    }

    @Test
    fun ordering_isAllOrNothing() {
        val q = Question.Ordering("q", "u", EASY, explanation = "e", prompt = "p", items = listOf("load", "link", "init"))
        assertTrue(check(q, UserAnswer.Order(listOf("load", "link", "init"))).isCorrect)
        assertFalse(check(q, UserAnswer.Order(listOf("load", "init", "link"))).isCorrect)
        assertFalse(check(q, UserAnswer.Order(listOf("load", "link"))).isCorrect)
    }

    @Test
    fun matching_needsEveryPairRight() {
        val q = Question.Matching(
            "q", "u", EASY, explanation = "e", prompt = "p",
            pairs = listOf(MatchingPair("List", "ordered"), MatchingPair("Set", "unique"))
        )
        assertTrue(check(q, UserAnswer.Pairs(mapOf("Set" to "unique", "List" to "ordered"))).isCorrect)
        assertFalse(check(q, UserAnswer.Pairs(mapOf("List" to "unique", "Set" to "ordered"))).isCorrect)
        assertFalse(check(q, UserAnswer.Pairs(mapOf("List" to "ordered"))).isCorrect)
    }

    @Test
    fun shortAnswer_usesSelfGrade() {
        val q = Question.ShortAnswer("q", "u", EASY, explanation = "e", prompt = "p", modelAnswer = "m", keyPoints = listOf("k"))
        assertTrue(check(q, UserAnswer.SelfGrade(true)).isCorrect)
        assertFalse(check(q, UserAnswer.SelfGrade(false)).isCorrect)
    }

    @Test
    fun result_carriesExplanation() {
        assertEquals("why", check(fillBlank("x"), UserAnswer.Text("y")).explanation)
    }

    @Test(expected = IllegalArgumentException::class)
    fun mismatchedAnswerType_throws() {
        check(fillBlank("x"), UserAnswer.Choice(0))
    }
}
