package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.follower.*
import com.qualcomm.robotcore.eventloop.opmode.*
import dev.nextftc.extensions.pedro.*
import dev.nextftc.ftc.*
import dev.nextftc.core.commands.CommandManager
import com.threedrd.nextftc.subsystems.SubsystemComponent
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mockito.*

class AutoTests : SubsystemTests() {
    lateinit var follower: Follower
    lateinit var component: PedroComponent

    @Before
    fun setUp() {
        follower = mock(Follower::class.java)
        component = PedroComponent { follower }.apply { preInit() }
    }

    @After
    fun tearDown() {
        CommandManager.cancelAll()
        component.postStop()
    }

    @Test
    fun autonomousLifecycleInitializesThenSchedulesWithoutResettingPoseAtStart() {
        useOpMode(AutonomousTestOpMode())
        val subsystems = SubsystemComponent(Auto)
        subsystems.preInit()
        assertEquals(setOf(Auto), subsystems.subsystems)
        verify(follower).setStartingPose(Nav.start)
        verify(follower).setPose(Nav.start)
        assertFalse(Auto.execute.isScheduled)

        clearInvocations(follower)
        subsystems.preStartButtonPressed()
        assertTrue(Auto.execute.isScheduled)
        verify(follower, never()).setStartingPose(any())
        verify(follower, never()).setPose(any())
    }

    @Test
    fun teleopOmitsAutoInitializationAndStart() {
        useOpMode(TeleopTestOpMode())
        val subsystems = SubsystemComponent(Auto)
        subsystems.preInit()
        subsystems.preStartButtonPressed()
        assertTrue(subsystems.subsystems.isEmpty())
        assertFalse(Auto.execute.isScheduled)
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
