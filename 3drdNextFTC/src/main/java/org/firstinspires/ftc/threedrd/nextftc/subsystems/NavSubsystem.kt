package org.firstinspires.ftc.threedrd.nextftc.subsystems

import com.pedropathing.geometry.*
import com.pedropathing.paths.*
import dev.nextftc.core.units.*
import kotlin.math.*
import org.firstinspires.ftc.threedrd.pedropathing.*

abstract class NavSubsystem(
    val robotLength: Distance,
    val robotWidth: Distance
) {

    fun pose(
        x: Distance,
        y: Distance,
        heading: Double = 0.0,
        axial: Axial = Axial.CENTER,
        lateral: Lateral = Lateral.CENTER,
        axialOffset: Distance = 0.inches,
        lateralOffset: Distance = 0.inches
    ): Pose {
        val axialHeading = heading.normalizeHeading()
        val adjustedAxial = axialOffset.inIn - axial.sign * robotLength.inIn / 2
        val lateralHeading = (heading + PI / 2).normalizeHeading()
        val adjustedLateral = lateralOffset.inIn - lateral.sign * robotWidth.inIn / 2

        return Pose(
            x.inIn + cos(axialHeading) * adjustedAxial + cos(lateralHeading) * adjustedLateral,
            y.inIn + sin(axialHeading) * adjustedAxial + sin(lateralHeading) * adjustedLateral,
            heading.normalizeHeading()
        )
    }

    fun pose(
        x: Distance,
        y: Distance,
        heading: Angle,
        axial: Axial = Axial.CENTER,
        lateral: Lateral = Lateral.CENTER,
        axialOffset: Distance = 0.inches,
        lateralOffset: Distance = 0.inches
    ) = pose(x, y, heading.inRad, axial, lateral, axialOffset, lateralOffset)

    fun line(start: Pose, end: Pose) = Path(BezierLine(start, end)).apply {
        setLinearHeadingInterpolation(start.heading, end.heading)
    }
}

enum class Axial(val sign: Int) {
    FRONT(+1), CENTER(0), BACK(-1)
}

enum class Lateral(val sign: Int) {
    LEFT(+1), CENTER(0), RIGHT(-1)
}

