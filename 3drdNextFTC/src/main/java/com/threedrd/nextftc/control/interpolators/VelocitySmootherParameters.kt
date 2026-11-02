package com.threedrd.nextftc.control.interpolators

class VelocitySmootherParameters(var timeConstant: Double) {
    init { require(timeConstant.isFinite() && timeConstant > 0.0) }
}
