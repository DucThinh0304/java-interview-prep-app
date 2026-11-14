package io.github.ducthinh0304.javainterviewprep.core.engine.srs

import io.github.ducthinh0304.javainterviewprep.core.model.ReviewItem
import io.github.ducthinh0304.javainterviewprep.core.model.ReviewResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt

@Singleton
class SpacedRepetitionEngine @Inject constructor() {

    fun calculateNextReview(
        currentItem: ReviewItem,
        result: ReviewResult,
        todayEpochDay: Long
    ): ReviewItem {
        return when (result) {
            ReviewResult.AGAIN -> {
                // Failed: resets interval, schedule for next day review
                currentItem.copy(
                    box = 1,
                    nextReviewEpochDay = todayEpochDay + 1,
                    intervalDays = 1,
                    repetitions = 0,
                    lapses = currentItem.lapses + 1,
                    easeFactor = max(1.3, currentItem.easeFactor - 0.2)
                )
            }
            ReviewResult.HARD -> {
                val nextInterval = max(1, (currentItem.intervalDays * 1.2).roundToInt())
                currentItem.copy(
                    nextReviewEpochDay = todayEpochDay + nextInterval,
                    intervalDays = nextInterval,
                    repetitions = currentItem.repetitions + 1,
                    easeFactor = max(1.3, currentItem.easeFactor - 0.15)
                )
            }
            ReviewResult.GOOD -> {
                val nextInterval = when (currentItem.repetitions) {
                    0 -> 1
                    1 -> 3
                    else -> max(4, (currentItem.intervalDays * currentItem.easeFactor).roundToInt())
                }
                currentItem.copy(
                    box = currentItem.box + 1,
                    nextReviewEpochDay = todayEpochDay + nextInterval,
                    intervalDays = nextInterval,
                    repetitions = currentItem.repetitions + 1
                )
            }
            ReviewResult.EASY -> {
                val nextEase = currentItem.easeFactor + 0.15
                val nextInterval = when (currentItem.repetitions) {
                    0 -> 2
                    1 -> 5
                    else -> max(6, (currentItem.intervalDays * nextEase * 1.3).roundToInt())
                }
                currentItem.copy(
                    box = currentItem.box + 2,
                    nextReviewEpochDay = todayEpochDay + nextInterval,
                    intervalDays = nextInterval,
                    repetitions = currentItem.repetitions + 1,
                    easeFactor = nextEase
                )
            }
        }
    }
}
