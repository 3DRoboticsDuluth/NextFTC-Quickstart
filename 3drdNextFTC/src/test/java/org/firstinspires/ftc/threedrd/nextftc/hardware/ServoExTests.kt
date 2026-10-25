package org.firstinspires.ftc.threedrd.nextftc.hardware

import org.firstinspires.ftc.threedrd.testing.*
import org.mockito.Mockito.*
import org.junit.Assert.*
import org.junit.*

class ServoExTests : SubsystemTests() {
    @Test
    fun initializationResolvesTheConfiguredServo() {
        val servo = ServoEx("unconfigured")

        servo.initialize()

        assertNotNull(servo.servo)
    }

    @Test
    fun updateAppliesTheActionAndReportsTheServo() {
        val servo = ServoEx("test")
        var actionApplied = false
        var reported: ServoEx? = null
        HardwareTelemetry.servo(servo)
        HardwareTelemetry.servo = { reported = this }

        servo.update { actionApplied = true }

        assertTrue(actionApplied)
        assertSame(servo, reported)
    }
    @Test
    fun nativeFactoryStaysLazyAndReportsRegisteredName() {
        val map = dev.nextftc.ftc.ActiveOpMode.hardwareMap
        clearInvocations(map)
        var configurations = 0
        val servo = ServoEx("namedServo") { configurations++ }
        val fluent = servo.reversed()
        assertSame(servo, fluent)
        assertEquals(dev.nextftc.hardware.impl.ServoEx::class.java, fluent.javaClass)
        assertEquals(0, configurations)
        verify(map, never()).get(com.qualcomm.robotcore.hardware.Servo::class.java, "namedServo")
        fluent.initialize()
        assertEquals(1, configurations)
        assertEquals("namedServo", fluent.name)
        verify(map).get(com.qualcomm.robotcore.hardware.Servo::class.java, "namedServo")
        verify(fluent.servo).setDirection(com.qualcomm.robotcore.hardware.Servo.Direction.REVERSE)
    }

    @Test
    fun originalConstructorAndStandaloneDeviceNameRemainAvailable() {
        val sdk = mock(com.qualcomm.robotcore.hardware.Servo::class.java)
        `when`(sdk.deviceName).thenReturn("Standalone Servo")
        val servo = ServoEx(sdk)
        servo.initialize()
        assertSame(sdk, servo.servo)
        assertEquals("Standalone Servo", servo.name)
    }

}
