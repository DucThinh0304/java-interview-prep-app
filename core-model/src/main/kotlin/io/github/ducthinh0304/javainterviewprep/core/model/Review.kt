package io.github.ducthinh0304.javainterviewprep.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ReviewItem(
    val exerciseId: String,
    val box: Int = 1,
    val nextReviewEpochDay: Long,
    val intervalDays: Int = 1,
    val easeFactor: Double = 2.5,
    val repetitions: Int = 0,
    val lapses: Int = 0
)

enum class ReviewResult {
    AGAIN,
    HARD,
    GOOD,
    EASY
}
