package org.firstinspires.ftc.threedrd.nextftc.control

import dev.nextftc.control.KineticState
import dev.nextftc.control.builder.controlSystem
import org.firstinspires.ftc.threedrd.nextftc.control.interpolators.VelocityRamp
import org.firstinspires.ftc.threedrd.nextftc.control.interpolators.VelocitySmoother
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.exp

class VelocityInterpolatorTests {
    @Test
    fun liveTuningPreservesTrajectoriesAndUsesTheNewSettingsGoingForward() {
        var time = 0.0
        val ramp = VelocityRamp(1.0) { time }
        val smoother = VelocitySmoother(1.0) { time }
        ramp.goal = KineticState(velocity = 1.0)
        smoother.goal = ramp.goal
        time = 0.5
        ramp.maxAcceleration = 2.0
        smoother.timeConstant = 0.5
        assertEquals(0.5, ramp.currentReference.velocity, 1e-12)
        assertEquals(1.0 - exp(-0.5), smoother.currentReference.velocity, 1e-12)
        time = 0.75
        assertEquals(1.0, ramp.currentReference.velocity, 1e-12)
        assertEquals(1.0 - exp(-1.0), smoother.currentReference.velocity, 1e-12)
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach {
            assertThrows(IllegalArgumentException::class.java) { ramp.maxAcceleration = it }
            assertThrows(IllegalArgumentException::class.java) { smoother.timeConstant = it }
        }
        assertEquals(2.0, ramp.maxAcceleration, 0.0)
        assertEquals(0.5, smoother.timeConstant, 0.0)
    }

    @Test
    fun exponentialUsesElapsedTimeRatherThanReadCount() {
        var time = 0.0
        val interpolator = VelocitySmoother(0.2) { time }
        assertEquals(0.2, interpolator.timeConstant, 0.0)
        interpolator.goal = KineticState(velocity = 1.0)
        time = 0.2
        val reference = interpolator.currentReference
        assertEquals(1.0 - exp(-1.0), reference.velocity, 1e-12)
        assertEquals(exp(-1.0) / 0.2, reference.acceleration, 1e-12)
        assertEquals(reference, interpolator.currentReference)
        time = 0.4
        assertEquals(1.0 - exp(-2.0), interpolator.currentReference.velocity, 1e-12)

        var otherTime = 0.0
        val other = VelocitySmoother(0.2) { otherTime }
        other.goal = interpolator.goal
        otherTime = 0.4
        assertEquals(interpolator.currentReference, other.currentReference)
    }

    @Test
    fun accelerationLimitHandlesAccelerationBrakingAndReversalWithoutOvershoot() {
        var time = 0.0
        val interpolator = VelocityRamp(2.0) { time }
        assertEquals(2.0, interpolator.maxAcceleration, 0.0)
        interpolator.goal = KineticState(velocity = 1.0)
        time = 0.1
        assertEquals(KineticState(velocity = 0.2, acceleration = 2.0), interpolator.currentReference)
        assertEquals(interpolator.currentReference, interpolator.currentReference)
        time = 1.0
        assertEquals(KineticState(velocity = 1.0), interpolator.currentReference)
        interpolator.goal = KineticState(velocity = -1.0)
        time = 1.25
        assertEquals(KineticState(velocity = 0.5, acceleration = -2.0), interpolator.currentReference)
        time = 3.0
        assertEquals(KineticState(velocity = -1.0), interpolator.currentReference)
        interpolator.goal = KineticState()
        time = 4.0
        assertEquals(KineticState(), interpolator.currentReference)
    }

    @Test
    fun changingGoalsDoesNotApplyNewTargetsToPastTime() {
        var time = 0.0
        val linear = VelocityRamp(1.0) { time }
        val exponential = VelocitySmoother(1.0) { time }
        listOf(linear, exponential).forEach { it.goal = KineticState(velocity = 1.0) }
        time = 0.5
        listOf(linear, exponential).forEach { it.goal = KineticState(velocity = -1.0) }
        assertEquals(0.5, linear.currentReference.velocity, 1e-12)
        assertEquals(1.0 - exp(-0.5), exponential.currentReference.velocity, 1e-12)
        time = 1.0
        assertEquals(0.0, linear.currentReference.velocity, 1e-12)
        assertEquals(-1.0 + (2.0 - exp(-0.5)) * exp(-0.5), exponential.currentReference.velocity, 1e-12)
    }

    @Test
    fun resetClearsGoalHistoryAndIdleTime() {
        var time = 0.0
        val interpolators = listOf(
            VelocityRamp(1.0) { time },
            VelocitySmoother(1.0) { time },
        )
        interpolators.forEach { it.goal = KineticState(velocity = 1.0) }
        time = 1.0
        interpolators.forEach {
            assertTrue(it.currentReference.velocity > 0.0)
            it.reset()
            assertEquals(KineticState(), it.goal)
        }
        time = 100.0
        interpolators.forEach {
            it.goal = KineticState(velocity = 1.0)
            assertEquals(KineticState(), it.currentReference)
        }
        time = 100.1
        interpolators.forEach { assertTrue(it.currentReference.velocity in 0.0..0.11) }
    }

    @Test
    fun integratesWithNextControlAndLeavesFinalGoalIntact() {
        var time = 0.0
        val controller = controlSystem {
            velPid(1.0)
            basicFF(kV = 1.0)
            interpolator(VelocityRamp(1.0) { time })
        }
        controller.goal = KineticState(velocity = 1.0)
        time = 0.25
        assertEquals(0.4, controller.calculate(KineticState(velocity = 0.1)), 1e-12)
        assertEquals(1.0, controller.goal.velocity, 0.0)
        assertEquals(0.25, controller.reference.velocity, 0.0)
        controller.reset()
        assertEquals(KineticState(), controller.goal)
        assertEquals(KineticState(), controller.reference)
    }

    @Test
    fun invalidSettingsAreRejectedAndDefaultClocksWork() {
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach {
            assertThrows(IllegalArgumentException::class.java) { VelocitySmoother(it) }
            assertThrows(IllegalArgumentException::class.java) { VelocityRamp(it) }
        }
        assertEquals(KineticState(), VelocitySmoother(1.0).currentReference)
        assertEquals(KineticState(), VelocityRamp(1.0).currentReference)
        listOf(Double.NaN, Double.POSITIVE_INFINITY).forEach { velocity ->
            listOf(VelocitySmoother(1.0), VelocityRamp(1.0)).forEach {
                assertThrows(IllegalArgumentException::class.java) { it.goal = KineticState(velocity = velocity) }
            }
        }
    }

    @Test
    fun clockDoesNotAdvanceOnSameOrBackwardTimeAndCanReset() {
        var time = 1.0
        val clock = ControlClock { time }
        assertEquals(0.0, clock.elapsed(), 0.0)
        assertEquals(0.0, clock.elapsed(), 0.0)
        time = 0.5
        assertEquals(0.0, clock.elapsed(), 0.0)
        time = 0.75
        assertEquals(0.0, clock.elapsed(), 0.0)
        time = 1.25
        assertEquals(0.25, clock.elapsed(), 0.0)
        clock.reset()
        assertEquals(0.0, clock.elapsed(), 0.0)
    }
}
