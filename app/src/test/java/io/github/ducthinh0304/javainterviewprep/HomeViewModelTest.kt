package io.github.ducthinh0304.javainterviewprep

import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.AnswerEvaluator
import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.EvaluationResult
import io.github.ducthinh0304.javainterviewprep.core.engine.db.DailyAnswerEntity
import io.github.ducthinh0304.javainterviewprep.core.engine.repository.ContentRepository
import io.github.ducthinh0304.javainterviewprep.core.engine.repository.ProgressRepository
import io.github.ducthinh0304.javainterviewprep.core.engine.repository.StatsRepository
import io.github.ducthinh0304.javainterviewprep.core.engine.repository.SubmitDailyAnswer
import io.github.ducthinh0304.javainterviewprep.core.model.Difficulty
import io.github.ducthinh0304.javainterviewprep.core.model.Pack
import io.github.ducthinh0304.javainterviewprep.core.model.Question
import io.github.ducthinh0304.javainterviewprep.core.model.Unit
import io.github.ducthinh0304.javainterviewprep.core.model.UserStats
import io.github.ducthinh0304.javainterviewprep.presentation.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertNull
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val contentRepository = mockk<ContentRepository>()
    private val progressRepository = mockk<ProgressRepository>()
    private val statsRepository = mockk<StatsRepository>()
    private val answerEvaluator = mockk<AnswerEvaluator>()
    private val submitDailyAnswer = mockk<SubmitDailyAnswer>()
    private val today = LocalDate.of(2026, 11, 15)
    private val clock = Clock.fixed(Instant.parse("2026-11-15T09:00:00Z"), ZoneOffset.UTC)

    private fun newViewModel() = HomeViewModel(
        contentRepository, progressRepository, statsRepository, answerEvaluator, submitDailyAnswer, clock
    )

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
        every { statsRepository.userStatsFlow } returns flowOf(UserStats(xp = 40))
        every { progressRepository.dueQuestions(Instant.now(clock)) } returns flowOf(emptyList())
        every { progressRepository.dailyAnswer(any()) } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsInitialDataSuccessfully() = runTest(testDispatcher) {
        val viewModel = newViewModel()
        testScheduler.advanceUntilIdle()

        val loadedState = viewModel.uiState.value
        assertNotNull(loadedState.dailyQuestion)
        assertEquals("test_ex_1", loadedState.dailyQuestion?.id)
        assertEquals(40, loadedState.stats.xp)
    }

    @Test
    fun selectOption_submitsEvaluationAndRecordsAttempt() = runTest(testDispatcher) {
        stubSubmission()

        val viewModel = newViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.selectOption(0)
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertEquals(0, currentState.selectedOptionIndex)
        assertEquals(true, currentState.evaluationResult?.isCorrect)
        assertEquals(10, currentState.lastEarnedXp)
    }

    @Test
    fun selectOption_doubleTap_recordsOnlyOnce() = runTest(testDispatcher) {
        stubSubmission()
        val viewModel = newViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.selectOption(0)
        viewModel.selectOption(1) // second tap lands before the first write finishes
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { submitDailyAnswer(any(), any(), any()) }
        assertEquals(0, viewModel.uiState.value.selectedOptionIndex)
    }

    @Test
    fun alreadyAnsweredToday_restoresResultAndBlocksReAnswer() = runTest(testDispatcher) {
        stubSubmission()
        every { progressRepository.dailyAnswer(today) } returns flowOf(
            DailyAnswerEntity(today, "test_ex_1", selectedOptionIndex = 0, wasCorrect = true, xpEarned = 10)
        )
        val viewModel = newViewModel()
        testScheduler.advanceUntilIdle()

        val restored = viewModel.uiState.value
        assertEquals(0, restored.selectedOptionIndex)
        assertEquals(true, restored.evaluationResult?.isCorrect)
        assertEquals(10, restored.lastEarnedXp)

        viewModel.selectOption(0)
        testScheduler.advanceUntilIdle()
        coVerify(exactly = 0) { submitDailyAnswer(any(), any(), any()) }
    }

    @Test
    fun savedAnswerForDifferentQuestion_isIgnored() = runTest(testDispatcher) {
        every { progressRepository.dailyAnswer(any()) } returns flowOf(
            DailyAnswerEntity(today, "other_q", selectedOptionIndex = 1, wasCorrect = false, xpEarned = 0)
        )
        val viewModel = newViewModel()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedOptionIndex)
        assertNull(viewModel.uiState.value.evaluationResult)
    }

    private fun stubSubmission() {
        coEvery { submitDailyAnswer(any(), any(), any()) } returns 10

        every {
            answerEvaluator.evaluateSingleChoice(dummyQuestion, 0)
        } returns EvaluationResult(
            isCorrect = true,
            selectedId = "0",
            explanation = "Sample explanation"
        )
    }
}
