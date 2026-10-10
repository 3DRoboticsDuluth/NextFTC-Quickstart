package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import com.pedropathing.ftc.drivetrains.*
import com.pedropathing.ftc.localization.Encoder
import com.pedropathing.ftc.localization.constants.*
import com.pedropathing.ftc.localization.localizers.*
import com.pedropathing.math.*
import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD
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
        val constants = DriveEncoderConstants()
            .leftFrontMotorName("leftMotor").leftRearMotorName("leftMotor")
            .rightFrontMotorName("rightMotor").rightRearMotorName("rightMotor")
            .forwardTicksToInches(0.025).strafeTicksToInches(0.0)
            .turnTicksToInches(0.1).robotWidth(16.0).robotLength(4.0)
            .leftFrontEncoderDirection(Encoder.FORWARD).leftRearEncoderDirection(Encoder.FORWARD)
            .rightFrontEncoderDirection(Encoder.FORWARD).rightRearEncoderDirection(Encoder.FORWARD)
        val localizer = DriveEncoderLocalizer(hardwareMap, constants)
        verify(hardwareMap, times(2)).get(DcMotorEx::class.java, "leftMotor")
        verify(hardwareMap, times(2)).get(DcMotorEx::class.java, "rightMotor")

        `when`(left.currentPosition).thenReturn(100)
        `when`(right.currentPosition).thenReturn(100)
        localizer.update()
        assertEquals(10.0, localizer.pose.x, 0.0001)
        assertEquals(0.0, localizer.pose.y, 0.0001)
        assertEquals(0.0, localizer.pose.heading, 0.0001)

        `when`(left.currentPosition).thenReturn(0)
        `when`(right.currentPosition).thenReturn(200)
        localizer.update()
        assertEquals(10.0, localizer.pose.x, 0.0001)
        assertEquals(0.0, localizer.pose.y, 0.0001)
        assertEquals(2.0, localizer.totalHeading, 0.0001)
    }

    @Test
    fun zeroStrafeProducesMatchingPowersForPairedSlots() {
        val voltageMapping = mock(HardwareMap.DeviceMapping::class.java)
        val voltage = mock(VoltageSensor::class.java)
        `when`(voltageMapping.iterator()).thenReturn(mutableListOf<HardwareDevice>(voltage).iterator())
        HardwareMap::class.java.getField("voltageSensor").set(hardwareMap, voltageMapping)
        listOf(left, right).forEach {
            `when`(it.motorType).thenReturn(MotorConfigurationType())
        }
        val constants = MecanumConstants()
            .leftFrontMotorName("leftMotor").leftRearMotorName("leftMotor")
            .rightFrontMotorName("rightMotor").rightRearMotorName("rightMotor")
        val drivetrain = Mecanum(hardwareMap, constants)
        assertEquals(listOf(left, left, right, right), drivetrain.motors)

        for (heading in listOf(0.0, Math.PI / 2)) {
            val powers = drivetrain.calculateDrive(Vector(), Vector(0.1, heading), Vector(0.3, heading), heading)
            assertEquals(powers[0], powers[1], 0.0001)
            assertEquals(powers[2], powers[3], 0.0001)
            assertTrue(powers[2] > powers[0])
            clearInvocations(left, right)
            drivetrain.runDrive(powers)
            verify(left, atLeastOnce()).setPower(powers[0])
            verify(right, atLeastOnce()).setPower(powers[2])
            drivetrain.breakFollowing()
            verify(left, atLeastOnce()).setPower(0.0)
            verify(right, atLeastOnce()).setPower(0.0)
        }
    }
}
