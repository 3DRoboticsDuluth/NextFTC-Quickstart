package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.*
import dev.nextftc.ftc.ActiveOpMode.hardwareMap

typealias MotorEx = dev.nextftc.hardware.impl.MotorEx

fun MotorEx(name: String) = MotorEx(name, configure = {})

fun MotorEx(
    name: String,
    cacheTolerance: Double = 0.01,
    configure: DcMotorEx.() -> Unit,
) = MotorEx(cacheTolerance, { hardwareMap.get(DcMotorEx::class.java, name).apply(configure) }, name)

var MotorEx.velocityPercentage: Double
    get() = velocity / motor.motorType.achieveableMaxTicksPerSecond
    set(value) { motor.velocity = motor.motorType.achieveableMaxTicksPerSecond * value }

fun MotorEx.initialize() { resetPIDFCache(); motor }
