package io.github.ducthinh0304.javainterviewprep.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ducthinh0304.javainterviewprep.core.engine.theme.AppTheme
import io.github.ducthinh0304.javainterviewprep.core.engine.theme.components.DevBadge
import io.github.ducthinh0304.javainterviewprep.core.engine.theme.components.DevCard
import io.github.ducthinh0304.javainterviewprep.core.engine.theme.components.DevCodeBlock
import io.github.ducthinh0304.javainterviewprep.core.model.Exercise

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            item {
                Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                HeaderSection(state = state)
            }

            item {
                DailyQuestionSection(
                    state = state,
                    onOptionSelected = viewModel::selectOption
                )
            }

            item {
                MiniLessonsSection(state = state)
            }

            item {
                ReRunQueueSection(state = state)
                Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
            }
        }
    }
}

@Composable
private fun HeaderSection(state: HomeUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppTheme.copyTone.appName,
                style = AppTheme.typography.headline,
                color = AppTheme.colors.textPrimary
            )
            DevBadge(
                text = "[${state.stats.level.badgeLabel}]",
                backgroundColor = AppTheme.colors.surfaceElevated,
                textColor = AppTheme.colors.accent,
                borderColor = AppTheme.colors.accent
            )
        }

        // Stats strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            DevCard(
                modifier = Modifier.weight(1f),
                backgroundColor = AppTheme.colors.surface
            ) {
                Column {
                    Text(
                        text = "XP",
                        style = AppTheme.typography.badge,
                        color = AppTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))
                    Text(
                        text = "${state.stats.xp}",
                        style = AppTheme.typography.title,
                        color = AppTheme.colors.textPrimary
                    )
                }
            }

            DevCard(
                modifier = Modifier.weight(1f),
                backgroundColor = AppTheme.colors.surface
            ) {
                Column {
                    Text(
                        text = "COMBO",
                        style = AppTheme.typography.badge,
                        color = AppTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))
                    Text(
                        text = "${state.stats.comboMultiplier}x",
                        style = AppTheme.typography.title,
                        color = if (state.stats.consecutiveCorrectAnswers > 1) AppTheme.colors.accent else AppTheme.colors.textPrimary
                    )
                }
            }

            DevCard(
                modifier = Modifier.weight(1f),
                backgroundColor = AppTheme.colors.surface
            ) {
                Column {
                    Text(
                        text = AppTheme.copyTone.streakLabel,
                        style = AppTheme.typography.badge,
                        color = AppTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))
                    Text(
                        text = "${state.stats.currentStreak}d",
                        style = AppTheme.typography.title,
                        color = AppTheme.colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyQuestionSection(
    state: HomeUiState,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppTheme.copyTone.dailyHeader,
                style = AppTheme.typography.badge,
                color = AppTheme.colors.textSecondary
            )
            state.dailyQuestion?.let {
                DevBadge(text = it.difficulty.name)
            }
        }

        when (val question = state.dailyQuestion) {
            is Exercise.MultipleChoice -> {
                DevCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                        Text(
                            text = question.prompt,
                            style = AppTheme.typography.body,
                            color = AppTheme.colors.textPrimary
                        )

                        question.codeSnippet?.let { snippet ->
                            DevCodeBlock(
                                code = snippet,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

                        // Options
                        question.options.forEach { option ->
                            val isSelected = state.selectedOptionId == option.id
                            val isEvaluated = state.evaluationResult != null

                            val borderColor = when {
                                !isEvaluated && isSelected -> AppTheme.colors.borderFocus
                                isEvaluated && option.isCorrect -> AppTheme.colors.accent
                                isEvaluated && isSelected && !option.isCorrect -> AppTheme.colors.error
                                else -> AppTheme.colors.borderHairline
                            }

                            val bgColor = when {
                                isEvaluated && option.isCorrect -> AppTheme.colors.surfaceElevated
                                isEvaluated && isSelected && !option.isCorrect -> AppTheme.colors.surfaceElevated
                                else -> AppTheme.colors.surface
                            }

                            DevCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = bgColor,
                                borderColor = borderColor,
                                onClick = if (!isEvaluated) { { onOptionSelected(option.id) } } else null
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option.text,
                                        style = AppTheme.typography.bodySmall,
                                        color = AppTheme.colors.textPrimary
                                    )
                                }
                            }
                        }

                        // Explanation after answer
                        state.evaluationResult?.let { result ->
                            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                            DevCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = AppTheme.colors.surfaceElevated,
                                borderColor = if (result.isCorrect) AppTheme.colors.accent else AppTheme.colors.warning
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                                    Text(
                                        text = if (result.isCorrect) "CORRECT (+${state.lastEarnedXp ?: 0} XP)" else "INCORRECT (Sent to Re-run)",
                                        style = AppTheme.typography.badge,
                                        color = if (result.isCorrect) AppTheme.colors.accent else AppTheme.colors.warning
                                    )
                                    Text(
                                        text = result.explanation,
                                        style = AppTheme.typography.bodySmall,
                                        color = AppTheme.colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            null -> {
                DevCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Loading daily drill...",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textMuted
                    )
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun MiniLessonsSection(state: HomeUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Text(
            text = "MINI LESSONS",
            style = AppTheme.typography.badge,
            color = AppTheme.colors.textSecondary
        )

        state.lessons.forEach { lesson ->
            DevCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lesson.title,
                            style = AppTheme.typography.title,
                            color = AppTheme.colors.textPrimary
                        )
                        DevBadge(text = "${lesson.durationMinutes}m")
                    }
                    Text(
                        text = lesson.contentMarkdown,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ReRunQueueSection(state: HomeUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppTheme.copyTone.reRunHeader,
                style = AppTheme.typography.badge,
                color = AppTheme.colors.textSecondary
            )
            DevBadge(text = "${state.reviewQueueCount} queued")
        }

        DevCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (state.reviewQueueCount == 0) {
                    "Queue is empty. Missed questions will automatically appear here for spaced repetition."
                } else {
                    "${state.reviewQueueCount} questions scheduled for review."
                },
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary
            )
        }
    }
}
