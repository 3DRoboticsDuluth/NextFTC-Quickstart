package com.threedrd.nextftc.subsystems

import com.pedropathing.follower.*
import com.pedropathing.geometry.*
import com.pedropathing.paths.*
import dev.nextftc.core.units.*
import dev.nextftc.extensions.pedro.*
import com.threedrd.pedropathing.*
import com.threedrd.testing.*
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
        drive.curve(end, holdEnd = false).start()
        drive.curve(middle, end).start()
        drive.curves(middle, end).start()
        verify(builder, times(5)).addPath(any(Curve::class.java))
    }

    @Test
    fun absoluteTurnsPreserveDirectionAndCancelPedroOnInterruption() {
        `when`(follower.heading).thenReturn(Math.toRadians(170.0))
        val turn = drive.turnTo((-170).deg)
        assertTrue(turn.requirements.contains(drive))
        turn.start()
        verify(follower).turn(doubleThat { kotlin.math.abs(it - Math.toRadians(20.0)) < 1e-12 })
        `when`(follower.isTurning).thenReturn(true, false)
        assertFalse(turn.isDone)
        assertTrue(turn.isDone)
        turn.stop(false)
        verify(follower, never()).breakFollowing()
        turn.stop(true)
        verify(follower).breakFollowing()

        `when`(follower.heading).thenReturn(Math.toRadians(-170.0))
        drive.turnTo(170.0).start()
        verify(follower).turn(doubleThat { kotlin.math.abs(it - Math.toRadians(-20.0)) < 1e-12 })
    }

    @Test
    fun pointToPointFacesTravelsAndFinishesWithTheRequestedHeading() {
        follower.pathConstraints = PathConstraints.defaultConstraints
        `when`(follower.pose).thenReturn(Pose())
        val destination = Pose(0.0, 10.0, Math.PI)
        for (reverse in listOf(false, true)) {
            for (hold in listOf(false, true)) {
                clearInvocations(builder, follower)
                val command = drive.to(destination, reverse, hold)
                assertTrue(command.requirements.contains(drive))
                command.start()
                repeat(12) { if (!command.isDone) command.update() }
                assertTrue(command.isDone)
                val path = ArgumentCaptor.forClass(Curve::class.java)
                verify(builder).addPath(path.capture())
                assertEquals(destination.x, path.value.controlPoints.last().x, 0.0)
                assertEquals(destination.y, path.value.controlPoints.last().y, 0.0)
                val heading = if (reverse) -Math.PI / 2 else Math.PI / 2
                verify(builder).setConstantHeadingInterpolation(heading)
                verify(follower).turn(heading)
                verify(follower).turn(Math.PI)
                if (hold) verify(follower).holdPoint(destination)
                else verify(follower).breakFollowing()
            }
        }
    }

    @Test
    fun nearbyDestinationOnlyTurnsAndLineUsesItsSuppliedHeading() {
        follower.pathConstraints = PathConstraints.defaultConstraints
        val pose = Pose(2.0, 3.0, 0.0)
        `when`(follower.pose).thenReturn(pose)
        val destination = Pose(2.0, 3.0, Math.PI / 2)
        val command = drive.to(destination)
        command.start()
        repeat(8) { if (!command.isDone) command.update() }
        assertTrue(command.isDone)
        verify(follower).turn(Math.PI / 2)
        verify(follower).holdPoint(destination)
        verify(builder, never()).addPath(any(Curve::class.java))
        drive.line(destination).start()
        verify(builder).setConstantHeadingInterpolation(Math.PI / 2)
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
    fun signedAxisCommandsUseTheExecutionPoseAndRetainRequirements() {
        `when`(follower.pose).thenReturn(Pose())
        val cases = listOf(
            drive.axial(5.inches) to Pose(10.0, 25.0),
            drive.axial(-5.0) to Pose(10.0, 15.0),
            drive.lateral(5.inches) to Pose(5.0, 20.0),
            drive.lateral(-5.0) to Pose(15.0, 20.0),
            drive.axial(0.0) to Pose(10.0, 20.0)
        )
        `when`(follower.pose).thenReturn(Pose(10.0, 20.0, Math.PI / 2))
        cases.forEach { (command, expected) ->
            clearInvocations(builder)
            assertTrue(command.requirements.contains(drive))
            assertTrue(command.name.endsWith(".axial") || command.name.endsWith(".lateral"))
            command.start()
            val curve = ArgumentCaptor.forClass(Curve::class.java)
            verify(builder).addPath(curve.capture())
            assertEquals(expected.x, curve.value.controlPoints.last().x, 0.0001)
            assertEquals(expected.y, curve.value.controlPoints.last().y, 0.0001)
            verify(builder).setLinearHeadingInterpolation(Math.PI / 2, Math.PI / 2, 0.33)
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun oldMovementNamesRemainCompatibleAliases() {
        `when`(follower.pose).thenReturn(Pose())
        listOf(drive.forward(2.inches), drive.forward(2.0), drive.strafe(2.inches), drive.strafe(2.0))
            .forEachIndexed { index, command ->
                clearInvocations(builder)
                command.start()
                val curve = ArgumentCaptor.forClass(Curve::class.java)
                verify(builder).addPath(curve.capture())
                assertEquals(if (index < 2) 2.0 else 0.0, curve.value.controlPoints.last().x, 0.0001)
                assertEquals(if (index < 2) 0.0 else 2.0, curve.value.controlPoints.last().y, 0.0001)
                assertTrue(command.requirements.contains(drive))
            }
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
