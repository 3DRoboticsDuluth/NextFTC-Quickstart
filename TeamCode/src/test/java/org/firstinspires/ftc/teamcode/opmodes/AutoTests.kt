package org.firstinspires.ftc.teamcode.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import org.junit.*
import org.junit.Assert.*

class AutoTests {
    @Test
    fun compositionCanBeCreatedAndIsRegisteredAsAutonomous() {
        assertNotNull(Auto())
        assertTrue(Auto::class.java.isAnnotationPresent(Autonomous::class.java))
    }
}
