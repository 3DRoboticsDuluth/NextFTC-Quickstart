package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import com.pedropathing.follower.Follower
import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.ftc.drivetrains.MecanumConstants
import com.pedropathing.ftc.localization.Encoder
import com.pedropathing.ftc.localization.constants.DriveEncoderConstants
import com.pedropathing.paths.PathConstraints
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.nextftc.core.units.inches
import kotlin.math.max

object Constants {
    val robotLength = 18.inches
    val robotWidth = 18.inches
    val robotRadius = max(robotLength.inIn, robotWidth.inIn) / 2

    var followerConstants = FollowerConstants()
    var pathConstraints = PathConstraints.defaultConstraints

    var driveConstants = MecanumConstants().apply {
        leftFrontMotorName = "leftMotor"
        leftRearMotorName = "intake"
        rightFrontMotorName = "rightMotor"
        rightRearMotorName = "flywheel"
        leftFrontMotorDirection = FORWARD
        leftRearMotorDirection = FORWARD
        rightFrontMotorDirection = FORWARD
        rightRearMotorDirection = FORWARD
    }

    var localizerConstants = DriveEncoderConstants().apply {
        leftFrontMotorName = "leftMotor"
        leftRearMotorName = "intake"
        rightFrontMotorName = "rightMotor"
        rightRearMotorName = "flywheel"
        leftFrontEncoderDirection = Encoder.FORWARD
        rightFrontEncoderDirection = Encoder.FORWARD
        leftRearEncoderDirection = Encoder.FORWARD
        rightRearEncoderDirection = Encoder.FORWARD
        forwardTicksToInches = 45.0
        strafeTicksToInches = 45.0
        turnTicksToInches = 45.0
        robot_Width = 16.0
        robot_Length = 16.0
    }

    fun createFollower(hardwareMap: HardwareMap): Follower =
        FollowerBuilder(followerConstants, hardwareMap)
            .pathConstraints(pathConstraints)
            .mecanumDrivetrain(driveConstants)
            .driveEncoderLocalizer(localizerConstants)
            .build()
}
