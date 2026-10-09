package org.firstinspires.ftc.threedrd.nextftc.opmodes

import com.qualcomm.robotcore.eventloop.opmode.*
import dev.nextftc.ftc.*

val ActiveOpMode.isAutonomous: Boolean
    get() = it!!.javaClass.isAnnotationPresent(Autonomous::class.java)

val ActiveOpMode.isTeleop: Boolean
    get() = it!!.javaClass.isAnnotationPresent(TeleOp::class.java)
