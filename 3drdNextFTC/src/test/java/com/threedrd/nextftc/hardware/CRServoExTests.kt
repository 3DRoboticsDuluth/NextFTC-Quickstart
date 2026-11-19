package com.threedrd.nextftc.hardware

import com.threedrd.testing.*
import org.mockito.Mockito.*
import org.junit.Assert.*
import org.junit.*

class CRServoExTests : SubsystemTests() {
    @Test
    fun initializationRestoresNativeReversalAfterSdkOpModeReset() {
        val controller = mock(com.qualcomm.robotcore.hardware.ServoController::class.java)
        val sdk = com.qualcomm.robotcore.hardware.CRServoImpl(controller, 0)
        `when`(dev.nextftc.ftc.ActiveOpMode.hardwareMap.get(com.qualcomm.robotcore.hardware.CRServo::class.java, "retainedReverse"))
            .thenReturn(sdk)
        val servo = CRServoEx("retainedReverse").reversed()
        servo.initialize()
        assertEquals(com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE, sdk.direction)
        repeat(2) {
            sdk.resetDeviceConfigurationForOpMode()
            assertEquals(com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD, sdk.direction)
            clearInvocations(controller)
            servo.initialize()
            assertEquals(com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE, sdk.direction)
            verifyNoInteractions(controller)
            servo.power = if (it == 0) 1.0 else -1.0
            verify(controller).setServoPosition(0, if (it == 0) 0.0 else 1.0)
        }
    }

    @Test
    fun initializationRetainsConfiguredForwardDirectionIndependently() {
        val controller = mock(com.qualcomm.robotcore.hardware.ServoController::class.java)
        val sdk = com.qualcomm.robotcore.hardware.CRServoImpl(controller, 0)
        val servo = CRServoEx(sdk)
        servo.initialize()
        sdk.direction = com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE
        servo.initialize()
        assertEquals(com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD, sdk.direction)
    }

    @Test
    fun initializationResolvesAndConfiguresTheServo() {
        var configured = false
        val servo = CRServoEx("configured") { configured = true }

        servo.initialize()

        assertNotNull(servo.servo)
        assertTrue(configured)
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
        `when`(sdk.direction).thenReturn(com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD)
        `when`(sdk.deviceName).thenReturn("Standalone CRServo")
        val servo = CRServoEx(sdk)
        servo.initialize()
        assertSame(sdk, servo.servo)
        assertEquals("Standalone CRServo", servo.name)
    }

}
