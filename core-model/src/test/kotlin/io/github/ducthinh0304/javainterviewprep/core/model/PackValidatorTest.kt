package io.github.ducthinh0304.javainterviewprep.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackValidatorTest {

    private fun createValidQuestion(
        id: String = "q1",
        unitId: String = "u1",
        prompt: String = "Prompt 1",
        explanation: String = "Valid explanation"
    ): Question = Question.SingleChoice(
        id = id,
        unitId = unitId,
        difficulty = Difficulty.EASY,
        prompt = prompt,
        explanation = explanation,
        options = listOf("Option A", "Option B"),
        correctIndex = 0
    )

    private fun createValidNote(
        id: String = "n1",
        quizQuestionIds: List<String> = listOf("q1", "q2")
    ): Note = Note(
        id = id,
        title = "Valid Note",
        bodyMarkdown = "Short markdown with code:\n```java\nint a = 1;\n```",
        quizQuestionIds = quizQuestionIds
    )

    private fun createBasePack(
        questions: List<Question> = listOf(createValidQuestion("q1"), createValidQuestion("q2", prompt = "Prompt 2")),
        notes: List<Note> = listOf(createValidNote("n1", listOf("q1", "q2")))
    ): Pack = Pack(
        id = "test_pack",
        title = "Test Pack",
        version = 1,
        units = listOf(
            Unit(
                id = "u1",
                title = "Unit 1",
                description = "Desc",
                notes = notes,
                questions = questions
            )
        )
    )

    @Test
    fun validPack_passesWithZeroErrors() {
        val pack = createBasePack()
        val result = PackValidator.validate(pack)
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun duplicateQuestionId_reportsError() {
        val q1 = createValidQuestion("dup_id", prompt = "First")
        val q2 = createValidQuestion("dup_id", prompt = "Second")
        val pack = createBasePack(questions = listOf(q1, q2))

        val result = PackValidator.validate(pack)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.rule == "DUPLICATE_ID" && it.entityId == "dup_id" })
    }

    @Test
    fun duplicateNoteId_reportsError() {
        val n1 = createValidNote("note_dup")
        val n2 = createValidNote("note_dup")
        val pack = createBasePack(notes = listOf(n1, n2))

        val result = PackValidator.validate(pack)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.rule == "DUPLICATE_ID" && it.entityId == "note_dup" })
    }

    @Test
    fun crossEntityIdCollision_reportsError() {
        val q = createValidQuestion("same_id")
        val n = createValidNote("same_id")
        val pack = createBasePack(questions = listOf(q, createValidQuestion("q2")), notes = listOf(n))

        val result = PackValidator.validate(pack)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.rule == "DUPLICATE_ID" && it.entityId == "same_id" })
    }

    @Test
    fun outOfRangeIndex_singleChoice() {
        val q = Question.SingleChoice(
            id = "sc_bad",
            unitId = "u1",
            difficulty = Difficulty.EASY,
            prompt = "Prompt",
            explanation = "Explanation",
            options = listOf("A", "B"),
            correctIndex = 5 // out of range
        )
        val pack = createBasePack(questions = listOf(q, createValidQuestion("q2")))

        val result = PackValidator.validate(pack)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.rule == "OUT_OF_RANGE_INDEX" && it.entityId == "sc_bad" })
    }

    @Test
    fun outOfRangeIndex_multiSelect() {
        val q = Question.MultiSelect(
            id = "ms_bad",
            unitId = "u1",
            difficulty = Difficulty.EASY,
            prompt = "Prompt",
            explanation = "Explanation",
            options = listOf("A", "B"),
            correctIndices = listOf(0, 3) // 3 is out of range
        )
        val pack = createBasePack(questions = listOf(q, createValidQuestion("q2")))

        val result = PackValidator.validate(pack)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.rule == "OUT_OF_RANGE_INDEX" && it.entityId == "ms_bad" })
    }

    @Test
    fun outOfRangeIndex_spotTheBug() {
        val q = Question.SpotTheBug(
            id = "stb_bad",
            unitId = "u1",
            difficulty = Difficulty.HARD,
            explanation = "Explanation",
            codeLines = listOf("line 0", "line 1"),
            buggyLineIndices = listOf(4) // 4 is out of range
        )
        val pack = createBasePack(questions = listOf(q, createValidQuestion("q2")))

        val result = PackValidator.validate(pack)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.rule == "OUT_OF_RANGE_INDEX" && it.entityId == "stb_bad" })
    }

    @Test
    fun missingExplanation_reportsError() {
        val q = createValidQuestion("q_no_exp", explanation = "   ")
        val pack = createBasePack(questions = listOf(q, createValidQuestion("q2")))

        val result = PackValidator.validate(pack)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.rule == "MISSING_EXPLANATION" && it.entityId == "q_no_exp" })
    }

    @Test
    fun tooFewOptions_reportsError() {
        val sc = Question.SingleChoice(
            id = "sc_1opt",
            unitId = "u1",
            difficulty = Difficulty.EASY,
            prompt = "Single opt",
            explanation = "Exp",
            options = listOf("Only one"),
            correctIndex = 0
        )
        val ord = Question.Ordering(
            id = "ord_1item",
            unitId = "u1",
            difficulty = Difficulty.EASY,
            prompt = "Order one",
            explanation = "Exp",
            items = listOf("Lone item")
        )
        val mat = Question.Matching(
            id = "mat_1pair",
            unitId = "u1",
            difficulty = Difficulty.EASY,
            prompt = "Match one",
            explanation = "Exp",
            pairs = listOf(MatchingPair("L", "R"))
        )
        val pack = createBasePack(questions = listOf(sc, ord, mat, createValidQuestion("q2")))

        val result = PackValidator.validate(pack)
        assertFalse(result.isValid)
        val tooFew = result.errors.filter { it.rule == "TOO_FEW_OPTIONS" }
        assertEquals(3, tooFew.size)
    }

    @Test
    fun notesWithoutQuizQuestions_orInvalidQuizCount() {
        val nEmpty = Note(id = "n_empty", title = "T", bodyMarkdown = "B", quizQuestionIds = emptyList())
        val nOne = Note(id = "n_one", title = "T", bodyMarkdown = "B", quizQuestionIds = listOf("q1"))
        val nFour = Note(id = "n_four", title = "T", bodyMarkdown = "B", quizQuestionIds = listOf("q1", "q2", "q3", "q4"))
        val nGhost = Note(id = "n_ghost", title = "T", bodyMarkdown = "B", quizQuestionIds = listOf("q1", "non_existent_q"))

        val pack = createBasePack(
            questions = listOf(createValidQuestion("q1"), createValidQuestion("q2"), createValidQuestion("q3"), createValidQuestion("q4")),
            notes = listOf(nEmpty, nOne, nFour, nGhost)
        )

        val result = PackValidator.validate(pack)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.rule == "INVALID_QUIZ_COUNT" && it.entityId == "n_empty" })
        assertTrue(result.errors.any { it.rule == "INVALID_QUIZ_COUNT" && it.entityId == "n_one" })
        assertTrue(result.errors.any { it.rule == "INVALID_QUIZ_COUNT" && it.entityId == "n_four" })
        assertTrue(result.errors.any { it.rule == "MISSING_QUIZ_QUESTION" && it.entityId == "n_ghost" })
    }

    @Test
    fun duplicatePrompts_withinUnitReportsError() {
        val q1 = createValidQuestion("q_dup1", prompt = "What is JVM?")
        val q2 = createValidQuestion("q_dup2", prompt = "  what is jvm?  ")
        val pack = createBasePack(questions = listOf(q1, q2))

        val result = PackValidator.validate(pack)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.rule == "DUPLICATE_PROMPT" })
    }

    @Test
    fun codeSnippetOver15Lines_reportsError() {
        val longCode = (1..18).joinToString("\n") { "int line$it = $it;" }

        val qLong = Question.SingleChoice(
            id = "q_long_code",
            unitId = "u1",
            difficulty = Difficulty.EASY,
            prompt = "Long code prompt",
            explanation = "Exp",
            code = longCode,
            options = listOf("A", "B"),
            correctIndex = 0
        )

        val nLong = Note(
            id = "n_long_code",
            title = "Long code note",
            bodyMarkdown = "```java\n$longCode\n```",
            quizQuestionIds = listOf("q1", "q2")
        )

        val pack = createBasePack(
            questions = listOf(qLong, createValidQuestion("q1"), createValidQuestion("q2")),
            notes = listOf(nLong)
        )

        val result = PackValidator.validate(pack)
        assertFalse(result.isValid)
        val codeTooLongErrors = result.errors.filter { it.rule == "CODE_TOO_LONG" }
        assertEquals(2, codeTooLongErrors.size)
        assertTrue(codeTooLongErrors.any { it.entityId == "q_long_code" })
        assertTrue(codeTooLongErrors.any { it.entityId == "n_long_code" })
    }

    @Test
    fun fillBlank_missingBlankOrEmptyAnswers_reportsError() {
        val fbNoBlank = Question.FillBlank(
            id = "fb_no_blank",
            unitId = "u1",
            difficulty = Difficulty.EASY,
            prompt = "No blank placeholder here.",
            explanation = "Exp",
            acceptedAnswers = listOf("val")
        )
        val fbNoAns = Question.FillBlank(
            id = "fb_no_ans",
            unitId = "u1",
            difficulty = Difficulty.EASY,
            prompt = "Here is the ___ placeholder.",
            explanation = "Exp",
            acceptedAnswers = emptyList()
        )
        val pack = createBasePack(questions = listOf(fbNoBlank, fbNoAns, createValidQuestion("q2")))

        val result = PackValidator.validate(pack)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.rule == "MISSING_BLANK" && it.entityId == "fb_no_blank" })
        assertTrue(result.errors.any { it.rule == "EMPTY_ACCEPTED_ANSWERS" && it.entityId == "fb_no_ans" })
    }

    @Test
    fun samplePack_parsesAndValidatesCleanly() {
        val json = kotlinx.serialization.json.Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
        val file = java.io.File("../content-java/src/main/assets/packs/java/pack.json")
        assertTrue("pack.json should exist at ${file.canonicalPath}", file.exists())

        val pack = json.decodeFromString<Pack>(file.readText())
        val validationResult = PackValidator.validate(pack)

        assertTrue(
            "Validation errors found: \n" + validationResult.errorMessages().joinToString("\n"),
            validationResult.isValid
        )
        assertEquals("unit_java_concurrency", pack.units[0].id)
        assertEquals(2, pack.units[0].notes.size)
        assertEquals(9, pack.units[0].questions.size)

        // Verify all 8 kinds exist in the sample pack
        val questionClasses = pack.allQuestions().map { it::class }.toSet()
        assertTrue(questionClasses.contains(Question.SingleChoice::class))
        assertTrue(questionClasses.contains(Question.MultiSelect::class))
        assertTrue(questionClasses.contains(Question.Ordering::class))
        assertTrue(questionClasses.contains(Question.Matching::class))
        assertTrue(questionClasses.contains(Question.PredictOutput::class))
        assertTrue(questionClasses.contains(Question.SpotTheBug::class))
        assertTrue(questionClasses.contains(Question.FillBlank::class))
        assertTrue(questionClasses.contains(Question.ShortAnswer::class))
    }
}
