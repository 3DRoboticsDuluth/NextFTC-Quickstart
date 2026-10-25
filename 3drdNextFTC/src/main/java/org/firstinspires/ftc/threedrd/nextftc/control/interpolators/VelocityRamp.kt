package org.firstinspires.ftc.threedrd.nextftc.control.interpolators

import dev.nextftc.control.KineticState
import dev.nextftc.control.interpolators.InterpolatorElement
import kotlin.math.sign
import org.firstinspires.ftc.threedrd.nextftc.control.ControlClock

/** Limits velocity change in velocity units per second; position is not profiled. */
class VelocityRamp(
    maxAcceleration: Double,
    seconds: () -> Double = { System.nanoTime() / 1e9 },
) : InterpolatorElement {
    init { require(maxAcceleration.isFinite() && maxAcceleration > 0.0) }

    private val clock = ControlClock(seconds)
    private var reference = KineticState()

    var maxAcceleration = maxAcceleration
        set(value) {
            require(value.isFinite() && value > 0.0)
            currentReference
            field = value
        }

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
                val change = (goal.velocity - reference.velocity).coerceIn(-maxAcceleration * dt, maxAcceleration * dt)
                val velocity = reference.velocity + change
                val acceleration = if (velocity == goal.velocity) 0.0 else (goal.velocity - velocity).sign * maxAcceleration
                reference = KineticState(velocity = velocity, acceleration = acceleration)
            }
            return reference
        }

    override fun reset() {
        goal = KineticState()
        reference = KineticState()
        clock.reset()
    }
}
