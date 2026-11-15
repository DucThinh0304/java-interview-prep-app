package io.github.ducthinh0304.javainterviewprep.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Unit(
    val id: String,
    val title: String,
    val description: String,
    val notes: List<Note> = emptyList(),
    val questions: List<Question> = emptyList()
)

typealias Topic = Unit
typealias StudyUnit = Unit

@Serializable
data class Pack(
    val id: String,
    val title: String,
    val version: Int,
    val units: List<Unit> = emptyList()
) {
    fun allQuestions(): List<Question> = units.flatMap { it.questions }

    fun allNotes(): List<Note> = units.flatMap { it.notes }

    fun findQuestion(id: String): Question? = allQuestions().firstOrNull { it.id == id }

    fun findNote(id: String): Note? = allNotes().firstOrNull { it.id == id }
}
