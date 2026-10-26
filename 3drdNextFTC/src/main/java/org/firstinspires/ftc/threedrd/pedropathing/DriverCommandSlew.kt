package org.firstinspires.ftc.threedrd.pedropathing

import dev.nextftc.control.KineticState
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import org.firstinspires.ftc.threedrd.nextftc.control.ControlClock
import org.firstinspires.ftc.threedrd.nextftc.control.interpolators.SCurve

internal data class CommandVector(val forward: Double = 0.0, val strafe: Double = 0.0) {
    operator fun plus(other: CommandVector) = CommandVector(forward + other.forward, strafe + other.strafe)
    operator fun minus(other: CommandVector) = CommandVector(forward - other.forward, strafe - other.strafe)
    operator fun times(scale: Double) = CommandVector(forward * scale, strafe * scale)
    fun limited(limit: Double): CommandVector {
        val magnitude = hypot(forward, strafe)
        return if (magnitude > limit) this * (limit / magnitude) else this
    }
    fun rotated(angle: Double) = CommandVector(
        forward * cos(angle) - strafe * sin(angle),
        forward * sin(angle) + strafe * cos(angle),
    )
}

internal class DriverCommandSlew(
    val parameters: DriverSlewParameters,
    seconds: () -> Double = { System.nanoTime() / 1e9 },
) {
    private val clock = ControlClock(seconds)
    private var profileSeconds = 0.0
    private val turnProfile = SCurve(parameters.turn) { profileSeconds }
    var translation = CommandVector()
        private set
    var translationSlew = CommandVector()
        private set

    fun calculate(powers: DoubleArray): DoubleArray {
        require(powers.all { it.isFinite() })
        val acceleration = parameters.translation.maxAcceleration
        val jerk = parameters.translation.maxJerk
        require(acceleration.isFinite() && acceleration > 0.0)
        require(jerk.isFinite() && jerk > 0.0)
        turnProfile.goal = KineticState(velocity = powers[2])
        // A stalled loop must not catch up with a large jump on its next driver update.
        var remaining = clock.elapsed().coerceAtMost(0.1)
        profileSeconds += remaining
        val target = CommandVector(powers[0], powers[1])
        val frequency = min(jerk / acceleration, 100.0)
        val maxStep = min(0.005, 0.05 / frequency)
        while (remaining > 1e-12) {
            val dt = min(remaining, maxStep)
            // Critically damped tracking, with vector balls bounding slew and its change.
            val requestedJerk = ((target - translation) * (frequency * frequency) -
                translationSlew * (2.0 * frequency)).limited(jerk)
            val boundedSlew = (translationSlew + requestedJerk * dt).limited(acceleration)
            val nextSlew = translationSlew + (boundedSlew - translationSlew).limited(jerk * dt)
            translation += (translationSlew + nextSlew) * (dt / 2.0)
            translationSlew = nextSlew
            remaining -= dt
        }
        return doubleArrayOf(translation.forward, translation.strafe, turnProfile.currentReference.velocity)
    }

    fun rotateTranslation(angle: Double) {
        translation = translation.rotated(angle)
        translationSlew = translationSlew.rotated(angle)
    }

    fun reset() {
        translation = CommandVector()
        translationSlew = CommandVector()
        profileSeconds = 0.0
        turnProfile.reset()
        clock.reset()
    }
}
