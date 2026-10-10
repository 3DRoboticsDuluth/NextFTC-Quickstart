package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.math.*
import org.junit.*
import org.mockito.Mockito.*

class FollowerTests {
    @Test
    fun resetsThePoseAndStopsMotorsWithoutAnotherUpdate() {
        val follower = mockFollower()
        val pose = Pose(1.0, 2.0, 3.0)
        follower.resetStartingPose(pose)
        follower.stopNow()
        inOrder(follower, follower.drivetrain).run {
            verify(follower).setPose(pose)
            verify(follower).stop()
            verify(follower.drivetrain).stop()
        }
    }
    @Test
    fun stillStopsHardwareWhenFollowerCleanupFails() {
        val follower = mockFollower()
        org.mockito.Mockito.doThrow(IllegalStateException("release failed")).`when`(follower).stop()
        org.junit.Assert.assertThrows(IllegalStateException::class.java) { follower.stopNow() }
        org.mockito.Mockito.verify(follower.drivetrain).stop()
    }
}
