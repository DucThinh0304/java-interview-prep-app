package io.github.ducthinh0304.javainterviewprep.core.engine.srs

import io.github.ducthinh0304.javainterviewprep.core.model.ReviewItem
import io.github.ducthinh0304.javainterviewprep.core.model.ReviewResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpacedRepetitionEngineTest {

    private val engine = SpacedRepetitionEngine()

    @Test
    fun againResult_resetsIntervalAndIncrementsLapses() {
        val current = ReviewItem(
            exerciseId = "ex_1",
            box = 3,
            nextReviewEpochDay = 20000L,
            intervalDays = 6,
            repetitions = 2,
            lapses = 0
        )

        val updated = engine.calculateNextReview(current, ReviewResult.AGAIN, todayEpochDay = 20000L)

        assertEquals(1, updated.box)
        assertEquals(20001L, updated.nextReviewEpochDay)
        assertEquals(1, updated.intervalDays)
        assertEquals(0, updated.repetitions)
        assertEquals(1, updated.lapses)
    }

    @Test
    fun goodResult_expandsInterval() {
        val current = ReviewItem(
            exerciseId = "ex_2",
            box = 1,
            nextReviewEpochDay = 20000L,
            intervalDays = 1,
            repetitions = 0,
            easeFactor = 2.5
        )

        val updated1 = engine.calculateNextReview(current, ReviewResult.GOOD, todayEpochDay = 20000L)
        assertEquals(2, updated1.box)
        assertEquals(1, updated1.repetitions)
        assertEquals(20001L, updated1.nextReviewEpochDay) // 0 repetitions -> interval 1

        val updated2 = engine.calculateNextReview(updated1, ReviewResult.GOOD, todayEpochDay = 20001L)
        assertEquals(3, updated2.box)
        assertEquals(2, updated2.repetitions)
        assertEquals(3, updated2.intervalDays) // 1 repetition -> interval 3
        assertEquals(20004L, updated2.nextReviewEpochDay)
    }
}
