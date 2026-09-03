package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.follower.Follower
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.nextftc.extensions.pedro.PedroComponent
import dev.nextftc.ftc.ActiveOpMode
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions

class AutoTests : SubsystemTests() {
    lateinit var follower: Follower
    lateinit var component: PedroComponent

    @Before
    fun setUp() {
        follower = mock(Follower::class.java)
        component = PedroComponent { follower }.apply { preInit() }
    }

    @After
    fun tearDown() = component.postStop()

    @Test
    fun resetsTheStartingPoseOnlyForAutonomous() {
        useOpMode(AutonomousTestOpMode())
        Auto.initialize()

        verify(follower).setStartingPose(Nav.start)
        verify(follower).setPose(Nav.start)

        clearInvocations(follower)
        useOpMode(TeleopTestOpMode())
        Auto.initialize()

        verifyNoInteractions(follower)
    }

    @Test
    fun exposesTheDeferredDriveCommand() {
        assertEquals("Drive.to", Auto.execute.name)
        assertTrue(Auto.execute.requirements.contains(Drive))
    }

    fun useOpMode(opMode: LinearOpMode) {
        val current = ActiveOpMode.it!!
        ActiveOpMode.it = opMode.apply {
            hardwareMap = current.hardwareMap
            telemetry = current.telemetry
        }
    }

    @Autonomous
    class AutonomousTestOpMode : LinearOpMode() {
        override fun runOpMode() = Unit
    }

    @TeleOp
    class TeleopTestOpMode : LinearOpMode() {
        override fun runOpMode() = Unit
    }
}
