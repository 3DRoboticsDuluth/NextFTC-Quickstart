package org.firstinspires.ftc.teamcode.subsystems

import com.threedrd.nextftc.config.Diagnostics.Level.INFO
import org.junit.Assert.*
import org.junit.*

class ConfigTests {
    @Test
    fun startsWithNeutralDiagnostics() {
        assertEquals(true, Config.robotCentric)
        assertEquals(INFO, Config.level)
        assertEquals("", Config.filter)
    }
}
