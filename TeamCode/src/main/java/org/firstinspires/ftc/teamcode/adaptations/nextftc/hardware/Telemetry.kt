package org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware

import com.pedropathing.drivetrain.Drivetrain
import com.pedropathing.ftc.drivetrains.Mecanum
import dev.nextftc.ftc.ActiveOpMode.hardwareMap
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE as CR_REVERSE
import com.qualcomm.robotcore.hardware.Servo.Direction.REVERSE
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit.AMPS
import org.firstinspires.ftc.threedrd.nextftc.hardware.CRServoEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.IMUEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.HardwareTelemetry
import org.firstinspires.ftc.threedrd.nextftc.hardware.MotorEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.ServoEx
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry.add
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.DEBUG
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.VERBOSE

fun configureHardwareTelemetry() {
    HardwareTelemetry.servo = { tel() }
    HardwareTelemetry.continuousServo = { tel() }
    HardwareTelemetry.motor = { tel() }
    HardwareTelemetry.imu = { tel() }
}

fun ServoEx.tel() {
    val source = name.humanize()
    add(source, DEBUG, "Position", "%.2f".format(position))
    add(source, VERBOSE, "Reversed", servo.direction == REVERSE)
}

fun CRServoEx.tel() {
    val source = name.humanize()
    add(source, DEBUG, "Power", "%.2f".format(power))
    add(source, VERBOSE, "Reversed", servo.direction == CR_REVERSE)
}

fun Drivetrain.tel() { (this as? Mecanum)?.motors?.tel() }

fun Iterable<DcMotorEx>.tel() = forEach { it.tel(hardwareMap.getNamesOf(it).first()) }

fun MotorEx.tel() = motor.tel(name)

fun DcMotorEx.tel(name: String) {
    val source = name.humanize()
    add(source, VERBOSE, "Current (A)", getCurrent(AMPS))
    add(source, DEBUG, "Power", power)
    add(source, DEBUG, "Velocity", velocity)
    add(source, DEBUG, "Position", currentPosition)
    add(source, VERBOSE, "Velocity (%)", velocity / motorType.achieveableMaxTicksPerSecond * 100)
    add(source, VERBOSE, "RPM", velocity / motorType.ticksPerRev * 60)
}

fun IMUEx.tel() {
    val source = name.humanize()
    add(source, DEBUG, "Yaw (deg)", "%.1f".format(imu.robotYawPitchRollAngles.getYaw(DEGREES)))
    add(source, DEBUG, "Pitch (deg)", "%.1f".format(imu.robotYawPitchRollAngles.getPitch(DEGREES)))
    add(source, DEBUG, "Roll (deg)", "%.1f".format(imu.robotYawPitchRollAngles.getRoll(DEGREES)))
}

fun String.humanize() = replace(Regex("([a-z])([A-Z])"), "$1 $2")
    .replaceFirstChar { it.uppercase() }
