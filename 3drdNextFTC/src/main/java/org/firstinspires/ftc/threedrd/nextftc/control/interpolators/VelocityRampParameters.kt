package org.firstinspires.ftc.threedrd.nextftc.control.interpolators

class VelocityRampParameters(var maxAcceleration: Double) {
    init { require(maxAcceleration.isFinite() && maxAcceleration > 0.0) }
}
