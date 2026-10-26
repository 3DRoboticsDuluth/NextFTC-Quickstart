package org.firstinspires.ftc.threedrd.pedropathing

import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import dev.nextftc.hardware.driving.*
import java.util.function.*

class PedroDriverControlled(
    drivePower: Supplier<Double>,
    strafePower: Supplier<Double>,
    turnPower: Supplier<Double>,
    val robotCentric: () -> Boolean,
    val headingOffset: () -> Double = { 0.0 },
    val slew: DriverSlewParameters? = null,
) : DriverControlledCommand(drivePower, strafePower, turnPower) {
    var drive: (Double) -> Double = { it }
    var strafe: (Double) -> Double = { it }
    var turn: (Double) -> Double = { it }
    private val shaping = slew?.let { DriverCommandSlew(it) }
    private var previousCentric: Boolean? = null
    private var previousOffset = 0.0

    override fun start() {
        shaping?.reset()
        previousCentric = null
        follower.startTeleopDrive()
    }

    override fun calculateAndSetPowers(powers: DoubleArray) {
        val centric = robotCentric()
        val offset = headingOffset()
        val shaped = shaping?.let {
            val previous = previousCentric
            if (previous != null && (previous != centric || previousOffset != offset)) {
                val heading = follower.pose.heading
                val oldFrame = previousOffset + if (previous) heading else 0.0
                val newFrame = offset + if (centric) heading else 0.0
                it.rotateTranslation(oldFrame - newFrame)
            }
            previousCentric = centric
            previousOffset = offset
            it.calculate(powers)
        } ?: powers
        follower.setTeleOpDrive(
            drive(shaped[0]),
            strafe(shaped[1]),
            turn(shaped[2]),
            centric,
            offset
        )
    }

    override fun stop(interrupted: Boolean) {
        shaping?.reset()
        previousCentric = null
        if (interrupted || shaping != null) follower.breakFollowing()
    }
}
