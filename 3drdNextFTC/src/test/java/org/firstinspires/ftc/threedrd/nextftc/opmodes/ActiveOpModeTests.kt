package org.firstinspires.ftc.threedrd.nextftc.opmodes

import com.qualcomm.robotcore.eventloop.opmode.*
import dev.nextftc.ftc.*
import org.junit.Assert.*
import org.junit.*

class ActiveOpModeTests {
    @Autonomous
    class Auto : LinearOpMode() {
        override fun runOpMode() = Unit
    }

    @TeleOp
    class Teleop : LinearOpMode() {
        override fun runOpMode() = Unit
    }

    @Test
    fun identifiesAutonomousOpModes() {
        ActiveOpMode.it = Auto()
        assertTrue(ActiveOpMode.isAutonomous)
        assertFalse(ActiveOpMode.isTeleop)

        ActiveOpMode.it = Teleop()
        assertFalse(ActiveOpMode.isAutonomous)
        assertTrue(ActiveOpMode.isTeleop)
    }
}
