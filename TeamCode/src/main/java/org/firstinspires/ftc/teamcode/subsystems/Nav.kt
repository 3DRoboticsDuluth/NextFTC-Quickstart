package org.firstinspires.ftc.teamcode.subsystems

import dev.nextftc.core.units.*
import org.firstinspires.ftc.threedrd.nextftc.subsystems.*
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.Constants.robotLength
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.Constants.robotWidth

object Nav : NavSubsystem(robotLength, robotWidth) {
    val start = pose(0.inches, 0.inches, 0.deg)
    val end = pose(24.inches, 0.inches, 0.deg)
}
