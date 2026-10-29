package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.geometry.*
import dev.nextftc.core.units.*
import kotlin.math.*
import org.junit.Assert.*
import org.junit.*

class PoseTests {
    @Test
    fun directionUsesFieldDisplacementRegardlessOfEitherHeadingAndDoesNotMutatePoses() {
        val origin = Pose(10.0, 20.0, PI / 2)
        val goal = Pose(13.0, 24.0, -PI)
        val direction = origin.directionTo(goal)
        assertEquals(0.6, direction.xComponent, 0.000000000001)
        assertEquals(0.8, direction.yComponent, 0.000000000001)
        assertEquals(1.0, direction.magnitude, 0.000000000001)
        assertEquals(-0.6, goal.directionTo(origin).xComponent, 0.000000000001)
        assertEquals(-0.8, goal.directionTo(origin).yComponent, 0.000000000001)
        assertEquals(10.0, origin.x, 0.0)
        assertEquals(20.0, origin.y, 0.0)
        assertEquals(PI / 2, origin.heading, 0.0)
        assertEquals(13.0, goal.x, 0.0)
        assertEquals(24.0, goal.y, 0.0)
        assertEquals(-PI, goal.heading, 0.0)
        val changedHeading = origin.withHeading(0.0).directionTo(goal.withHeading(0.0))
        assertEquals(direction.xComponent, changedHeading.xComponent, 0.0)
        assertEquals(direction.yComponent, changedHeading.yComponent, 0.0)
    }

    @Test
    fun directionPreservesExactZeroComponentsAlongBothAxes() {
        val origin = Pose(10.0, 20.0)
        for ((dx, dy) in listOf(5.0 to 0.0, -5.0 to 0.0, 0.0 to 5.0, 0.0 to -5.0)) {
            val direction = origin.directionTo(Pose(origin.x + dx, origin.y + dy))
            assertEquals(dx / 5, direction.xComponent, 0.0)
            assertEquals(dy / 5, direction.yComponent, 0.0)
            assertEquals(1.0, direction.magnitude, 0.0)
        }
    }

    @Test
    fun coincidentPositionsReturnFreshZeroVectorsEvenWhenHeadingsDiffer() {
        val origin = Pose(10.0, 20.0, PI)
        val goal = Pose(10.0, 20.0, 0.0)
        val direction = origin.directionTo(goal)
        assertEquals(0.0, direction.xComponent, 0.0)
        assertEquals(0.0, direction.yComponent, 0.0)
        assertEquals(0.0, direction.magnitude, 0.0)
        direction.setOrthogonalComponents(3.0, 4.0)
        val next = origin.directionTo(goal)
        assertNotSame(direction, next)
        assertEquals(0.0, next.magnitude, 0.0)
    }

    @Test
    fun translatesRelativeToHeading() {
        val pose = Pose(10.0, 20.0, PI / 2)

        val axial = pose.axial(5.0)
        val lateral = pose.lateral(5.0)
        val measuredAxial = pose.axial(5.inches)
        val measuredLateral = pose.lateral(5.inches)

        assertEquals(10.0, axial.x, 0.0001)
        assertEquals(25.0, axial.y, 0.0001)
        assertEquals(5.0, lateral.x, 0.0001)
        assertEquals(20.0, lateral.y, 0.0001)
        assertEquals(axial.x, measuredAxial.x, 0.0)
        assertEquals(axial.y, measuredAxial.y, 0.0)
        assertEquals(lateral.x, measuredLateral.x, 0.0)
        assertEquals(lateral.y, measuredLateral.y, 0.0)
    }

    @Test
    fun transformsHeadingAndFacesPoses() {
        val pose = Pose(1.0, 2.0, Math.toRadians(170.0))

        assertEquals(Math.toRadians(-10.0), pose.reverse().heading, 0.0001)
        assertEquals(0.0, pose.face(Pose(2.0, 2.0)).heading, 0.0001)
        assertEquals(PI / 2, pose.face(Pose(2.0, 2.0), 90.0).heading, 0.0001)
        assertEquals(Math.toRadians(-170.0), pose.turn(20.0).heading, 0.0001)
    }

    @Test
    fun midpointUsesTheShortestHeadingDifference() {
        val midpoint = Pose(0.0, 0.0, Math.toRadians(170.0))
            .midpoint(Pose(2.0, 4.0, Math.toRadians(-170.0)))

        assertEquals(1.0, midpoint.x, 0.0)
        assertEquals(2.0, midpoint.y, 0.0)
        assertEquals(PI, midpoint.heading, 0.0001)
    }

    @Test
    fun normalizesAnyNumberOfRevolutions() {
        assertEquals(PI, (5 * PI).normalizeHeading(), 0.0)
        assertEquals(-PI, (-5 * PI).normalizeHeading(), 0.0)
    }
}
