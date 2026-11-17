package io.github.ducthinh0304.javainterviewprep.core.engine.gamification

import io.github.ducthinh0304.javainterviewprep.core.model.DevLevel

data class LevelProgress(
    val level: DevLevel,
    val next: DevLevel?,
    /** XP earned since reaching [level]. */
    val xpIntoLevel: Int,
    /** XP between [level] and [next]; null at the top level. */
    val xpForNext: Int?
) {
    val fraction: Float get() = xpForNext?.let { xpIntoLevel.toFloat() / it } ?: 1f
}

/** Thresholds are config so each app can pace its own ladder. */
class LevelLadder(private val minXp: Map<DevLevel, Int>) {

    init {
        require(minXp.keys == DevLevel.entries.toSet()) { "Every DevLevel needs a threshold" }
        require(minXp.getValue(DevLevel.SNAPSHOT) == 0) { "SNAPSHOT must start at 0 XP" }
        require(DevLevel.entries.zipWithNext().all { (a, b) -> minXp.getValue(a) < minXp.getValue(b) }) {
            "Thresholds must strictly increase"
        }
    }

    fun levelFor(xp: Int): DevLevel = DevLevel.entries.last { xp >= minXp.getValue(it) }

    fun progress(xp: Int): LevelProgress {
        val level = levelFor(xp)
        val next = DevLevel.entries.getOrNull(level.ordinal + 1)
        val floor = minXp.getValue(level)
        return LevelProgress(level, next, xp - floor, next?.let { minXp.getValue(it) - floor })
    }

    /** The new level if going from [xpBefore] to [xpAfter] crossed a threshold, else null. */
    fun levelUp(xpBefore: Int, xpAfter: Int): DevLevel? =
        levelFor(xpAfter).takeIf { it > levelFor(xpBefore) }

    companion object {
        val DEFAULT = LevelLadder(
            mapOf(
                DevLevel.SNAPSHOT to 0,
                DevLevel.ALPHA to 100,
                DevLevel.BETA to 300,
                DevLevel.RC to 700,
                DevLevel.GA to 1500,
                DevLevel.LTS to 3000
            )
        )
    }
}
