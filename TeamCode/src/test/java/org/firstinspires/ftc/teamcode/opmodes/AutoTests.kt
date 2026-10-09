package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.core.commands.*
import org.firstinspires.ftc.teamcode.subsystems.Auto.execute
import org.junit.*
import org.junit.Assert.*

class AutoTests {
    @After
    fun tearDown() {
        CommandManager.cancelAll()
        CommandManager.run()
    }

    @Test
    fun compositionCanBeCreated() {
        assertNotNull(Auto())
    }

    @Test
    fun schedulesTheExampleRoutineAtStart() {
        Auto().onStartButtonPressed()

        assertTrue(execute.isScheduled)
    }
}
