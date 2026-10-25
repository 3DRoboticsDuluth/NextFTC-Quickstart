package org.firstinspires.ftc.threedrd.nextftc.control

/** Experimental power-cap feedback, not a hardware-enforced current limit. */
class CurrentLimiter(
    val maxCurrent: Double,
    val reductionRate: Double = 2.0,
    val recoveryRate: Double = 0.5,
    seconds: () -> Double = { System.nanoTime() / 1e9 },
) {
    init {
        require(maxCurrent.isFinite() && maxCurrent > 0.0)
        require(reductionRate.isFinite() && reductionRate > 0.0)
        require(recoveryRate.isFinite() && recoveryRate > 0.0)
    }

    private val clock = ControlClock(seconds)
    var powerLimit = 1.0
        private set

    /** Supply a fresh current sample in amperes once per motor update. */
    fun limit(power: Double, current: Double): Double {
        require(power.isFinite())
        require(current.isFinite() && current >= 0.0)
        val rate = if (current > maxCurrent) -reductionRate else recoveryRate
        powerLimit = (powerLimit + rate * clock.elapsed()).coerceIn(0.0, 1.0)
        return power.coerceIn(-powerLimit, powerLimit)
    }

    fun reset() {
        powerLimit = 1.0
        clock.reset()
    }
}
