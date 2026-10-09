package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import com.pedropathing.follower.*
import com.pedropathing.ftc.*
import com.pedropathing.ftc.drivetrains.*
import com.pedropathing.ftc.localization.constants.*
import com.pedropathing.paths.*
import com.qualcomm.robotcore.hardware.*
import dev.nextftc.core.units.*
import kotlin.math.*

object Constants {
    val robotLength = 18.inches
    val robotWidth = 18.inches
    val robotRadius = max(robotLength.inIn, robotWidth.inIn) / 2

    var followerConstants = FollowerConstants()
    var pathConstraints = PathConstraints.defaultConstraints
    var driveConstants = MecanumConstants()
    var localizerConstants = DriveEncoderConstants()

    fun createFollower(hardwareMap: HardwareMap): Follower =
        FollowerBuilder(followerConstants, hardwareMap)
            .pathConstraints(pathConstraints)
            .mecanumDrivetrain(driveConstants)
            .driveEncoderLocalizer(localizerConstants)
            .build()
}
