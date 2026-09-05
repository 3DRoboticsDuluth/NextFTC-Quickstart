package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.hardware.Gamepad
import dev.nextftc.bindings.BindingManager
import dev.nextftc.core.commands.utility.NullCommand
import dev.nextftc.extensions.pedro.PedroComponent
import dev.nextftc.ftc.ActiveOpMode
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.memberProperties
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.DEBUG
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry as TeamTelemetry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class DriveTests : SubsystemTests() {
    lateinit var follower: Follower
    lateinit var component: PedroComponent

    @Before
    fun setUp() {
        follower = mock(Follower::class.java)
        component = PedroComponent { follower }.apply { preInit() }
        ActiveOpMode.it!!.gamepad1 = Gamepad()
        Config.config.robotCentric = true
        Config.state.teleop = false
        Drive.POWER_LOW = 0.35
        Drive.POWER_HIGH = 0.70
        Drive.initialize()
        Drive.controls()
    }

    @After
    fun tearDown() = component.postStop()

    @Test
    fun exposesSimpleConfigurablePowerLevels() {
        assertTrue(Drive::class.java.isAnnotationPresent(Configurable::class.java))
        val settings = Drive::class.memberProperties
            .filterIsInstance<KMutableProperty<*>>()
            .filter { it.visibility == PUBLIC }
            .map { it.name }
            .toSet()

        assertTrue(settings.containsAll(setOf("POWER_LOW", "POWER_HIGH")))
        assertEquals(0.70, Drive.driverControlled.scalar, 0.0)
    }

    @Test
    fun driverInputsUsePedroSignConventionAndLiveCentricMode() {
        ActiveOpMode.it!!.gamepad1.left_stick_y = 0.25f
        ActiveOpMode.it!!.gamepad1.left_stick_x = -0.5f
        ActiveOpMode.it!!.gamepad1.right_stick_x = 0.75f
        Drive.initialize()
        Drive.driverControlled.scalar = 1.0
        BindingManager.update()

        Drive.driverControlled.update()
        Config.config.robotCentric = false
        Drive.driverControlled.update()

        verify(follower).setTeleOpDrive(-0.25, 0.5, -0.75, true, 0.0)
        verify(follower).setTeleOpDrive(-0.25, 0.5, -0.75, false, 0.0)
    }

    @Test
    fun speedCommandsAndDefaultCommandFollowTeleopState() {
        Drive.low.start()
        assertEquals(0.35, Drive.driverControlled.scalar, 0.0)
        Drive.high.start()
        assertEquals(0.70, Drive.driverControlled.scalar, 0.0)

        assertTrue(Drive.defaultCommand is NullCommand)
        Config.state.teleop = true
        assertSame(Drive.driverControlled, Drive.defaultCommand)
    }

    @Test
    fun periodicReportsPowerAndPose() {
        TeamTelemetry.LEVEL = DEBUG
        `when`(follower.pose).thenReturn(Pose(12.34, 56.78, Math.toRadians(89.94)))
        clearInvocations(ActiveOpMode.telemetry)

        Drive.periodic()

        verify(ActiveOpMode.telemetry).addData("I | Drive | Power", "0.70" as Any)
        verify(ActiveOpMode.telemetry).addData("D | Drive | X", "12.3" as Any)
        verify(ActiveOpMode.telemetry).addData("D | Drive | Y", "56.8" as Any)
        verify(ActiveOpMode.telemetry).addData("D | Drive | Heading (deg)", "89.9" as Any)
    }
}
