package io.github.ducthinh0304.javainterviewprep.core.engine.gamification

import org.junit.Assert.assertEquals
import org.junit.Test

class ComboTrackerTest {

    private fun afterCorrect(n: Int) = (1..n).fold(ComboTracker()) { c, _ -> c.next(true) }

    @Test
    fun startsAtX1() {
        assertEquals(0, ComboTracker().streak)
        assertEquals(1.0, ComboTracker().multiplier, 0.0)
    }

    @Test
    fun multiplierStepsThroughEveryTier() {
        val expected = listOf(1.0, 1.0, 1.0, 1.5, 1.5, 1.5, 2.0, 2.0, 2.0, 2.0, 3.0)
        expected.forEachIndexed { streak, multiplier ->
            assertEquals("streak $streak", multiplier, afterCorrect(streak).multiplier, 0.0)
        }
    }

    @Test
    fun capsAtX3() {
        assertEquals(3.0, afterCorrect(10).multiplier, 0.0)
        assertEquals(3.0, afterCorrect(50).multiplier, 0.0)
    }

    @Test
    fun wrongAnswerResetsFromAnyTier() {
        listOf(0, 2, 3, 6, 10, 25).forEach { n ->
            val reset = afterCorrect(n).next(false)
            assertEquals(0, reset.streak)
            assertEquals(1.0, reset.multiplier, 0.0)
        }
    }

    @Test
    fun climbsAgainAfterReset() {
        val c = afterCorrect(7).next(false).next(true).next(true).next(true)
        assertEquals(3, c.streak)
        assertEquals(1.5, c.multiplier, 0.0)
    }

    @Test
    fun isImmutable() {
        val c = ComboTracker(4)
        c.next(true)
        c.next(false)
        assertEquals(4, c.streak)
    }
}
