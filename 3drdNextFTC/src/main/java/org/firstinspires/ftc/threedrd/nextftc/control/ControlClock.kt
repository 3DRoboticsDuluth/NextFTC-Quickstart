package org.firstinspires.ftc.threedrd.nextftc.control

internal class ControlClock(private val seconds: () -> Double) {
    private var previous: Double? = null

    fun elapsed(): Double {
        val now = seconds()
        val elapsed = previous?.let { (now - it).coerceAtLeast(0.0) } ?: 0.0
        previous = previous?.coerceAtLeast(now) ?: now
        return elapsed
    }

    fun reset() { previous = null }
}
