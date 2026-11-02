package org.firstinspires.ftc.teamcode.subsystems

import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import dev.nextftc.ftc.*
import com.threedrd.nextftc.opmodes.*
import com.threedrd.nextftc.subsystems.*
import com.threedrd.pedropathing.*

object Auto : Subsystem() {
    val execute = Drive.to(Nav.end)

    override fun initialize() {
        if (ActiveOpMode.isAutonomous) follower.resetStartingPose(Nav.start)
    }
}
