package org.firstinspires.ftc.threedrd.nextftc.control

/** Experimental power-cap feedback, not a hardware-enforced current limit. */
class CurrentLimiter internal constructor(
    val parameters: CurrentLimitParameters,
    seconds: () -> Double,
) {
    constructor(parameters: CurrentLimitParameters) : this(parameters, { System.nanoTime() / 1e9 })
    constructor(maxCurrent: Double, reductionRate: Double = 2.0, recoveryRate: Double = 0.5) :
        this(CurrentLimitParameters(maxCurrent, reductionRate, recoveryRate))

    private val clock = ControlClock(seconds)
    var powerLimit = 1.0
        private set

    /** Supply a fresh current sample in amperes once per motor update. */
    fun limit(power: Double, current: Double): Double {
        require(power.isFinite())
        require(current.isFinite() && current >= 0.0)
        val threshold = parameters.maxCurrent
        val reduction = parameters.reductionRate
        val recovery = parameters.recoveryRate
        require(threshold.isFinite() && threshold > 0.0)
        require(reduction.isFinite() && reduction > 0.0)
        require(recovery.isFinite() && recovery > 0.0)
        val rate = if (current > threshold) -reduction else recovery
        powerLimit = (powerLimit + rate * clock.elapsed()).coerceIn(0.0, 1.0)
        return power.coerceIn(-powerLimit, powerLimit)
    }

    fun reset() {
        powerLimit = 1.0
        clock.reset()
    }
}
