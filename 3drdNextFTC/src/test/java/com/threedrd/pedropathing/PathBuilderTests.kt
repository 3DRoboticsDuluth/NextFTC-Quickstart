package com.threedrd.pedropathing

import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathBuilder
import com.pedropathing.paths.PathConstraints
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock

class PathBuilderTests {
    @Test
    fun interpolatesAllWaypointsWithMatchingJoinTangents() {
        val points = arrayOf(Pose(1.0, 2.0), Pose(4.0, 8.0), Pose(10.0, 3.0), Pose(15.0, 7.0), Pose(20.0, -2.0))
        val builder = PathBuilder(mock(Follower::class.java), PathConstraints.defaultConstraints)
        assertSame(builder, builder.through(points.first(), *points.drop(1).toTypedArray()))
        val chain = builder.build()
        assertEquals(points.size - 1, chain.size())
        for (index in 0 until chain.size()) {
            val curve = chain.getPath(index)
            assertEquals(points[index].x, curve.getPose(0.0).x, 0.000001)
            assertEquals(points[index].y, curve.getPose(0.0).y, 0.000001)
            assertEquals(points[index + 1].x, curve.getPose(1.0).x, 0.000001)
            assertEquals(points[index + 1].y, curve.getPose(1.0).y, 0.000001)
            if (index + 1 < chain.size()) {
                val leaving = curve.curve.getDerivative(1.0)
                val entering = chain.getPath(index + 1).curve.getDerivative(0.0)
                assertEquals(leaving.xComponent, entering.xComponent, 0.000001)
                assertEquals(leaving.yComponent, entering.yComponent, 0.000001)
            }
        }
    }

    @Test
    fun singleDestinationProducesAStraightCurveAndRepeatedPointsStayFinite() {
        val follower = mock(Follower::class.java)
        val start = Pose(2.0, 4.0)
        val end = Pose(8.0, 10.0)
        val straight = PathBuilder(follower, PathConstraints.defaultConstraints).through(start, end).build()
        assertEquals(1, straight.size())
        assertEquals(5.0, straight.getPath(0).getPose(0.5).x, 0.000001)
        assertEquals(7.0, straight.getPath(0).getPose(0.5).y, 0.000001)
        val repeated = PathBuilder(follower, PathConstraints.defaultConstraints).through(start, start, end, end).build()
        for (index in 0 until repeated.size()) {
            for (t in listOf(0.0, 0.5, 1.0)) {
                val pose = repeated.getPath(index).getPose(t)
                assertTrue(pose.x.isFinite())
                assertTrue(pose.y.isFinite())
            }
        }
    }

    @Test
    fun rejectsMissingDestinations() {
        val builder = PathBuilder(mock(Follower::class.java), PathConstraints.defaultConstraints)
        assertThrows(IllegalArgumentException::class.java) { builder.through(Pose()) }
    }
}
