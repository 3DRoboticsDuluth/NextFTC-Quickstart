package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.*
import dev.nextftc.ftc.ActiveOpMode.hardwareMap

typealias ServoEx = dev.nextftc.hardware.impl.ServoEx

fun ServoEx(name: String) = ServoEx(name, configure = {})

fun ServoEx(
    name: String,
    cacheTolerance: Double = 0.01,
    configure: Servo.() -> Unit,
) = ServoEx(cacheTolerance, { hardwareMap.get(Servo::class.java, name).apply(configure) })

val ServoEx.name get() = hardwareMap.getNamesOf(servo).firstOrNull() ?: servo.deviceName

fun ServoEx.initialize() { servo }
