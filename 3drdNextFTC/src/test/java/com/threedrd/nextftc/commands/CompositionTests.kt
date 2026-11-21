package com.threedrd.nextftc.commands

import dev.nextftc.core.commands.Command
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

class CompositionTests {
    @Test
    fun onlyIfSelectsAtStartAndKeepsThatChoiceForThePhase() {
        val original = mock(Command::class.java)
        var enabled = false
        var evaluations = 0
        val conditional = original.onlyIf { evaluations++; enabled }
        assertEquals(0, evaluations)
        enabled = true
        conditional.start()
        assertEquals(1, evaluations)
        verify(original).start()
        enabled = false
        conditional.update()
        verify(original).update()
        `when`(original.isDone).thenReturn(true)
        assertTrue(conditional.isDone)
        assertEquals(1, evaluations)
        conditional.stop(true)
        verify(original).stop(true)
    }

    @Test
    fun onlyIfSkipsFalseAndReevaluatesWhenStartedAgain() {
        val original = mock(Command::class.java)
        var enabled = false
        val conditional = original.onlyIf { enabled }
        conditional.start()
        conditional.update()
        assertTrue(conditional.isDone)
        conditional.stop(false)
        verify(original, never()).start()
        verify(original, never()).update()
        verify(original, never()).stop(anyBoolean())
        enabled = true
        conditional.start()
        verify(original).start()
        conditional.stop(false)
        verify(original).stop(false)
    }
}
