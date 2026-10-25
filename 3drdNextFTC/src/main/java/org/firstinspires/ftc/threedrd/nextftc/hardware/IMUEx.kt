package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.*
import dev.nextftc.ftc.ActiveOpMode.hardwareMap

typealias IMUEx = dev.nextftc.hardware.impl.IMUEx

fun IMUEx(name: String = "imu", configure: IMU.() -> Unit = {}) =
    IMUEx(imuFactory = { hardwareMap.get(IMU::class.java, name).apply(configure) })

val IMUEx.name get() = hardwareMap.getNamesOf(imu).firstOrNull() ?: imu.deviceName

fun IMUEx.initialize() { imu }
