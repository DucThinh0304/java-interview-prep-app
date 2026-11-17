package io.github.ducthinh0304.javainterviewprep.core.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PackSchemaTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun serializeAndDeserialize_allEightQuestionKinds() {
        val questions: List<Question> = listOf(
            Question.SingleChoice(
                id = "sc_1",
                unitId = "u1",
                difficulty = Difficulty.EASY,
                tags = listOf("java", "syntax"),
                docsUrl = "https://docs.oracle.com",
                explanation = "Java is always pass by value.",
                prompt = "Is Java pass-by-value or pass-by-reference?",
                code = "int x = 5;\nmodify(x);",
                options = listOf("Pass-by-value", "Pass-by-reference"),
                correctIndex = 0
            ),
            Question.MultiSelect(
                id = "ms_1",
                unitId = "u1",
                difficulty = Difficulty.MEDIUM,
                tags = listOf("concurrency", "keywords"),
                explanation = "Synchronized ensures both mutual exclusion and visibility.",
                prompt = "Which guarantees are provided by the synchronized keyword in Java?",
                options = listOf("Mutual exclusion", "Visibility", "Non-blocking execution", "Compile-time inlining"),
                correctIndices = listOf(0, 1)
            ),
            Question.Ordering(
                id = "ord_1",
                unitId = "u1",
                difficulty = Difficulty.MEDIUM,
                tags = listOf("thread", "lifecycle"),
                explanation = "NEW -> RUNNABLE -> TIMED_WAITING -> TERMINATED.",
                prompt = "Order the thread states during Thread.sleep(1000) execution:",
                items = listOf("NEW", "RUNNABLE", "TIMED_WAITING", "TERMINATED")
            ),
            Question.Matching(
                id = "mat_1",
                unitId = "u1",
                difficulty = Difficulty.HARD,
                tags = listOf("concurrency", "util"),
                explanation = "Correct matching of java.util.concurrent synchronizers.",
                prompt = "Match the concurrency utility to its primary behavior:",
                pairs = listOf(
                    MatchingPair("CountDownLatch", "Non-reusable barrier awaiting countdown to zero"),
                    MatchingPair("CyclicBarrier", "Reusable synchronization point for a fixed party of threads"),
                    MatchingPair("Semaphore", "Maintains permits for resource throttling")
                )
            ),
            Question.PredictOutput(
                id = "pred_1",
                unitId = "u1",
                difficulty = Difficulty.EASY,
                tags = listOf("string", "pool"),
                explanation = "String literals with identical content share the same intern pool reference.",
                code = "String a = \"java\";\nString b = \"java\";\nSystem.out.println(a == b);",
                options = listOf("true", "false", "Compilation error", "NullPointerException"),
                correctIndex = 0
            ),
            Question.SpotTheBug(
                id = "bug_1",
                unitId = "u1",
                difficulty = Difficulty.HARD,
                tags = listOf("concurrency", "singleton"),
                explanation = "Double-checked locking requires volatile on the instance field to prevent instruction reordering.",
                codeLines = listOf(
                    "public class Singleton {",
                    "    private static Singleton instance;", // Line 1: bug (missing volatile)
                    "    public static Singleton get() {",
                    "        if (instance == null) {",
                    "            synchronized (Singleton.class) {",
                    "                if (instance == null) instance = new Singleton();",
                    "            }",
                    "        }",
                    "        return instance;",
                    "    }",
                    "}"
                ),
                buggyLineIndices = listOf(1)
            ),
            Question.FillBlank(
                id = "fill_1",
                unitId = "u1",
                difficulty = Difficulty.EASY,
                tags = listOf("keywords", "memory"),
                explanation = "Volatile guarantees memory visibility without mutual exclusion locks.",
                prompt = "To prevent thread-local caching of a field, mark it with the ___ keyword.",
                acceptedAnswers = listOf("volatile")
            ),
            Question.ShortAnswer(
                id = "sa_1",
                unitId = "u1",
                difficulty = Difficulty.HARD,
                tags = listOf("concurrency", "locking"),
                explanation = "Optimistic locking assumes collisions are rare and verifies at commit; pessimistic locks upfront.",
                prompt = "What is the difference between optimistic locking and pessimistic locking?",
                modelAnswer = "Pessimistic locking locks records before access (e.g., synchronized, select for update). Optimistic locking reads without locking, using version checks or CAS to detect conflicts at write time.",
                keyPoints = listOf(
                    "Pessimistic locks upfront to prevent concurrent modifications",
                    "Optimistic allows concurrent reads and checks version or CAS upon write",
                    "Optimistic yields higher throughput under low contention"
                )
            )
        )

        val note = Note(
            id = "note_1",
            title = "Java Memory Model & Volatiles",
            bodyMarkdown = "### Visibility\n`volatile` establishes a happens-before order.\n```java\nint x = 1;\n```",
            diagramKey = "jmm_happens_before",
            quizQuestionIds = listOf("sc_1", "fill_1")
        )

        val unit = Unit(
            id = "u1",
            title = "Concurrency & Memory Model",
            description = "Mastering JVM threading, memory visibility, and locks.",
            notes = listOf(note),
            questions = questions
        )

        val pack = Pack(
            id = "java_pack",
            title = "Java Interview Prep Pack",
            version = 1,
            units = listOf(unit)
        )

        // Encode to JSON
        val encodedJson = json.encodeToString(pack)
        assertTrue(encodedJson.contains("\"type\": \"SingleChoice\""))
        assertTrue(encodedJson.contains("\"type\": \"MultiSelect\""))
        assertTrue(encodedJson.contains("\"type\": \"Ordering\""))
        assertTrue(encodedJson.contains("\"type\": \"Matching\""))
        assertTrue(encodedJson.contains("\"type\": \"PredictOutput\""))
        assertTrue(encodedJson.contains("\"type\": \"SpotTheBug\""))
        assertTrue(encodedJson.contains("\"type\": \"FillBlank\""))
        assertTrue(encodedJson.contains("\"type\": \"ShortAnswer\""))

        // Decode from JSON
        val decodedPack = json.decodeFromString<Pack>(encodedJson)
        assertEquals("java_pack", decodedPack.id)
        assertEquals(1, decodedPack.units.size)

        val decodedUnit = decodedPack.units[0]
        assertEquals(1, decodedUnit.notes.size)
        assertEquals(8, decodedUnit.questions.size)

        // Verify polymorphism preservation
        assertTrue(decodedUnit.questions[0] is Question.SingleChoice)
        assertTrue(decodedUnit.questions[1] is Question.MultiSelect)
        assertTrue(decodedUnit.questions[2] is Question.Ordering)
        assertTrue(decodedUnit.questions[3] is Question.Matching)
        assertTrue(decodedUnit.questions[4] is Question.PredictOutput)
        assertTrue(decodedUnit.questions[5] is Question.SpotTheBug)
        assertTrue(decodedUnit.questions[6] is Question.FillBlank)
        assertTrue(decodedUnit.questions[7] is Question.ShortAnswer)

        val fb = decodedUnit.questions[6] as Question.FillBlank
        assertEquals(listOf("volatile"), fb.acceptedAnswers)
    }
}
