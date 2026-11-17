package io.github.ducthinh0304.javainterviewprep.core.engine.evaluation

import io.github.ducthinh0304.javainterviewprep.core.model.Question
import java.util.Locale

/** What the user submitted. Each question kind expects exactly one of these. */
sealed interface UserAnswer {
    /** SingleChoice, PredictOutput. */
    data class Choice(val index: Int) : UserAnswer

    /** MultiSelect (option indices), SpotTheBug (line indices). */
    data class Choices(val indices: Set<Int>) : UserAnswer

    /** Ordering: the items in the order the user arranged them. */
    data class Order(val items: List<String>) : UserAnswer

    /** Matching: left -> right. */
    data class Pairs(val pairs: Map<String, String>) : UserAnswer

    /** FillBlank. */
    data class Text(val text: String) : UserAnswer

    /** ShortAnswer: the user compares with the model answer and grades themselves. */
    data class SelfGrade(val gotIt: Boolean) : UserAnswer
}

data class CheckResult(val isCorrect: Boolean, val explanation: String)

object AnswerChecker {

    fun check(question: Question, answer: UserAnswer): CheckResult {
        val isCorrect = when (question) {
            is Question.SingleChoice -> answer.of<UserAnswer.Choice>(question).index == question.correctIndex
            is Question.PredictOutput -> answer.of<UserAnswer.Choice>(question).index == question.correctIndex
            is Question.MultiSelect -> answer.of<UserAnswer.Choices>(question).indices == question.correctIndices.toSet()
            is Question.SpotTheBug -> answer.of<UserAnswer.Choices>(question).indices == question.buggyLineIndices.toSet()
            is Question.Ordering -> answer.of<UserAnswer.Order>(question).items == question.items
            is Question.Matching -> answer.of<UserAnswer.Pairs>(question).pairs == question.pairs.associate { it.left to it.right }
            is Question.FillBlank -> {
                val input = normalizeBlank(answer.of<UserAnswer.Text>(question).text)
                input.isNotEmpty() && question.acceptedAnswers.any { normalizeBlank(it) == input }
            }
            is Question.ShortAnswer -> answer.of<UserAnswer.SelfGrade>(question).gotIt
        }
        return CheckResult(isCorrect, question.explanation)
    }

    /** Case-insensitive, whitespace runs collapsed to one space, trailing semicolons dropped. */
    fun normalizeBlank(text: String): String =
        text.trim().replace(WHITESPACE, " ").trimEnd(';', ' ').lowercase(Locale.ROOT)

    private val WHITESPACE = Regex("\\s+")

    // A mismatched answer type is a UI bug, not a wrong answer: fail loudly.
    private inline fun <reified T : UserAnswer> UserAnswer.of(question: Question): T =
        this as? T ?: throw IllegalArgumentException(
            "${question::class.simpleName} expects ${T::class.simpleName}, got ${this::class.simpleName}"
        )
}
