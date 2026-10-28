package org.firstinspires.ftc.threedrd.nextftc.opmodes

import com.qualcomm.robotcore.eventloop.opmode.*
import dev.nextftc.ftc.*

val ActiveOpMode.isAutonomous: Boolean
    get() = isAuto

val ActiveOpMode.isTeleop: Boolean
    get() = org.firstinspires.ftc.threedrd.nextftc.opmodes.isTeleop

val isAuto: Boolean
    get() = ActiveOpMode.it!!.javaClass.isAnnotationPresent(Autonomous::class.java)

val isTeleop: Boolean
    get() = ActiveOpMode.it!!.javaClass.isAnnotationPresent(TeleOp::class.java)
