package org.firstinspires.ftc.threedrd.nextftc.control

import dev.nextftc.control.KineticState
import dev.nextftc.control.builder.controlSystem
import org.firstinspires.ftc.threedrd.nextftc.control.interpolators.SCurve
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class SCurveTests {
    @Test
    fun trapezoidalAccelerationSpinsUpAndSettlesExactly() {
        var time = 0.0
        val curve = SCurve(2.0, 8.0) { time }
        assertEquals(2.0, curve.maxAcceleration(), 0.0)
        assertEquals(8.0, curve.maxJerk(), 0.0)
        curve.goal = KineticState(velocity = 1.0)
        assertEquals(KineticState(), curve.currentReference)
        time = 0.125
        assertEquals(KineticState(velocity = 0.0625, acceleration = 1.0), curve.currentReference)
        assertEquals(curve.currentReference, curve.currentReference)
        time = 0.375
        assertEquals(KineticState(velocity = 0.5, acceleration = 2.0), curve.currentReference)
        time = 0.625
        assertEquals(KineticState(velocity = 0.9375, acceleration = 1.0), curve.currentReference)
        time = 0.75
        assertEquals(KineticState(velocity = 1.0), curve.currentReference)
        time = 10.0
        assertEquals(KineticState(velocity = 1.0), curve.currentReference)
    }

    @Test
    fun shortMovesUseTriangularAccelerationAndWorkInBothDirections() {
        for (direction in listOf(-1.0, 1.0)) {
            var time = 0.0
            val curve = SCurve(2.0, 8.0) { time }
            curve.goal = KineticState(velocity = direction * 0.125)
            time = 0.125
            assertEquals(direction * 0.0625, curve.currentReference.velocity, 1e-12)
            assertEquals(direction, curve.currentReference.acceleration, 1e-12)
            time = 0.25
            assertEquals(KineticState(velocity = direction * 0.125), curve.currentReference)
        }
    }

    @Test
    fun retargetingPreservesVelocityAndAccelerationIncludingUnavoidableOvershoot() {
        var time = 0.0
        val curve = SCurve(2.0, 8.0) { time }
        curve.goal = KineticState(velocity = 1.0)
        time = 0.125
        val before = curve.currentReference
        curve.goal = KineticState(velocity = -0.2)
        assertEquals(before, curve.currentReference)
        time = 0.25
        assertEquals(0.125, curve.currentReference.velocity, 1e-12)
        assertEquals(0.0, curve.currentReference.acceleration, 1e-12)
        time = 2.0
        assertEquals(KineticState(velocity = -0.2), curve.currentReference)

        curve.goal = KineticState(velocity = 1.0)
        time = 2.125
        val moving = curve.currentReference
        curve.goal = KineticState(velocity = moving.velocity + moving.acceleration * moving.acceleration / 16.0)
        time = 2.25
        assertEquals(curve.goal.velocity, curve.currentReference.velocity, 1e-12)
        assertEquals(0.0, curve.currentReference.acceleration, 1e-12)
    }

    @Test
    fun liveLimitsPreserveStateAndReduceExistingAccelerationWithBoundedJerk() {
        var time = 0.0
        var acceleration = 2.0
        var jerk = 8.0
        var accelerationReads = 0
        var jerkReads = 0
        val curve = SCurve({ accelerationReads++; acceleration }, { jerkReads++; jerk }, { time })
        curve.goal = KineticState(velocity = 2.0)
        time = 0.125
        val before = curve.currentReference
        acceleration = 0.5
        jerk = 4.0
        accelerationReads = 0
        jerkReads = 0
        assertEquals(before, curve.currentReference)
        assertEquals(1, accelerationReads)
        assertEquals(1, jerkReads)
        time = 0.1875
        assertEquals(0.75, curve.currentReference.acceleration, 1e-12)
        time = 0.25
        assertEquals(0.5, curve.currentReference.acceleration, 1e-12)
        time = 10.0
        assertEquals(KineticState(velocity = 2.0), curve.currentReference)
    }

    @Test
    fun repeatedGoalAssignmentsAndIrregularReadsDoNotChangeTheTrajectory() {
        var time = 0.0
        val frequent = SCurve(2.0, 8.0) { time }
        val sparse = SCurve(2.0, 8.0) { time }
        frequent.goal = KineticState(velocity = 1.0)
        sparse.goal = frequent.goal
        var previousAcceleration = 0.0
        for (step in 1..600) {
            time = step / 1000.0
            frequent.goal = KineticState(velocity = 1.0)
            val state = frequent.currentReference
            assertTrue(abs(state.acceleration) <= 2.0 + 1e-10)
            assertTrue(abs(state.acceleration - previousAcceleration) <= 0.008 + 1e-10)
            assertTrue(state.velocity in 0.0..1.0)
            previousAcceleration = state.acceleration
        }
        assertEquals(sparse.currentReference.velocity, frequent.currentReference.velocity, 1e-10)
        assertEquals(sparse.currentReference.acceleration, frequent.currentReference.acceleration, 1e-10)
    }

    @Test
    fun resetWorksWithInvalidSuppliersAndDiscardsIdleTime() {
        var time = 0.0
        var acceleration = 2.0
        var jerk = 8.0
        val curve = SCurve({ acceleration }, { jerk }, { time })
        curve.goal = KineticState(velocity = 1.0)
        time = 0.1
        assertTrue(curve.currentReference.velocity > 0.0)
        for (invalid in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            acceleration = invalid
            assertThrows(IllegalArgumentException::class.java) { curve.currentReference }
            acceleration = 2.0
            jerk = invalid
            assertThrows(IllegalArgumentException::class.java) { curve.currentReference }
            jerk = 8.0
            assertThrows(IllegalArgumentException::class.java) { SCurve(invalid, 8.0) }
            assertThrows(IllegalArgumentException::class.java) { SCurve(2.0, invalid) }
        }
        assertThrows(IllegalArgumentException::class.java) { curve.goal = KineticState(velocity = Double.NaN) }
        acceleration = Double.NaN
        jerk = Double.NaN
        curve.reset()
        assertEquals(KineticState(), curve.goal)
        acceleration = 2.0
        jerk = 8.0
        time = 100.0
        curve.goal = KineticState(velocity = 1.0)
        assertEquals(KineticState(), curve.currentReference)
        assertEquals(KineticState(), SCurve(2.0, 8.0).currentReference)
        assertEquals(KineticState(), SCurve({ 2.0 }, { 8.0 }).currentReference)
    }

    @Test
    fun integratesWithNextControlWithoutChangingTheFinalGoal() {
        var time = 0.0
        val controller = controlSystem {
            velPid(1.0)
            basicFF(kV = 1.0)
            interpolator(SCurve(2.0, 8.0) { time })
        }
        controller.goal = KineticState(velocity = 1.0)
        time = 0.125
        assertEquals(0.125, controller.calculate(), 1e-12)
        assertEquals(1.0, controller.goal.velocity, 0.0)
        controller.reset()
        assertEquals(KineticState(), controller.reference)
    }

    @Test
    fun subnormalProfileArithmeticSettlesWithoutDivisionByZero() {
        var time = 0.0
        val curve = SCurve(1.0, 1e-300) { time }
        curve.goal = KineticState(velocity = 1e-300)
        time = 1.0
        assertEquals(KineticState(velocity = 1e-300), curve.currentReference)
    }
}
