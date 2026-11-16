package com.threedrd.pedropathing

import com.bylazar.field.Circle
import com.bylazar.field.PanelsField
import com.qualcomm.robotcore.hardware.HardwareMap
import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose
import com.pedropathing.util.PoseHistory
import com.threedrd.testing.SubsystemTests
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import dev.nextftc.ftc.ActiveOpMode
import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import org.junit.After
import org.mockito.Mockito.*

class PedroComponentTests : SubsystemTests() {
    @Before
    @After
    fun clearRetainedFollower() {
        for (field in listOf("follower", "hardwareMap"))
            PedroComponent::class.java.getDeclaredField(field).apply { isAccessible = true }.set(null, null)
    }

    @Test
    fun failedCreationCanBeRetried() {
        val config = mock(PedroConfig::class.java)
        val robot = mock(Follower::class.java)
        `when`(config.createFollower(ActiveOpMode.hardwareMap))
            .thenThrow(IllegalStateException("Disconnected")).thenReturn(robot)
        val component = PedroComponent(config)
        assertThrows(IllegalStateException::class.java) { component.preInit() }
        try {
            component.preInit()
            assertSame(robot, follower)
            verify(config, times(2)).createFollower(ActiveOpMode.hardwareMap)
        } finally {
            component.preStop()
            component.postStop()
        }
    }

    @Test
    fun rebuiltHardwareMapReplacesTheFollowerAndRetriesFailedCreation() {
        val config = mock(PedroConfig::class.java)
        val previousMap = ActiveOpMode.hardwareMap
        val nextMap = mock(HardwareMap::class.java)
        val previous = mock(Follower::class.java)
        val replacement = mock(Follower::class.java)
        `when`(config.createFollower(previousMap)).thenReturn(previous)
        `when`(config.createFollower(nextMap))
            .thenThrow(IllegalStateException("Disconnected")).thenReturn(replacement)
        val first = PedroComponent(config)
        first.preInit()
        first.preStop()
        first.postStop()
        clearInvocations(previous)
        requireNotNull(ActiveOpMode.it).hardwareMap = nextMap
        val next = PedroComponent(config)
        try {
            assertThrows(IllegalStateException::class.java) { next.preInit() }
            next.preInit()
            assertSame(replacement, follower)
            verifyNoInteractions(previous)
            verify(config).createFollower(previousMap)
            verify(config, times(2)).createFollower(nextMap)
            val order = inOrder(replacement)
            order.verify(replacement).breakFollowing()
            order.verify(replacement).update()
            verify(replacement, never()).setPose(any())
            verify(replacement, never()).setStartingPose(any())
        } finally {
            next.preStop()
            next.postStop()
            requireNotNull(ActiveOpMode.it).hardwareMap = previousMap
        }
    }

    @Test
    fun combinesLifecycleDrawingAndRetainedLocalization() {
        val robot = mock(Follower::class.java)
        val config = mock(PedroConfig::class.java)
        `when`(config.robotRadius).thenReturn(7.0)
        `when`(config.createFollower(ActiveOpMode.hardwareMap)).thenReturn(robot)
        val history = mock(PoseHistory::class.java)
        `when`(history.xPositionsArray).thenReturn(doubleArrayOf())
        `when`(history.yPositionsArray).thenReturn(doubleArrayOf())
        `when`(robot.poseHistory).thenReturn(history)
        `when`(robot.pose).thenReturn(Pose(10.0, 20.0, 0.0))
        val component = PedroComponent(config)
        verify(config, never()).createFollower(ActiveOpMode.hardwareMap)
        try {
            component.preInit()
            assertSame(robot, follower)
            component.preWaitForStart()
            component.drawing.field.lastUpdate = 0
            component.postWaitForStart()
            assertEquals(PanelsField.presets.DEFAULT_FTC, component.drawing.field.lastCanvas.preset)
            assertEquals(7.0, (component.drawing.field.lastCanvas.items.first() as Circle).r, 0.0)
            component.preUpdate()
            component.drawing.field.lastUpdate = 0
            component.postUpdate()
            assertEquals(7.0, (component.drawing.field.lastCanvas.items.first() as Circle).r, 0.0)
            verify(robot, times(3)).update()
            clearInvocations(robot)
            component.preStop()
            verify(robot).breakFollowing()
            component.postStop()
            assertThrows(IllegalStateException::class.java) { follower }
            val pushedPose = Pose(24.0, 36.0, 1.0)
            doAnswer { `when`(robot.pose).thenReturn(pushedPose); null }.`when`(robot).update()
            val next = PedroComponent(config)
            assertNotSame(component, next)
            next.preInit()
            assertSame(robot, follower)
            assertSame(pushedPose, follower.pose)
            verify(config).createFollower(ActiveOpMode.hardwareMap)
            verify(robot).update()
            verify(robot, never()).setPose(any())
        } finally {
            component.preStop()
            component.postStop()
        }
    }
}
