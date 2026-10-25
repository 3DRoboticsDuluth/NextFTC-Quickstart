package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.*
import dev.nextftc.ftc.ActiveOpMode.hardwareMap

typealias CRServoEx = dev.nextftc.hardware.impl.CRServoEx

fun CRServoEx(name: String) = CRServoEx(name, configure = {})

fun CRServoEx(
    name: String,
    cacheTolerance: Double = 0.01,
    configure: CRServo.() -> Unit,
) = CRServoEx(cacheTolerance, { hardwareMap.get(CRServo::class.java, name).apply(configure) })

val CRServoEx.name get() = hardwareMap.getNamesOf(servo).firstOrNull() ?: servo.deviceName

fun CRServoEx.initialize() { servo }
