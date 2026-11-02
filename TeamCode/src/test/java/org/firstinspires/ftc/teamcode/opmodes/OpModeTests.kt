package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.extensions.pedro.*
import com.threedrd.nextftc.bindings.*
import com.threedrd.nextftc.config.*
import com.threedrd.nextftc.telemetry.*
import com.threedrd.pedropathing.*
import org.firstinspires.ftc.teamcode.subsystems.*
import org.junit.Assert.*
import org.junit.*

class OpModeTests {
    @Test
    fun compositionCanBeCreated() {
        val opMode = object : OpMode() {}

        assertNotNull(opMode)
        assertTrue(opMode.components.contains(TelemetryComponent))
        assertEquals(1, opMode.components.count { it === BindingsComponent })
        assertTrue(opMode.components.any { it is PedroComponent })
        assertTrue(opMode.components.any { it is PedroDrawingComponent })
        val component = opMode.components.filterIsInstance<ConfigComponent<*>>().single()
        assertSame(Config, component.config)
        assertEquals("config.json", component.persistence.fileName)
        assertEquals(Config::class.java, component.persistence.type)
    }
}
