package io.github.ducthinh0304.javainterviewprep.core.engine.repository

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ContentRepositoryTest {

    private val repository = ContentRepository(
        context = ApplicationProvider.getApplicationContext(),
        json = Json { ignoreUnknownKeys = true; isLenient = true }
    )

    @Test
    fun getPack_loadsAndValidatesPackFromAssets() = runTest {
        val pack = repository.getPack("test-fixture")

        assertEquals("test_fixture_pack", pack.id)
        assertEquals(2, pack.allQuestions().size)
    }

    @Test
    fun getPack_cachesInMemory_returnsSameInstanceOnSecondCall() = runTest {
        val first = repository.getPack("test-fixture")
        val second = repository.getPack("test-fixture")

        assertSame(first, second)
    }

    @Test
    fun getDailyQuestion_picksQuestionDeterministicallyByEpochDay() = runTest {
        val pack = repository.getPack("test-fixture")
        val questionCount = pack.allQuestions().size

        val daily = repository.getDailyQuestion(epochDay = 0L, packId = "test-fixture")
        assertEquals(pack.allQuestions()[0L.mod(questionCount.toLong()).toInt()].id, daily?.id)
    }
}
