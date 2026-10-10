package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.*
import org.firstinspires.ftc.threedrd.pedropathing.PedroComponent.Companion.follower
import dev.nextftc.ftc.Gamepads.gamepad1
import org.firstinspires.ftc.threedrd.nextftc.subsystems.*
import org.firstinspires.ftc.threedrd.pedropathing.*
import org.firstinspires.ftc.teamcode.subsystems.Config.state
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.tel

@Configurable
object Drive : DriveSubsystem() {
    var POWER_LOW = 0.35
    var POWER_HIGH = 0.70

    val driverControlled = PedroDriverControlled(
        gamepad1.leftStickY.negate(),
        gamepad1.leftStickX.negate(),
        gamepad1.rightStickX.negate(),
        { Config.robotCentric }
    ).apply { requires(this@Drive) }

    val low by instant { driverControlled.scalar = POWER_LOW }
    val high by instant { driverControlled.scalar = POWER_HIGH }

    override val defaultCommand
        get() = if (state.teleop) driverControlled else super.defaultCommand

    override fun initialize() {
        driverControlled.scalar = POWER_HIGH
    }

    override fun controls() {
        val driving = !gamepad1.back
        (driving and gamepad1.dpadDown) whenBecomesTrue low
        (driving and gamepad1.dpadUp) whenBecomesTrue high
    }

    override fun periodic() {
        follower.drivetrain.tel()
        tel.info("Power", "%.2f".format(driverControlled.scalar))
        tel.debug("X", "%.1f".format(follower.pose().x()))
        tel.debug("Y", "%.1f".format(follower.pose().y()))
        tel.debug("Heading (deg)", "%.1f".format(Math.toDegrees(follower.pose().heading())))
    }
}
