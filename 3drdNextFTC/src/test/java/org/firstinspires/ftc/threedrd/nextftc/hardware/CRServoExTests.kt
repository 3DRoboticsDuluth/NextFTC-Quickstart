package org.firstinspires.ftc.threedrd.nextftc.hardware

import org.firstinspires.ftc.threedrd.testing.*
import org.mockito.Mockito.*
import org.junit.Assert.*
import org.junit.*

class CRServoExTests : SubsystemTests() {
    @Test
    fun initializationResolvesAndConfiguresTheServo() {
        var configured = false
        val servo = CRServoEx("configured") { configured = true }

        servo.initialize()

        assertNotNull(servo.servo)
        assertTrue(configured)
    }

    @Test
    fun updateAppliesTheActionAndReportsTheServo() {
        val servo = CRServoEx("test")
        var actionApplied = false
        var reported: CRServoEx? = null
        HardwareTelemetry.continuousServo(servo)
        HardwareTelemetry.continuousServo = { reported = this }

        servo.update { actionApplied = true }

        assertTrue(actionApplied)
        assertSame(servo, reported)
    }
    @Test
    fun nativeFactoryStaysLazyAndReportsRegisteredName() {
        val map = dev.nextftc.ftc.ActiveOpMode.hardwareMap
        clearInvocations(map)
        var configurations = 0
        val servo = CRServoEx("namedCRServo") { configurations++ }
        val fluent = servo.reversed()
        assertSame(servo, fluent)
        assertEquals(dev.nextftc.hardware.impl.CRServoEx::class.java, fluent.javaClass)
        assertEquals(0, configurations)
        verify(map, never()).get(com.qualcomm.robotcore.hardware.CRServo::class.java, "namedCRServo")
        fluent.initialize()
        assertEquals(1, configurations)
        assertEquals("namedCRServo", fluent.name)
        verify(map).get(com.qualcomm.robotcore.hardware.CRServo::class.java, "namedCRServo")
        verify(fluent.servo).setDirection(com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE)
    }

    @Test
    fun originalConstructorAndStandaloneDeviceNameRemainAvailable() {
        val sdk = mock(com.qualcomm.robotcore.hardware.CRServo::class.java)
        `when`(sdk.deviceName).thenReturn("Standalone CRServo")
        val servo = CRServoEx(sdk)
        servo.initialize()
        assertSame(sdk, servo.servo)
        assertEquals("Standalone CRServo", servo.name)
    }

}
