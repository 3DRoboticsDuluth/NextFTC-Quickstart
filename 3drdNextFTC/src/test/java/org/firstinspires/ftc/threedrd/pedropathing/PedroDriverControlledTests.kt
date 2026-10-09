package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.follower.*
import dev.nextftc.extensions.pedro.*
import java.util.function.*
import org.junit.*
import org.junit.Assert.*
import org.firstinspires.ftc.threedrd.testing.*
import org.mockito.Mockito.*

class PedroDriverControlledTests : SubsystemTests() {
    val follower = mock(Follower::class.java)
    val component = PedroComponent { follower }.apply { preInit() }

    @After
    fun tearDown() = component.postStop()

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
}
