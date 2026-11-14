package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import com.pedropathing.follower.Follower
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.geometry.Pose
import com.pedropathing.telemetry.SelectableOpMode
import com.pedropathing.telemetry.Selector
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.eventloop.opmode.Utility
import com.qualcomm.robotcore.hardware.HardwareMap
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

class TuningTests {
    @After
    fun reset() {
        Tuning.follower = null
    }

    @Test
    fun registersOnlyAsAPedroUtility() {
        val annotation = requireNotNull(Tuning::class.java.getAnnotation(Utility::class.java))
        assertEquals("Pedro Tuning", annotation.name)
        assertFalse(Tuning::class.java.isAnnotationPresent(TeleOp::class.java))
        assertFalse(Tuning::class.java.isAnnotationPresent(Autonomous::class.java))
        assertEquals(SelectableOpMode::class.java, Tuning::class.java.superclass)
    }

    @Test
    fun offersTheUpstreamTuningFolders() {
        val tuning = Tuning()
        val field = SelectableOpMode::class.java.getDeclaredField("selector").apply { isAccessible = true }
        val selector = field.get(tuning) as Selector<*>
        for (folder in listOf("Localization", "Automatic", "Manual", "Tests", "Swerve")) {
            assertTrue(selector.lines.toString(), selector.lines.any { folder in it })
        }
        selector.select()
        for (test in listOf("Localization Test", "Offsets Tuner", "Forward Tuner", "Lateral Tuner", "Turn Tuner")) {
            assertTrue(selector.lines.toString(), selector.lines.any { test in it })
        }
    }

    @Test
    fun selectionRebuildsTheFollowerFromTheTeamConstants() {
        val tuning = Tuning()
        tuning.hardwareMap = mock(HardwareMap::class.java)
        val previous = mock(Follower::class.java)
        val fresh = mock(Follower::class.java)
        Tuning.follower = previous
        mockConstruction(FollowerBuilder::class.java, { builder, context ->
            assertSame(Constants.followerConstants, context.arguments()[0])
            assertSame(tuning.hardwareMap, context.arguments()[1])
            `when`(builder.pathConstraints(Constants.pathConstraints)).thenReturn(builder)
            `when`(builder.mecanumDrivetrain(Constants.driveConstants)).thenReturn(builder)
            `when`(builder.driveEncoderLocalizer(Constants.localizerConstants)).thenReturn(builder)
            `when`(builder.build()).thenReturn(fresh)
        }).use { builders ->
            tuning.onSelect()
            assertEquals(1, builders.constructed().size)
            assertSame(fresh, Tuning.follower)
            verifyNoInteractions(previous)
            val start = ArgumentCaptor.forClass(Pose::class.java)
            verify(fresh).setStartingPose(start.capture())
            assertEquals(0.0, start.value.x, 0.0)
            assertEquals(0.0, start.value.y, 0.0)
            assertEquals(0.0, start.value.heading, 0.0)
            verify(fresh).poseHistory
        }
    }

    @Test
    fun stopRobotRequestsZeroRobotCentricDrive() {
        val follower = mock(Follower::class.java)
        Tuning.follower = follower
        Tuning.stopRobot()
        val order = inOrder(follower)
        order.verify(follower).startTeleopDrive(true)
        order.verify(follower).setTeleOpDrive(0.0, 0.0, 0.0, true)
    }
}
