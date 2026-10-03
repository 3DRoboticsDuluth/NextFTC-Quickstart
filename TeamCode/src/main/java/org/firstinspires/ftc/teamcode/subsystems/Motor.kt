package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.qualcomm.robotcore.hardware.DcMotor.RunMode
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_WITHOUT_ENCODER
import dev.nextftc.ftc.Gamepads.gamepad1
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.tel
import org.firstinspires.ftc.threedrd.nextftc.hardware.MotorEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.update
import org.firstinspires.ftc.threedrd.nextftc.subsystems.ManuallyRegistered
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Subsystem

@Configurable
@ManuallyRegistered
object Motor : Subsystem() {
    var POWER = 0.20

    val motor = MotorEx("leftMotor")

    val forwardWithEncoder by instant { run(RUN_USING_ENCODER, POWER) }
    val reverseWithEncoder by instant { run(RUN_USING_ENCODER, -POWER) }
    val forwardWithoutEncoder by instant { run(RUN_WITHOUT_ENCODER, POWER) }
    val reverseWithoutEncoder by instant { run(RUN_WITHOUT_ENCODER, -POWER) }
    val off by instant { motor.update { power = 0.0 } }

    override fun initialize() {
        motor.motor.power = 0.0
        motor.motor.mode = RUN_WITHOUT_ENCODER
    }

    override fun controls() {
        gamepad1.a whenBecomesTrue forwardWithEncoder
        gamepad1.b whenBecomesTrue reverseWithEncoder
        gamepad1.x whenBecomesTrue forwardWithoutEncoder
        gamepad1.y whenBecomesTrue reverseWithoutEncoder
        gamepad1.leftBumper whenBecomesTrue off
    }

    override fun periodic() {
        tel.info("Controls", "A/B: with encoder | X/Y: without | LB: stop")
        motor.tel()
    }

    override fun stop() {
        motor.motor.power = 0.0
    }

    fun run(mode: RunMode, selectedPower: Double) {
        motor.update {
            power = 0.0
            power = selectedPower
            motor.mode = mode
        }
    }
}
