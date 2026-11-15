package io.github.ducthinh0304.javainterviewprep

import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.AnswerEvaluator
import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.EvaluationResult
import io.github.ducthinh0304.javainterviewprep.core.engine.repository.ContentRepository
import io.github.ducthinh0304.javainterviewprep.core.engine.repository.UserProgressRepository
import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import io.github.ducthinh0304.javainterviewprep.core.model.Pack
import io.github.ducthinh0304.javainterviewprep.core.model.Question
import io.github.ducthinh0304.javainterviewprep.core.model.Unit
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

    private val dummyQuestion = Question.SingleChoice(
        id = "test_ex_1",
        unitId = "topic_1",
        difficulty = Difficulty.EASY,
        prompt = "Sample prompt",
        explanation = "Sample explanation",
        options = listOf("Option A", "Option B"),
        correctIndex = 0
    )

    private val dummyPack = Pack(
        id = "java",
        title = "Java",
        version = 1,
        units = listOf(
            Unit(
                id = "topic_1",
                title = "Topic 1",
                description = "Desc",
                notes = emptyList(),
                questions = listOf(dummyQuestion)
            )
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { contentRepository.getPack(any()) } returns dummyPack
        coEvery { contentRepository.getDailyQuestion(any(), any()) } returns dummyQuestion
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
            answerEvaluator.evaluateSingleChoice(dummyQuestion, 0)
        } returns EvaluationResult(
            isCorrect = true,
            selectedId = "0",
            explanation = "Sample explanation"
        )

        val viewModel = HomeViewModel(contentRepository, userProgressRepository, answerEvaluator)
        testScheduler.advanceUntilIdle()

        viewModel.selectOption(0)
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertEquals(0, currentState.selectedOptionIndex)
        assertEquals(true, currentState.evaluationResult?.isCorrect)
        assertEquals(10, currentState.lastEarnedXp)
    }
}
