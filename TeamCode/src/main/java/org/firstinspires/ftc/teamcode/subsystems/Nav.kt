package org.firstinspires.ftc.teamcode.subsystems

import dev.nextftc.core.units.deg
import dev.nextftc.core.units.inches
import org.firstinspires.ftc.threedrd.nextftc.subsystems.NavSubsystem
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.Constants.robotLength
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.Constants.robotWidth

object Nav : NavSubsystem(robotLength, robotWidth) {
    val start = pose(0.inches, 0.inches, 0.deg)
    val end = pose(24.inches, 0.inches, 0.deg)
}
