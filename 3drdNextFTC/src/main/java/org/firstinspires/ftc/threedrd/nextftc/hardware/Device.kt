package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.*
import dev.nextftc.ftc.ActiveOpMode.hardwareMap
import kotlin.reflect.*

class Device<T : HardwareDevice>(
    override val name: String,
    val type: Class<T>,
    configure: T.() -> Unit = {},
) : Hardware {
    private val configure = configure

    lateinit var device: T

    override fun initialize() {
        device = hardwareMap.get(type, name).apply(configure)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = device
}
