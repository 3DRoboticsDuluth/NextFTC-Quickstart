package com.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.*
import dev.nextftc.ftc.ActiveOpMode.hardwareMap

typealias MotorEx = dev.nextftc.hardware.impl.MotorEx

val MotorEx.isPositionMode get() = motor.mode == RUN_TO_POSITION
val MotorEx.isVelocityMode get() = motor.mode == RUN_USING_ENCODER
val MotorEx.isPowerMode get() = motor.mode == RUN_WITHOUT_ENCODER

fun MotorEx(name: String) = MotorEx(name, configure = {})

fun MotorEx(
    name: String,
    cacheTolerance: Double = 0.01,
    configure: DcMotorEx.() -> Unit,
) = MotorEx(cacheTolerance, { hardwareMap.get(DcMotorEx::class.java, name).apply(configure) }, name)

var MotorEx.velocityPercentage: Double
    get() = velocity / motor.motorType.achieveableMaxTicksPerSecond
    set(value) { motor.velocity = motor.motorType.achieveableMaxTicksPerSecond * value * direction }

fun MotorEx.initialize() { resetPIDFCache(); motor }
