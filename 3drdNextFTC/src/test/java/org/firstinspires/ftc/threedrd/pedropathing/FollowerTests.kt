package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.follower.*
import com.pedropathing.geometry.*
import org.junit.*
import org.mockito.Mockito.*

class FollowerTests {
    @Test
    fun resetStartingPoseEstablishesTheFrameBeforeTheExactPose() {
        val follower = mock(Follower::class.java)
        val pose = Pose(1.0, 2.0, 3.0)

        follower.resetStartingPose(pose)

        inOrder(follower).run {
            verify(follower).setStartingPose(pose)
            verify(follower).setPose(pose)
        }
    }
}
