package org.firstinspires.ftc.threedrd.nextftc.control

class CurrentLimitParameters(var maxCurrent: Double, var reductionRate: Double = 2.0, var recoveryRate: Double = 0.5) {
    init {
        require(maxCurrent.isFinite() && maxCurrent > 0.0)
        require(reductionRate.isFinite() && reductionRate > 0.0)
        require(recoveryRate.isFinite() && recoveryRate > 0.0)
    }
}
