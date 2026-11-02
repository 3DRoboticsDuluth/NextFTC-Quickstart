package com.threedrd.pedropathing

import com.threedrd.nextftc.control.interpolators.SCurveParameters

/** Normalized command derivatives, not physical chassis acceleration or electrical limits. */
class DriverSlewParameters(
    val translation: SCurveParameters = SCurveParameters(4.0, 40.0),
    val turn: SCurveParameters = SCurveParameters(8.0, 80.0),
)
