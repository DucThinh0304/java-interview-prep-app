package io.github.ducthinh0304.javainterviewprep.core.engine.gamification

import io.github.ducthinh0304.javainterviewprep.core.model.DevLevel
import io.github.ducthinh0304.javainterviewprep.core.model.DevLevel.ALPHA
import io.github.ducthinh0304.javainterviewprep.core.model.DevLevel.BETA
import io.github.ducthinh0304.javainterviewprep.core.model.DevLevel.GA
import io.github.ducthinh0304.javainterviewprep.core.model.DevLevel.LTS
import io.github.ducthinh0304.javainterviewprep.core.model.DevLevel.RC
import io.github.ducthinh0304.javainterviewprep.core.model.DevLevel.SNAPSHOT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LevelLadderTest {

    private val ladder = LevelLadder(mapOf(SNAPSHOT to 0, ALPHA to 10, BETA to 20, RC to 40, GA to 80, LTS to 160))

    @Test
    fun levelFor_usesConfiguredThresholds() {
        assertEquals(SNAPSHOT, ladder.levelFor(0))
        assertEquals(SNAPSHOT, ladder.levelFor(9))
        assertEquals(ALPHA, ladder.levelFor(10))
        assertEquals(BETA, ladder.levelFor(39))
        assertEquals(RC, ladder.levelFor(40))
        assertEquals(GA, ladder.levelFor(159))
        assertEquals(LTS, ladder.levelFor(160))
        assertEquals(LTS, ladder.levelFor(100_000))
    }

    @Test
    fun progress_towardsNextLevel() {
        val p = ladder.progress(25) // BETA 20..40
        assertEquals(LevelProgress(BETA, RC, xpIntoLevel = 5, xpForNext = 20), p)
        assertEquals(0.25f, p.fraction, 0f)
    }

    @Test
    fun progress_atTopLevelIsFull() {
        val p = ladder.progress(500)
        assertEquals(LTS, p.level)
        assertNull(p.next)
        assertNull(p.xpForNext)
        assertEquals(1f, p.fraction, 0f)
    }

    @Test
    fun levelUp_firesOnlyWhenCrossingAThreshold() {
        assertNull(ladder.levelUp(0, 9))
        assertEquals(ALPHA, ladder.levelUp(9, 10))
        assertNull(ladder.levelUp(10, 19))
        assertEquals(RC, ladder.levelUp(15, 45)) // skipping BETA reports the level reached
        assertNull(ladder.levelUp(200, 300))
    }

    @Test
    fun default_matchesTheOriginalCurve() {
        assertEquals(listOf(0, 100, 300, 700, 1500, 3000), DevLevel.entries.map { lvl ->
            (0..5000).first { LevelLadder.DEFAULT.levelFor(it) == lvl }
        })
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMissingLevel() {
        LevelLadder(mapOf(SNAPSHOT to 0, ALPHA to 10))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonZeroStart() {
        LevelLadder(mapOf(SNAPSHOT to 5, ALPHA to 10, BETA to 20, RC to 40, GA to 80, LTS to 160))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonIncreasingThresholds() {
        LevelLadder(mapOf(SNAPSHOT to 0, ALPHA to 20, BETA to 20, RC to 40, GA to 80, LTS to 160))
    }
}
