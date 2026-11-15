package io.github.ducthinh0304.javainterviewprep.core.engine.evaluation

import io.github.ducthinh0304.javainterviewprep.core.model.Question
import javax.inject.Inject
import javax.inject.Singleton

data class EvaluationResult(
    val isCorrect: Boolean,
    val selectedId: String,
    val explanation: String
)

@Singleton
class AnswerEvaluator @Inject constructor() {

    fun evaluateSingleChoice(
        question: Question.SingleChoice,
        selectedIndex: Int
    ): EvaluationResult {
        val isCorrect = selectedIndex == question.correctIndex
        return EvaluationResult(
            isCorrect = isCorrect,
            selectedId = selectedIndex.toString(),
            explanation = question.explanation
        )
    }

    fun evaluateMultiSelect(
        question: Question.MultiSelect,
        selectedIndices: Set<Int>
    ): EvaluationResult {
        val isCorrect = selectedIndices == question.correctIndices.toSet()
        return EvaluationResult(
            isCorrect = isCorrect,
            selectedId = selectedIndices.sorted().joinToString(","),
            explanation = question.explanation
        )
    }

    fun evaluatePredictOutput(
        question: Question.PredictOutput,
        selectedIndex: Int
    ): EvaluationResult {
        val isCorrect = selectedIndex == question.correctIndex
        return EvaluationResult(
            isCorrect = isCorrect,
            selectedId = selectedIndex.toString(),
            explanation = question.explanation
        )
    }

    fun evaluateSpotTheBug(
        question: Question.SpotTheBug,
        selectedLineIndices: Set<Int>
    ): EvaluationResult {
        val isCorrect = selectedLineIndices == question.buggyLineIndices.toSet()
        return EvaluationResult(
            isCorrect = isCorrect,
            selectedId = selectedLineIndices.sorted().joinToString(","),
            explanation = question.explanation
        )
    }

    fun evaluateFillBlank(
        question: Question.FillBlank,
        userAnswer: String
    ): EvaluationResult {
        val isCorrect = question.isAnswerAccepted(userAnswer)
        return EvaluationResult(
            isCorrect = isCorrect,
            selectedId = userAnswer,
            explanation = question.explanation
        )
    }

    fun evaluateOrdering(
        question: Question.Ordering,
        userOrder: List<String>
    ): EvaluationResult {
        val isCorrect = userOrder == question.items
        return EvaluationResult(
            isCorrect = isCorrect,
            selectedId = userOrder.joinToString(" -> "),
            explanation = question.explanation
        )
    }

    fun evaluateMatching(
        question: Question.Matching,
        userPairs: Map<String, String> // left -> right
    ): EvaluationResult {
        val expected = question.pairs.associate { it.left to it.right }
        val isCorrect = userPairs == expected
        return EvaluationResult(
            isCorrect = isCorrect,
            selectedId = userPairs.toString(),
            explanation = question.explanation
        )
    }
}
