package io.github.ducthinh0304.javainterviewprep.core.engine.testutil

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class MutableClock(
    private var currentInstant: Instant,
    private val zone: ZoneId = ZoneId.of("UTC")
) : Clock() {

    fun advanceByDays(days: Long) {
        currentInstant = currentInstant.plusSeconds(days * 86_400)
    }

    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock = MutableClock(currentInstant, zone)

    override fun instant(): Instant = currentInstant
}
