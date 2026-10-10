package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.algorithm.*
import com.pedropathing.drivetrain.*
import com.pedropathing.follower.*
import com.pedropathing.localization.*
import com.pedropathing.math.*
import org.mockito.Mockito.*

fun mockFollower(): Follower = mock(Follower::class.java, withSettings().useConstructor(
    mock(Localizer::class.java), mock(Drivetrain::class.java), mock(Algorithm::class.java)
)).also { `when`(it.pose()).thenReturn(Pose.zero()) }

fun assertPose(expected: Pose, actual: Pose) {
    org.junit.Assert.assertEquals(expected.x(), actual.x(), 0.0001)
    org.junit.Assert.assertEquals(expected.y(), actual.y(), 0.0001)
    org.junit.Assert.assertEquals(expected.heading(), actual.heading(), 0.0001)
}
