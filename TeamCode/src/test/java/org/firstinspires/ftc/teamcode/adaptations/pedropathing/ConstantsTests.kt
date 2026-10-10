package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import com.pedropathing.algorithm.*
import com.pedropathing.revhub.drivetrains.*
import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.*
import org.firstinspires.ftc.threedrd.pedropathing.*
import org.junit.Assert.*
import org.junit.*
import org.mockito.Mockito.*

class ConstantsTests {
    @Test
    fun usesExplicitNeutralMotorNamesAndUntunedInputs() {
        assertEquals(18.0, Constants.robotLength.inIn, 0.0)
        assertEquals(18.0, Constants.robotWidth.inIn, 0.0)
        assertEquals(9.0, Constants.robotRadius, 0.0)
        assertEquals("leftFront", Constants.driveConfig.frontLeftName.get())
        assertEquals("leftRear", Constants.driveConfig.backLeftName.get())
        assertEquals("rightFront", Constants.driveConfig.frontRightName.get())
        assertEquals("rightRear", Constants.driveConfig.backRightName.get())
        assertEquals("leftFront", Constants.localizerConfig.frontLeftName)
        assertEquals("leftRear", Constants.localizerConfig.backLeftName)
        assertEquals("rightFront", Constants.localizerConfig.frontRightName)
        assertEquals("rightRear", Constants.localizerConfig.backRightName)
        assertEquals(0.0, Constants.foresightConfig.headingFeedback.get().calculate(1.0, 1.0), 0.0)
        assertEquals(1.0, Constants.foresightConfig.maxAchievableForwardVelocity.get(), 0.0)
    }

    @Test
    fun constructsThePedro3FollowerFromReplaceableRobotConfiguration() {
        val drive = Constants.driveConfig
        val localizer = Constants.localizerConfig
        val foresight = Constants.foresightConfig
        val map = mock(HardwareMap::class.java)
        val motor = mock(DcMotorEx::class.java)
        `when`(motor.direction).thenReturn(FORWARD)
        for (name in listOf("leftFront", "leftRear", "rightFront", "rightRear")) {
            `when`(map.get(DcMotorEx::class.java, name)).thenReturn(motor)
        }
        try {
            Constants.driveConfig = drive
            Constants.localizerConfig = localizer
            Constants.foresightConfig = foresight
            val follower = Constants.createFollower(map)
            assertTrue(follower.drivetrain is Mecanum)
            assertSame(drive, (follower.drivetrain as Mecanum).config)
            assertTrue(follower.localizer is DriveEncoderLocalizer)
            assertSame(localizer, (follower.localizer as DriveEncoderLocalizer).config)
            assertTrue(follower.algorithm() is Foresight)
            assertSame(foresight, (follower.algorithm() as Foresight).config)
            assertEquals(0.0, follower.pose().x(), 0.0)
            assertEquals(0.0, follower.pose().y(), 0.0)
        } finally {
            Constants.driveConfig = drive
            Constants.localizerConfig = localizer
            Constants.foresightConfig = foresight
        }
    }
}
