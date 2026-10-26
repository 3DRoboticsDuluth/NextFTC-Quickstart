package org.firstinspires.ftc.threedrd.nextftc.control.interpolators

import dev.nextftc.control.KineticState
import dev.nextftc.control.interpolators.InterpolatorElement
import kotlin.math.exp
import org.firstinspires.ftc.threedrd.nextftc.control.ControlClock

/** Velocity smoothing with a time constant in seconds, independent of loop rate. */
class VelocitySmoother internal constructor(
    val parameters: VelocitySmootherParameters,
    seconds: () -> Double,
) : InterpolatorElement {
    constructor(parameters: VelocitySmootherParameters) : this(parameters, { System.nanoTime() / 1e9 })
    constructor(timeConstant: Double) : this(VelocitySmootherParameters(timeConstant))

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
            val tau = parameters.timeConstant
            require(tau.isFinite() && tau > 0.0)
            val dt = clock.elapsed()
            if (dt > 0.0) {
                val velocity = goal.velocity + (reference.velocity - goal.velocity) * exp(-dt / tau)
                reference = KineticState(velocity = velocity, acceleration = (goal.velocity - velocity) / tau)
            }
            return reference
        }

    override fun reset() {
        target = KineticState()
        reference = KineticState()
        clock.reset()
    }
}
