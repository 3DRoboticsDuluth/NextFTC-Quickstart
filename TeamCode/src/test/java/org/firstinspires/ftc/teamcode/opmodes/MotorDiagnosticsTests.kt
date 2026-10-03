package org.firstinspires.ftc.teamcode.opmodes

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.subsystems.Config
import org.firstinspires.ftc.teamcode.subsystems.Motor
import org.firstinspires.ftc.threedrd.nextftc.subsystems.SubsystemComponent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotorDiagnosticsTests {
    @Test
    fun registersOnlyTheConfigurationAndDiagnosticMotorSubsystems() {
        val opMode = MotorDiagnostics()
        val subsystems = opMode.components
            .filterIsInstance<SubsystemComponent>()
            .single()
            .subsystems

        assertTrue(MotorDiagnostics::class.java.isAnnotationPresent(TeleOp::class.java))
        assertEquals(setOf(Config, Motor), subsystems)
    }
}
