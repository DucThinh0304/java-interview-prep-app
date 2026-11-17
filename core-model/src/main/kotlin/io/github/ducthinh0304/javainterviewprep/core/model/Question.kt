package io.github.ducthinh0304.javainterviewprep.core.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class MatchingPair(
    val left: String,
    val right: String
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface Question {
    val id: String
    val unitId: String
    val difficulty: Difficulty
    val tags: List<String>
    val docsUrl: String?
    val explanation: String
    val prompt: String

    @Serializable
    @SerialName("SingleChoice")
    data class SingleChoice(
        override val id: String,
        override val unitId: String,
        override val difficulty: Difficulty,
        override val tags: List<String> = emptyList(),
        override val docsUrl: String? = null,
        override val explanation: String,
        override val prompt: String,
        val code: String? = null,
        val options: List<String>,
        val correctIndex: Int
    ) : Question

    @Serializable
    @SerialName("MultiSelect")
    data class MultiSelect(
        override val id: String,
        override val unitId: String,
        override val difficulty: Difficulty,
        override val tags: List<String> = emptyList(),
        override val docsUrl: String? = null,
        override val explanation: String,
        override val prompt: String,
        val code: String? = null,
        val options: List<String>,
        val correctIndices: List<Int>
    ) : Question

    @Serializable
    @SerialName("Ordering")
    data class Ordering(
        override val id: String,
        override val unitId: String,
        override val difficulty: Difficulty,
        override val tags: List<String> = emptyList(),
        override val docsUrl: String? = null,
        override val explanation: String,
        override val prompt: String,
        val items: List<String>
    ) : Question

    @Serializable
    @SerialName("Matching")
    data class Matching(
        override val id: String,
        override val unitId: String,
        override val difficulty: Difficulty,
        override val tags: List<String> = emptyList(),
        override val docsUrl: String? = null,
        override val explanation: String,
        override val prompt: String,
        val pairs: List<MatchingPair>
    ) : Question

    @Serializable
    @SerialName("PredictOutput")
    data class PredictOutput(
        override val id: String,
        override val unitId: String,
        override val difficulty: Difficulty,
        override val tags: List<String> = emptyList(),
        override val docsUrl: String? = null,
        override val explanation: String,
        val code: String,
        val options: List<String>,
        val correctIndex: Int,
        override val prompt: String = "What does this code print or throw?"
    ) : Question

    @Serializable
    @SerialName("SpotTheBug")
    data class SpotTheBug(
        override val id: String,
        override val unitId: String,
        override val difficulty: Difficulty,
        override val tags: List<String> = emptyList(),
        override val docsUrl: String? = null,
        override val explanation: String,
        val codeLines: List<String>,
        val buggyLineIndices: List<Int>,
        override val prompt: String = "Spot the bug in the following code."
    ) : Question

    @Serializable
    @SerialName("FillBlank")
    data class FillBlank(
        override val id: String,
        override val unitId: String,
        override val difficulty: Difficulty,
        override val tags: List<String> = emptyList(),
        override val docsUrl: String? = null,
        override val explanation: String,
        override val prompt: String,
        val acceptedAnswers: List<String>
    ) : Question

    @Serializable
    @SerialName("ShortAnswer")
    data class ShortAnswer(
        override val id: String,
        override val unitId: String,
        override val difficulty: Difficulty,
        override val tags: List<String> = emptyList(),
        override val docsUrl: String? = null,
        override val explanation: String,
        override val prompt: String,
        val modelAnswer: String,
        val keyPoints: List<String>
    ) : Question
}
