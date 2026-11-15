package io.github.ducthinh0304.javainterviewprep.core.engine.evaluation

import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import io.github.ducthinh0304.javainterviewprep.core.model.MatchingPair
import io.github.ducthinh0304.javainterviewprep.core.model.Question
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerEvaluatorTest {

    private val evaluator = AnswerEvaluator()

    @Test
    fun evaluateSingleChoice_verifiesSelectedIndex() {
        val question = Question.SingleChoice(
            id = "q1",
            unitId = "u1",
            difficulty = Difficulty.EASY,
            prompt = "Is Java pass-by-value?",
            explanation = "Java is always pass-by-value.",
            options = listOf("Yes, always pass-by-value", "No, pass-by-reference"),
            correctIndex = 0
        )

        val resultCorrect = evaluator.evaluateSingleChoice(question, 0)
        assertTrue(resultCorrect.isCorrect)

        val resultIncorrect = evaluator.evaluateSingleChoice(question, 1)
        assertFalse(resultIncorrect.isCorrect)
    }

    @Test
    fun evaluateMultiSelect_verifiesAllSelectedIndices() {
        val question = Question.MultiSelect(
            id = "q2",
            unitId = "u1",
            difficulty = Difficulty.MEDIUM,
            prompt = "Select all guarantees of synchronized:",
            explanation = "Both mutual exclusion and visibility.",
            options = listOf("Mutual Exclusion", "Visibility", "Lock-free"),
            correctIndices = listOf(0, 1)
        )

        assertTrue(evaluator.evaluateMultiSelect(question, setOf(0, 1)).isCorrect)
        assertFalse(evaluator.evaluateMultiSelect(question, setOf(0)).isCorrect)
        assertFalse(evaluator.evaluateMultiSelect(question, setOf(0, 1, 2)).isCorrect)
    }

    @Test
    fun evaluateFillBlank_normalizesCaseAndWhitespace() {
        val question = Question.FillBlank(
            id = "q3",
            unitId = "u1",
            difficulty = Difficulty.EASY,
            prompt = "Field visibility keyword: ___",
            explanation = "volatile",
            acceptedAnswers = listOf("volatile")
        )

        assertTrue(evaluator.evaluateFillBlank(question, "volatile").isCorrect)
        assertTrue(evaluator.evaluateFillBlank(question, "  VOLATILE  ").isCorrect)
        assertFalse(evaluator.evaluateFillBlank(question, "synchronized").isCorrect)
    }

    @Test
    fun evaluateMatching_verifiesAllPairs() {
        val question = Question.Matching(
            id = "q4",
            unitId = "u1",
            difficulty = Difficulty.HARD,
            prompt = "Match synchronizers",
            explanation = "Matching synchronizers to behaviors",
            pairs = listOf(
                MatchingPair("A", "1"),
                MatchingPair("B", "2")
            )
        )

        assertTrue(evaluator.evaluateMatching(question, mapOf("A" to "1", "B" to "2")).isCorrect)
        assertFalse(evaluator.evaluateMatching(question, mapOf("A" to "2", "B" to "1")).isCorrect)
    }
}
