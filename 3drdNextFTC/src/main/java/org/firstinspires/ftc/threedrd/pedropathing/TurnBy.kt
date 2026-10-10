package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.math.*
import dev.nextftc.core.commands.*
import dev.nextftc.core.units.*
import kotlin.math.*
import org.firstinspires.ftc.threedrd.pedropathing.PedroComponent.Companion.follower

/** Short successive hold targets preserve the requested direction, even past one revolution. */
class TurnBy(val angle: Angle, val tolerance: Angle) : Command() {
    private var origin = Pose.zero()
    private var previousHeading = 0.0
    private var remaining = 0.0

    override fun start() {
        origin = follower.pose()
        previousHeading = origin.heading()
        remaining = angle.inRad
        target()
    }

    override fun update() {
        val heading = follower.pose().heading()
        remaining -= (heading - previousHeading).normalizeHeading()
        previousHeading = heading
        target()
    }

    private fun target() = follower.hold(origin.withHeading(previousHeading + remaining.coerceIn(-PI / 2, PI / 2)))
    override val isDone get() = abs(remaining) <= tolerance.inRad

    override fun stop(interrupted: Boolean) {
        if (interrupted) follower.stopNow()
    }
}
