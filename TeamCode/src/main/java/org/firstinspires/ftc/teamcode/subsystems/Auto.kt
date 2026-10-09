package org.firstinspires.ftc.teamcode.subsystems

import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import dev.nextftc.ftc.*
import org.firstinspires.ftc.threedrd.nextftc.opmodes.*
import org.firstinspires.ftc.threedrd.nextftc.subsystems.*
import org.firstinspires.ftc.threedrd.pedropathing.*

object Auto : Subsystem() {
    val execute = Drive.to(Nav.end)

    override fun initialize() {
        if (ActiveOpMode.isAutonomous) follower.resetStartingPose(Nav.start)
    }
}
