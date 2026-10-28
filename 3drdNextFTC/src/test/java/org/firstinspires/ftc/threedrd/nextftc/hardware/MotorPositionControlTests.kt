package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_WITHOUT_ENCODER
import com.qualcomm.robotcore.hardware.DcMotorEx
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.threedrd.testing.SubsystemTests
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

class MotorPositionControlTests : SubsystemTests() {
    @Test
    fun positionControlConfiguresHubModeBeforeSpeedWithoutChangingTargetOrZero() {
        val sdk = mock(DcMotorEx::class.java)
        `when`(sdk.currentPosition).thenReturn(100)
        val motor = MotorEx(sdk)
        motor.currentPosition = 25.0
        clearInvocations(sdk)
        motor.positionControl(0.2)
        val order = inOrder(sdk)
        order.verify(sdk).mode = RUN_TO_POSITION
        order.verify(sdk).power = 0.2
        verifyNoMoreInteractions(sdk)
        assertEquals(25.0, motor.currentPosition, 0.0)
        assertEquals(dev.nextftc.hardware.impl.MotorEx::class.java, motor.javaClass)
    }

    @Test
    fun explicitRepeatedCallsUpdateNormalizedSpeedIncludingZeroAndFullSpeed() {
        val sdk = mock(DcMotorEx::class.java)
        val motor = MotorEx(sdk)
        motor.positionControl(0.2)
        motor.positionControl(0.4)
        motor.positionControl(0.0)
        motor.positionControl(1.0)
        verify(sdk, times(4)).mode = RUN_TO_POSITION
        verify(sdk).power = 0.2
        verify(sdk).power = 0.4
        verify(sdk).power = 0.0
        verify(sdk).power = 1.0
        verify(sdk, never()).setTargetPosition(anyInt())
    }

    @Test
    fun invalidSpeedFailsBeforeHardwareAcquisition() {
        val map = ActiveOpMode.hardwareMap
        val motor = MotorEx("invalidPositionSpeed")
        clearInvocations(map)
        for (speed in listOf(-0.1, 1.1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) { motor.positionControl(speed) }
        }
        verifyNoInteractions(map)
    }

    @Test
    fun hubOperationsAcquireLazyHardwareOnlyWhenExplicitlyCalled() {
        val map = ActiveOpMode.hardwareMap
        clearInvocations(map)
        val motor = MotorEx("hubPosition")
        verifyNoInteractions(map)
        motor.positionControl(0.2)
        verify(map).get(DcMotorEx::class.java, "hubPosition")
        motor.stopPositionControl()
        verify(map, times(1)).get(DcMotorEx::class.java, "hubPosition")
        verify(motor.motor).power = 0.0
    }

    @Test
    fun stopWritesZeroDespiteStaleNativePowerCacheAndCanBeRepeated() {
        val sdk = mock(DcMotorEx::class.java)
        val motor = MotorEx(sdk)
        assertEquals(0.0, motor.power, 0.0)
        motor.positionControl(0.3)
        clearInvocations(sdk)
        // Upstream power can skip its SDK write because its cache is still zero.
        motor.power = 0.0
        verify(sdk).mode = RUN_WITHOUT_ENCODER
        verify(sdk, never()).setPower(anyDouble())
        clearInvocations(sdk)
        motor.stopPositionControl()
        motor.stopPositionControl()
        verify(sdk, times(2)).power = 0.0
        verify(sdk, times(2)).mode = RUN_WITHOUT_ENCODER
        assertEquals(0.0, motor.power, 0.0)
        verifyNoMoreInteractions(sdk)
    }
    @Test
    fun stopSynchronizesNativeCacheAndAllowsReturningToPreviousManualPower() {
        val sdk = mock(DcMotorEx::class.java)
        val motor = MotorEx(sdk)
        motor.power = 0.7
        motor.positionControl(0.2)
        clearInvocations(sdk)
        motor.stopPositionControl()
        val order = inOrder(sdk)
        order.verify(sdk).power = 0.0
        order.verify(sdk).mode = RUN_WITHOUT_ENCODER
        order.verify(sdk).power = 0.0
        assertEquals(0.0, motor.power, 0.0)
        clearInvocations(sdk)
        motor.power = 0.7
        verify(sdk).power = 0.7
    }

    @Test
    fun positionModePreservesLogicalZeroByDefaultAndStopsBeforeSelectingCurrentHoldTarget() {
        val sdk = mock(DcMotorEx::class.java)
        `when`(sdk.currentPosition).thenReturn(100)
        val motor = MotorEx(sdk)
        motor.currentPosition = 25.0
        clearInvocations(sdk)
        motor.positionMode()
        val order = inOrder(sdk)
        order.verify(sdk).power = 0.0
        order.verify(sdk).mode = RUN_WITHOUT_ENCODER
        order.verify(sdk).currentPosition
        order.verify(sdk).targetPosition = 100
        order.verify(sdk).mode = RUN_TO_POSITION
        assertEquals(25.0, motor.currentPosition, 0.0)
        verify(sdk, never()).mode = com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER
    }

    @Test
    fun explicitZeroOnPositionModeRecapturesDatumOnEveryInitialization() {
        val sdk = mock(DcMotorEx::class.java)
        `when`(sdk.currentPosition).thenReturn(100)
        val motor = MotorEx(sdk)
        motor.positionMode(zero = true)
        assertEquals(0.0, motor.currentPosition, 0.0)
        verify(sdk).targetPosition = 100
        `when`(sdk.currentPosition).thenReturn(250)
        motor.positionMode(zero = true)
        assertEquals(0.0, motor.currentPosition, 0.0)
        verify(sdk).targetPosition = 250
    }

    @Test
    fun speedIsAHubCommandAndDoesNotChangeModeTargetOrMeasuredVelocity() {
        val sdk = mock(DcMotorEx::class.java)
        `when`(sdk.mode).thenReturn(RUN_TO_POSITION)
        `when`(sdk.power).thenReturn(0.3)
        `when`(sdk.velocity).thenReturn(125.0)
        val motor = MotorEx(sdk)
        clearInvocations(sdk)
        assertEquals(0.3, motor.speed, 0.0)
        motor.speed = 0.2
        motor.speed = 0.5
        motor.speed = 0.0
        motor.speed = 1.0
        verify(sdk).power = 0.2
        verify(sdk).power = 0.5
        verify(sdk).power = 0.0
        verify(sdk).power = 1.0
        verify(sdk, never()).setMode(any())
        verify(sdk, never()).setTargetPosition(anyInt())
        assertEquals(125.0, motor.velocity, 0.0)
        motor.stop()
        verify(sdk, atLeastOnce()).power = 0.0
        verify(sdk).mode = RUN_WITHOUT_ENCODER
    }

    @Test
    fun speedRequiresPositionModeAndInvalidValuesFailBeforeAcquisition() {
        val map = ActiveOpMode.hardwareMap
        val motor = MotorEx("invalidSpeedProperty")
        clearInvocations(map)
        for (value in listOf(-1.0, 2.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) { motor.speed = value }
        }
        verifyNoInteractions(map)
        val sdk = mock(DcMotorEx::class.java)
        `when`(sdk.mode).thenReturn(RUN_WITHOUT_ENCODER)
        val manual = MotorEx(sdk)
        assertThrows(IllegalStateException::class.java) { manual.speed }
        assertThrows(IllegalStateException::class.java) { manual.speed = 0.2 }
        verify(sdk, never()).setPower(anyDouble())
    }

}
