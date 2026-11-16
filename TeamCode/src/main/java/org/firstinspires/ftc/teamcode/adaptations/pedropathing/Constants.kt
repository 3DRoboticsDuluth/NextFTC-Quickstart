package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import com.threedrd.pedropathing.PedroConfig
import com.pedropathing.follower.*
import com.pedropathing.ftc.*
import com.pedropathing.ftc.drivetrains.*
import com.pedropathing.ftc.localization.constants.*
import com.pedropathing.paths.*
import com.qualcomm.robotcore.hardware.*
import dev.nextftc.core.units.*
import kotlin.math.*

object Constants : PedroConfig {
    val robotLength = 18.inches
    val robotWidth = 18.inches
    override val robotRadius = max(robotLength.inIn, robotWidth.inIn) / 2

    var followerConstants = FollowerConstants()
    var pathConstraints = PathConstraints.defaultConstraints
    var driveConstants = MecanumConstants()
    var localizerConstants = DriveEncoderConstants()

    override fun createFollower(hardwareMap: HardwareMap): Follower =
        FollowerBuilder(followerConstants, hardwareMap)
            .pathConstraints(pathConstraints)
            .mecanumDrivetrain(driveConstants)
            .driveEncoderLocalizer(localizerConstants)
            .build()
}
