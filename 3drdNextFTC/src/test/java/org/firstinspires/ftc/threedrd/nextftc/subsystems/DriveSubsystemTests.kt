package org.firstinspires.ftc.threedrd.nextftc.subsystems

import com.pedropathing.follower.*
import com.pedropathing.geometry.*
import com.pedropathing.paths.*
import dev.nextftc.core.units.*
import dev.nextftc.extensions.pedro.*
import org.firstinspires.ftc.threedrd.pedropathing.*
import org.firstinspires.ftc.threedrd.testing.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.*
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*

class DriveSubsystemTests : SubsystemTests() {
    private val drive = TestDriveSubsystem()
    lateinit var follower: Follower
    lateinit var component: PedroComponent
    lateinit var builder: PathBuilder
    lateinit var chain: PathChain

    @Before
    fun setUp() {
        follower = mock(Follower::class.java)
        builder = mock(PathBuilder::class.java)
        chain = mock(PathChain::class.java)
        `when`(follower.pathBuilder()).thenReturn(builder)
        `when`(builder.addPath(any(Curve::class.java))).thenReturn(builder)
        `when`(builder.setLinearHeadingInterpolation(anyDouble(), anyDouble(), anyDouble()))
            .thenReturn(builder)
        `when`(builder.build()).thenReturn(chain)
        component = PedroComponent { follower }.apply { preInit() }
    }

    @After
    fun tearDown() = component.postStop()

    @Test
    fun followsPedroPathsAndBuildsCurves() {
        val start = Pose(1.0, 2.0, 0.25)
        val middle = Pose(2.0, 3.0, 0.75)
        val end = Pose(3.0, 4.0, 1.25)
        val path = Path(BezierLine(start, end))
        `when`(follower.pose).thenReturn(start)

        drive.follow(path).start()
        drive.follow(PathChain(path)).start()
        drive.follow(PathChain(path), false, 0.5).start()
        verify(follower, times(2)).followPath(any(PathChain::class.java))
        verify(follower).followPath(any(PathChain::class.java),
            org.mockito.ArgumentMatchers.eq(0.5), org.mockito.ArgumentMatchers.eq(false))

        clearInvocations(builder, follower)
        drive.paths { addPath(BezierLine(start, end)) }.start()
        drive.to(end).start()
        drive.curve(end, holdEnd = false).start()
        drive.curve(middle, end).start()
        drive.curves(middle, end).start()
        verify(builder, times(6)).addPath(any(Curve::class.java))
    }

    @Test
    fun movesRelativeToThePoseAvailableAtExecutionTime() {
        `when`(follower.pose).thenReturn(Pose(10.0, 20.0, Math.PI / 2))

        drive.forward(5.0).start()
        var curve = ArgumentCaptor.forClass(Curve::class.java)
        verify(builder).addPath(curve.capture())
        assertEquals(10.0, curve.value.controlPoints.last().x, 0.0001)
        assertEquals(25.0, curve.value.controlPoints.last().y, 0.0001)

        clearInvocations(builder, follower)
        drive.strafe(5.inches).start()
        curve = ArgumentCaptor.forClass(Curve::class.java)
        verify(builder).addPath(curve.capture())
        assertEquals(5.0, curve.value.controlPoints.last().x, 0.0001)
        assertEquals(20.0, curve.value.controlPoints.last().y, 0.0001)

        clearInvocations(builder, follower)
        drive.strafe(5.0).start()
        verify(builder).addPath(any(Curve::class.java))

        drive.turn(90.0).start()
        drive.turn(90.deg).start()
        verify(follower, times(2)).turn(Math.PI / 2, true)
    }

    @Test
    fun exposesPathProgressConditions() {
        val distance = drive.until(10.inches)
        `when`(follower.distanceTraveledOnPath).thenReturn(9.0, 10.0)
        assertFalse(distance.isDone)
        assertTrue(distance.isDone)

        val remaining = drive.until((-10).inches)
        `when`(follower.distanceRemaining).thenReturn(10.0, 9.0)
        assertFalse(remaining.isDone)
        assertTrue(remaining.isDone)

        val completion = drive.until(75.pct)
        `when`(follower.pathCompletion).thenReturn(0.74, 0.75)
        assertFalse(completion.isDone)
        assertTrue(completion.isDone)

        val completionFromEnd = drive.until((-25).pct)
        `when`(follower.pathCompletion).thenReturn(0.75, 0.74)
        assertFalse(completionFromEnd.isDone)
        assertTrue(completionFromEnd.isDone)

        val t = drive.until(50.pctT)
        `when`(follower.currentTValue).thenReturn(0.49, 0.5)
        assertFalse(t.isDone)
        assertTrue(t.isDone)

        val tFromEnd = drive.until((-50).pctT)
        `when`(follower.currentTValue).thenReturn(0.5, 0.49)
        assertFalse(tFromEnd.isDone)
        assertTrue(tFromEnd.isDone)

        val idle = drive.untilNotBusy()
        `when`(follower.isBusy).thenReturn(true, false)
        assertFalse(idle.isDone)
        assertTrue(idle.isDone)
    }

    @Test
    fun holdStopAndLifecycleStopControlPedro() {
        val pose = Pose(1.0, 2.0, 0.5)
        `when`(follower.pose).thenReturn(pose)

        drive.hold.start()
        verify(follower).holdPoint(pose)
        drive.stop.start()
        drive.stop()
        verify(follower, times(2)).breakFollowing()
    }
}

private class TestDriveSubsystem : DriveSubsystem()
