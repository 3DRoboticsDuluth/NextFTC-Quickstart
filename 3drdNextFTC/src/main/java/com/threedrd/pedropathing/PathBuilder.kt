package com.threedrd.pedropathing

import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathBuilder

fun PathBuilder.through(start: Pose, vararg poses: Pose): PathBuilder {
    require(poses.isNotEmpty()) { "through requires at least one destination pose" }
    val previous = start.minus(poses.first().minus(start))
    return curveThrough(previous, start, 0.5, *poses)
}
