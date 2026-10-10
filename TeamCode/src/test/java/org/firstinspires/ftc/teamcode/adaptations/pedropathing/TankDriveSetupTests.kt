package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import com.pedropathing.revhub.drivetrains.*
import com.pedropathing.drivetrain.*
import org.firstinspires.ftc.threedrd.pedropathing.*
import com.pedropathing.revhub.localizers.*
import com.pedropathing.math.*
import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.*
import com.qualcomm.robotcore.hardware.configuration.typecontainers.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mockito.*

class TankDriveSetupTests {
    val hardwareMap = mock(HardwareMap::class.java)
    val left = mock(DcMotorEx::class.java)
    val right = mock(DcMotorEx::class.java)

    @Before
    fun configureMotors() {
        `when`(hardwareMap.get(DcMotorEx::class.java, "leftMotor")).thenReturn(left)
        `when`(hardwareMap.get(DcMotorEx::class.java, "rightMotor")).thenReturn(right)
        listOf(left, right).forEach { `when`(it.direction).thenReturn(FORWARD) }
    }

    @Test
    fun pairedEncodersMeasureForwardAndTurnWithoutLateralMotion() {
        val constants = DriveEncoderConfig("leftMotor", "rightMotor", "leftMotor", "rightMotor").apply {
            forwardTicksToInches = 0.025; strafeTicksToInches = 0.0; turnTicksToInches = 0.1
            robotWidth = 16.0; robotLength = 4.0
            frontLeftDirection = Encoder.FORWARD; backLeftDirection = Encoder.FORWARD
            frontRightDirection = Encoder.FORWARD; backRightDirection = Encoder.FORWARD
        }
        val localizer = DriveEncoderLocalizer(hardwareMap, constants)
        verify(hardwareMap, times(2)).get(DcMotorEx::class.java, "leftMotor")
        verify(hardwareMap, times(2)).get(DcMotorEx::class.java, "rightMotor")

        `when`(left.currentPosition).thenReturn(100)
        `when`(right.currentPosition).thenReturn(100)
        localizer.update()
        assertEquals(10.0, localizer.state().pose().x(), 0.0001)
        assertEquals(0.0, localizer.state().pose().y(), 0.0001)
        assertEquals(0.0, localizer.state().pose().heading(), 0.0001)

        `when`(left.currentPosition).thenReturn(0)
        `when`(right.currentPosition).thenReturn(200)
        localizer.update()
        assertEquals(10.0, localizer.state().pose().x(), 0.0001)
        assertEquals(0.0, localizer.state().pose().y(), 0.0001)
        assertEquals(2.0, localizer.state().pose().heading(), 0.0001)
    }

    @Test
    fun zeroStrafeProducesMatchingPowersForPairedSlots() {
        val config = MecanumConfig {
            it.frontLeftName.set("leftMotor"); it.backLeftName.set("leftMotor")
            it.frontRightName.set("rightMotor"); it.backRightName.set("rightMotor")
            it.frontLeftDirection.set(FORWARD); it.backLeftDirection.set(FORWARD)
            it.frontRightDirection.set(FORWARD); it.backRightDirection.set(FORWARD)
        }
        val drivetrain = MecanumDrive(hardwareMap, config)
        assertEquals(listOf(left, right, left, right), drivetrain.motors)
        clearInvocations(hardwareMap)
        assertEquals(listOf(left, right, left, right), drivetrain.motors)
        verifyNoInteractions(hardwareMap)
        val powers = drivetrain.computeWheelPowersUnnormalized(DrivePowers(0.3, 0.0, 0.1))
        assertEquals(powers[0], powers[2], 0.0001)
        assertEquals(powers[1], powers[3], 0.0001)
        clearInvocations(left, right)
        drivetrain.applyDrive(DrivePowers(0.3, 0.0, 0.1))
        verify(left, atLeastOnce()).setPower(powers[0])
        verify(right, atLeastOnce()).setPower(powers[1])
        drivetrain.stop()
        verify(left, atLeastOnce()).setPower(0.0)
        verify(right, atLeastOnce()).setPower(0.0)
    }
}
