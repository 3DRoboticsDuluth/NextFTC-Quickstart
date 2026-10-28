package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION

fun MotorEx.positionControl(speed: Double) {
    require(speed in 0.0..1.0) { "Position-control speed must be between 0 and 1" }
    motor.apply {
        mode = RUN_TO_POSITION
        power = speed
    }
}

fun MotorEx.positionMode(zero: Boolean = false) {
    stop()
    if (zero) this.zero()
    motor.apply {
        targetPosition = currentPosition
        mode = RUN_TO_POSITION
    }
}

var MotorEx.speed: Double
    get() = positionMotor().power
    set(value) {
        require(value in 0.0..1.0) { "Position-control speed must be between 0 and 1" }
        positionMotor().power = value
    }

private fun MotorEx.positionMotor() = motor.also {
    check(it.mode == RUN_TO_POSITION) { "Call positionMode() before using speed" }
}

fun MotorEx.stopPositionControl() = stop()

fun MotorEx.stop() {
    motor.power = 0.0
    power = 0.0
}
