package org.firstinspires.ftc.threedrd.nextftc.subsystems

import com.pedropathing.api.*
import com.pedropathing.follower.*
import com.pedropathing.math.*
import com.pedropathing.paths.*
import dev.nextftc.core.units.deg
import dev.nextftc.core.units.inches
import org.firstinspires.ftc.threedrd.pedropathing.*
import org.firstinspires.ftc.threedrd.testing.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.*
import org.mockito.Mockito.*

class DriveSubsystemTests : SubsystemTests() {
    private val drive = TestDriveSubsystem()
    lateinit var follower: Follower
    lateinit var component: PedroComponent

    @Before
    fun setUp() {
        follower = mockFollower()
        component = PedroComponent { follower }.apply { preInit() }
    }

    @After
    fun tearDown() = component.postStop()

    private fun endpoint(command: dev.nextftc.core.commands.Command): Pose {
        clearInvocations(follower)
        command.start()
        val path = ArgumentCaptor.forClass(Path::class.java)
        verify(follower).follow(path.capture())
        assertTrue(command.requirements.contains(drive))
        return path.value.endPose()
    }

    @Test
    fun followsNativePathsAndBuildsDeferredCurves() {
        val start = Pose(1.0, 2.0, 0.25)
        val middle = Pose(2.0, 3.0, 0.75)
        val end = Pose(3.0, 4.0, 1.25)
        `when`(follower.pose()).thenReturn(start)
        val path = Paths.line(start, end).linear(start, end)
        val command = drive.follow(path)
        command.start()
        verify(follower).follow(path)
        assertTrue(command.requirements.contains(drive))
        assertPose(end, endpoint(drive.paths { path }))
        assertPose(end, endpoint(drive.to(end)))
        assertPose(end, endpoint(drive.curve(end, holdEnd = false)))
        assertPose(end, endpoint(drive.curve(middle, end)))
        assertPose(end, endpoint(drive.curves(middle, end)))
        assertThrows(IllegalArgumentException::class.java) { drive.curve().start() }
    }

    @Test
    fun movesRelativeToThePoseAvailableAtExecutionTime() {
        val forward = drive.forward(5.0)
        `when`(follower.pose()).thenReturn(Pose(10.0, 20.0, Math.PI / 2))
        val end = endpoint(forward)
        assertEquals(10.0, end.x(), 0.0001)
        assertEquals(25.0, end.y(), 0.0001)
        val strafe = endpoint(drive.strafe(5.inches))
        assertEquals(5.0, strafe.x(), 0.0001)
        assertEquals(20.0, strafe.y(), 0.0001)
        assertPose(strafe, endpoint(drive.strafe(5.0)))
        drive.turn(90.0).start()
        drive.turn(90.deg).start()
        val target = ArgumentCaptor.forClass(Pose::class.java)
        verify(follower, times(2)).hold(target.capture())
        target.allValues.forEach { assertPose(Pose(10.0, 20.0, Math.PI), it) }
        drive.turnTolerance = 2.deg
        assertEquals(2.0, drive.turnTolerance.inDeg, 0.0)
    }

    @Test
    fun exposesWholePathDistanceAndCompletionConditions() {
        val path = Paths.line(Pose.zero(), Pose(20.0, 0.0)).constant(0.0)
        PedroComponent.progress.start(path)
        `when`(follower.pathIndex()).thenReturn(0)
        `when`(follower.currentCurve()).thenReturn(path.curve)
        val distance = drive.until(10.inches)
        `when`(follower.parametricCompletion()).thenReturn(0.45, 0.5)
        assertFalse(distance.isDone)
        assertTrue(distance.isDone)
        val remaining = drive.until((-10).inches)
        `when`(follower.parametricCompletion()).thenReturn(0.5, 0.55)
        assertFalse(remaining.isDone)
        assertTrue(remaining.isDone)
        val completion = drive.until(75.pct)
        `when`(follower.parametricCompletion()).thenReturn(0.74, 0.75)
        assertFalse(completion.isDone)
        assertTrue(completion.isDone)
        val fromEnd = drive.until((-25).pct)
        `when`(follower.parametricCompletion()).thenReturn(0.75, 0.74)
        assertFalse(fromEnd.isDone)
        assertTrue(fromEnd.isDone)
        `when`(follower.following()).thenReturn(true)
        val t = drive.until(50.pctT)
        `when`(follower.parametricCompletion()).thenReturn(0.49, 0.5)
        assertFalse(t.isDone)
        assertTrue(t.isDone)
        val tFromEnd = drive.until((-50).pctT)
        `when`(follower.parametricCompletion()).thenReturn(0.5, 0.49)
        assertFalse(tFromEnd.isDone)
        assertTrue(tFromEnd.isDone)
        val idle = drive.untilNotBusy()
        `when`(follower.following()).thenReturn(true, false)
        assertFalse(idle.isDone)
        assertTrue(idle.isDone)
    }

    @Test
    fun holdAndBothStopEntryPointsActImmediately() {
        val pose = Pose(1.0, 2.0, 0.5)
        `when`(follower.pose()).thenReturn(pose)
        drive.hold.start()
        verify(follower).hold(pose)
        drive.stop.start()
        drive.stop()
        verify(follower, times(2)).stop()
        verify(follower.drivetrain, times(2)).stop()
    }
}

private class TestDriveSubsystem : DriveSubsystem()
