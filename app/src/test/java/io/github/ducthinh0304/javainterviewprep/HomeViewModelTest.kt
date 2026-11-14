package io.github.ducthinh0304.javainterviewprep

import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.AnswerEvaluator
import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.EvaluationResult
import io.github.ducthinh0304.javainterviewprep.core.engine.repository.ContentRepository
import io.github.ducthinh0304.javainterviewprep.core.engine.repository.UserProgressRepository
import io.github.ducthinh0304.javainterviewprep.core.model.ContentPack
import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import io.github.ducthinh0304.javainterviewprep.core.model.Exercise
import io.github.ducthinh0304.javainterviewprep.core.model.QuizOption
import io.github.ducthinh0304.javainterviewprep.core.model.UserStats
import io.github.ducthinh0304.javainterviewprep.presentation.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val contentRepository = mockk<ContentRepository>()
    private val userProgressRepository = mockk<UserProgressRepository>()
    private val answerEvaluator = mockk<AnswerEvaluator>()

    private val dummyExercise = Exercise.MultipleChoice(
        id = "test_ex_1",
        topicId = "topic_1",
        difficulty = Difficulty.JUNIOR,
        prompt = "Sample prompt",
        explanation = "Sample explanation",
        options = listOf(
            QuizOption("opt_1", "Option A", isCorrect = true),
            QuizOption("opt_2", "Option B", isCorrect = false)
        )
    )

    private val dummyPack = ContentPack(
        id = "java",
        title = "Java",
        version = 1,
        description = "Test pack",
        questions = listOf(dummyExercise),
        lessons = emptyList()
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { contentRepository.getPack(any()) } returns dummyPack
        coEvery { contentRepository.getDailyQuestion(any(), any()) } returns dummyExercise
        every { userProgressRepository.userStatsFlow } returns flowOf(UserStats(xp = 40))
        every { userProgressRepository.reviewQueueFlow } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsInitialDataSuccessfully() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(contentRepository, userProgressRepository, answerEvaluator)
        testScheduler.advanceUntilIdle()

        val loadedState = viewModel.uiState.value
        assertNotNull(loadedState.dailyQuestion)
        assertEquals("test_ex_1", loadedState.dailyQuestion?.id)
        assertEquals(40, loadedState.stats.xp)
    }

    @Test
    fun selectOption_submitsEvaluationAndRecordsAttempt() = runTest(testDispatcher) {
        coEvery {
            userProgressRepository.recordAttempt(any(), any(), any(), any())
        } returns Pair(UserStats(xp = 50), 10)

        every {
            answerEvaluator.evaluateMultipleChoice(dummyExercise, "opt_1")
        } returns EvaluationResult(
            isCorrect = true,
            selectedId = "opt_1",
            explanation = "Sample explanation"
        )

        val viewModel = HomeViewModel(contentRepository, userProgressRepository, answerEvaluator)
        testScheduler.advanceUntilIdle()

        viewModel.selectOption("opt_1")
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertEquals("opt_1", currentState.selectedOptionId)
        assertEquals(true, currentState.evaluationResult?.isCorrect)
        assertEquals(10, currentState.lastEarnedXp)
    }
}
