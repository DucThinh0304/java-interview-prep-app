package io.github.ducthinh0304.javainterviewprep.core.model

import kotlinx.serialization.Serializable

@Serializable
data class QuizOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean = false
)

@Serializable
sealed interface Exercise {
    val id: String
    val topicId: String
    val difficulty: Difficulty
    val prompt: String
    val explanation: String
    val tags: List<String>

    @Serializable
    data class MultipleChoice(
        override val id: String,
        override val topicId: String,
        override val difficulty: Difficulty,
        override val prompt: String,
        override val explanation: String,
        override val tags: List<String> = emptyList(),
        val codeSnippet: String? = null,
        val options: List<QuizOption>
    ) : Exercise

    @Serializable
    data class CodeOutput(
        override val id: String,
        override val topicId: String,
        override val difficulty: Difficulty,
        override val prompt: String,
        override val explanation: String,
        override val tags: List<String> = emptyList(),
        val codeSnippet: String,
        val expectedOutput: String,
        val options: List<String>
    ) : Exercise
}

@Serializable
data class Lesson(
    val id: String,
    val topicId: String,
    val title: String,
    val durationMinutes: Int = 1,
    val contentMarkdown: String,
    val keyTakeaways: List<String>,
    val quiz: List<Exercise>
)

@Serializable
data class ContentPack(
    val id: String,
    val title: String,
    val version: Int,
    val description: String,
    val questions: List<Exercise>,
    val lessons: List<Lesson>
)
