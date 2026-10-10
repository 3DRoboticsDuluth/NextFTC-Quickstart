package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.follower.*
import com.qualcomm.robotcore.eventloop.opmode.*
import org.firstinspires.ftc.threedrd.pedropathing.*
import dev.nextftc.ftc.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mockito.*

class AutoTests : SubsystemTests() {
    lateinit var follower: Follower
    lateinit var component: PedroComponent

    @Before
    fun setUp() {
        follower = mock(Follower::class.java, org.mockito.Mockito.withSettings().useConstructor(
            mock(com.pedropathing.localization.Localizer::class.java),
            mock(com.pedropathing.drivetrain.Drivetrain::class.java),
            mock(com.pedropathing.algorithm.Algorithm::class.java)
        ))
        component = PedroComponent { follower }.apply { preInit() }
    }

    @After
    fun tearDown() = component.postStop()

    @Test
    fun resetsTheStartingPoseOnlyForAutonomous() {
        useOpMode(AutonomousTestOpMode())
        Auto.initialize()

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
