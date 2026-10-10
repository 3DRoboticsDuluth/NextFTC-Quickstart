package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.drivetrain.*
import com.pedropathing.math.*
import java.util.function.*
import org.junit.*
import org.junit.Assert.*
import org.firstinspires.ftc.threedrd.testing.*
import org.mockito.*
import org.mockito.Mockito.*

class PedroDriverControlledTests : SubsystemTests() {
    val follower = mockFollower()
    val component = PedroComponent { follower }.apply { preInit() }

    @After
    fun tearDown() = component.postStop()

    @Test
    fun appliesScalarTransformsAndLiveCentricMode() {
        var robotCentric = false
        val command = PedroDriverControlled(Supplier { 0.5 }, Supplier { -0.25 }, Supplier { 1.0 }, { robotCentric })
            .apply { scalar = 0.5 }
        assertFalse(command.robotCentric())
        assertEquals(0.0, command.headingOffset(), 0.0)
        assertEquals(1.0, command.drive(1.0), 0.0)
        assertEquals(1.0, command.strafe(1.0), 0.0)
        assertEquals(1.0, command.turn(1.0), 0.0)
        command.drive = { it * 2 }
        command.strafe = { it * 2 }
        command.turn = { it / 2 }
        `when`(follower.pose()).thenReturn(Pose(0.0, 0.0, Math.PI / 2))
        command.start()
        command.update()
        robotCentric = true
        command.update()
        val powers = ArgumentCaptor.forClass(DrivePowers::class.java)
        verify(follower, times(3)).manual(powers.capture())
        assertEquals(0.0, powers.allValues[0].forward(), 0.0)
        assertEquals(-0.25, powers.allValues[1].forward(), 0.0001)
        assertEquals(-0.5, powers.allValues[1].strafe(), 0.0001)
        assertEquals(0.25, powers.allValues[1].turn(), 0.0)
        assertEquals(0.5, powers.allValues[2].forward(), 0.0)
        assertEquals(-0.25, powers.allValues[2].strafe(), 0.0)
    }

    @Test
    fun appliesHeadingOffsetAndStopsOnlyWhenInterrupted() {
        val command = PedroDriverControlled(Supplier { 1.0 }, Supplier { 0.0 }, Supplier { 0.0 }, { false }, { Math.PI / 2 })
        command.update()
        val powers = ArgumentCaptor.forClass(DrivePowers::class.java)
        verify(follower).manual(powers.capture())
        assertEquals(0.0, powers.value.forward(), 0.0001)
        assertEquals(-1.0, powers.value.strafe(), 0.0001)
        command.stop(false)
        verify(follower, never()).stop()
        command.stop(true)
        verify(follower).stop()
        verify(follower.drivetrain).stop()
    }
    @Test
    fun robotCentricArcadeKeepsStrafeZeroAtAnyHeading() {
        val command = PedroDriverControlled(Supplier { 0.6 }, Supplier { 0.0 }, Supplier { -0.2 }, { true })
            .apply { scalar = 0.5 }
        `when`(follower.pose()).thenReturn(Pose(0.0, 0.0, Math.PI / 2))
        command.update()
        val powers = ArgumentCaptor.forClass(DrivePowers::class.java)
        verify(follower).manual(powers.capture())
        assertEquals(0.3, powers.value.forward(), 0.0)
        assertEquals(0.0, powers.value.strafe(), 0.0)
        assertEquals(-0.1, powers.value.turn(), 0.0)
    }

}
