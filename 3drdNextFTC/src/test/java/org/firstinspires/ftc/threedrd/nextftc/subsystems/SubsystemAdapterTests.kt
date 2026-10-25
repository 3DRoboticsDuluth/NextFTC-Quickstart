package org.firstinspires.ftc.threedrd.nextftc.subsystems

import org.firstinspires.ftc.threedrd.nextftc.hardware.*
import com.qualcomm.robotcore.eventloop.opmode.*
import com.qualcomm.robotcore.util.*
import dev.nextftc.ftc.*
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.OFF
import org.firstinspires.ftc.threedrd.nextftc.logging.*
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.INFO
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry as TeamTelemetry
import org.firstinspires.ftc.robotcore.external.*
import org.junit.Assert.*
import org.junit.*
import org.mockito.Mockito.*

class SubsystemAdapterTests {
    private object TestSubsystem : Subsystem()

    private class TestHardware(
        override val name: String,
        var failure: Exception? = null
    ) : Hardware {
        var initializations = 0

        override fun initialize() {
            initializations++
            failure?.let { throw it }
        }
    }

    private class HardwareSubsystem(
        val device: TestHardware,
        val duplicate: TestHardware? = null
    ) : Subsystem()

    private class NativeHardwareSubsystem(
        val motor: MotorEx,
        val imu: IMUEx,
        val duplicateMotor: MotorEx? = null,
        val duplicateImu: IMUEx? = null
    ) : Subsystem()

    private class NativeServoSubsystem(
        val servo: ServoEx,
        val continuousServo: CRServoEx,
        val alias: ServoEx = servo
    ) : Subsystem()

    private class CommandSubsystem(val device: TestHardware) : Subsystem() {
        var executions = 0
        val delegated by instant { executions++ }
    }

    private lateinit var telemetry: Telemetry

    @Before
    fun setUp() {
        telemetry = mock(Telemetry::class.java)
        val telemetryLog = mock(Telemetry.Log::class.java)
        org.mockito.Mockito.`when`(telemetry.log()).thenReturn(telemetryLog)
        org.mockito.Mockito.`when`(telemetryLog.capacity).thenReturn(9)
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }.apply { this.telemetry = this@SubsystemAdapterTests.telemetry }
        Logging.FILTER = ""
        Logging.DISPLAY_FILTER = ""
        TeamTelemetry.FILTER = ""
        TeamTelemetry.DISPLAY_FILTER = ""
        TeamTelemetry.initialize()
        Logging.LEVEL = OFF
        TeamTelemetry.LEVEL = INFO
    }

    @Test
    fun createsOwnedInstantCommands() {
        var executions = 0

        val unnamed = TestSubsystem.instant(Runnable { executions++ })
        val named = TestSubsystem.instant("Reset", Runnable { executions++ })

        assertTrue(unnamed.requirements.contains(TestSubsystem))
        assertTrue(named.requirements.contains(TestSubsystem))
        assertEquals("Reset", named.name)

        unnamed.start()
        named.start()
        TestSubsystem.start()
        assertEquals(2, executions)
    }

    @Test
    fun discoversEachHardwarePropertyOnlyOnce() {
        val hardware = TestHardware("ready")
        val subsystem = HardwareSubsystem(hardware, hardware)

        subsystem.initializeHardware()

        assertFalse(subsystem.disabled)
        assertTrue(subsystem.errors.isEmpty())
        assertEquals(1, hardware.initializations)
        verify(telemetry, never()).addData(
            "E | HardwareSubsystem | Status", "Disabled (see Logcat)"
        )
    }

    @Test
    fun retriesFailedHardwareForTheNextInitialization() {
        val hardware = TestHardware("recovering", IllegalStateException("missing"))
        val subsystem = HardwareSubsystem(hardware)
        subsystem.initializeHardware()

        hardware.failure = null
        subsystem.initializeHardware()

        assertFalse(subsystem.disabled)
        assertEquals(2, hardware.initializations)
    }

    @Test
    fun failedHardwareDisablesAndReportsItsSubsystem() {
        val hardware = TestHardware("bad", IllegalStateException("missing"))
        val subsystem = HardwareSubsystem(hardware)

        mockStatic(RobotLog::class.java).use { robotLog ->
            subsystem.initializeHardware()
            subsystem.reportDisabled()

            robotLog.verify {
                RobotLog.ee(
                    "HardwareSubsystem",
                    "Hardware | bad: java.lang.IllegalStateException: missing"
                )
            }
        }

        assertTrue(subsystem.disabled)
        assertEquals(1, hardware.initializations)
        verify(telemetry).addData(
            "E | HardwareSubsystem | Status", "Disabled (see Logcat)" as Any
        )
    }

    @Test
    fun disabledSubsystemCommandsDoNotExecute() {
        val subsystem = CommandSubsystem(
            TestHardware("bad", IllegalStateException("missing"))
        )
        val direct = subsystem.instant { subsystem.executions++ }

        subsystem.initializeHardware()
        direct.start()
        subsystem.delegated.start()

        assertEquals(0, subsystem.executions)
    }
    @Test
    fun discoversNativeInstancesAndAliasesWithoutEagerHardwareLookup() {
        val previous = ActiveOpMode.it
        var motorConfigurations = 0
        var imuConfigurations = 0
        val motor: MotorEx
        val imu: IMUEx
        ActiveOpMode.it = null
        try {
            motor = MotorEx("driveMotor") { motorConfigurations++ }
            imu = IMUEx("headingSensor") { imuConfigurations++ }
        } finally {
            ActiveOpMode.it = previous
        }
        assertEquals(0, motorConfigurations)
        assertEquals(0, imuConfigurations)
        val map = mock(com.qualcomm.robotcore.hardware.HardwareMap::class.java)
        val sdkMotor = mock(com.qualcomm.robotcore.hardware.DcMotorEx::class.java)
        val sdkImu = mock(com.qualcomm.robotcore.hardware.IMU::class.java)
        `when`(map.get(com.qualcomm.robotcore.hardware.DcMotorEx::class.java, "driveMotor")).thenReturn(sdkMotor)
        `when`(map.get(com.qualcomm.robotcore.hardware.IMU::class.java, "headingSensor")).thenReturn(sdkImu)
        ActiveOpMode.it!!.hardwareMap = map
        val subsystem = NativeHardwareSubsystem(motor, imu, motor, imu)
        subsystem.initializeHardware()
        assertFalse(subsystem.disabled)
        assertEquals(1, motorConfigurations)
        assertEquals(1, imuConfigurations)
        assertSame(sdkMotor, motor.motor)
        assertSame(sdkImu, imu.imu)
        verify(map).get(com.qualcomm.robotcore.hardware.DcMotorEx::class.java, "driveMotor")
        verify(map).get(com.qualcomm.robotcore.hardware.IMU::class.java, "headingSensor")
    }

    @Test
    fun nativeHardwareFailuresAreIsolatedAndRetriedWithoutReacquiringForNames() {
        var failing = true
        var motorAttempts = 0
        var imuAttempts = 0
        val sdkMotor = mock(com.qualcomm.robotcore.hardware.DcMotorEx::class.java)
        val sdkImu = mock(com.qualcomm.robotcore.hardware.IMU::class.java)
        val motor = MotorEx(motorFactory = {
            motorAttempts++
            check(!failing) { "motor missing" }
            sdkMotor
        })
        val imu = IMUEx(imuFactory = {
            imuAttempts++
            check(!failing) { "imu missing" }
            sdkImu
        })
        val subsystem = NativeHardwareSubsystem(motor, imu)
        val healthyHardware = TestHardware("healthy")
        val healthy = HardwareSubsystem(healthyHardware)
        subsystem.initializeHardware()
        healthy.initializeHardware()
        assertTrue(subsystem.disabled)
        assertEquals(2, subsystem.errors.size)
        assertTrue(subsystem.errors[0].startsWith("motor:"))
        assertTrue(subsystem.errors[1].startsWith("imu:"))
        assertEquals(1, motorAttempts)
        assertEquals(1, imuAttempts)
        assertFalse(healthy.disabled)
        assertEquals(1, healthyHardware.initializations)
        failing = false
        subsystem.initializeHardware()
        assertFalse(subsystem.disabled)
        assertEquals(2, motorAttempts)
        assertEquals(2, imuAttempts)
    }

    @Test
    fun discoversNativeServoFluentResultsAndPreservesFailureIsolation() {
        var failing = true
        var servoAcquisitions = 0
        var continuousAcquisitions = 0
        val sdkServo = mock(com.qualcomm.robotcore.hardware.Servo::class.java)
        val sdkContinuous = mock(com.qualcomm.robotcore.hardware.CRServo::class.java)
        `when`(sdkServo.direction).thenReturn(com.qualcomm.robotcore.hardware.Servo.Direction.FORWARD)
        `when`(sdkContinuous.direction).thenReturn(com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD)
        val previous = ActiveOpMode.it
        ActiveOpMode.it = null
        val servo: ServoEx
        val continuous: CRServoEx
        try {
            servo = ServoEx(servoFactory = {
                servoAcquisitions++
                check(!failing) { "servo missing" }
                sdkServo
            }).reversed()
            continuous = CRServoEx(servoFactory = {
                continuousAcquisitions++
                sdkContinuous
            }).reversed()
        } finally {
            ActiveOpMode.it = previous
        }
        assertEquals(0, servoAcquisitions)
        assertEquals(0, continuousAcquisitions)
        val subsystem = NativeServoSubsystem(servo, continuous)
        subsystem.initializeHardware()
        assertTrue(subsystem.disabled)
        assertEquals(1, subsystem.errors.size)
        assertTrue(subsystem.errors.single().startsWith("servo:"))
        assertEquals(1, servoAcquisitions)
        assertEquals(1, continuousAcquisitions)
        failing = false
        subsystem.initializeHardware()
        assertFalse(subsystem.disabled)
        assertEquals(2, servoAcquisitions)
        assertEquals(1, continuousAcquisitions)
        assertSame(sdkServo, servo.servo)
        assertSame(sdkContinuous, continuous.servo)
        verify(sdkServo).setDirection(com.qualcomm.robotcore.hardware.Servo.Direction.REVERSE)
        verify(sdkContinuous).setDirection(com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE)
    }

}
