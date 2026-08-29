package org.firstinspires.ftc.threedrd.nextftc.subsystems

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.threedrd.nextftc.config.Setting
import org.firstinspires.ftc.threedrd.nextftc.config.SettingItem
import org.firstinspires.ftc.threedrd.nextftc.config.ConfigComponent
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.OFF as LOG_OFF
import org.firstinspires.ftc.threedrd.nextftc.logging.Logging
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.OFF as TEL_OFF
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry
import org.firstinspires.ftc.threedrd.nextftc.subsystems.ConfigSubsystem.Change.NEXT
import org.firstinspires.ftc.threedrd.nextftc.subsystems.ConfigSubsystem.Change.PREV
import org.firstinspires.ftc.threedrd.testing.SubsystemTests
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.verify

class ConfigSubsystemTests : SubsystemTests() {
    private object MinimalConfig : ConfigSubsystem() {
        class Values(
            @Setting var enabled: Boolean = false,
            @Setting(live = true, inc = 0.5, min = 0.0, max = 1.0) var liveValue: Double = 0.5
        )

        override val config = Values()
        var changed: SettingItem? = null

        override fun onChange(item: SettingItem) {
            super.onChange(item)
            changed = item
        }
    }

    @Before
    fun reset() {
        MinimalConfig.config.enabled = false
        MinimalConfig.config.liveValue = 0.5
        MinimalConfig.changed = null
        MinimalConfig.state.apply {
            auto = false
            teleop = false
            started = false
            interrupt = false
            configurable = false
            setting = 0
        }
        ConfigComponent.onChange = {}
    }

    @Test
    fun configurationWithoutDiagnosticsDefaultsBothDisplaysOff() {
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }

        MinimalConfig.initialize()

        assertEquals(TEL_OFF, Telemetry.LEVEL)
        assertEquals(LOG_OFF, Logging.LEVEL)
        assertFalse(MinimalConfig.state.auto)
        assertFalse(MinimalConfig.state.interrupt)
        assertEquals(listOf("Enabled", "Live Value"), MinimalConfig.items.map { it.key })
    }

    @Test
    fun configurationCanUseTheDefaultChangeHook() {
        MinimalConfig.state.configurable = true
        MinimalConfig.config.enabled = false

        MinimalConfig.changeValue(NEXT)

        assertEquals(true, MinimalConfig.config.enabled)
    }

    @Test
    fun navigationEditingAndCaptionsRespectRuntimeState() {
        assertEquals(-1, PREV.sign)
        assertEquals(1, NEXT.sign)
        assertEquals("Enabled", MinimalConfig.caption(MinimalConfig.items[0]))

        MinimalConfig.changeItem(NEXT)
        assertEquals(0, MinimalConfig.state.setting)

        MinimalConfig.edit.start()
        assertTrue(MinimalConfig.state.configurable)
        assertEquals(">Enabled", MinimalConfig.caption(MinimalConfig.items[0]))
        assertEquals("Live Value", MinimalConfig.caption(MinimalConfig.items[1]))

        MinimalConfig.nextItem.start()
        assertEquals(1, MinimalConfig.state.setting)
        MinimalConfig.nextItem.start()
        assertEquals(1, MinimalConfig.state.setting)
        MinimalConfig.prevItem.start()
        assertEquals(0, MinimalConfig.state.setting)

        MinimalConfig.start()
        assertTrue(MinimalConfig.state.started)
        assertEquals("xEnabled", MinimalConfig.caption(MinimalConfig.items[0]))
        MinimalConfig.nextValue.start()
        assertFalse(MinimalConfig.config.enabled)

        MinimalConfig.state.setting = 1
        assertEquals(">Live Value", MinimalConfig.caption(MinimalConfig.items[1]))
        MinimalConfig.nextValue.start()
        assertEquals(1.0, MinimalConfig.config.liveValue, 0.0)
        assertEquals(MinimalConfig.items[1], MinimalConfig.changed)
        MinimalConfig.prevValue.start()
        assertEquals(0.5, MinimalConfig.config.liveValue, 0.0)

        MinimalConfig.done.start()
        assertFalse(MinimalConfig.state.configurable)
        MinimalConfig.changeValue(NEXT)
    }

    @Test
    fun actualChangesNotifyPersistenceAndPeriodicDisplaysEverySetting() {
        var persisted = 0
        ConfigComponent.onChange = { persisted++ }
        MinimalConfig.state.configurable = true

        MinimalConfig.changeValue(NEXT)
        assertEquals(1, persisted)
        assertEquals(MinimalConfig.items[0], MinimalConfig.changed)

        MinimalConfig.state.setting = 1
        MinimalConfig.config.liveValue = 1.0
        MinimalConfig.changeValue(NEXT)
        assertEquals(1, persisted)

        clearInvocations(ActiveOpMode.telemetry)
        MinimalConfig.periodic()
        verify(ActiveOpMode.telemetry).addData("Enabled", true as Any)
        verify(ActiveOpMode.telemetry).addData(">Live Value", 1.0 as Any)
    }
}
