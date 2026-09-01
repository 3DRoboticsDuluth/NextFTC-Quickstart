package org.firstinspires.ftc.teamcode.subsystems

import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Subsystem

object Timing : Subsystem() {
    var playTimer = ElapsedTime()
    var periodicTimer = ElapsedTime()

    override fun initialize() {
        playTimer.reset()
        periodicTimer.reset()
    }

    override fun start() = playTimer.reset()

    override fun periodic() {
        val milliseconds = periodicTimer.milliseconds()

        tel.debug("Runtime (s)", "%.1f".format(playTimer.seconds()))
        tel.debug("Loop (ms)", "%.0f".format(milliseconds))
        tel.debug("Rate (Hz)", "%.1f".format(1000 / milliseconds))

        periodicTimer.reset()
    }
}
