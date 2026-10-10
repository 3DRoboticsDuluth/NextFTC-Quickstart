package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.paths.*
import dev.nextftc.core.commands.*
import org.firstinspires.ftc.threedrd.pedropathing.PedroComponent.Companion.follower
import org.firstinspires.ftc.threedrd.pedropathing.PedroComponent.Companion.progress

class FollowPath(val path: Path, val holdEnd: Boolean? = null) : Command() {
    private var previousHoldEnd = true

    override fun start() {
        progress.start(path)
        previousHoldEnd = follower.holdEnd.get()
        holdEnd?.let { follower.holdEnd.set(it) }
        try {
            follower.follow(path)
        } catch (failure: Throwable) {
            try {
                follower.stopNow()
            } finally {
                follower.holdEnd.set(previousHoldEnd)
            }
            throw failure
        }
    }

    // Foresight's busy flag alone does not describe the follower's terminal mode.
    override val isDone get() = !follower.following()

    override fun stop(interrupted: Boolean) {
        try {
            if (interrupted) follower.stopNow()
        } finally {
            follower.holdEnd.set(previousHoldEnd)
        }
    }
}
