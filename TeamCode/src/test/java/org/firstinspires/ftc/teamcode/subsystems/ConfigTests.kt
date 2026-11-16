package org.firstinspires.ftc.teamcode.subsystems

import com.threedrd.nextftc.config.Diagnostics.Level.INFO
import org.junit.Assert.*
import org.junit.*

class ConfigTests {
    @Test
    fun generatedMenuOrderMatchesConfigDeclarations() {
        val metadata = Class.forName(Config::class.java.name + "\$SettingOrder", true, Config::class.java.classLoader)
        val names = (metadata.getField("names").get(null) as Array<*>).toList()
        assertEquals(listOf("robotCentric", "level"), names)
        assertEquals(listOf("Robot Centric", "Level"), Config.items.map { it.key })
    }

    @Test
    fun startsWithNeutralDiagnostics() {
        assertEquals(true, Config.robotCentric)
        assertEquals(INFO, Config.level)
        assertEquals("", Config.filter)
    }
}
