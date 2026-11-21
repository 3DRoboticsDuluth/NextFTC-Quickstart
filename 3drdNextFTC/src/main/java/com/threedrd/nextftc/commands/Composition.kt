package com.threedrd.nextftc.commands

import dev.nextftc.core.commands.*
import dev.nextftc.core.commands.conditionals.IfElseCommand

fun Command.onlyIf(predicate: () -> Boolean): Command = IfElseCommand(predicate, this)
