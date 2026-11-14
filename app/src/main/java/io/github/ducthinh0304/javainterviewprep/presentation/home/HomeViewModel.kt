package io.github.ducthinh0304.javainterviewprep.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.AnswerEvaluator
import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.EvaluationResult
import io.github.ducthinh0304.javainterviewprep.core.engine.repository.ContentRepository
import io.github.ducthinh0304.javainterviewprep.core.engine.repository.UserProgressRepository
import io.github.ducthinh0304.javainterviewprep.core.model.Exercise
import io.github.ducthinh0304.javainterviewprep.core.model.Lesson
import io.github.ducthinh0304.javainterviewprep.core.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val stats: UserStats = UserStats(),
    val dailyQuestion: Exercise? = null,
    val lessons: List<Lesson> = emptyList(),
    val reviewQueueCount: Int = 0,
    val selectedOptionId: String? = null,
    val evaluationResult: EvaluationResult? = null,
    val lastEarnedXp: Int? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val userProgressRepository: UserProgressRepository,
    private val answerEvaluator: AnswerEvaluator
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeProgress()
    }

    private fun loadData() {
        viewModelScope.launch {
            val todayEpochDay = LocalDate.now().toEpochDay()
            val pack = contentRepository.getPack("java")
            val daily = contentRepository.getDailyQuestion(todayEpochDay, "java")

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                dailyQuestion = daily,
                lessons = pack.lessons
            )
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            combine(
                userProgressRepository.userStatsFlow,
                userProgressRepository.reviewQueueFlow
            ) { stats, queue ->
                Pair(stats, queue.size)
            }.collect { (stats, queueCount) ->
                _uiState.value = _uiState.value.copy(
                    stats = stats,
                    reviewQueueCount = queueCount
                )
            }
        }
    }

    fun selectOption(optionId: String) {
        val currentDaily = _uiState.value.dailyQuestion as? Exercise.MultipleChoice ?: return
        if (_uiState.value.evaluationResult != null) return // Already submitted

        val result = answerEvaluator.evaluateMultipleChoice(currentDaily, optionId)
        val todayEpochDay = LocalDate.now().toEpochDay()

        viewModelScope.launch {
            val (_, earnedXp) = userProgressRepository.recordAttempt(
                exerciseId = currentDaily.id,
                difficulty = currentDaily.difficulty,
                isCorrect = result.isCorrect,
                todayEpochDay = todayEpochDay
            )

            _uiState.value = _uiState.value.copy(
                selectedOptionId = optionId,
                evaluationResult = result,
                lastEarnedXp = earnedXp
            )
        }
    }
}
