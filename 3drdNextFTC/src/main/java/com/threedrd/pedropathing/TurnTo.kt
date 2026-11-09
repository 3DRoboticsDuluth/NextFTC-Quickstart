package com.threedrd.pedropathing

import dev.nextftc.core.commands.Command
import dev.nextftc.core.units.Angle
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower

/** Absolute heading turn that preserves direction around Pedro 2.1.2's turnTo bug. */
class TurnTo(val heading: Angle) : Command() {
    init {
        named("TurnTo($heading)")
    }

    override fun start() {
        follower.turn((heading.inRad - follower.heading).normalizeHeading())
    }

    override val isDone get() = !follower.isTurning

    override fun stop(interrupted: Boolean) {
        if (interrupted) follower.breakFollowing()
    }
}
