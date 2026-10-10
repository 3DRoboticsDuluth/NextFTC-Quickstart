package org.firstinspires.ftc.threedrd.nextftc.subsystems

import com.pedropathing.math.*
import org.firstinspires.ftc.threedrd.pedropathing.assertPose
import dev.nextftc.core.units.deg
import dev.nextftc.core.units.inches
import kotlin.math.*
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Axial.*
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Lateral.*
import org.junit.Assert.*
import org.junit.*

class NavSubsystemTests {
    val nav = object : NavSubsystem(10.inches, 6.inches) {}

    @Test
    fun createsCenteredAndRobotRelativePoses() {
        val defaulted = nav.pose(20.inches, 30.inches, 0.0)
        assertEquals(20.0, defaulted.x(), 0.0)
        assertEquals(30.0, defaulted.y(), 0.0)

        val centered = nav.pose(20.inches, 30.inches, 90.deg)
        assertEquals(20.0, centered.x(), 0.0)
        assertEquals(30.0, centered.y(), 0.0)
        assertEquals(PI / 2, centered.heading(), 0.0)

        val offset = nav.pose(
            20.inches, 30.inches, PI / 2,
            axial = FRONT,
            lateral = LEFT,
            axialOffset = 2.inches,
            lateralOffset = 1.inches
        )
        assertEquals(22.0, offset.x(), 0.0001)
        assertEquals(27.0, offset.y(), 0.0001)

        val opposite = nav.pose(20.inches, 30.inches, axial = BACK, lateral = RIGHT)
        assertEquals(25.0, opposite.x(), 0.0001)
        assertEquals(33.0, opposite.y(), 0.0001)
    }

    @Test
    fun connectsPosesWithLinearHeadingInterpolation() {
        val start = Pose(1.0, 2.0, 0.25)
        val end = Pose(3.0, 4.0, 1.25)

        val path = nav.line(start, end)

        assertPose(start, path.get(0.0))
        assertPose(end, path.get(1.0))
        assertEquals(start.heading(), path.heading(0.0), 0.0)
        assertEquals(end.heading(), path.heading(1.0), 0.0)
    }

    @Test
    fun positioningDirectionsExposeExpectedSigns() {
        assertEquals(1, FRONT.sign)
        assertEquals(0, Axial.CENTER.sign)
        assertEquals(-1, BACK.sign)
        assertEquals(1, LEFT.sign)
        assertEquals(0, Lateral.CENTER.sign)
        assertEquals(-1, RIGHT.sign)
    }
}
