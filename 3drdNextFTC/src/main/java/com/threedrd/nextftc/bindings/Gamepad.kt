package com.threedrd.nextftc.bindings

import com.qualcomm.robotcore.hardware.Gamepad
import dev.nextftc.ftc.GamepadEx

fun Gamepad.startRumble() = rumble(Gamepad.RUMBLE_DURATION_CONTINUOUS)
fun GamepadEx.startRumble() = gamepad().startRumble()
fun GamepadEx.stopRumble() = gamepad().stopRumble()
