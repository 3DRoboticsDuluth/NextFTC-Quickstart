package org.firstinspires.ftc.threedrd.nextftc.control

import org.junit.Assert.*
import org.junit.Test

class CurrentLimiterTests {
    @Test
    fun liveTuningPreservesTheCapAndChangesReductionThresholdAndRecovery() {
        var time = 0.0
        val parameters = CurrentLimitParameters(5.0)
        val limiter = CurrentLimiter(parameters) { time }
        limiter.limit(1.0, 6.0)
        time = 0.25
        assertEquals(0.5, limiter.limit(1.0, 6.0), 0.0)
        parameters.maxCurrent = 10.0
        parameters.reductionRate = 1.0
        parameters.recoveryRate = 1.0
        assertEquals(0.5, limiter.powerLimit, 0.0)
        time = 0.5
        assertEquals(0.75, limiter.limit(1.0, 6.0), 0.0)
        parameters.maxCurrent = 5.0
        time = 0.75
        assertEquals(0.5, limiter.limit(1.0, 6.0), 0.0)
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach {
            parameters.maxCurrent = it
            assertThrows(IllegalArgumentException::class.java) { limiter.limit(1.0, 6.0) }
            parameters.maxCurrent = 5.0
            parameters.reductionRate = it
            assertThrows(IllegalArgumentException::class.java) { limiter.limit(1.0, 6.0) }
            parameters.reductionRate = 1.0
            parameters.recoveryRate = it
            assertThrows(IllegalArgumentException::class.java) { limiter.limit(1.0, 6.0) }
            parameters.recoveryRate = 1.0
        }
        assertEquals(5.0, limiter.parameters.maxCurrent, 0.0)
        assertEquals(1.0, limiter.parameters.reductionRate, 0.0)
        assertEquals(1.0, limiter.parameters.recoveryRate, 0.0)
        assertEquals(0.5, limiter.powerLimit, 0.0)
        assertEquals(0.5, CurrentLimiter(CurrentLimitParameters(5.0)).limit(0.5, 0.0), 0.0)
    }

    @Test
    fun overcurrentReducesPowerInEitherDirectionAndRecoveryIsGradual() {
        var time = 0.0
        val limiter = CurrentLimiter(CurrentLimitParameters(5.0, 2.0, 0.5)) { time }
        assertEquals(5.0, limiter.parameters.maxCurrent, 0.0)
        assertEquals(2.0, limiter.parameters.reductionRate, 0.0)
        assertEquals(0.5, limiter.parameters.recoveryRate, 0.0)
        assertEquals(1.0, limiter.limit(2.0, 6.0), 0.0)
        time = 0.25
        assertEquals(0.5, limiter.limit(1.0, 6.0), 0.0)
        assertEquals(-0.5, limiter.limit(-1.0, 6.0), 0.0)
        assertEquals(0.2, limiter.limit(0.2, 6.0), 0.0)
        time = 0.5
        assertEquals(0.0, limiter.limit(1.0, 6.0), 0.0)
        time = 1.0
        assertEquals(0.0, limiter.limit(-1.0, 6.0), 0.0)
        time = 1.5
        assertEquals(0.25, limiter.limit(1.0, 5.0), 0.0)
        time = 5.0
        assertEquals(1.0, limiter.limit(1.0, 0.0), 0.0)
        assertEquals(1.0, limiter.powerLimit, 0.0)
        assertEquals(0.0, limiter.limit(0.0, 6.0), 0.0)
    }

    @Test
    fun resetRestoresCapAndDiscardsIdleTime() {
        var time = 0.0
        val limiter = CurrentLimiter(CurrentLimitParameters(5.0)) { time }
        limiter.limit(1.0, 6.0)
        time = 0.25
        assertEquals(0.5, limiter.limit(1.0, 6.0), 0.0)
        limiter.reset()
        assertEquals(1.0, limiter.powerLimit, 0.0)
        time = 100.0
        assertEquals(1.0, limiter.limit(1.0, 6.0), 0.0)
    }

    @Test
    fun motorLimitersHaveIndependentHistory() {
        var time = 0.0
        val parameters = CurrentLimitParameters(5.0)
        val left = CurrentLimiter(parameters) { time }
        val right = CurrentLimiter(parameters) { time }
        left.limit(1.0, 6.0)
        right.limit(1.0, 2.0)
        time = 0.25
        assertEquals(0.5, left.limit(1.0, 6.0), 0.0)
        assertEquals(1.0, right.limit(1.0, 2.0), 0.0)
        parameters.maxCurrent = 10.0
        time = 0.5
        assertEquals(0.625, left.limit(1.0, 6.0), 0.0)
        assertEquals(1.0, right.limit(1.0, 2.0), 0.0)
        assertSame(parameters, left.parameters)
        assertSame(parameters, right.parameters)
    }

    @Test
    fun validatesParametersAndSamplesAndSupportsDefaultClock() {
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach {
            assertThrows(IllegalArgumentException::class.java) { CurrentLimiter(it) }
            assertThrows(IllegalArgumentException::class.java) { CurrentLimiter(5.0, reductionRate = it) }
            assertThrows(IllegalArgumentException::class.java) { CurrentLimiter(5.0, recoveryRate = it) }
        }
        val limiter = CurrentLimiter(5.0)
        listOf(Double.NaN, Double.POSITIVE_INFINITY).forEach {
            assertThrows(IllegalArgumentException::class.java) { limiter.limit(it, 0.0) }
            assertThrows(IllegalArgumentException::class.java) { limiter.limit(1.0, it) }
        }
        assertThrows(IllegalArgumentException::class.java) { limiter.limit(1.0, -1.0) }
        assertEquals(0.5, limiter.limit(0.5, 0.0), 0.0)
    }
}
