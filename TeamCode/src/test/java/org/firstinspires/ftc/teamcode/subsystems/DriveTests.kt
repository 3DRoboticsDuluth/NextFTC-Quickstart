package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.*
import com.pedropathing.ftc.drivetrains.*
import com.qualcomm.robotcore.hardware.*
import com.pedropathing.follower.*
import com.pedropathing.geometry.*
import dev.nextftc.bindings.*
import dev.nextftc.core.commands.utility.*
import dev.nextftc.extensions.pedro.*
import dev.nextftc.ftc.*
import kotlin.reflect.*
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.*
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.DEBUG
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry as TeamTelemetry
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mockito.*

class DriveTests : SubsystemTests() {
    lateinit var follower: Follower
    lateinit var component: PedroComponent
    lateinit var motors: List<DcMotorEx>

    @Before
    fun setUp() {
        follower = mock(Follower::class.java)
        component = PedroComponent { follower }.apply { preInit() }
        ActiveOpMode.it!!.gamepad1 = Gamepad()
        Config.robotCentric = true
        Config.state.teleop = false
        Drive.POWER_LOW = 0.35
        Drive.POWER_HIGH = 0.70
        val drivetrain = mock(Mecanum::class.java)
        motors = List(4) { mock(DcMotorEx::class.java) }
        follower.drivetrain = drivetrain
        `when`(drivetrain.motors).thenReturn(motors)
        motors.forEachIndexed { index, motor ->
            val type = mock(com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType::class.java)
            `when`(motor.motorType).thenReturn(type)
            `when`(type.achieveableMaxTicksPerSecond).thenReturn(500.0)
            `when`(type.ticksPerRev).thenReturn(100.0)
            `when`(ActiveOpMode.hardwareMap.getNamesOf(motor)).thenReturn(setOf("driveMotor$index"))
        }
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
        Config.robotCentric = false
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
    @Test
    fun periodicReportsAllFourActualMotorsWithoutChangingThem() {
        TeamTelemetry.LEVEL = org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.VERBOSE
        `when`(follower.pose).thenReturn(Pose())
        motors.forEachIndexed { index, motor ->
            val type = mock(com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType::class.java)
            `when`(type.achieveableMaxTicksPerSecond).thenReturn(500.0)
            `when`(type.ticksPerRev).thenReturn(100.0)
            `when`(motor.motorType).thenReturn(type)
            `when`(motor.power).thenReturn(index * 0.1)
            `when`(motor.velocity).thenReturn(index * 10.0)
            `when`(motor.currentPosition).thenReturn(index * 100)
            `when`(motor.getCurrent(org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit.AMPS)).thenReturn(index * 0.2)
            clearInvocations(motor)
        }
        clearInvocations(ActiveOpMode.telemetry, ActiveOpMode.hardwareMap)
        Drive.periodic()
        verify(ActiveOpMode.hardwareMap, org.mockito.Mockito.never()).get(
            org.mockito.ArgumentMatchers.eq(DcMotorEx::class.java), org.mockito.ArgumentMatchers.anyString()
        )
        motors.forEachIndexed { index, motor ->
            val source = "Drive Motor$index"
            verify(ActiveOpMode.telemetry).addData("D | $source | Power", (index * 0.1) as Any)
            verify(ActiveOpMode.telemetry).addData("D | $source | Position", (index * 100) as Any)
            verify(ActiveOpMode.telemetry).addData("D | $source | Velocity", (index * 10.0) as Any)
            verify(ActiveOpMode.telemetry).addData("V | $source | Current (A)", (index * 0.2) as Any)
            verify(motor, org.mockito.Mockito.never()).setPower(org.mockito.ArgumentMatchers.anyDouble())
            verify(motor, org.mockito.Mockito.never()).setMode(org.mockito.ArgumentMatchers.any())
        }
    }
}
