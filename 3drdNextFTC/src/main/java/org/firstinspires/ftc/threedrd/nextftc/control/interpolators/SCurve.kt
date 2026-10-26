package org.firstinspires.ftc.threedrd.nextftc.control.interpolators

import dev.nextftc.control.KineticState
import dev.nextftc.control.interpolators.InterpolatorElement
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt
import org.firstinspires.ftc.threedrd.nextftc.control.ControlClock

/** Velocity profile with bounded acceleration and jerk, ending at zero acceleration. */
class SCurve(
    val maxAcceleration: () -> Double,
    val maxJerk: () -> Double,
    seconds: () -> Double = { System.nanoTime() / 1e9 },
) : InterpolatorElement {
    constructor(maxAcceleration: Double, maxJerk: Double, seconds: () -> Double = { System.nanoTime() / 1e9 }) :
        this({ maxAcceleration }, { maxJerk }, seconds) {
        require(maxAcceleration.isFinite() && maxAcceleration > 0.0)
        require(maxJerk.isFinite() && maxJerk > 0.0)
    }

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
            val acceleration = maxAcceleration()
            val jerk = maxJerk()
            require(acceleration.isFinite() && acceleration > 0.0)
            require(jerk.isFinite() && jerk > 0.0)
            val dt = clock.elapsed()
            if (dt > 0.0) reference = advance(dt, acceleration, jerk)
            return reference
        }

    private class Phase(val jerk: Double, val duration: Double)

    private fun advance(dt: Double, accelerationLimit: Double, jerkLimit: Double): KineticState {
        val phases = mutableListOf<Phase>()
        var velocity = reference.velocity
        var acceleration = reference.acceleration

        // A lowered live limit cannot erase existing acceleration instantaneously.
        if (abs(acceleration) > accelerationLimit) {
            val duration = (abs(acceleration) - accelerationLimit) / jerkLimit
            val jerk = -acceleration.sign * jerkLimit
            phases += Phase(jerk, duration)
            velocity += acceleration * duration + jerk * duration * duration / 2.0
            acceleration = acceleration.sign * accelerationLimit
        }

        val error = goal.velocity - velocity
        val stoppingChange = acceleration * abs(acceleration) / (2.0 * jerkLimit)
        val direction = (error - stoppingChange).sign

        if (direction == 0.0) {
            phases += Phase(-acceleration.sign * jerkLimit, abs(acceleration) / jerkLimit)
        } else {
            // Area under the acceleration curve must equal the remaining velocity change.
            val peak = sqrt((jerkLimit * direction * error + acceleration * acceleration / 2.0)
                .coerceAtLeast(0.0)).coerceAtMost(accelerationLimit)
            val rampChange = (peak * peak - acceleration * acceleration / 2.0) / jerkLimit
            val hold = if (peak > 0.0) ((direction * error - rampChange) / peak).coerceAtLeast(0.0) else 0.0
            phases += Phase(direction * jerkLimit, ((peak - direction * acceleration) / jerkLimit).coerceAtLeast(0.0))
            phases += Phase(0.0, hold)
            phases += Phase(-direction * jerkLimit, peak / jerkLimit)
        }

        // Integrate each constant-jerk phase exactly, even when one update crosses phases.
        var remaining = dt
        velocity = reference.velocity
        acceleration = reference.acceleration
        for (phase in phases) {
            val elapsed = remaining.coerceAtMost(phase.duration)
            velocity += acceleration * elapsed + phase.jerk * elapsed * elapsed / 2.0
            acceleration += phase.jerk * elapsed
            if (remaining < phase.duration) return KineticState(velocity = velocity, acceleration = acceleration)
            remaining -= phase.duration
        }
        return KineticState(velocity = goal.velocity)
    }

    override fun reset() {
        target = KineticState()
        reference = KineticState()
        clock.reset()
    }
}
