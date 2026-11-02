package org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware

import com.pedropathing.drivetrain.*
import com.pedropathing.ftc.drivetrains.*
import dev.nextftc.ftc.ActiveOpMode.hardwareMap
import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE as CR_REVERSE
import com.qualcomm.robotcore.hardware.Servo.Direction.REVERSE
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit.AMPS
import com.threedrd.nextftc.hardware.*
import com.threedrd.nextftc.telemetry.Tel

fun ServoEx.update(action: ServoEx.() -> Unit) { action(); tel() }

fun CRServoEx.update(action: CRServoEx.() -> Unit) { action(); tel() }

fun MotorEx.update(action: MotorEx.() -> Unit) { action(); tel() }

fun IMUEx.update(action: IMUEx.() -> Unit) { action(); tel() }

fun ServoEx.tel() {
    val tel = Tel(name.humanize())
    tel.debug("Position") { "%.2f".format(position) }
    tel.verbose("Reversed") { servo.direction == REVERSE }
}

fun CRServoEx.tel() {
    val tel = Tel(name.humanize())
    tel.debug("Power") { "%.2f".format(power) }
    tel.verbose("Reversed") { servo.direction == CR_REVERSE }
}

fun Drivetrain.tel() { (this as? Mecanum)?.motors?.tel() }

fun Iterable<DcMotorEx>.tel() = distinct().forEach { it.tel(hardwareMap.getNamesOf(it).first()) }

fun MotorEx.tel() = motor.tel(name)

fun DcMotorEx.tel(name: String) {
    val tel = Tel(name.humanize())
    tel.verbose("Current (A)") { getCurrent(AMPS) }
    tel.debug("Power") { power }
    tel.debug("Velocity") { velocity }
    tel.debug("Position") { currentPosition }
    tel.verbose("Velocity (%)") { velocity / motorType.achieveableMaxTicksPerSecond * 100 }
    tel.verbose("RPM") { velocity / motorType.ticksPerRev * 60 }
}

fun IMUEx.tel() {
    val tel = Tel(name.humanize())
    tel.debug("Yaw (deg)") { "%.1f".format(imu.robotYawPitchRollAngles.getYaw(DEGREES)) }
    tel.debug("Pitch (deg)") { "%.1f".format(imu.robotYawPitchRollAngles.getPitch(DEGREES)) }
    tel.debug("Roll (deg)") { "%.1f".format(imu.robotYawPitchRollAngles.getRoll(DEGREES)) }
}

fun String.humanize() = replace(Regex("([a-z])([A-Z])"), "$1 $2")
    .replaceFirstChar { it.uppercase() }
