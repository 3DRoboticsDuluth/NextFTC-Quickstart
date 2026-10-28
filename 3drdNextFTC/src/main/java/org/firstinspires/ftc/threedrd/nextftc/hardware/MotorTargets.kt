package org.firstinspires.ftc.threedrd.nextftc.hardware

import dev.nextftc.core.units.Angle
import dev.nextftc.core.units.Distance
import java.util.WeakHashMap
import kotlin.math.roundToInt

private class Calibration(val value: Double, val perTick: Boolean = false) {
    fun scale(ticks: Double?): Double {
        val result = if (perTick) value else value / checkNotNull(ticks) { "Configure ticks() before using travel() targets" }
        require(result.isFinite() && result > 0.0) { "Travel per tick must be positive and finite" }
        return result
    }
}

private val counts = WeakHashMap<MotorEx, Double>()
private val distances = WeakHashMap<MotorEx, Calibration>()
private val angles = WeakHashMap<MotorEx, Calibration>()

fun MotorEx.ticks(value: Double) = apply {
    require(value.isFinite() && value > 0.0) { "Ticks per revolution must be positive and finite" }
    counts[this] = value
}

fun MotorEx.travel(value: Distance) = apply {
    require(value.value.isFinite() && value.value > 0.0) { "Travel per revolution must be positive and finite" }
    distances[this] = Calibration(value.value)
}

fun MotorEx.travel(value: Angle) = apply {
    require(value.inRad.isFinite() && value.inRad > 0.0) { "Angular travel per revolution must be positive and finite" }
    angles[this] = Calibration(value.inRad)
}

fun MotorEx.distancePerTick(value: Distance) = apply {
    require(value.value.isFinite() && value.value > 0.0) { "Distance per tick must be positive and finite" }
    distances[this] = Calibration(value.value, perTick = true)
}

fun MotorEx.anglePerTick(value: Angle) = apply {
    require(value.inRad.isFinite() && value.inRad > 0.0) { "Angle per tick must be positive and finite" }
    angles[this] = Calibration(value.inRad, perTick = true)
}

var MotorEx.targetDistance: Distance
    get() = Distance.fromMm(distanceScale() * targetTicks)
    set(value) { targetTicks = value.value / distanceScale() }

var MotorEx.targetAngle: Angle
    get() = Angle.fromRad(angleScale() * targetTicks)
    set(value) { targetTicks = value.inRad / angleScale() }

private fun MotorEx.distanceScale() = checkNotNull(distances[this]) { "Configure travel(Distance) or distancePerTick() first" }.scale(counts[this])
private fun MotorEx.angleScale() = checkNotNull(angles[this]) { "Configure travel(Angle) or anglePerTick() first" }.scale(counts[this])

private var MotorEx.targetTicks: Double
    get() {
        val sdk = motor
        return currentPosition + (sdk.targetPosition.toDouble() - rawTicks) * direction
    }
    set(value) {
        require(value.isFinite()) { "Target must convert to finite encoder ticks" }
        val sdk = motor
        val raw = rawTicks + (value - currentPosition) * direction
        require(raw in Int.MIN_VALUE.toDouble()..Int.MAX_VALUE.toDouble()) { "Target exceeds SDK encoder range" }
        sdk.targetPosition = raw.roundToInt()
    }
