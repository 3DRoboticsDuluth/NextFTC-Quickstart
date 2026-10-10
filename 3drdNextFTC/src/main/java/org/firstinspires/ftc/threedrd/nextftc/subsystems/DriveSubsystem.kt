package org.firstinspires.ftc.threedrd.nextftc.subsystems

import com.pedropathing.api.*
import com.pedropathing.math.*
import com.pedropathing.paths.*
import dev.nextftc.core.commands.delays.*
import dev.nextftc.core.units.*
import org.firstinspires.ftc.threedrd.nextftc.commands.*
import org.firstinspires.ftc.threedrd.pedropathing.*
import org.firstinspires.ftc.threedrd.pedropathing.PedroComponent.Companion.follower
import org.firstinspires.ftc.threedrd.pedropathing.PedroComponent.Companion.progress

abstract class DriveSubsystem : Subsystem() {
    protected open val headingEnd = 0.33
    var turnTolerance = 1.deg

    val hold by instant { follower.hold(follower.pose()) }
    val stop by instant { follower.stopNow() }

    override fun stop() = follower.stopNow()

    fun follow(path: Path, holdEnd: Boolean? = null) = FollowPath(path, holdEnd).requires(this)

    fun paths(holdEnd: Boolean = false, build: () -> Path) =
        DeferredCommand(this) { follow(build(), holdEnd) }.named("${javaClass.simpleName}.paths")

    fun to(pose: Pose, holdEnd: Boolean = true) = paths(holdEnd) {
        val start = follower.pose()
        Paths.curve(start, start.midpoint(pose), pose).linear(start, pose, headingEnd)
    }.named("${javaClass.simpleName}.to")

    fun curve(vararg poses: Pose, holdEnd: Boolean = true) = paths(holdEnd) {
        require(poses.isNotEmpty()) { "curve requires an endpoint" }
        val start = follower.pose()
        val points = mutableListOf(start)
        points.addAll(poses)
        if (points.size < 3) points.add(1, start.midpoint(poses.last()))
        Paths.curve(*points.toTypedArray()).linear(start, poses.last(), headingEnd)
    }.named("${javaClass.simpleName}.curve")

    fun curves(vararg poses: Pose, holdEnd: Boolean = true) = paths(holdEnd) {
        var start = follower.pose()
        Paths.path(*poses.map { end ->
            val path = Paths.curve(start, start.midpoint(end), end).linear(start, end, headingEnd)
            start = end
            path
        }.toTypedArray())
    }.named("${javaClass.simpleName}.curves")

    fun forward(distance: Distance) = DeferredCommand(this) {
        to(follower.pose().axial(distance))
    }.named("${javaClass.simpleName}.forward")

    fun forward(distance: Double) = forward(distance.inches)

    fun strafe(distance: Distance) = DeferredCommand(this) {
        to(follower.pose().lateral(distance))
    }.named("${javaClass.simpleName}.strafe")

    fun strafe(distance: Double) = strafe(distance.inches)

    fun turn(angle: Angle) = TurnBy(angle, turnTolerance).requires(this).named("${javaClass.simpleName}.turn")

    fun turn(degrees: Double) = turn(degrees.deg)

    fun until(distance: Distance) = WaitUntil {
        if (distance.inIn >= 0) progress.traveled(follower) >= distance.inIn
        else progress.remaining(follower) < -distance.inIn
    }

    fun until(completion: PathCompletion) = WaitUntil {
        if (completion.value >= 0) progress.completion(follower) >= completion.value
        else progress.completion(follower) < 1 + completion.value
    }

    fun until(t: PathT) = WaitUntil {
        if (t.value >= 0) progress.t(follower) >= t.value
        else progress.t(follower) < 1 + t.value
    }

    fun untilNotBusy() = WaitUntil { !follower.following() }
}
