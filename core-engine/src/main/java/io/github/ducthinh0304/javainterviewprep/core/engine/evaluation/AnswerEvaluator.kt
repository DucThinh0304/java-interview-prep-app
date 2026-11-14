package io.github.ducthinh0304.javainterviewprep.core.engine.evaluation

import io.github.ducthinh0304.javainterviewprep.core.model.Exercise
import javax.inject.Inject
import javax.inject.Singleton

data class EvaluationResult(
    val isCorrect: Boolean,
    val selectedId: String,
    val explanation: String
)

@Singleton
class AnswerEvaluator @Inject constructor() {

    fun evaluateMultipleChoice(
        exercise: Exercise.MultipleChoice,
        selectedOptionId: String
    ): EvaluationResult {
        val selectedOption = exercise.options.firstOrNull { it.id == selectedOptionId }
        val isCorrect = selectedOption?.isCorrect == true
        return EvaluationResult(
            isCorrect = isCorrect,
            selectedId = selectedOptionId,
            explanation = exercise.explanation
        )
    }

    fun evaluateCodeOutput(
        exercise: Exercise.CodeOutput,
        selectedOutput: String
    ): EvaluationResult {
        val isCorrect = exercise.expectedOutput.trim() == selectedOutput.trim()
        return EvaluationResult(
            isCorrect = isCorrect,
            selectedId = selectedOutput,
            explanation = exercise.explanation
        )
    }
}
