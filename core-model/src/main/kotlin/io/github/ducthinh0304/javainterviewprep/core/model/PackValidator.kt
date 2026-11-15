package io.github.ducthinh0304.javainterviewprep.core.model

data class ValidationError(
    val entityId: String,
    val rule: String,
    val message: String
)

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError>
) {
    fun errorMessages(): List<String> = errors.map { "[${it.rule}] (id: ${it.entityId}) ${it.message}" }
}

class PackValidationException(message: String, val errors: List<ValidationError>) :
    IllegalArgumentException(message)

object PackValidator {

    private const val MAX_CODE_SNIPPET_LINES = 15
    private val FENCED_CODE_BLOCK_REGEX = Regex("```[a-zA-Z0-9_-]*\\n([\\s\\S]*?)```")

    fun validate(pack: Pack): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // 1. Pack-level ID checks
        val allUnitIds = mutableSetOf<String>()
        val allNoteIds = mutableSetOf<String>()
        val allQuestionIds = mutableSetOf<String>()
        val allEntityIds = mutableSetOf<String>()

        if (pack.id.isBlank()) {
            errors.add(ValidationError(pack.id, "BLANK_ID", "Pack ID cannot be blank"))
        }

        // 2. Validate Units
        for (unit in pack.units) {
            if (unit.id.isBlank()) {
                errors.add(ValidationError(unit.id, "BLANK_ID", "Unit ID cannot be blank"))
            } else {
                if (!allUnitIds.add(unit.id)) {
                    errors.add(ValidationError(unit.id, "DUPLICATE_ID", "Duplicate Unit ID: '${unit.id}'"))
                }
                if (!allEntityIds.add(unit.id)) {
                    errors.add(ValidationError(unit.id, "DUPLICATE_ID", "Unit ID '${unit.id}' collides with another entity ID"))
                }
            }

            // Duplicate prompt detection within unit
            val promptsSeen = mutableMapOf<String, String>() // normalized prompt -> questionId

            // Validate Questions within Unit
            for (question in unit.questions) {
                validateQuestion(
                    question = question,
                    enclosingUnitId = unit.id,
                    errors = errors,
                    allQuestionIds = allQuestionIds,
                    allEntityIds = allEntityIds,
                    promptsSeen = promptsSeen
                )
            }

            // Validate Notes within Unit
            for (note in unit.notes) {
                validateNote(
                    note = note,
                    errors = errors,
                    allNoteIds = allNoteIds,
                    allEntityIds = allEntityIds,
                    availableQuestionIds = allQuestionIds
                )
            }
        }

        // Post-validation: ensure notes' quizQuestionIds exist in the pack
        for (unit in pack.units) {
            for (note in unit.notes) {
                for (quizId in note.quizQuestionIds) {
                    if (!allQuestionIds.contains(quizId)) {
                        errors.add(
                            ValidationError(
                                entityId = note.id,
                                rule = "MISSING_QUIZ_QUESTION",
                                message = "Note '${note.id}' references non-existent quizQuestionId '$quizId'"
                            )
                        )
                    }
                }
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    fun validateOrThrow(pack: Pack) {
        val result = validate(pack)
        if (!result.isValid) {
            val message = "Pack '${pack.id}' validation failed with ${result.errors.size} error(s):\n" +
                result.errorMessages().joinToString("\n")
            throw PackValidationException(message, result.errors)
        }
    }

    private fun validateQuestion(
        question: Question,
        enclosingUnitId: String,
        errors: MutableList<ValidationError>,
        allQuestionIds: MutableSet<String>,
        allEntityIds: MutableSet<String>,
        promptsSeen: MutableMap<String, String>
    ) {
        val id = question.id
        if (id.isBlank()) {
            errors.add(ValidationError(id, "BLANK_ID", "Question ID cannot be blank"))
        } else {
            if (!allQuestionIds.add(id)) {
                errors.add(ValidationError(id, "DUPLICATE_ID", "Duplicate Question ID: '$id'"))
            }
            if (!allEntityIds.add(id)) {
                errors.add(ValidationError(id, "DUPLICATE_ID", "Question ID '$id' collides with another entity ID"))
            }
        }

        if (question.unitId != enclosingUnitId) {
            errors.add(
                ValidationError(
                    entityId = id,
                    rule = "UNIT_MISMATCH",
                    message = "Question '$id' unitId '${question.unitId}' does not match enclosing unit '$enclosingUnitId'"
                )
            )
        }

        // Missing explanation check
        if (question.explanation.isBlank()) {
            errors.add(
                ValidationError(
                    entityId = id,
                    rule = "MISSING_EXPLANATION",
                    message = "Question '$id' is missing a required explanation"
                )
            )
        }

        // Duplicate prompt check
        val normalizedPrompt = question.prompt.trim().lowercase()
        if (normalizedPrompt.isNotBlank()) {
            val existing = promptsSeen[normalizedPrompt]
            if (existing != null) {
                errors.add(
                    ValidationError(
                        entityId = id,
                        rule = "DUPLICATE_PROMPT",
                        message = "Duplicate prompt found between question '$id' and '$existing': '${question.prompt.take(50)}...'"
                    )
                )
            } else {
                promptsSeen[normalizedPrompt] = id
            }
        }

        // Question kind specific checks
        when (question) {
            is Question.SingleChoice -> {
                if (question.options.size < 2) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "TOO_FEW_OPTIONS",
                            message = "SingleChoice '$id' has fewer than 2 options (found ${question.options.size})"
                        )
                    )
                }
                if (question.correctIndex !in question.options.indices) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "OUT_OF_RANGE_INDEX",
                            message = "SingleChoice '$id' correctIndex ${question.correctIndex} is out of range (options count: ${question.options.size})"
                        )
                    )
                }
                question.code?.let { code ->
                    val lineCount = code.trim().lines().size
                    if (lineCount > MAX_CODE_SNIPPET_LINES) {
                        errors.add(
                            ValidationError(
                                entityId = id,
                                rule = "CODE_TOO_LONG",
                                message = "SingleChoice '$id' code snippet exceeds $MAX_CODE_SNIPPET_LINES lines (found $lineCount lines)"
                            )
                        )
                    }
                }
            }

            is Question.MultiSelect -> {
                if (question.options.size < 2) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "TOO_FEW_OPTIONS",
                            message = "MultiSelect '$id' has fewer than 2 options (found ${question.options.size})"
                        )
                    )
                }
                if (question.correctIndices.isEmpty()) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "OUT_OF_RANGE_INDEX",
                            message = "MultiSelect '$id' has empty correctIndices"
                        )
                    )
                }
                for (index in question.correctIndices) {
                    if (index !in question.options.indices) {
                        errors.add(
                            ValidationError(
                                entityId = id,
                                rule = "OUT_OF_RANGE_INDEX",
                                message = "MultiSelect '$id' correctIndex $index is out of range (options count: ${question.options.size})"
                            )
                        )
                    }
                }
                question.code?.let { code ->
                    val lineCount = code.trim().lines().size
                    if (lineCount > MAX_CODE_SNIPPET_LINES) {
                        errors.add(
                            ValidationError(
                                entityId = id,
                                rule = "CODE_TOO_LONG",
                                message = "MultiSelect '$id' code snippet exceeds $MAX_CODE_SNIPPET_LINES lines (found $lineCount lines)"
                            )
                        )
                    }
                }
            }

            is Question.Ordering -> {
                if (question.items.size < 2) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "TOO_FEW_OPTIONS",
                            message = "Ordering '$id' has fewer than 2 items (found ${question.items.size})"
                        )
                    )
                }
            }

            is Question.Matching -> {
                if (question.pairs.size < 2) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "TOO_FEW_OPTIONS",
                            message = "Matching '$id' has fewer than 2 pairs (found ${question.pairs.size})"
                        )
                    )
                }
            }

            is Question.PredictOutput -> {
                if (question.options.size < 2) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "TOO_FEW_OPTIONS",
                            message = "PredictOutput '$id' has fewer than 2 options (found ${question.options.size})"
                        )
                    )
                }
                if (question.correctIndex !in question.options.indices) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "OUT_OF_RANGE_INDEX",
                            message = "PredictOutput '$id' correctIndex ${question.correctIndex} is out of range (options count: ${question.options.size})"
                        )
                    )
                }
                val lineCount = question.code.trim().lines().size
                if (lineCount > MAX_CODE_SNIPPET_LINES) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "CODE_TOO_LONG",
                            message = "PredictOutput '$id' code snippet exceeds $MAX_CODE_SNIPPET_LINES lines (found $lineCount lines)"
                        )
                    )
                }
            }

            is Question.SpotTheBug -> {
                if (question.codeLines.isEmpty()) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "OUT_OF_RANGE_INDEX",
                            message = "SpotTheBug '$id' has no code lines"
                        )
                    )
                }
                if (question.codeLines.size > MAX_CODE_SNIPPET_LINES) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "CODE_TOO_LONG",
                            message = "SpotTheBug '$id' code lines exceed $MAX_CODE_SNIPPET_LINES lines (found ${question.codeLines.size} lines)"
                        )
                    )
                }
                if (question.buggyLineIndices.isEmpty()) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "OUT_OF_RANGE_INDEX",
                            message = "SpotTheBug '$id' has no buggyLineIndices specified"
                        )
                    )
                }
                for (index in question.buggyLineIndices) {
                    if (index !in question.codeLines.indices) {
                        errors.add(
                            ValidationError(
                                entityId = id,
                                rule = "OUT_OF_RANGE_INDEX",
                                message = "SpotTheBug '$id' buggyLineIndex $index is out of range (code lines count: ${question.codeLines.size})"
                            )
                        )
                    }
                }
            }

            is Question.FillBlank -> {
                if (!question.prompt.contains("___")) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "MISSING_BLANK",
                            message = "FillBlank '$id' prompt must contain '___'"
                        )
                    )
                }
                if (question.acceptedAnswers.isEmpty() || question.acceptedAnswers.all { it.isBlank() }) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "EMPTY_ACCEPTED_ANSWERS",
                            message = "FillBlank '$id' must provide at least one accepted answer"
                        )
                    )
                }
            }

            is Question.ShortAnswer -> {
                if (question.modelAnswer.isBlank()) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "MISSING_MODEL_ANSWER",
                            message = "ShortAnswer '$id' modelAnswer cannot be blank"
                        )
                    )
                }
                if (question.keyPoints.isEmpty()) {
                    errors.add(
                        ValidationError(
                            entityId = id,
                            rule = "MISSING_KEY_POINTS",
                            message = "ShortAnswer '$id' must contain at least one keyPoint"
                        )
                    )
                }
            }
        }
    }

    private fun validateNote(
        note: Note,
        errors: MutableList<ValidationError>,
        allNoteIds: MutableSet<String>,
        allEntityIds: MutableSet<String>,
        availableQuestionIds: Set<String>
    ) {
        val id = note.id
        if (id.isBlank()) {
            errors.add(ValidationError(id, "BLANK_ID", "Note ID cannot be blank"))
        } else {
            if (!allNoteIds.add(id)) {
                errors.add(ValidationError(id, "DUPLICATE_ID", "Duplicate Note ID: '$id'"))
            }
            if (!allEntityIds.add(id)) {
                errors.add(ValidationError(id, "DUPLICATE_ID", "Note ID '$id' collides with another entity ID"))
            }
        }

        // Quiz questions count check: exactly 2 or 3 questions
        if (note.quizQuestionIds.size !in 2..3) {
            errors.add(
                ValidationError(
                    entityId = id,
                    rule = "INVALID_QUIZ_COUNT",
                    message = "Note '$id' must have between 2 and 3 quiz questions (found ${note.quizQuestionIds.size})"
                )
            )
        }

        // Check fenced code block lines in note body markdown
        FENCED_CODE_BLOCK_REGEX.findAll(note.bodyMarkdown).forEach { match ->
            val codeContent = match.groupValues[1]
            val lineCount = codeContent.trim().lines().size
            if (lineCount > MAX_CODE_SNIPPET_LINES) {
                errors.add(
                    ValidationError(
                        entityId = id,
                        rule = "CODE_TOO_LONG",
                        message = "Note '$id' contains a code block exceeding $MAX_CODE_SNIPPET_LINES lines (found $lineCount lines)"
                    )
                )
            }
        }
    }
}
