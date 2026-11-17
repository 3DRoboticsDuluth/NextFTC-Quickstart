package com.threedrd.nextftc.control

import dev.nextftc.core.units.*
import kotlin.math.PI
import org.junit.Assert.*
import org.junit.Test

class ServoAngleTests {
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
        aiming.minAngle = -90.0
        aiming.maxAngle = 90.0
        assertTrue(aiming.configured)
        assertEquals(0.75, aiming.calculate(45.deg, 0.5)!!, 0.0)
        aiming.maxAngle = 180.0
        assertEquals(0.5, aiming.calculate(45.deg, 0.5)!!, 0.0)
        assertFalse(other.configured)
        aiming.minAngle = 180.0
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
