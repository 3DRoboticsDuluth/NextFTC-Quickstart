package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.follower.*
import com.pedropathing.paths.PathConstraints
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
        assertFalse(CommandManager.hasCommandsUsing(Drive))

        `when`(follower.pose).thenReturn(Nav.end)
        follower.pathConstraints = PathConstraints.defaultConstraints
        clearInvocations(follower)
        subsystems.preStartButtonPressed()
        CommandManager.run()
        assertTrue(CommandManager.hasCommandsUsing(Drive))
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
        assertFalse(CommandManager.hasCommandsUsing(Drive))
        verifyNoInteractions(follower)
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
