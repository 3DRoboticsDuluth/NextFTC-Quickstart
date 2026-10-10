package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.math.*
import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.*
import kotlin.math.*
import org.junit.Assert.*
import org.junit.*
import org.mockito.Mockito.*

class DriveEncoderLocalizerTests {
    val config = DriveEncoderConfig("fl", "fr", "bl", "br")
    val motors = List(4) { mock(DcMotorEx::class.java) }
    val positions = IntArray(4)
    var time = 0L
    val map = mock(HardwareMap::class.java).also { hardware ->
        listOf("fl", "fr", "bl", "br").forEachIndexed { index, name ->
            `when`(hardware.get(DcMotorEx::class.java, name)).thenReturn(motors[index])
            `when`(motors[index].direction).thenReturn(FORWARD)
            `when`(motors[index].currentPosition).thenAnswer { positions[index] }
        }
    }

    private fun localizer() = DriveEncoderLocalizer(map, config) { time }

    @Test
    fun preservesFourEncoderSumConversionsAndFieldVelocity() {
        config.forwardTicksToInches = 0.25
        config.strafeTicksToInches = 0.5
        config.turnTicksToInches = 0.1
        config.robotWidth = 10.0
        config.robotLength = 10.0
        val localizer = localizer()
        assertSame(config, localizer.config)
        assertEquals(time, localizer.nanoTime())
        assertEquals("fl", config.frontLeftName)
        assertEquals("fr", config.frontRightName)
        assertEquals("bl", config.backLeftName)
        assertEquals("br", config.backRightName)
        positions[0] = -10; positions[1] = 10; positions[2] = -10; positions[3] = 10
        time = 1_000_000_000
        localizer.update()
        assertEquals(10.0, localizer.pose().x(), 0.0001)
        assertEquals(0.0, localizer.pose().y(), 0.0001)
        assertEquals(10.0, localizer.velocity().vx, 0.0001)
        assertEquals(0.0, localizer.velocity().omega, 0.0)
        positions[0] = 0; positions[1] = 20; positions[2] = -20; positions[3] = 0
        time += 1_000_000_000
        localizer.update()
        assertEquals(10.0, localizer.pose().x(), 0.0001)
        assertEquals(20.0, localizer.pose().y(), 0.0001)
        assertEquals(20.0, localizer.velocity().vy, 0.0001)
    }

    @Test
    fun integratesRotationAndCombinedMotionWithThePoseExponential() {
        config.frontLeftDirection = 1.0
        config.backLeftDirection = 1.0
        config.frontRightDirection = 1.0
        config.backRightDirection = 1.0
        config.robotWidth = 1.0
        config.robotLength = 1.0
        config.turnTicksToInches = PI / 4
        val localizer = localizer()
        positions[0] = -1; positions[1] = 1; positions[2] = -1; positions[3] = 1
        time = 1_000_000_000
        localizer.update()
        assertEquals(PI / 2, localizer.pose().heading(), 0.0001)
        assertEquals(PI / 2, localizer.velocity().omega, 0.0001)
        localizer.setPose(Pose(5.0, 6.0, PI / 2))
        positions[0] += 1; positions[1] += 1; positions[2] += 1; positions[3] += 1
        time += 1_000_000_000
        localizer.update()
        assertEquals(5.0, localizer.pose().x(), 0.0001)
        assertEquals(10.0, localizer.pose().y(), 0.0001)
        localizer.reset()
        assertEquals(0.0, localizer.pose().x(), 0.0)
        assertEquals(0.0, localizer.pose().heading(), 0.0)
        assertEquals(0.0, localizer.velocity().vy, 0.0)
    }

    @Test
    fun resetsEncoderReferenceWithoutMovingAndHandlesZeroElapsedTime() {
        val localizer = localizer()
        localizer.setPose(Pose(10.0, 20.0, PI / 2))
        positions[0] = -1; positions[1] = 1; positions[2] = -1; positions[3] = 1
        localizer.update()
        assertEquals(10.0, localizer.pose().x(), 0.0001)
        assertEquals(24.0, localizer.pose().y(), 0.0001)
        assertEquals(0.0, localizer.velocity().vx, 0.0)
        assertEquals(0.0, localizer.velocity().vy, 0.0)
        localizer.setPose(Pose(3.0, 4.0, 0.0))
        time = 1_000_000_000
        localizer.update()
        assertEquals(3.0, localizer.pose().x(), 0.0)
        assertEquals(4.0, localizer.pose().y(), 0.0)
        assertEquals(0.0, localizer.velocity().omega, 0.0)
        // The production default clock is also usable without an injected timer.
        val timed = DriveEncoderLocalizer(map, config)
        assertTrue(timed.nanoTime() > 0L)
        config.robotWidth = -1.0
        config.robotLength = 1.0
        assertThrows(IllegalArgumentException::class.java) { localizer() }
    }

    @Test
    fun honorsMotorDirectionAlongWithIndependentEncoderSigns() {
        `when`(motors[0].direction).thenReturn(REVERSE)
        `when`(motors[2].direction).thenReturn(REVERSE)
        val localizer = localizer()
        positions.fill(1)
        time = 1_000_000_000
        localizer.update()
        assertEquals(4.0, localizer.pose().x(), 0.0)
        assertEquals(0.0, localizer.pose().y(), 0.0)
    }
}
