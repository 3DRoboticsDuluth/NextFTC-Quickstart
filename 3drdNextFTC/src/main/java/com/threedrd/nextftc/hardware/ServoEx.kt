package com.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.*
import dev.nextftc.ftc.ActiveOpMode.hardwareMap
import java.util.WeakHashMap

typealias ServoEx = dev.nextftc.hardware.impl.ServoEx

private class ServoConfiguration {
    private val pending = mutableListOf<Servo.() -> Unit>()
    private var device: Servo? = null
    private var rangeMin: Double? = null
    private var rangeMax = 1.0

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

    fun scaleRange(min: Double, max: Double, owner: ServoEx) {
        if (min == rangeMin && max == rangeMax) return
        configure { scaleRange(min, max) }
        // Reapply the logical demand: the native cache cannot see an SDK range change.
        device?.position = owner.position
        rangeMin = min
        rangeMax = max
    }

    fun restoreRange(owner: ServoEx) {
        if (device == null) return
        val min = rangeMin ?: return
        rangeMin = null
        scaleRange(min, rangeMax, owner)
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
    }.scaleRange(min, max, this)
}

val ServoEx.name get() = hardwareMap.getNamesOf(servo).firstOrNull() ?: servo.deviceName

fun ServoEx.initialize() { configurations[this]?.restoreRange(this); servo }
