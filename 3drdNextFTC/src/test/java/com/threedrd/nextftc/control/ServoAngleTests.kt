package com.threedrd.nextftc.control

import dev.nextftc.core.units.*
import kotlin.math.PI
import org.junit.Assert.*
import org.junit.Test

class ServoAngleTests {
    @Test
    fun interiorMeasurementsMapIntoSeparateLiveSafeTravel() {
        val aiming = ServoAngle(0.0, 180.0, 0.27, 0.72)
        for ((angle, physical) in listOf(-28.0 to 0.2, 0.0 to 0.27, 90.0 to 0.495, 180.0 to 0.72, 212.0 to 0.8)) {
            val logical = aiming.calculate(angle.deg, 0.5, 0.2, 0.8)!!
            assertEquals(physical, 0.2 + logical * 0.6, 0.000001)
        }
        assertEquals(0.27, aiming.calculate(0.deg, 0.5)!!, 0.000001)
        assertEquals(0.07 / 0.5, aiming.calculate(0.deg, 0.5, 0.2, 0.7)!!, 0.000001)
        aiming.position1 = 0.3
        assertEquals(0.1 / 0.6, aiming.calculate(0.deg, 0.5, 0.2, 0.8)!!, 0.000001)
        aiming.position2 = 0.75
        assertEquals(0.55 / 0.6, aiming.calculate(180.deg, 0.5, 0.2, 0.8)!!, 0.000001)
        val reversed = ServoAngle(180.0, 0.0, 0.72, 0.27)
        assertEquals(0.07 / 0.6, reversed.calculate(0.deg, 0.5, 0.2, 0.8)!!, 0.000001)
        assertNull(aiming.calculate(90.deg, 0.5, 0.6, 0.8))
    }

    @Test
    fun invalidMeasuredPositionsAndSafeTravelReturnNoTarget() {
        for (value in listOf(-0.1, 1.1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            for (aiming in listOf(ServoAngle(0.0, 90.0, value, 0.75), ServoAngle(0.0, 90.0, 0.25, value))) {
                assertFalse(aiming.configured)
                assertNull(aiming.calculate(0.deg, 0.5))
            }
        }
        assertFalse(ServoAngle(0.0, 90.0, 0.5, 0.5).configured)
        val aiming = ServoAngle(0.0, 90.0)
        for ((min, max) in listOf(-0.1 to 1.0, 1.1 to 1.0, Double.NaN to 1.0,
            0.0 to -0.1, 0.0 to 1.1, 0.0 to Double.NaN, 0.5 to 0.5, 0.8 to 0.2)) {
            assertNull(aiming.calculate(0.deg, 0.5, min, max))
        }
        assertFalse(ServoAngle(0.0, 1.0, 0.0, Double.MIN_VALUE).configured)
        assertNull(ServoAngle(1.7e308, 1.79e308, 0.0, 0.1).calculate(0.deg, 0.5))
        assertNull(ServoAngle(1e300, 2e300).calculate(0.deg, 0.5, 0.5, 0.5000000000000001))
    }

    @Test
    fun mapsMeasuredEndpointsAndMidpointsToLogicalPositionsInEitherDirection() {
        val increasing = ServoAngle(-90.0, 90.0)
        val reversed = ServoAngle(90.0, -90.0)
        for ((angle, expected) in listOf(-90.0 to 0.0, 0.0 to 0.5, 90.0 to 1.0)) {
            assertEquals(expected, increasing.calculate(angle.deg, 0.5)!!, 0.000001)
            assertEquals(1 - expected, reversed.calculate(angle.deg, 0.5)!!, 0.000001)
        }
        assertEquals(1.0, increasing.calculate((PI / 2).rad, 0.5)!!, 0.000001)
    }

    @Test
    fun wrapsBearingsAcrossZeroWithoutAssumingTheShortestAngleIsReachable() {
        val aiming = ServoAngle(300.0, 420.0)
        assertEquals(0.25, aiming.calculate((-30).deg, 0.5)!!, 0.000001)
        assertEquals(0.25, aiming.calculate(690.deg, 0.5)!!, 0.000001)
        assertEquals(0.75, aiming.calculate(30.deg, 0.5)!!, 0.000001)
        assertNull(aiming.calculate(180.deg, 0.5))
    }

    @Test
    fun choosesTheReachableTurnNearestCurrentPositionAndClampsOnlyThatReference() {
        val aiming = ServoAngle(0.0, 720.0)
        assertEquals(0.125, aiming.calculate(90.deg, 0.1)!!, 0.0)
        assertEquals(0.625, aiming.calculate(90.deg, 0.9)!!, 0.0)
        assertEquals(0.125, aiming.calculate(90.deg, -2.0)!!, 0.0)
        assertEquals(0.625, aiming.calculate(90.deg, 2.0)!!, 0.0)
        assertEquals(0.875, ServoAngle(720.0, 0.0).calculate(90.deg, 0.9)!!, 0.0)
    }

    @Test
    fun liveCalibrationIsReadEachCallAndInstancesDoNotShareSettings() {
        val aiming = ServoAngle()
        val other = ServoAngle()
        assertFalse(aiming.configured)
        assertNull(aiming.calculate(0.deg, 0.5))
        aiming.angle1 = -90.0
        aiming.angle2 = 90.0
        assertTrue(aiming.configured)
        assertEquals(0.75, aiming.calculate(45.deg, 0.5)!!, 0.0)
        aiming.angle2 = 180.0
        assertEquals(0.5, aiming.calculate(45.deg, 0.5)!!, 0.0)
        assertFalse(other.configured)
        aiming.angle1 = 180.0
        assertNull(aiming.calculate(45.deg, 0.5))
    }

    @Test
    fun invalidCalibrationAndNonfiniteInputsReturnNoTarget() {
        val invalid = listOf(
            Double.NaN to 90.0, Double.POSITIVE_INFINITY to 90.0,
            -90.0 to Double.NaN, -90.0 to Double.NEGATIVE_INFINITY,
            -Double.MAX_VALUE to Double.MAX_VALUE, 90.0 to 90.0
        )
        for ((min, max) in invalid) {
            val aiming = ServoAngle(min, max)
            assertFalse(aiming.configured)
            assertNull(aiming.calculate(0.deg, 0.5))
        }
        val aiming = ServoAngle(-45.0, 45.0)
        assertNull(aiming.calculate(90.deg, 0.5))
        for (value in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            assertNull(aiming.calculate(value.deg, 0.5))
            assertNull(aiming.calculate(0.deg, value))
        }
    }
}
