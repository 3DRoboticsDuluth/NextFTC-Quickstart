package org.firstinspires.ftc.threedrd.nextftc.control.interpolators

import dev.nextftc.control.KineticState
import dev.nextftc.control.interpolators.InterpolatorElement
import kotlin.math.sign
import org.firstinspires.ftc.threedrd.nextftc.control.ControlClock

/** Limits velocity change in velocity units per second; position is not profiled. */
class VelocityRamp internal constructor(
    val parameters: VelocityRampParameters,
    seconds: () -> Double,
) : InterpolatorElement {
    constructor(parameters: VelocityRampParameters) : this(parameters, { System.nanoTime() / 1e9 })
    constructor(maxAcceleration: Double) : this(VelocityRampParameters(maxAcceleration))

    private val clock = ControlClock(seconds)
    private var reference = KineticState()

    private var target = KineticState()
    override var goal: KineticState
        get() = target
        set(value) {
            require(value.velocity.isFinite())
            currentReference
            target = value
        }

    override val currentReference: KineticState
        get() {
            val accelerationLimit = parameters.maxAcceleration
            require(accelerationLimit.isFinite() && accelerationLimit > 0.0)
            val dt = clock.elapsed()
            if (dt > 0.0) {
                val change = (goal.velocity - reference.velocity).coerceIn(-accelerationLimit * dt, accelerationLimit * dt)
                val velocity = reference.velocity + change
                val acceleration = if (velocity == goal.velocity) 0.0 else (goal.velocity - velocity).sign * accelerationLimit
                reference = KineticState(velocity = velocity, acceleration = acceleration)
            }
            return reference
        }

    override fun reset() {
        target = KineticState()
        reference = KineticState()
        clock.reset()
    }
}
