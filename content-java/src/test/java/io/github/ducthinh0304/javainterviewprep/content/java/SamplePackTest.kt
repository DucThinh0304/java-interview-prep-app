package io.github.ducthinh0304.javainterviewprep.content.java

import io.github.ducthinh0304.javainterviewprep.core.model.Pack
import io.github.ducthinh0304.javainterviewprep.core.model.PackValidator
import io.github.ducthinh0304.javainterviewprep.core.model.Question
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SamplePackTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun samplePack_containsRequiredContentAndValidates() {
        val packFile = File("src/main/assets/packs/java/pack.json")
        assertTrue("pack.json must exist in assets", packFile.exists())

        val pack = json.decodeFromString<Pack>(packFile.readText())
        val result = PackValidator.validate(pack)

        assertTrue(
            "Sample pack validation failed: " + result.errorMessages().joinToString("\n"),
            result.isValid
        )

        assertEquals("java_core_pack", pack.id)
        assertEquals(1, pack.units.size)

        val unit = pack.units[0]
        assertEquals(2, unit.notes.size)
        assertTrue(unit.questions.size >= 8)

        // Verify each note has 2-3 quizQuestionIds
        for (note in unit.notes) {
            assertTrue(
                "Note ${note.id} should have 2..3 quiz questions",
                note.quizQuestionIds.size in 2..3
            )
            for (qId in note.quizQuestionIds) {
                assertTrue("Question $qId referenced in ${note.id} must exist", pack.findQuestion(qId) != null)
            }
        }

        // Verify at least one question of each kind exists
        val kinds = pack.allQuestions().map { it::class.simpleName }.toSet()
        val expectedKinds = setOf(
            "SingleChoice",
            "MultiSelect",
            "Ordering",
            "Matching",
            "PredictOutput",
            "SpotTheBug",
            "FillBlank",
            "ShortAnswer"
        )
        assertEquals(expectedKinds, kinds)
    }
}
