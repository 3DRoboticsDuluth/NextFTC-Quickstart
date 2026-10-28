package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION

fun MotorEx.positionControl(speed: Double) {
    require(speed in 0.0..1.0) { "Position-control speed must be between 0 and 1" }
    motor.apply {
        mode = RUN_TO_POSITION
        power = speed
    }
}

fun MotorEx.stopPositionControl() {
    motor.power = 0.0
    power = 0.0
}
