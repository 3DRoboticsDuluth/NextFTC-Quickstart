package org.firstinspires.ftc.threedrd.nextftc.hardware

import org.firstinspires.ftc.threedrd.testing.*
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
}
