package org.firstinspires.ftc.teamcode.subsystems

import dev.nextftc.core.units.deg
import dev.nextftc.core.units.inches
import kotlin.math.PI
import org.junit.Assert.assertEquals
import org.junit.Test

class NavTests {
    @Test
    fun usesTemplateRobotDimensionsAndProvidesTypedPoses() {
        assertEquals(18.0, Nav.robotLength.inIn, 0.0)
        assertEquals(18.0, Nav.robotWidth.inIn, 0.0)

        val pose = Nav.pose(12.inches, 24.inches, 90.deg)

        assertEquals(12.0, pose.x, 0.0001)
        assertEquals(24.0, pose.y, 0.0001)
        assertEquals(PI / 2, pose.heading, 0.0)
        assertEquals(0.0, Nav.start.x, 0.0)
        assertEquals(0.0, Nav.start.y, 0.0)
        assertEquals(0.0, Nav.start.heading, 0.0)
        assertEquals(24.0, Nav.end.x, 0.0001)
        assertEquals(0.0, Nav.end.y, 0.0)
        assertEquals(0.0, Nav.end.heading, 0.0)
    }
}
