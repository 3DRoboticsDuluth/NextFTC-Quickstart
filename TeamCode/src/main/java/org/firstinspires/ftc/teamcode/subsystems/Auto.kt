package org.firstinspires.ftc.teamcode.subsystems

import org.firstinspires.ftc.threedrd.pedropathing.PedroComponent.Companion.follower
import dev.nextftc.ftc.*
import org.firstinspires.ftc.threedrd.nextftc.opmodes.isAutonomous
import org.firstinspires.ftc.threedrd.nextftc.subsystems.*
import org.firstinspires.ftc.threedrd.pedropathing.resetStartingPose

object Auto : Subsystem() {
    val execute = Drive.to(Nav.end)

    override fun initialize() {
        if (ActiveOpMode.isAutonomous) follower.resetStartingPose(Nav.start)
    }
}
