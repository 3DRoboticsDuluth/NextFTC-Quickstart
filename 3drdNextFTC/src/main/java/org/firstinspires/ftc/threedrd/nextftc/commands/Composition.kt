package org.firstinspires.ftc.threedrd.nextftc.commands

import dev.nextftc.core.commands.*
import dev.nextftc.core.commands.groups.*

fun Command.alongWith(vararg commands: Command) = ParallelGroup(this, *commands)
