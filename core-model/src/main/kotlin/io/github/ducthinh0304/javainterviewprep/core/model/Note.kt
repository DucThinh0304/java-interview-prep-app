package io.github.ducthinh0304.javainterviewprep.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val title: String,
    val bodyMarkdown: String,
    val diagramKey: String? = null,
    val quizQuestionIds: List<String>
)
