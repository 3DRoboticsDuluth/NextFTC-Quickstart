package org.firstinspires.ftc.threedrd.nextftc.control.interpolators

class SCurveParameters(var maxAcceleration: Double, var maxJerk: Double) {
    init {
        require(maxAcceleration.isFinite() && maxAcceleration > 0.0)
        require(maxJerk.isFinite() && maxJerk > 0.0)
    }
}
