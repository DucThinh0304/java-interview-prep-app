package io.github.ducthinh0304.javainterviewprep.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.AnswerChecker
import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.CheckResult
import io.github.ducthinh0304.javainterviewprep.core.engine.evaluation.UserAnswer
import io.github.ducthinh0304.javainterviewprep.core.engine.gamification.LevelLadder
import io.github.ducthinh0304.javainterviewprep.core.engine.repository.ContentRepository
import io.github.ducthinh0304.javainterviewprep.core.engine.repository.ProgressRepository
import io.github.ducthinh0304.javainterviewprep.core.engine.repository.StatsRepository
import io.github.ducthinh0304.javainterviewprep.core.engine.repository.SubmitDailyAnswer
import io.github.ducthinh0304.javainterviewprep.core.model.DevLevel
import io.github.ducthinh0304.javainterviewprep.core.model.Note
import io.github.ducthinh0304.javainterviewprep.core.model.Question
import io.github.ducthinh0304.javainterviewprep.core.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val stats: UserStats = UserStats(),
    val level: DevLevel = DevLevel.SNAPSHOT,
    val dailyQuestion: Question? = null,
    val notes: List<Note> = emptyList(),
    val reviewQueueCount: Int = 0,
    val selectedOptionIndex: Int? = null,
    val evaluationResult: CheckResult? = null,
    val lastEarnedXp: Int? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val progressRepository: ProgressRepository,
    private val statsRepository: StatsRepository,
    private val levelLadder: LevelLadder,
    private val submitDailyAnswer: SubmitDailyAnswer,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeProgress()
    }

    private fun loadData() {
        viewModelScope.launch {
            val today = LocalDate.now(clock)
            val pack = contentRepository.getPack()
            val daily = contentRepository.getDailyQuestion(today.toEpochDay())
            // Restore today's answer so a restart can't re-answer (and re-earn XP for) the daily.
            val saved = progressRepository.dailyAnswer(today).first()
                ?.takeIf { daily != null && it.questionId == daily.id }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                dailyQuestion = daily,
                notes = pack.allNotes(),
                selectedOptionIndex = saved?.selectedOptionIndex,
                evaluationResult = if (daily != null && saved != null) evaluate(daily, saved.selectedOptionIndex) else null,
                lastEarnedXp = saved?.xpEarned
            )
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            combine(
                statsRepository.userStatsFlow,
                progressRepository.dueQuestions(Instant.now(clock))
            ) { stats, dueQuestions ->
                Pair(stats, dueQuestions.size)
            }.collect { (stats, queueCount) ->
                _uiState.value = _uiState.value.copy(
                    stats = stats,
                    level = levelLadder.levelFor(stats.xp),
                    reviewQueueCount = queueCount
                )
            }
        }
    }

    fun selectOption(optionIndex: Int) {
        val daily = _uiState.value.dailyQuestion ?: return
        // Lock synchronously, before the DB writes start, so a fast double-tap is a no-op.
        if (_uiState.value.selectedOptionIndex != null) return
        val result = evaluate(daily, optionIndex) ?: return
        _uiState.value = _uiState.value.copy(selectedOptionIndex = optionIndex)

        viewModelScope.launch {
            val earnedXp = submitDailyAnswer(daily, optionIndex, result.isCorrect)

            _uiState.value = _uiState.value.copy(
                evaluationResult = result,
                lastEarnedXp = earnedXp
            )
        }
    }

    private fun evaluate(question: Question, optionIndex: Int): CheckResult? = when (question) {
        is Question.SingleChoice, is Question.PredictOutput -> AnswerChecker.check(question, UserAnswer.Choice(optionIndex))
        else -> null
    }
}
