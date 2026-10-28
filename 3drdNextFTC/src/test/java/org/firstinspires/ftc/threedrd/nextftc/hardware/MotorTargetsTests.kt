package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.DcMotorEx
import dev.nextftc.core.units.*
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.threedrd.testing.SubsystemTests
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

class MotorTargetsTests : SubsystemTests() {
    @Test
    fun calibrationIsLazyFluentAndRetainsTheNativeTypeWithoutAnOpMode() {
        val active = ActiveOpMode.it
        try {
            ActiveOpMode.it = null
            val motor = MotorEx("lazyTargets")
            assertSame(motor, motor.distancePerTick(0.01.inches).anglePerTick(1.deg))
            assertEquals(dev.nextftc.hardware.impl.MotorEx::class.java, motor.javaClass)
        } finally {
            ActiveOpMode.it = active
        }
    }

    @Test
    fun distanceConvertsUnitsAndRoundsSdkTargetsWithoutChangingModeOrPower() {
        val sdk = mock(DcMotorEx::class.java)
        val motor = MotorEx(sdk).distancePerTick(0.01.inches)
        motor.targetDistance = 6.inches
        verify(sdk).targetPosition = 600
        clearInvocations(sdk)
        `when`(sdk.targetPosition).thenReturn(600)
        assertEquals(15.24, motor.targetDistance.inCm, 1e-9)
        motor.distancePerTick(1.mm)
        motor.targetDistance = 1.6.mm
        verify(sdk).targetPosition = 2
        motor.targetDistance = (-1.6).mm
        verify(sdk).targetPosition = -2
        verify(sdk, never()).setMode(any())
        verify(sdk, never()).setPower(anyDouble())
    }

    @Test
    fun angleSupportsRadiansDegreesAndMultipleRevolutions() {
        val sdk = mock(DcMotorEx::class.java)
        val motor = MotorEx(sdk).anglePerTick(1.deg)
        motor.targetAngle = 2.rev
        verify(sdk).targetPosition = 720
        `when`(sdk.targetPosition).thenReturn(720)
        assertEquals(4 * Math.PI, motor.targetAngle.inRad, 1e-9)
        motor.anglePerTick(0.5.rad)
        motor.targetAngle = (-1).rad
        verify(sdk).targetPosition = -2
    }

    @Test
    fun targetsRespectNativeZeroAndLogicalReversalEvenAfterMovement() {
        val sdk = mock(DcMotorEx::class.java)
        `when`(sdk.currentPosition).thenReturn(100)
        val motor = MotorEx(sdk).distancePerTick(1.mm).anglePerTick(1.rad)
        motor.zero()
        motor.targetDistance = 10.mm
        verify(sdk).targetPosition = 110
        motor.reverse()
        motor.targetDistance = 10.mm
        motor.targetAngle = 10.rad
        verify(sdk, times(2)).targetPosition = 90
        `when`(sdk.targetPosition).thenReturn(90)
        `when`(sdk.currentPosition).thenReturn(115)
        assertEquals(10.0, motor.targetDistance.inMm, 0.0)
        assertEquals(10.0, motor.targetAngle.inRad, 0.0)
        motor.currentPosition = 20.0
        motor.targetDistance = 25.mm
        verify(sdk, times(2)).targetPosition = 110
    }

    @Test
    fun calibrationsAreIndependentBetweenMotorsAndDimensions() {
        val firstSdk = mock(DcMotorEx::class.java)
        val secondSdk = mock(DcMotorEx::class.java)
        val first = MotorEx(firstSdk).distancePerTick(1.mm).anglePerTick(2.rad)
        val second = MotorEx(secondSdk).distancePerTick(2.mm).anglePerTick(1.rad)
        first.targetDistance = 10.mm
        second.targetDistance = 10.mm
        verify(firstSdk).targetPosition = 10
        verify(secondSdk).targetPosition = 5
        first.targetAngle = 6.rad
        second.targetAngle = 6.rad
        verify(firstSdk).targetPosition = 3
        verify(secondSdk).targetPosition = 6
    }

    @Test
    fun missingCalibrationFailsBeforeHardwareLookupForReadsAndWrites() {
        val map = ActiveOpMode.hardwareMap
        val motor = MotorEx("unconfiguredTargets")
        clearInvocations(map)
        assertThrows(IllegalStateException::class.java) { motor.targetDistance }
        assertThrows(IllegalStateException::class.java) { motor.targetDistance = 1.mm }
        assertThrows(IllegalStateException::class.java) { motor.targetAngle }
        assertThrows(IllegalStateException::class.java) { motor.targetAngle = 1.rad }
        verifyNoInteractions(map)
    }

    @Test
    fun invalidCalibrationDoesNotAcquireHardwareOrReplaceValidSettings() {
        val map = ActiveOpMode.hardwareMap
        val motor = MotorEx("invalidTargets").distancePerTick(1.mm).anglePerTick(1.rad)
        clearInvocations(map)
        for (value in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) { motor.distancePerTick(value.mm) }
            assertThrows(IllegalArgumentException::class.java) { motor.anglePerTick(value.rad) }
        }
        verifyNoInteractions(map)
        motor.targetDistance = 5.mm
        verify(motor.motor).targetPosition = 5
        motor.targetAngle = 6.rad
        verify(motor.motor).targetPosition = 6
    }

    @Test
    fun invalidTargetsFailBeforeAcquisitionAndOverflowDoesNotWriteTarget() {
        val map = ActiveOpMode.hardwareMap
        val motor = MotorEx("badTargetValues").distancePerTick(1.mm).anglePerTick(1.rad)
        clearInvocations(map)
        for (value in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) { motor.targetDistance = value.mm }
            assertThrows(IllegalArgumentException::class.java) { motor.targetAngle = value.rad }
        }
        verifyNoInteractions(map)
        motor.initialize()
        clearInvocations(motor.motor)
        for (value in listOf(Int.MAX_VALUE.toDouble() + 1, Int.MIN_VALUE.toDouble() - 1)) {
            assertThrows(IllegalArgumentException::class.java) { motor.targetDistance = value.mm }
        }
        verify(motor.motor, never()).setTargetPosition(anyInt())
        motor.targetDistance = Int.MAX_VALUE.mm
        motor.targetAngle = Int.MIN_VALUE.rad
        verify(motor.motor).targetPosition = Int.MAX_VALUE
        verify(motor.motor).targetPosition = Int.MIN_VALUE
    }

    @Test
    fun nativeFluentConfigurationAndSdkDirectionRemainInTheTargetCoordinateFrame() {
        val map = ActiveOpMode.hardwareMap
        clearInvocations(map)
        val motor = MotorEx("fluentTargets") {
            direction = com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE
        }.distancePerTick(1.mm).reversed().zeroed()
        verifyNoInteractions(map)
        motor.targetDistance = 4.mm
        verify(motor.motor).targetPosition = -4
        verify(motor.motor).direction = com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE
        `when`(motor.motor.targetPosition).thenReturn(-4)
        assertEquals(4.0, motor.targetDistance.inMm, 0.0)
    }
    @Test
    fun ticksAndTravelAreLazyFluentAndOrderIndependentForBothDimensions() {
        val active = ActiveOpMode.it
        try {
            ActiveOpMode.it = null
            val motor = MotorEx("fluentTravel")
            assertSame(motor, motor.ticks(384.5).travel(336.mm).travel(360.deg))
            assertEquals(dev.nextftc.hardware.impl.MotorEx::class.java, motor.javaClass)
        } finally {
            ActiveOpMode.it = active
        }
        val sdk = mock(DcMotorEx::class.java)
        val motor = MotorEx(sdk).travel(336.mm).travel(360.deg).ticks(384.5)
        motor.targetDistance = 10.inches
        verify(sdk).targetPosition = 291
        `when`(sdk.targetPosition).thenReturn(291)
        assertEquals(291 * 336.0 / 384.5, motor.targetDistance.inMm, 1e-9)
        motor.targetAngle = 2.rev
        verify(sdk).targetPosition = 769
        `when`(sdk.targetPosition).thenReturn(769)
        assertEquals(2.0, motor.targetAngle.inRev, 1e-9)
    }

    @Test
    fun partialConfigurationFailsAtTargetUseWithoutAcquiringHardware() {
        val map = ActiveOpMode.hardwareMap
        val ticksOnly = MotorEx("ticksOnly").ticks(384.5)
        val travelOnly = MotorEx("travelOnly").travel(336.mm).travel(360.deg)
        clearInvocations(map)
        for (motor in listOf(ticksOnly, travelOnly)) {
            assertThrows(IllegalStateException::class.java) { motor.targetDistance }
            assertThrows(IllegalStateException::class.java) { motor.targetDistance = 1.mm }
            assertThrows(IllegalStateException::class.java) { motor.targetAngle }
            assertThrows(IllegalStateException::class.java) { motor.targetAngle = 1.deg }
        }
        verifyNoInteractions(map)
        val sdk = mock(DcMotorEx::class.java)
        MotorEx(sdk).ticks(100.0).power = 0.2
        verify(sdk).power = 0.2
    }

    @Test
    fun invalidFluentValuesFailImmediatelyWithoutReplacingSettings() {
        val map = ActiveOpMode.hardwareMap
        val motor = MotorEx("badFluentTravel").ticks(100.0).travel(200.mm).travel(300.rad)
        clearInvocations(map)
        for (value in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) { motor.ticks(value) }
            assertThrows(IllegalArgumentException::class.java) { motor.travel(value.mm) }
            assertThrows(IllegalArgumentException::class.java) { motor.travel(value.rad) }
        }
        verifyNoInteractions(map)
        motor.targetDistance = 20.mm
        verify(motor.motor).targetPosition = 10
        motor.targetAngle = 60.rad
        verify(motor.motor).targetPosition = 20
    }

    @Test
    fun fluentAndPerTickSettingsHaveExplicitPerDimensionPrecedence() {
        val sdk = mock(DcMotorEx::class.java)
        val motor = MotorEx(sdk).ticks(100.0).travel(200.mm).travel(300.rad)
        motor.distancePerTick(1.mm).anglePerTick(1.rad).ticks(50.0)
        motor.targetDistance = 10.mm
        motor.targetAngle = 20.rad
        verify(sdk).targetPosition = 10
        verify(sdk).targetPosition = 20
        motor.travel(200.mm)
        motor.targetDistance = 20.mm
        verify(sdk).targetPosition = 5
        motor.targetAngle = 30.rad
        verify(sdk).targetPosition = 30
        motor.travel(300.rad).ticks(100.0)
        motor.targetAngle = 60.rad
        verify(sdk, times(2)).targetPosition = 20
        `when`(sdk.targetPosition).thenReturn(5)
        assertEquals(10.0, motor.targetDistance.inMm, 0.0)
    }

    @Test
    fun fluentMetadataIsPerMotorAndUnrepresentableRatiosFailWithoutLookup() {
        val sdk = mock(DcMotorEx::class.java)
        val first = MotorEx(sdk).ticks(100.0).travel(200.mm)
        first.targetDistance = 10.mm
        verify(sdk).targetPosition = 5
        val map = ActiveOpMode.hardwareMap
        val second = MotorEx("secondFluent").travel(200.mm)
        clearInvocations(map)
        assertThrows(IllegalStateException::class.java) { second.targetDistance = 10.mm }
        second.ticks(Double.MAX_VALUE).travel(Double.MIN_VALUE.mm)
        assertThrows(IllegalArgumentException::class.java) { second.targetDistance = 10.mm }
        second.ticks(Double.MIN_VALUE).travel(Double.MAX_VALUE.mm)
        assertThrows(IllegalArgumentException::class.java) { second.targetDistance }
        verifyNoInteractions(map)
    }

}
