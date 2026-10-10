package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.api.*
import com.pedropathing.algorithm.*
import com.pedropathing.drivetrain.*
import com.pedropathing.localization.*
import com.pedropathing.paths.*
import com.pedropathing.follower.*
import com.pedropathing.math.*
import dev.nextftc.core.units.deg
import dev.nextftc.ftc.*
import kotlin.math.*
import org.firstinspires.ftc.threedrd.testing.*
import org.junit.Assert.*
import org.junit.*
import org.mockito.*
import org.mockito.Mockito.*

class PedroIntegrationTests : SubsystemTests() {
    @Test
    fun componentCreatesUpdatesStopsAndReinitializesTheFollower() {
        val follower = mockFollower()
        val component = PedroComponent { map ->
            assertSame(ActiveOpMode.hardwareMap, map)
            follower
        }
        assertSame(follower, component.createFollower(ActiveOpMode.hardwareMap))
        component.preInit()
        assertSame(follower, PedroComponent.follower)
        component.preWaitForStart()
        component.preUpdate()
        verify(follower, times(2)).update()
        component.postStop()
        verify(follower).stop()
        verify(follower.drivetrain).stop()
        assertThrows(IllegalStateException::class.java) { PedroComponent.follower }
        component.postStop()
        verify(follower, times(1)).stop()
        component.preInit()
        assertEquals(0.0, PedroComponent.progress.length, 0.0)
        component.postStop()
    }

    @Test
    fun cleanupClearsTheFollowerEvenWhenHardwareStopFails() {
        val follower = mockFollower()
        val component = PedroComponent { follower }.apply { preInit() }
        doThrow(IllegalStateException("motor failed")).`when`(follower.drivetrain).stop()
        assertThrows(IllegalStateException::class.java) { component.postStop() }
        assertThrows(IllegalStateException::class.java) { PedroComponent.follower }
        component.postStop()
    }

    @Test
    fun followingKeepsHoldingUntilCompletionAndRestoresOptions() {
        val follower = mockFollower()
        val component = PedroComponent { follower }.apply { preInit() }
        try {
            val path = Paths.line(Pose.zero(), Pose(10.0, 0.0)).constant(0.0)
            val defaults = FollowPath(path)
            assertSame(path, defaults.path)
            assertNull(defaults.holdEnd)
            defaults.start()
            assertTrue(follower.holdEnd.get())
            `when`(follower.following()).thenReturn(true, false)
            assertFalse(defaults.isDone)
            assertTrue(defaults.isDone)
            defaults.stop(false)
            verify(follower, never()).stop()
            val noHold = FollowPath(path, false)
            noHold.start()
            assertFalse(follower.holdEnd.get())
            noHold.stop(true)
            assertTrue(follower.holdEnd.get())
            verify(follower).stop()
            verify(follower.drivetrain).stop()
            follower.holdEnd.set(false)
            val hold = FollowPath(path, true)
            hold.start()
            assertTrue(follower.holdEnd.get())
            hold.stop(false)
            assertFalse(follower.holdEnd.get())
            assertEquals(10.0, PedroComponent.progress.length, 0.0)
        } finally {
            component.postStop()
        }
    }

    @Test
    fun failedStartAndFailedCancellationRestoreFollowerOptions() {
        val follower = mockFollower()
        val component = PedroComponent { follower }.apply { preInit() }
        val path = Paths.line(Pose.zero(), Pose(10.0, 0.0)).constant(0.0)
        try {
            doThrow(IllegalStateException("follow failed")).`when`(follower).follow(path)
            assertThrows(IllegalStateException::class.java) { FollowPath(path, false).start() }
            assertTrue(follower.holdEnd.get())
            verify(follower.drivetrain).stop()
            doNothing().`when`(follower).follow(path)
            val command = FollowPath(path, false)
            command.start()
            doThrow(IllegalStateException("motor failed")).`when`(follower.drivetrain).stop()
            assertThrows(IllegalStateException::class.java) { command.stop(true) }
            assertTrue(follower.holdEnd.get())
            doNothing().`when`(follower.drivetrain).stop()
        } finally {
            component.postStop()
        }
    }

    @Test
    fun actualFollowerCompletionUsesModeDespiteAStaleAlgorithmBusyFlag() {
        val algorithm = mock(Algorithm::class.java)
        val drivetrain = mock(Drivetrain::class.java)
        val follower = Follower(mock(Localizer::class.java), drivetrain, algorithm)
        val component = PedroComponent { follower }.apply { preInit() }
        `when`(algorithm.isBusy).thenReturn(true)
        `when`(algorithm.calculatePath(any(), any(), any(), anyDouble())).thenAnswer { invocation ->
            invocation.getArgument<PathTracker>(1).advance()
            DrivePowers.zero()
        }
        try {
            val path = Paths.line(Pose.zero(), Pose(10.0, 0.0)).constant(0.0)
            val command = FollowPath(path, true)
            command.start()
            assertFalse(command.isDone)
            follower.update(0.02)
            assertFalse(command.isDone)
            follower.update(0.02)
            assertTrue(follower.holding())
            assertTrue(command.isDone)
            assertTrue(follower.isBusy)
            command.stop(false)
            assertTrue(follower.holding())
            val noHold = FollowPath(path, false)
            noHold.start()
            follower.update(0.02)
            follower.update(0.02)
            assertTrue(follower.idle())
            assertTrue(noHold.isDone)
            noHold.stop(false)
        } finally {
            component.postStop()
        }
    }

    @Test
    fun progressCountsAllSegmentsAndHandlesIdleAndZeroLengthPaths() {
        val follower = mockFollower()
        val progress = PathProgress()
        assertEquals(0.0, progress.completion(follower), 0.0)
        assertEquals(0.0, progress.t(follower), 0.0)
        val invalid = mock(Path::class.java)
        `when`(invalid.segments).thenReturn(emptyList())
        assertThrows(IllegalArgumentException::class.java) { progress.start(invalid) }
        assertEquals(0.0, progress.length, 0.0)
        val first = Paths.line(Pose.zero(), Pose(10.0, 0.0)).constant(0.0)
        val second = Paths.line(Pose(10.0, 0.0), Pose(10.0, 20.0)).constant(0.0)
        progress.start(Paths.path(first, second))
        assertEquals(30.0, progress.length, 0.0)
        `when`(follower.following()).thenReturn(true)
        `when`(follower.parametricCompletion()).thenReturn(0.25)
        assertEquals(0.25, progress.t(follower), 0.0)
        `when`(follower.following()).thenReturn(false)
        assertEquals(1.0, progress.t(follower), 0.0)
        `when`(follower.pathIndex()).thenReturn(0)
        `when`(follower.currentCurve()).thenReturn(first.curve)
        `when`(follower.parametricCompletion()).thenReturn(0.5)
        assertEquals(25.0, progress.remaining(follower), 0.0)
        assertEquals(5.0, progress.traveled(follower), 0.0)
        `when`(follower.pathIndex()).thenReturn(1)
        `when`(follower.currentCurve()).thenReturn(second.curve)
        assertEquals(10.0, progress.remaining(follower), 0.0)
        assertEquals(2.0 / 3, progress.completion(follower), 0.0001)
        `when`(follower.pathIndex()).thenReturn(2, -1)
        assertEquals(0.0, progress.remaining(follower), 0.0)
        assertEquals(1.0, progress.completion(follower), 0.0)
    }

    @Test
    fun turnsPreserveDirectionAcrossHeadingWrapAndMultipleRevolutions() {
        val follower = mockFollower()
        val component = PedroComponent { follower }.apply { preInit() }
        try {
            `when`(follower.pose()).thenReturn(Pose(2.0, 3.0, 350.deg.inRad))
            val turn = TurnBy(450.deg, 1.deg)
            assertEquals(450.0, turn.angle.inDeg, 0.0)
            assertEquals(1.0, turn.tolerance.inDeg, 0.0)
            turn.start()
            assertFalse(turn.isDone)
            for (heading in listOf(80.0, 170.0, 260.0, 350.0, 80.0)) {
                `when`(follower.pose()).thenReturn(Pose(2.0, 3.0, heading.deg.inRad))
                turn.update()
            }
            assertTrue(turn.isDone)
            val target = ArgumentCaptor.forClass(Pose::class.java)
            verify(follower, times(6)).hold(target.capture())
            assertEquals(80.deg.inRad, target.allValues.first().heading(), 0.0001)
            assertEquals(80.deg.inRad, target.value.heading(), 0.0001)
            assertEquals(2.0, target.value.x(), 0.0)
            turn.stop(false)
            verify(follower, never()).stop()
            turn.stop(true)
            verify(follower.drivetrain).stop()
            `when`(follower.pose()).thenReturn(Pose.zero())
            val reverse = TurnBy((-90).deg, 1.deg)
            reverse.start()
            `when`(follower.pose()).thenReturn(Pose(0.0, 0.0, 3 * PI / 2))
            reverse.update()
            assertTrue(reverse.isDone)
            val zero = TurnBy(0.deg, 1.deg)
            zero.start()
            assertTrue(zero.isDone)
        } finally {
            component.postStop()
        }
    }
}
