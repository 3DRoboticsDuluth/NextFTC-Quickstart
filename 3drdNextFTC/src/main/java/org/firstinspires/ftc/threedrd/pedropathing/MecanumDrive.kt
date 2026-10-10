package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.revhub.drivetrains.*
import com.qualcomm.robotcore.hardware.*

/** Exposes Pedro's existing motors once during construction for read-only telemetry. */
class MecanumDrive(hardwareMap: HardwareMap, config: MecanumConfig) : Mecanum(hardwareMap, config) {
    val motors = (motorField.get(this) as Array<*>).map { (it as CachedMotor).raw() }

    companion object {
        private val motorField = Mecanum::class.java.getDeclaredField("motors").apply { isAccessible = true }
    }
}
