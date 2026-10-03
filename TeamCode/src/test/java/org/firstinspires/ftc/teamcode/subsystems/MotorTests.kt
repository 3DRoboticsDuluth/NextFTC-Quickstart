package org.firstinspires.ftc.teamcode.subsystems

import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_WITHOUT_ENCODER
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.configureHardwareTelemetry
import org.firstinspires.ftc.threedrd.nextftc.subsystems.ManuallyRegistered
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.verify

class MotorTests : SubsystemTests() {
    @Before
    fun setUp() {
        configureHardwareTelemetry()
        Motor.POWER = 0.20
        Motor.initialize()
        Motor.controls()
        clearInvocations(Motor.motor.motor, ActiveOpMode.telemetry)
    }

    @Test
    fun exposesFourPowerAndEncoderModeCombinations() {
        val motor = Motor.motor.motor

        Motor.forwardWithEncoder.start()
        verify(motor, atLeastOnce()).mode = RUN_USING_ENCODER
        verify(motor).power = 0.20

        clearInvocations(motor)
        Motor.reverseWithEncoder.start()
        verify(motor, atLeastOnce()).mode = RUN_USING_ENCODER
        verify(motor).power = -0.20

        clearInvocations(motor)
        Motor.forwardWithoutEncoder.start()
        verify(motor, atLeastOnce()).mode = RUN_WITHOUT_ENCODER
        verify(motor).power = 0.20

        clearInvocations(motor)
        Motor.reverseWithoutEncoder.start()
        verify(motor, atLeastOnce()).mode = RUN_WITHOUT_ENCODER
        verify(motor).power = -0.20

        assertEquals("Motor.forwardWithEncoder", Motor.forwardWithEncoder.name)
        assertEquals("Motor.reverseWithEncoder", Motor.reverseWithEncoder.name)
        assertEquals("Motor.forwardWithoutEncoder", Motor.forwardWithoutEncoder.name)
        assertEquals("Motor.reverseWithoutEncoder", Motor.reverseWithoutEncoder.name)
    }

    @Test
    fun stopsFromTheCommandAndSubsystemLifecycle() {
        val motor = Motor.motor.motor

        Motor.off.start()
        Motor.stop()

        verify(motor, atLeastOnce()).power = 0.0
    }

    @Test
    fun reportsItsControlsAndIsExcludedFromAutomaticDiscovery() {
        Motor.periodic()

        assertTrue(Motor::class.java.isAnnotationPresent(ManuallyRegistered::class.java))
        verify(ActiveOpMode.telemetry).addData(
            "I | Motor | Controls",
            "A/B: with encoder | X/Y: without | LB: stop" as Any
        )
    }
}
