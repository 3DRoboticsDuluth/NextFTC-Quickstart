package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.follower.*
import com.pedropathing.paths.*

/** Resolves segment lengths once, rather than rebuilding a path in periodic waits. */
class PathProgress {
    private var remainingAfter = doubleArrayOf()
    var length = 0.0
        private set

    fun start(path: Path) {
        val lengths = path.segments.map { it.curve.length() }
        require(lengths.isNotEmpty()) { "A path must contain at least one segment" }
        length = lengths.sum()
        remainingAfter = DoubleArray(lengths.size)
        var remaining = 0.0
        var index = lengths.lastIndex
        while (index >= 0) {
            remainingAfter[index] = remaining
            remaining += lengths[index]
            index--
        }
    }

    fun remaining(follower: Follower): Double {
        val index = follower.pathIndex()
        if (index !in remainingAfter.indices) return 0.0
        return remainingAfter[index] + follower.currentCurve().remainingDistance(follower.parametricCompletion())
    }

    fun traveled(follower: Follower) = length - remaining(follower)
    fun completion(follower: Follower) = if (length == 0.0) 0.0 else traveled(follower) / length
    fun t(follower: Follower) = when {
        remainingAfter.isEmpty() -> 0.0
        follower.following() -> follower.parametricCompletion()
        else -> 1.0
    }
}
