package io.github.ducthinh0304.javainterviewprep.core.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseModelTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun devLevel_thresholdsMapCorrectly() {
        assertEquals(DevLevel.SNAPSHOT, DevLevel.fromXp(0))
        assertEquals(DevLevel.SNAPSHOT, DevLevel.fromXp(99))
        assertEquals(DevLevel.ALPHA, DevLevel.fromXp(100))
        assertEquals(DevLevel.BETA, DevLevel.fromXp(300))
        assertEquals(DevLevel.RC, DevLevel.fromXp(700))
        assertEquals(DevLevel.GA, DevLevel.fromXp(1500))
        assertEquals(DevLevel.LTS, DevLevel.fromXp(3000))
        assertEquals(DevLevel.LTS, DevLevel.fromXp(9999))
    }

    @Test
    fun userStats_nextLevelCalculation() {
        val stats = UserStats(xp = 50)
        assertEquals(DevLevel.SNAPSHOT, stats.level)
        assertEquals(50, stats.xpToNextLevel) // 100 - 50 = 50
    }

    @Test
    fun exercise_serializationRoundTrip() {
        val exercise: Exercise = Exercise.MultipleChoice(
            id = "jvm_mem_1",
            topicId = "jvm-memory",
            difficulty = Difficulty.MID,
            prompt = "Where are Java object instances typically allocated?",
            explanation = "Object instances are typically allocated on the Java heap.",
            tags = listOf("jvm", "memory", "heap"),
            options = listOf(
                QuizOption("opt_1", "On the thread stack"),
                QuizOption("opt_2", "In the Java Heap", isCorrect = true),
                QuizOption("opt_3", "In the Program Counter (PC) register"),
                QuizOption("opt_4", "In native off-heap memory only")
            )
        )

        val encoded = json.encodeToString(exercise)
        val decoded = json.decodeFromString<Exercise>(encoded)

        assertTrue(decoded is Exercise.MultipleChoice)
        val mc = decoded as Exercise.MultipleChoice
        assertEquals("jvm_mem_1", mc.id)
        assertEquals(4, mc.options.size)
        assertEquals(true, mc.options[1].isCorrect)
    }
}
