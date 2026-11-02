package com.threedrd.nextftc.subsystems

import com.pedropathing.geometry.*
import com.pedropathing.paths.*
import dev.nextftc.core.commands.delays.*
import dev.nextftc.core.units.*
import dev.nextftc.extensions.pedro.*
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import com.threedrd.nextftc.commands.*
import com.threedrd.pedropathing.*

abstract class DriveSubsystem : Subsystem() {
    protected open val headingEnd = 0.33

    val hold by instant { follower.holdPoint(follower.pose) }
    val stop by instant { follower.breakFollowing() }

    override fun stop() {
        follower.breakFollowing()
    }

    fun follow(path: Path, holdEnd: Boolean? = null, maxPower: Double? = null) =
        FollowPath(path, holdEnd, maxPower).requires(this)

    fun follow(path: PathChain, holdEnd: Boolean? = null, maxPower: Double? = null) =
        FollowPath(path, holdEnd, maxPower).requires(this)

    fun paths(holdEnd: Boolean = false, build: PathBuilder.() -> Unit) =
        DeferredCommand(this) {
            val builder = follower.pathBuilder().apply(build)
            follow(builder.build(), holdEnd)
        }.named("${javaClass.simpleName}.paths")

    fun to(pose: Pose, holdEnd: Boolean = true) = paths(holdEnd) {
        val start = follower.pose
        addPath(BezierCurve(start, start.midpoint(pose), pose))
        setLinearHeadingInterpolation(start.heading, pose.heading, headingEnd)
    }.named("${javaClass.simpleName}.to")

    fun curve(vararg poses: Pose, holdEnd: Boolean = true) = paths(holdEnd) {
        val start = follower.pose
        val points = mutableListOf<FuturePose>(start)
        points.addAll(poses)
        if (points.size < 3) points.add(1, start.midpoint(poses.last()))
        addPath(BezierCurve(*points.toTypedArray()))
        setLinearHeadingInterpolation(start.heading, poses.last().heading, headingEnd)
    }.named("${javaClass.simpleName}.curve")

    fun curves(vararg poses: Pose, holdEnd: Boolean = true) = paths(holdEnd) {
        var start = follower.pose
        poses.forEach { end ->
            addPath(BezierCurve(start, start.midpoint(end), end))
            setLinearHeadingInterpolation(start.heading, end.heading, headingEnd)
            start = end
        }
    }.named("${javaClass.simpleName}.curves")

    fun axial(distance: Distance) = DeferredCommand(this) {
        to(follower.pose.axial(distance.inIn))
    }.named("${javaClass.simpleName}.axial")

    fun axial(distance: Double) = axial(distance.inches)

    fun lateral(distance: Distance) = DeferredCommand(this) {
        to(follower.pose.lateral(distance.inIn))
    }.named("${javaClass.simpleName}.lateral")

    fun lateral(distance: Double) = lateral(distance.inches)

    @Deprecated("Use axial for signed robot-relative movement", ReplaceWith("axial(distance)"))
    fun forward(distance: Distance) = axial(distance)

    @Deprecated("Use axial for signed robot-relative movement", ReplaceWith("axial(distance)"))
    fun forward(distance: Double) = axial(distance)

    @Deprecated("Use lateral for signed robot-relative movement", ReplaceWith("lateral(distance)"))
    fun strafe(distance: Distance) = lateral(distance)

    @Deprecated("Use lateral for signed robot-relative movement", ReplaceWith("lateral(distance)"))
    fun strafe(distance: Double) = lateral(distance)

    fun turn(angle: Angle) = TurnBy(angle).requires(this).named("${javaClass.simpleName}.turn")

    fun turn(degrees: Double) = turn(degrees.deg)

    fun until(distance: Distance) = WaitUntil {
        if (distance.inIn >= 0) follower.distanceTraveledOnPath >= distance.inIn
        else follower.distanceRemaining < -distance.inIn
    }

    fun until(completion: PathCompletion) = WaitUntil {
        if (completion.value >= 0) follower.pathCompletion >= completion.value
        else follower.pathCompletion < 1 + completion.value
    }

    fun until(t: PathT) = WaitUntil {
        if (t.value >= 0) follower.currentTValue >= t.value
        else follower.currentTValue < 1 + t.value
    }

    fun untilNotBusy() = WaitUntil { !follower.isBusy }
}

