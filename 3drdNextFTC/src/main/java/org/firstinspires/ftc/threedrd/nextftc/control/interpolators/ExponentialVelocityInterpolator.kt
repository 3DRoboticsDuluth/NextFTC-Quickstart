package org.firstinspires.ftc.threedrd.nextftc.control.interpolators

import dev.nextftc.control.KineticState
import dev.nextftc.control.interpolators.InterpolatorElement
import kotlin.math.exp
import org.firstinspires.ftc.threedrd.nextftc.control.ControlClock

/** Velocity smoothing with a time constant in seconds, independent of loop rate. */
class ExponentialVelocityInterpolator(
    val timeConstant: Double,
    seconds: () -> Double = { System.nanoTime() / 1e9 },
) : InterpolatorElement {
    init { require(timeConstant.isFinite() && timeConstant > 0.0) }

    private val clock = ControlClock(seconds)
    private var reference = KineticState()

    override var goal = KineticState()
        set(value) {
            require(value.velocity.isFinite())
            currentReference
            field = value
        }

    override val currentReference: KineticState
        get() {
            val dt = clock.elapsed()
            if (dt > 0.0) {
                val velocity = goal.velocity + (reference.velocity - goal.velocity) * exp(-dt / timeConstant)
                reference = KineticState(velocity = velocity, acceleration = (goal.velocity - velocity) / timeConstant)
            }
            return reference
        }

    override fun reset() {
        goal = KineticState()
        reference = KineticState()
        clock.reset()
    }
}
