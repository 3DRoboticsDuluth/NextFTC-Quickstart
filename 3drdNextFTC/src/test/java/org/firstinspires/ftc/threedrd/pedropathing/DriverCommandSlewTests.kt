package org.firstinspires.ftc.threedrd.pedropathing

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt
import org.junit.Assert.*
import org.junit.Test

class DriverCommandSlewTests {
    private fun magnitude(vector: CommandVector) = hypot(vector.forward, vector.strafe)

    @Test
    fun diagonalsShareTheSameVectorBudgetAndTurningIsIndependent() {
        var time = 0.0
        val parameters = DriverSlewParameters()
        val axial = DriverCommandSlew(parameters) { time }
        val diagonal = DriverCommandSlew(parameters) { time }
        val axisTarget = doubleArrayOf(1.0, 0.0, 0.5)
        val diagonalTarget = doubleArrayOf(1.0 / sqrt(2.0), 1.0 / sqrt(2.0), 0.5)
        axial.calculate(axisTarget)
        diagonal.calculate(diagonalTarget)
        assertSame(parameters, axial.parameters)
        for (step in 1..200) {
            time = step / 100.0
            val axis = axial.calculate(axisTarget)
            val both = diagonal.calculate(diagonalTarget)
            assertEquals(axis[0], hypot(both[0], both[1]), 1e-10)
            assertEquals(both[0], both[1], 1e-10)
            assertEquals(axis[2], both[2], 1e-10)
        }
        assertEquals(1.0, axial.translation.forward, 1e-5)
    }

    @Test
    fun vectorSlewAndJerkStayBoundedThroughReleaseAndReversal() {
        var time = 0.0
        val parameters = DriverSlewParameters().apply {
            translation.maxAcceleration = 0.5
            translation.maxJerk = 2.0
        }
        val shaping = DriverCommandSlew(parameters) { time }
        shaping.calculate(doubleArrayOf(1.0, 1.0, 1.0))
        var previous = shaping.translation
        var previousSlew = shaping.translationSlew
        for (step in 1..1600) {
            time = step / 100.0
            val input = when {
                step < 400 -> doubleArrayOf(1.0, 1.0, 1.0)
                step < 800 -> doubleArrayOf(-1.0, -1.0, -1.0)
                else -> doubleArrayOf(0.0, 0.0, 0.0)
            }
            shaping.calculate(input)
            assertTrue(magnitude(shaping.translationSlew) <= 0.5 + 1e-10)
            assertTrue(magnitude(shaping.translationSlew - previousSlew) <= 0.02 + 1e-10)
            assertTrue(magnitude(shaping.translation - previous) <= 0.005 + 1e-10)
            previous = shaping.translation
            previousSlew = shaping.translationSlew
        }
        assertTrue(magnitude(shaping.translation) < 1e-5)
        assertTrue(magnitude(shaping.translationSlew) < 1e-5)
    }

    @Test
    fun liveEditsPreserveStateAndReducedAccelerationIsApproachedGradually() {
        var time = 0.0
        val parameters = DriverSlewParameters()
        val shaping = DriverCommandSlew(parameters) { time }
        val input = doubleArrayOf(1.0, 0.0, 1.0)
        shaping.calculate(input)
        time = 0.05
        shaping.calculate(input)
        val before = shaping.translation
        val slew = shaping.translationSlew
        parameters.translation.maxAcceleration = 0.1
        parameters.translation.maxJerk = 1.0
        parameters.turn.maxAcceleration = 1.0
        parameters.turn.maxJerk = 2.0
        shaping.calculate(input)
        assertEquals(before, shaping.translation)
        assertEquals(slew, shaping.translationSlew)
        time = 0.06
        shaping.calculate(input)
        assertTrue(magnitude(shaping.translationSlew - slew) <= 0.01 + 1e-10)
        assertTrue(magnitude(shaping.translationSlew) > 0.1)
        time = 0.07
        shaping.calculate(input)
    }

    @Test
    fun frameRotationPreservesBothVectorStatesAndDoesNotAffectTurn() {
        var time = 0.0
        val shaping = DriverCommandSlew(DriverSlewParameters()) { time }
        shaping.calculate(doubleArrayOf(1.0, 0.0, 0.5))
        time = 0.05
        val before = shaping.calculate(doubleArrayOf(1.0, 0.0, 0.5))
        val slew = shaping.translationSlew
        shaping.rotateTranslation(Math.PI / 2)
        assertEquals(0.0, shaping.translation.forward, 1e-12)
        assertEquals(before[0], shaping.translation.strafe, 1e-12)
        assertEquals(slew.forward, shaping.translationSlew.strafe, 1e-12)
        val after = shaping.calculate(doubleArrayOf(0.0, 1.0, 0.5))
        assertEquals(before[2], after[2], 0.0)
    }

    @Test
    fun resetClearsHistoryAndLongPausesCannotJumpToFullDemand() {
        var time = 0.0
        val shaping = DriverCommandSlew(DriverSlewParameters()) { time }
        val demand = doubleArrayOf(1.0, 1.0, 1.0)
        shaping.calculate(demand)
        time = 100.0
        val capped = shaping.calculate(demand)
        assertTrue(capped[0] > 0.0 && capped[0] < 0.2)
        assertTrue(capped[2] > 0.0 && capped[2] < 1.0)
        shaping.parameters.translation.maxAcceleration = Double.NaN
        shaping.reset()
        assertEquals(CommandVector(), shaping.translation)
        assertEquals(CommandVector(), shaping.translationSlew)
        shaping.parameters.translation.maxAcceleration = 4.0
        time = 200.0
        assertArrayEquals(doubleArrayOf(0.0, 0.0, 0.0), shaping.calculate(demand), 0.0)
        assertArrayEquals(doubleArrayOf(0.0, 0.0, 0.0), DriverCommandSlew(DriverSlewParameters()).calculate(doubleArrayOf(0.0, 0.0, 0.0)), 0.0)
    }

    @Test
    fun validatesInputsAndBothTranslationLimitsBeforeAdvancingState() {
        val parameters = DriverSlewParameters()
        val shaping = DriverCommandSlew(parameters) { 0.0 }
        for (invalid in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            parameters.translation.maxAcceleration = invalid
            assertThrows(IllegalArgumentException::class.java) { shaping.calculate(doubleArrayOf(0.0, 0.0, 0.0)) }
            parameters.translation.maxAcceleration = 4.0
            parameters.translation.maxJerk = invalid
            assertThrows(IllegalArgumentException::class.java) { shaping.calculate(doubleArrayOf(0.0, 0.0, 0.0)) }
            parameters.translation.maxJerk = 40.0
        }
        for (axis in 0..2) {
            val input = doubleArrayOf(0.0, 0.0, 0.0)
            input[axis] = Double.NaN
            assertThrows(IllegalArgumentException::class.java) { shaping.calculate(input) }
        }
        assertEquals(CommandVector(), shaping.translation)
        assertTrue(abs(CommandVector(1.0, 0.0).limited(0.5).forward - 0.5) < 1e-12)
    }
}
