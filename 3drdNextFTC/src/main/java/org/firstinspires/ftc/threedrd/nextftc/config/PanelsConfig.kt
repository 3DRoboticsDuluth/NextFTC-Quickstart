package org.firstinspires.ftc.threedrd.nextftc.config

import java.lang.reflect.*

internal object PanelsConfig {
    fun refresher(api: Class<*>): (Any) -> Unit {
        val methods = api.methods.filter {
            it.name == "refreshClass" && it.parameterCount == 1
        }
        val current = methods.firstOrNull {
            Modifier.isStatic(it.modifiers) && it.parameterTypes.single() == Class::class.java
        }
        if (current != null) return { current.invoke(null, it.javaClass) }

        val legacy = methods.firstOrNull {
            !Modifier.isStatic(it.modifiers) && it.parameterTypes.single() == Any::class.java
        } ?: error("Panels refreshClass API is unavailable")
        val instance = api.getField("INSTANCE").get(null)
        return { legacy.invoke(instance, it) }
    }
}
