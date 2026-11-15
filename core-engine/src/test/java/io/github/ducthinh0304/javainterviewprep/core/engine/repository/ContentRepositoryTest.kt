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
        assertEquals(3, pack.allQuestions().size)
    }

    @Test
    fun getPack_cachesInMemory_returnsSameInstanceOnSecondCall() = runTest {
        val first = repository.getPack("test-fixture")
        val second = repository.getPack("test-fixture")

        assertSame(first, second)
    }

    @Test
    fun getDailyQuestion_rotatesOnlyThroughAnswerableKinds() = runTest {
        // Fixture: 2 SingleChoice + 1 FillBlank. Day 2 would land on the FillBlank without the filter.
        val picks = (0L..3L).map { repository.getDailyQuestion(epochDay = it, packId = "test-fixture")?.id }

        assertEquals(listOf("q_fixture_1", "q_fixture_2", "q_fixture_1", "q_fixture_2"), picks)
    }
}
