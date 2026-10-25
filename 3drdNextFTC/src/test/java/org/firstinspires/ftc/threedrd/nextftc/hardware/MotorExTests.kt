package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.configuration.typecontainers.*
import org.firstinspires.ftc.threedrd.testing.*
import org.junit.Assert.*
import org.junit.*
import org.mockito.Mockito.*

class MotorExTests : SubsystemTests() {
    @Test
    fun initializationResolvesAndConfiguresTheMotor() {
        var configured = false
        clearInvocations(dev.nextftc.ftc.ActiveOpMode.hardwareMap)
        val motor = MotorEx("configured") { configured = true }
        assertFalse(configured)
        assertEquals("configured", motor.name)
        verify(dev.nextftc.ftc.ActiveOpMode.hardwareMap, never()).get(
            com.qualcomm.robotcore.hardware.DcMotorEx::class.java, "configured"
        )

        motor.initialize()

        assertNotNull(motor.motor)
        assertEquals(dev.nextftc.hardware.impl.MotorEx::class.java, motor.javaClass)
        assertTrue(configured)
    }

    @Test
    fun fluentConfigurationReturnsTheAdapter() {
        val motor = MotorEx("fluent", configure = {})
        motor.initialize()

        assertSame(
            motor,
            motor.reversed().zeroed().atPosition(12.0).floatMode().brakeMode()
        )
    }

    @Test
    fun getsAndSetsVelocityAsAPercentageOfTheConfiguredMaximum() {
        val motor = MotorEx("percentage", configure = {})
        motor.initialize()
        val type = mock(MotorConfigurationType::class.java)
        `when`(motor.motor.motorType).thenReturn(type)
        `when`(type.achieveableMaxTicksPerSecond).thenReturn(500.0)
        `when`(motor.motor.velocity).thenReturn(125.0)

        assertEquals(0.25, motor.velocityPercentage, 0.0)
        motor.velocityPercentage = 0.5

        verify(motor.motor).velocity = 250.0
    }

    @Test
    fun updateAppliesTheActionAndReportsTheMotor() {
        val motor = MotorEx("test", configure = {})
        var actionApplied = false
        var reported: MotorEx? = null
        HardwareTelemetry.motor(motor)
        HardwareTelemetry.motor = { reported = this }

        motor.update { actionApplied = true }

        assertTrue(actionApplied)
        assertSame(motor, reported)
    }
    @Test
    fun originalConstructorAndPreviouslyNestedApiRemainDirect() {
        val map = dev.nextftc.ftc.ActiveOpMode.hardwareMap
        val sdk = mock(com.qualcomm.robotcore.hardware.DcMotorEx::class.java)
        `when`(map.get("nativeMotor")).thenReturn(sdk)
        val motor = MotorEx("nativeMotor", cacheTolerance = 0.01)
        verify(map, never()).get("nativeMotor")
        motor.initialize()
        motor.initialize()
        verify(map).get("nativeMotor")
        assertSame(sdk, motor.motor)
        `when`(sdk.currentPosition).thenReturn(123)
        assertEquals(123.0, motor.rawTicks, 0.0)
        motor.direction = 1
        motor.reverse()
        assertEquals(-1, motor.direction)
        motor.zero()
        assertEquals(0.0, motor.currentPosition, 0.0)
        motor.zeroPowerBehavior = com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE
        verify(sdk).setZeroPowerBehavior(com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE)
    }

    @Test
    fun shortDeclarationUsesLazyTypedLookup() {
        val map = dev.nextftc.ftc.ActiveOpMode.hardwareMap
        clearInvocations(map)
        val motor = MotorEx("shortMotor")
        verify(map, never()).get(com.qualcomm.robotcore.hardware.DcMotorEx::class.java, "shortMotor")
        motor.initialize()
        verify(map).get(com.qualcomm.robotcore.hardware.DcMotorEx::class.java, "shortMotor")
        assertEquals(dev.nextftc.hardware.impl.MotorEx::class.java, motor.javaClass)
    }

}
