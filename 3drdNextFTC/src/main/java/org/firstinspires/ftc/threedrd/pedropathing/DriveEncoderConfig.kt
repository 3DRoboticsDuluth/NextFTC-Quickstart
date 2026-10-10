package org.firstinspires.ftc.threedrd.pedropathing

/** Conversion factors multiply the four-encoder sums, as in Pedro 2's drive localizer. */
class DriveEncoderConfig(
    val frontLeftName: String,
    val frontRightName: String,
    val backLeftName: String,
    val backRightName: String
) {
    var frontLeftDirection = -1.0
    var frontRightDirection = 1.0
    var backLeftDirection = -1.0
    var backRightDirection = 1.0

    var forwardTicksToInches = 1.0
    var strafeTicksToInches = 1.0
    var turnTicksToInches = 1.0
    var robotWidth = 1.0
    var robotLength = 1.0
}
