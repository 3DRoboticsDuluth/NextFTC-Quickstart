package org.firstinspires.ftc.teamcode.subsystems

import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics.Level.INFO
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigTests {
    @Test
    fun startsWithNeutralDiagnostics() {
        assertEquals(INFO, Config.config.level)
        assertEquals("", Config.config.filter)
    }
}
