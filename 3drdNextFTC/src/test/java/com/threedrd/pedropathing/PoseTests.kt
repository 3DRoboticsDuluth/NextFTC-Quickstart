package com.threedrd.pedropathing

import com.pedropathing.geometry.*
import dev.nextftc.core.units.*
import kotlin.math.*
import org.junit.Assert.*
import org.junit.*

class PoseTests {
    @Test
    fun isFacingUsesSignedShortestBearingAndInclusiveTypedToleranceWithoutMutatingPoses() {
        val target = Pose(10.0, 0.0, 2.0)
        for ((heading, expected) in listOf(
            0.0 to true, PI / 2 to true, -PI / 2 to true,
            PI to false, PI / 2 + 0.01 to false, -PI / 2 - 0.01 to false,
            2 * PI to true, 3 * PI / 2 to true
        )) {
            val pose = Pose(0.0, 0.0, heading)
            assertEquals(expected, pose.isFacing(target, 90.deg))
            assertEquals(heading, pose.heading, 0.0)
            assertEquals(0.0, pose.x, 0.0)
            assertEquals(0.0, pose.y, 0.0)
        }
        assertTrue(Pose(5.0, -5.0, PI / 4).isFacing(target, 0.deg))
        assertFalse(Pose(5.0, -5.0, 0.0).isFacing(target, 30.deg))
        assertTrue(Pose(5.0, -5.0, 0.0).isFacing(target, (PI / 4).rad))
        assertEquals(10.0, target.x, 0.0)
        assertEquals(0.0, target.y, 0.0)
        assertEquals(2.0, target.heading, 0.0)
    }

    @Test
    fun transformRotatesLocalOffsetsAndAddsMountingHeadingOnceWithoutMutatingInputs() {
        val local = Pose(4.0, 2.0, PI / 6)
        val cases = listOf(
            Triple(0.0, 14.0, 22.0),
            Triple(PI / 2, 8.0, 24.0),
            Triple(PI, 6.0, 18.0),
            Triple(-PI / 2, 12.0, 16.0)
        )
        for ((heading, x, y) in cases) {
            val parent = Pose(10.0, 20.0, heading)
            val transformed = parent.transform(local)
            assertEquals(x, transformed.x, 0.000000001)
            assertEquals(y, transformed.y, 0.000000001)
            assertEquals(heading + PI / 6, transformed.heading, 0.000000001)
            assertSame(parent.coordinateSystem, transformed.coordinateSystem)
            assertNotSame(parent, transformed)
            assertNotSame(local, transformed)
            assertEquals(10.0, parent.x, 0.0)
            assertEquals(20.0, parent.y, 0.0)
            assertEquals(heading, parent.heading, 0.0)
        }
        assertEquals(4.0, local.x, 0.0)
        assertEquals(2.0, local.y, 0.0)
        assertEquals(PI / 6, local.heading, 0.0)
    }

    @Test
    fun transformHandlesNegativeOffsetsAtANoncardinalHeading() {
        val transformed = Pose(-7.0, 3.0, PI / 4).transform(Pose(-4.0, 2.0, -PI / 3))
        assertEquals(-7.0 - 3 * sqrt(2.0), transformed.x, 0.000000001)
        assertEquals(3.0 - sqrt(2.0), transformed.y, 0.000000001)
        assertEquals(-PI / 12, transformed.heading, 0.000000001)
    }

    @Test
    fun transformWithZeroDisplacementPreservesPositionAndNativeHeadingSum() {
        val parent = Pose(10.0, 20.0, Math.toRadians(170.0))
        val transformed = parent.transform(Pose(0.0, 0.0, Math.toRadians(30.0)))
        assertEquals(10.0, transformed.x, 0.0)
        assertEquals(20.0, transformed.y, 0.0)
        assertEquals(Math.toRadians(200.0), transformed.heading, 0.000000001)
    }

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
