package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import com.pedropathing.follower.Follower
import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.ftc.drivetrains.MecanumConstants
import com.pedropathing.ftc.localization.constants.DriveEncoderConstants
import com.pedropathing.paths.PathConstraints
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.nextftc.core.units.inches
import kotlin.math.max

object Constants {
    // Template dimensions and Pedro defaults; replace these when starting a robot season.
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
