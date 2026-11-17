package io.github.ducthinh0304.javainterviewprep.core.engine.gamification

/** Immutable combo state: [next] returns the state after one more answer. */
data class ComboTracker(val streak: Int = 0) {

    val multiplier: Double
        get() = TIERS.last { streak >= it.first }.second

    fun next(isCorrect: Boolean): ComboTracker = ComboTracker(if (isCorrect) streak + 1 else 0)

    companion object {
        /** consecutive correct answers needed -> multiplier. */
        val TIERS = listOf(0 to 1.0, 3 to 1.5, 6 to 2.0, 10 to 3.0)
    }
}
