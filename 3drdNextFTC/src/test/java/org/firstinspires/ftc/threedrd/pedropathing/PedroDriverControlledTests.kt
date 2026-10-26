package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.follower.*
import dev.nextftc.extensions.pedro.*
import java.util.function.*
import org.junit.*
import org.junit.Assert.*
import org.firstinspires.ftc.threedrd.testing.*
import org.mockito.Mockito.*
import com.pedropathing.geometry.Pose

class PedroDriverControlledTests : SubsystemTests() {
    val follower = mock(Follower::class.java)
    val component = PedroComponent { follower }.apply { preInit() }

    @After
    fun tearDown() = component.postStop()

    private fun setClock(command: PedroDriverControlled, seconds: () -> Double) {
        val shaping = command.javaClass.getDeclaredField("shaping").apply { isAccessible = true }.get(command)
        val clock = shaping.javaClass.getDeclaredField("clock").apply { isAccessible = true }.get(shaping)
        clock.javaClass.getDeclaredField("seconds").apply { isAccessible = true }.set(clock, seconds)
    }

    private fun output() = mockingDetails(follower).invocations.last { it.method.name == "setTeleOpDrive" }
        .arguments.take(3).map { it as Double }.toDoubleArray()

    @Test
    fun appliesScalarAndCurrentCentricMode() {
        var robotCentric = false
        val command = PedroDriverControlled(
            Supplier { 0.5 },
            Supplier { -0.25 },
            Supplier { 1.0 },
            { robotCentric }
        ).apply { scalar = 0.5 }

        assertFalse(command.robotCentric())
        assertNull(command.slew)
        assertEquals(0.0, command.headingOffset(), 0.0)
        assertEquals(1.0, command.drive(1.0), 0.0)
        assertEquals(1.0, command.strafe(1.0), 0.0)
        assertEquals(1.0, command.turn(1.0), 0.0)
        command.drive = { it }
        command.strafe = { it }
        command.turn = { it }

        command.start()
        command.update()
        robotCentric = true
        command.update()

        verify(follower).startTeleopDrive()
        verify(follower).setTeleOpDrive(0.25, -0.125, 0.5, false, 0.0)
        verify(follower).setTeleOpDrive(0.25, -0.125, 0.5, true, 0.0)
    }

    @Test
    fun arcadeRecipeKeepsStrafeZeroAndInputsRobotCentric() {
        val command = PedroDriverControlled(
            Supplier { 0.6 }, Supplier { 0.0 }, Supplier { -0.2 }, { true }
        ).apply { scalar = 0.5 }

        command.start()
        command.update()

        verify(follower).startTeleopDrive()
        verify(follower).setTeleOpDrive(0.3, 0.0, -0.1, true, 0.0)
    }

    @Test
    fun breaksFollowingOnlyWhenInterrupted() {
        val command = PedroDriverControlled(
            Supplier { 0.0 },
            Supplier { 0.0 },
            Supplier { 0.0 },
            { false }
        )

        command.stop(false)
        verify(follower, never()).breakFollowing()

        command.stop(true)
        verify(follower).breakFollowing()
    }

    @Test
    fun shapesScaledInputsBeforeAssistCallbacksWithoutShapingTheirCorrections() {
        var time = 0.0
        var manualForward = 0.0
        val parameters = DriverSlewParameters()
        val command = PedroDriverControlled({ 1.0 }, { 1.0 }, { 1.0 }, { false }, slew = parameters).apply {
            scalar = 0.5
            drive = { manualForward = it; it + 0.25 }
            strafe = { -0.3 }
            turn = { 0.4 }
        }
        assertSame(parameters, command.slew)
        setClock(command) { time }
        command.start()
        command.update()
        assertArrayEquals(doubleArrayOf(0.25, -0.3, 0.4), output(), 0.0)
        time = 0.05
        command.update()
        assertTrue(manualForward > 0.0 && manualForward < 0.5)
        assertEquals(manualForward + 0.25, output()[0], 0.0)
        for (step in 6..200) {
            time = step / 100.0
            command.update()
        }
        assertEquals(0.5, manualForward, 1e-5)
        assertArrayEquals(doubleArrayOf(0.75, -0.3, 0.4), output(), 1e-5)
    }

    @Test
    fun centricAndOffsetChangesReexpressExistingStateWithoutChangingItsFieldDirection() {
        var time = 0.0
        var robotCentric = false
        var offset = 0.0
        `when`(follower.pose).thenReturn(Pose(0.0, 0.0, Math.PI / 2))
        val command = PedroDriverControlled({ 1.0 }, { 0.0 }, { 0.0 }, { robotCentric }, { offset }, DriverSlewParameters())
        setClock(command) { time }
        command.start()
        command.update()
        time = 0.05
        command.update()
        val forward = output()[0]
        assertTrue(forward > 0.0)
        robotCentric = true
        command.update()
        assertArrayEquals(doubleArrayOf(0.0, -forward, 0.0), output(), 1e-12)
        robotCentric = false
        command.update()
        assertArrayEquals(doubleArrayOf(forward, 0.0, 0.0), output(), 1e-12)
        offset = Math.PI / 2
        command.update()
        assertArrayEquals(doubleArrayOf(0.0, -forward, 0.0), output(), 1e-12)
        robotCentric = true
        offset = -Math.PI / 2
        command.update()
        assertArrayEquals(doubleArrayOf(forward, 0.0, 0.0), output(), 1e-12)
        offset = 0.0
        command.update()
        assertArrayEquals(doubleArrayOf(0.0, -forward, 0.0), output(), 1e-12)
    }

    @Test
    fun shapedCommandsStopImmediatelyOnEitherEndingAndRestartFromRest() {
        var time = 0.0
        val command = PedroDriverControlled({ 1.0 }, { 0.0 }, { 1.0 }, { true }, slew = DriverSlewParameters())
        setClock(command) { time }
        command.start()
        command.update()
        time = 0.05
        command.update()
        assertTrue(output()[0] > 0.0)
        command.stop(false)
        verify(follower).breakFollowing()
        time = 100.0
        command.start()
        command.update()
        assertArrayEquals(doubleArrayOf(0.0, 0.0, 0.0), output(), 0.0)
        time = 100.01
        command.update()
        assertTrue(output()[0] > 0.0)
        command.stop(true)
        verify(follower, times(2)).breakFollowing()
    }
}
