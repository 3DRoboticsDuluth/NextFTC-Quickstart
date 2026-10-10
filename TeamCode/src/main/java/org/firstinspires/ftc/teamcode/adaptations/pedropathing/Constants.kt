package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import com.pedropathing.algorithm.*
import com.pedropathing.controllers.*
import com.pedropathing.follower.*
import com.pedropathing.math.*
import org.firstinspires.ftc.threedrd.pedropathing.*
import com.pedropathing.revhub.drivetrains.*
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.*
import com.qualcomm.robotcore.hardware.*
import dev.nextftc.core.units.inches
import kotlin.math.max

object Constants {
    val robotLength = 18.inches
    val robotWidth = 18.inches
    val robotRadius = max(robotLength.inIn, robotWidth.inIn) / 2

    var driveConfig = MecanumConfig {
        it.frontLeftName.set("leftFront")
        it.backLeftName.set("leftRear")
        it.frontRightName.set("rightFront")
        it.backRightName.set("rightRear")
        it.frontLeftDirection.set(REVERSE)
        it.backLeftDirection.set(REVERSE)
        it.frontRightDirection.set(FORWARD)
        it.backRightDirection.set(FORWARD)
    }
    var localizerConfig = DriveEncoderConfig("leftFront", "rightFront", "leftRear", "rightRear")

    // Template inputs only: measure/tune before autonomous use. Zero gains avoid uncalibrated corrections.
    var foresightConfig = ForesightConfig {
        it.headingFeedback.set(Controller.proportional(0.0))
        it.forwardTranslational.set(Controller.proportional(0.0))
        it.strafeTranslational.set(Controller.proportional(0.0))
        it.brake.set(Controller.proportional(0.0))
        it.coast.set(Controller.proportional(0.0))
        it.linearBrakeCoefficients.set(Matrix.diag(1.0, 1.0))
        it.quadraticBrakeCoefficients.set(Matrix.zero(2))
        it.headingBrakeCoefficients.set(Vector2D.cartesian(1.0, 0.0))
        it.maxAchievableForwardVelocity.set(1.0)
        it.maxAchievableStrafeVelocity.set(1.0)
        it.naturalForwardDeceleration.set(1.0)
        it.naturalStrafeDeceleration.set(1.0)
    }

    fun createFollower(hardwareMap: HardwareMap): Follower {
        val drivetrain = MecanumDrive(hardwareMap, driveConfig)
        return Follower(DriveEncoderLocalizer(hardwareMap, localizerConfig), drivetrain, Foresight(foresightConfig))
    }
}
