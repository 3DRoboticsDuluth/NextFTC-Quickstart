package com.threedrd.nextftc.control

import dev.nextftc.core.units.Angle
import kotlin.math.*

/** Maps output angles to logical positions within a servo's scaled travel. Endpoints are degrees. */
class ServoAngle(var minAngle: Double = 0.0, var maxAngle: Double = 0.0) {
    val configured get() = minAngle.isFinite() && maxAngle.isFinite() &&
        (maxAngle - minAngle).isFinite() && minAngle != maxAngle

    /** Returns null for invalid calibration or an unreachable bearing; never moves hardware. */
    fun calculate(angle: Angle, currentPosition: Double): Double? {
        if (!configured || !angle.inDeg.isFinite() || !currentPosition.isFinite()) return null
        val bearing = angle.inDeg % 360.0
        val firstTurn = ceil((minOf(minAngle, maxAngle) - bearing) / 360.0)
        val lastTurn = floor((maxOf(minAngle, maxAngle) - bearing) / 360.0)
        if (firstTurn > lastTurn) return null
        val currentAngle = minAngle + currentPosition.coerceIn(0.0, 1.0) * (maxAngle - minAngle)
        val turns = round((currentAngle - bearing) / 360.0).coerceIn(firstTurn, lastTurn)
        return ((bearing + turns * 360.0 - minAngle) / (maxAngle - minAngle)).coerceIn(0.0, 1.0)
    }
}
