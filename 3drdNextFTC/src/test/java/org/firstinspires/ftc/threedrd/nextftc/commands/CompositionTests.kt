package org.firstinspires.ftc.threedrd.nextftc.commands

import dev.nextftc.core.commands.utility.*
import org.junit.Assert.*
import org.junit.*

class CompositionTests {
    @Test
    fun alongWithComposesCommandsInParallel() {
        val first = NullCommand().named("First")
        val second = NullCommand().named("Second")

        val group = first.alongWith(second)

        assertArrayEquals(arrayOf(first, second), group.commands)
    }
}
