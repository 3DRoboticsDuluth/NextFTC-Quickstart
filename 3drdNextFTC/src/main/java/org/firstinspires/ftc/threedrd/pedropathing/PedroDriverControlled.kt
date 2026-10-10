package org.firstinspires.ftc.threedrd.pedropathing

import org.firstinspires.ftc.threedrd.pedropathing.PedroComponent.Companion.follower
import dev.nextftc.hardware.driving.*
import java.util.function.*
import com.pedropathing.drivetrain.*
import com.pedropathing.follower.*

class PedroDriverControlled(
    drivePower: Supplier<Double>,
    strafePower: Supplier<Double>,
    turnPower: Supplier<Double>,
    val robotCentric: () -> Boolean,
    val headingOffset: () -> Double = { 0.0 }
) : DriverControlledCommand(drivePower, strafePower, turnPower) {
    var drive: (Double) -> Double = { it }
    var strafe: (Double) -> Double = { it }
    var turn: (Double) -> Double = { it }

    override fun start() = follower.manual(DrivePowers.zero())

    override fun calculateAndSetPowers(powers: DoubleArray) {
        val input = DrivePowers(drive(powers[0]), strafe(powers[1]), turn(powers[2]))
        follower.manual(if (robotCentric()) input else ManualDrive.fieldCentric(input, follower.pose().heading(), headingOffset()))
    }

    override fun stop(interrupted: Boolean) {
        if (interrupted) follower.stopNow()
    }
}
