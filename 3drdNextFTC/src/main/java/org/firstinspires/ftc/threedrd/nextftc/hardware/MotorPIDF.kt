package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER
import com.qualcomm.robotcore.hardware.PIDFCoefficients
import java.util.WeakHashMap

private val positionGains = WeakHashMap<MotorEx, Double>()
private val velocityGains = WeakHashMap<MotorEx, List<Any>>()

var MotorEx.positionP: Double
    get() = motor.getPIDFCoefficients(RUN_TO_POSITION).p
    set(value) {
        if (positionGains[this] == value) return
        motor.setPositionPIDFCoefficients(value)
        positionGains[this] = value
    }

var MotorEx.velocityPIDF: PIDFCoefficients
    get() = motor.getPIDFCoefficients(RUN_USING_ENCODER)
    set(value) {
        val snapshot = listOf(value.p, value.i, value.d, value.f, value.algorithm)
        if (velocityGains[this] == snapshot) return
        motor.setPIDFCoefficients(RUN_USING_ENCODER, PIDFCoefficients(value))
        velocityGains[this] = snapshot
    }

internal fun MotorEx.resetPIDFCache() {
    positionGains.remove(this)
    velocityGains.remove(this)
}
