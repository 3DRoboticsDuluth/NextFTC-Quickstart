package org.firstinspires.ftc.threedrd.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebounceTests {
    @Test
    fun providesAUsableSystemClockByDefault() {
        val debounce = Debounce()

        assertTrue(debounce.clock() > 0.0)
        assertFalse(debounce.previous)
        assertTrue(debounce.changedAt > 0.0)
        debounce.clock = { 1.0 }
        debounce.previous = true
        debounce.changedAt = 0.5
        assertEquals(1.0, debounce.clock(), 0.0)
        assertTrue(debounce.previous)
        assertEquals(0.5, debounce.changedAt, 0.0)
    }

    @Test
    fun acceptsOnlySeparatedRisingEdges() {
        var time = 0.0
        val debounce = Debounce { time }

        assertFalse(debounce.triggered(false, 0.1))
        time = 0.2
        assertTrue(debounce.triggered(true, 0.1))
        time = 0.4
        assertFalse(debounce.triggered(true, 0.1))
        assertFalse(debounce.triggered(false, 0.1))
        time = 0.6
        assertTrue(debounce.triggered(true, 0.1))

        debounce.reset()
        assertFalse(debounce.previous)
        assertFalse(debounce.triggered(true, 0.1))
    }
}
