package org.firstinspires.ftc.threedrd.nextftc.bindings

import com.qualcomm.robotcore.eventloop.opmode.*
import dev.nextftc.bindings.*
import dev.nextftc.ftc.*
import org.junit.Assert.*
import org.junit.*

class BindingsComponentTests {
    var pressed = false
    var first = 0
    var second = 0
    val button = Button { pressed }.also(BindingManager::add)

    @Before
    fun reset() {
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }
        BindingsComponent.preInit()
    }

    @Test
    fun initializationClearsBindingsFromAnAbortedOpMode() {
        button.whenTrue { first++ }

        BindingsComponent.preInit()
        button.whenTrue { second++ }
        pressed = true
        BindingsComponent.preWaitForStart()

        assertEquals(0, first)
        assertEquals(1, second)
    }

    @Test
    fun bindingsUpdateDuringTheOpModeAndClearAtStop() {
        button.whenTrue { first++ }
        pressed = true

        BindingsComponent.preWaitForStart()
        BindingsComponent.preUpdate()
        BindingsComponent.postStop()
        BindingsComponent.preUpdate()

        assertEquals(2, first)
    }
}
