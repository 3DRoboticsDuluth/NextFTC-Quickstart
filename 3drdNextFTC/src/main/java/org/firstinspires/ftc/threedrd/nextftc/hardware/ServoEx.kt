package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.*
import dev.nextftc.ftc.ActiveOpMode.hardwareMap
import java.util.WeakHashMap

typealias ServoEx = dev.nextftc.hardware.impl.ServoEx

private class ServoConfiguration {
    private val pending = mutableListOf<Servo.() -> Unit>()
    private var device: Servo? = null

    fun configure(block: Servo.() -> Unit) {
        val sdk = device
        if (sdk == null) pending += block else sdk.block()
    }

    fun initialize(sdk: Servo): Servo {
        pending.forEach { it(sdk) }
        pending.clear()
        device = sdk
        return sdk
    }
}

private val configurations = WeakHashMap<ServoEx, ServoConfiguration>()

fun ServoEx(name: String) = ServoEx(name, configure = {})

fun ServoEx(
    name: String,
    cacheTolerance: Double = 0.01,
    configure: Servo.() -> Unit,
): ServoEx {
    val configuration = ServoConfiguration()
    configuration.configure(configure)
    return ServoEx(cacheTolerance, {
        configuration.initialize(hardwareMap.get(Servo::class.java, name))
    }).also { configurations[it] = configuration }
}

fun ServoEx.scaleRange(min: Double, max: Double) = apply {
    requireNotNull(configurations[this]) {
        "scaleRange requires the project ServoEx factory"
    }.configure { scaleRange(min, max) }
}

val ServoEx.name get() = hardwareMap.getNamesOf(servo).firstOrNull() ?: servo.deviceName

fun ServoEx.initialize() { servo }
