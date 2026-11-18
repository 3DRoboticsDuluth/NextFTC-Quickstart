package com.threedrd.nextftc.control

import dev.nextftc.core.units.Angle
import kotlin.math.*

/** Maps two measured angle/position pairs to logical positions within scaled servo travel. */
class ServoAngle(
    var angle1: Double = 0.0,
    var angle2: Double = 0.0,
    var position1: Double = 0.0,
    var position2: Double = 1.0,
) {
    val configured get() = angle1.isFinite() && angle2.isFinite() &&
        position1 in 0.0..1.0 && position2 in 0.0..1.0 && position1 != position2 &&
        slope.isFinite() && slope != 0.0

    private val slope get() = (angle2 - angle1) / (position2 - position1)

    /** Returns null for invalid calibration or an unreachable bearing; never moves hardware. */
    fun calculate(angle: Angle, currentPosition: Double, minPosition: Double = 0.0, maxPosition: Double = 1.0): Double? {
        if (!configured || !angle.inDeg.isFinite() || !currentPosition.isFinite()) return null
        if (!(minPosition in 0.0..1.0 && maxPosition in 0.0..1.0 && minPosition < maxPosition)) return null
        val minAngle = angle1 + (minPosition - position1) * slope
        val maxAngle = angle1 + (maxPosition - position1) * slope
        if (!(maxAngle - minAngle).isFinite() || minAngle == maxAngle) return null
        val bearing = angle.inDeg % 360.0
        val firstTurn = ceil((minOf(minAngle, maxAngle) - bearing) / 360.0)
        val lastTurn = floor((maxOf(minAngle, maxAngle) - bearing) / 360.0)
        if (firstTurn > lastTurn) return null
        val currentAngle = minAngle + currentPosition.coerceIn(0.0, 1.0) * (maxAngle - minAngle)
        val turns = round((currentAngle - bearing) / 360.0).coerceIn(firstTurn, lastTurn)
        return ((bearing + turns * 360.0 - minAngle) / (maxAngle - minAngle)).coerceIn(0.0, 1.0)
    }
}
