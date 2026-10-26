package org.firstinspires.ftc.threedrd.nextftc.hardware

import org.firstinspires.ftc.threedrd.testing.*
import org.mockito.Mockito.*
import org.junit.Assert.*
import org.junit.*

class ServoExTests : SubsystemTests() {
    @Test
    fun fluentRangePreservesLazyLookupNativeIdentityAndQueuedConfigurationOrder() {
        val map = dev.nextftc.ftc.ActiveOpMode.hardwareMap
        clearInvocations(map)
        val servo = ServoEx("fluentRange") { scaleRange(0.1, 0.9) }
        val fluent = servo.scaleRange(0.2, 0.8).reversed()
        assertSame(servo, fluent)
        assertEquals(dev.nextftc.hardware.impl.ServoEx::class.java, fluent.javaClass)
        verify(map, never()).get(com.qualcomm.robotcore.hardware.Servo::class.java, "fluentRange")
        // Early native SDK access must also run queued configuration, not just initialize().
        val sdk = fluent.servo
        val order = inOrder(sdk)
        order.verify(sdk).scaleRange(0.1, 0.9)
        order.verify(sdk).scaleRange(0.2, 0.8)
        order.verify(sdk).direction = com.qualcomm.robotcore.hardware.Servo.Direction.REVERSE
        fluent.initialize()
        verify(map).get(com.qualcomm.robotcore.hardware.Servo::class.java, "fluentRange")
        verify(sdk).scaleRange(0.2, 0.8)
    }

    @Test
    fun rangeConfigurationAfterAcquisitionAppliesImmediatelyWithoutAnotherLookup() {
        val servo = ServoEx("liveRange")
        servo.initialize()
        val map = dev.nextftc.ftc.ActiveOpMode.hardwareMap
        clearInvocations(map)
        assertSame(servo, servo.scaleRange(0.25, 0.75))
        verify(servo.servo).scaleRange(0.25, 0.75)
        verify(map, never()).get(com.qualcomm.robotcore.hardware.Servo::class.java, "liveRange")
    }

    @Test
    fun bridgeConstructionDoesNotRequireAnActiveOpMode() {
        val opMode = dev.nextftc.ftc.ActiveOpMode.it
        try {
            dev.nextftc.ftc.ActiveOpMode.it = null
            val servo = ServoEx("inactiveRange").scaleRange(0.1, 0.9).reversed()
            assertEquals(dev.nextftc.hardware.impl.ServoEx::class.java, servo.javaClass)
        } finally {
            dev.nextftc.ftc.ActiveOpMode.it = opMode
        }
    }

    @Test
    fun configurationFailuresRemainLazyAndCanBeRetriedWithoutLosingQueuedActions() {
        var attempts = 0
        val servo = ServoEx("retryRange") {
            attempts++
            if (attempts == 1) error("Device not ready")
        }.scaleRange(0.1, 0.8)
        assertEquals(0, attempts)
        assertThrows(IllegalStateException::class.java) { servo.initialize() }
        servo.initialize()
        assertEquals(2, attempts)
        verify(servo.servo).scaleRange(0.1, 0.8)
        servo.initialize()
        assertEquals(2, attempts)
    }

    @Test
    fun unregisteredNativeInstancesAreRejectedWithoutForcingAcquisition() {
        var lookups = 0
        val servo = dev.nextftc.hardware.impl.ServoEx {
            lookups++
            mock(com.qualcomm.robotcore.hardware.Servo::class.java)
        }
        val error = assertThrows(IllegalArgumentException::class.java) { servo.scaleRange(0.1, 0.9) }
        assertEquals("scaleRange requires the project ServoEx factory", error.message)
        assertEquals(0, lookups)
    }

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
