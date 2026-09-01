package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.core.commands.CommandManager
import org.firstinspires.ftc.teamcode.subsystems.Auto.execute
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
