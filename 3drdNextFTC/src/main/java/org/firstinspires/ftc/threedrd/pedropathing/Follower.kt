package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.follower.*
import com.pedropathing.math.*

/** Pedro 3's localizer establishes its encoder reference when the pose is set. */
fun Follower.resetStartingPose(pose: Pose) = setPose(pose)

/** Follower.stop() changes mode; actuator cleanup must not wait for another update. */
fun Follower.stopNow() {
    try {
        stop()
    } finally {
        drivetrain.stop()
    }
}
