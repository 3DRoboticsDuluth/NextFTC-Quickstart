package org.firstinspires.ftc.threedrd.nextftc.subsystems

import org.firstinspires.ftc.threedrd.nextftc.opmodes.isAuto
import org.firstinspires.ftc.threedrd.nextftc.opmodes.isTeleop

enum class DiscoveryMode { BOTH, AUTO, TELEOP }

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Discovery(
    val mode: DiscoveryMode = DiscoveryMode.BOTH,
    val owned: Boolean = false,
    val disabled: Boolean = false,
)

internal fun Class<*>.participates(): Boolean {
    val options = getAnnotation(Discovery::class.java) ?: return true
    if (options.disabled) return false
    return when (options.mode) {
        DiscoveryMode.BOTH -> true
        DiscoveryMode.AUTO -> isAuto
        DiscoveryMode.TELEOP -> isTeleop
    }
}
