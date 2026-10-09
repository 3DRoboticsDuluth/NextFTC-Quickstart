package org.firstinspires.ftc.threedrd.nextftc.commands

import org.firstinspires.ftc.threedrd.nextftc.subsystems.*
import org.junit.Assert.*
import org.junit.*

class InstantCommandTests {
    private object TestSubsystem : Subsystem()

    @Test
    fun retainsItsOwnerAndAction() {
        var ran = false
        val action = Runnable { ran = true }
        val command = InstantCommand(TestSubsystem, action)

        assertSame(TestSubsystem, command.owner)
        assertSame(action, command.action)
        assertTrue(command.requirements.contains(TestSubsystem))

        command.start()
        assertTrue(ran)
    }
}
