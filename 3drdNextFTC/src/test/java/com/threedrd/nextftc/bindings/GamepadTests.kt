package com.threedrd.nextftc.bindings

import com.qualcomm.robotcore.hardware.Gamepad
import dev.nextftc.ftc.GamepadEx
import org.junit.Test
import org.mockito.Mockito.*

class GamepadTests {
    @Test
    fun sdkStartRumbleRequestsOneContinuousEffect() {
        val gamepad = mock(Gamepad::class.java)
        gamepad.startRumble()
        verify(gamepad).rumble(Gamepad.RUMBLE_DURATION_CONTINUOUS)
        verifyNoMoreInteractions(gamepad)
    }

    @Test
    fun enhancedGamepadRumbleHelpersResolveTheCurrentSdkGamepadEachTime() {
        val first = mock(Gamepad::class.java)
        val second = mock(Gamepad::class.java)
        var current = first
        val gamepad = GamepadEx { current }
        gamepad.startRumble()
        gamepad.stopRumble()
        current = second
        gamepad.startRumble()
        gamepad.stopRumble()
        for (sdk in listOf(first, second)) {
            verify(sdk).rumble(Gamepad.RUMBLE_DURATION_CONTINUOUS)
            verify(sdk).stopRumble()
            verifyNoMoreInteractions(sdk)
        }
    }
}
