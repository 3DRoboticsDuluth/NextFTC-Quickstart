package org.firstinspires.ftc.teamcode.subsystems

import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import com.threedrd.nextftc.subsystems.*
import com.threedrd.nextftc.subsystems.DiscoveryMode.AUTO
import com.threedrd.pedropathing.*

@Discovery(mode = AUTO)
object Auto : Subsystem() {
    val execute = Drive.to(Nav.end)

    override fun initialize() = follower.resetStartingPose(Nav.start)
    override fun start() { execute.schedule() }
}
