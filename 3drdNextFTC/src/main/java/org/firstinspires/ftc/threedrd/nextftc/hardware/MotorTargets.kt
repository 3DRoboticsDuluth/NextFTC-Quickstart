package org.firstinspires.ftc.threedrd.nextftc.hardware

import dev.nextftc.core.units.Angle
import dev.nextftc.core.units.Distance
import java.util.WeakHashMap
import kotlin.math.roundToInt

private val distances = WeakHashMap<MotorEx, Double>()
private val angles = WeakHashMap<MotorEx, Double>()

fun MotorEx.distancePerTick(value: Distance) = apply {
    require(value.value.isFinite() && value.value > 0.0) { "Distance per tick must be positive and finite" }
    distances[this] = value.value
}

fun MotorEx.anglePerTick(value: Angle) = apply {
    require(value.inRad.isFinite() && value.inRad > 0.0) { "Angle per tick must be positive and finite" }
    angles[this] = value.inRad
}

var MotorEx.targetDistance: Distance
    get() = Distance.fromMm(checkNotNull(distances[this]) { "Configure distancePerTick first" } * targetTicks)
    set(value) { targetTicks = value.value / checkNotNull(distances[this]) { "Configure distancePerTick first" } }

var MotorEx.targetAngle: Angle
    get() = Angle.fromRad(checkNotNull(angles[this]) { "Configure anglePerTick first" } * targetTicks)
    set(value) { targetTicks = value.inRad / checkNotNull(angles[this]) { "Configure anglePerTick first" } }

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
