package io.github.ducthinh0304.javainterviewprep.core.engine.evaluation

import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import io.github.ducthinh0304.javainterviewprep.core.model.Exercise
import io.github.ducthinh0304.javainterviewprep.core.model.QuizOption
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerEvaluatorTest {

    private val evaluator = AnswerEvaluator()

    @Test
    fun evaluateMultipleChoice_verifiesSelectedOption() {
        val exercise = Exercise.MultipleChoice(
            id = "q1",
            topicId = "topic1",
            difficulty = Difficulty.JUNIOR,
            prompt = "Is Java pass-by-value?",
            explanation = "Java is always pass-by-value.",
            options = listOf(
                QuizOption("opt_yes", "Yes, always pass-by-value", isCorrect = true),
                QuizOption("opt_no", "No, objects are pass-by-reference", isCorrect = false)
            )
        )

        val resultCorrect = evaluator.evaluateMultipleChoice(exercise, "opt_yes")
        assertTrue(resultCorrect.isCorrect)

        val resultIncorrect = evaluator.evaluateMultipleChoice(exercise, "opt_no")
        assertFalse(resultIncorrect.isCorrect)
    }
}
