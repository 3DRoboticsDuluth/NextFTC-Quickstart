package org.firstinspires.ftc.teamcode.opmodes

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.nextftc.ftc.NextFTCOpMode
import dev.nextftc.ftc.components.BulkReadComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.configureHardwareTelemetry
import org.firstinspires.ftc.teamcode.subsystems.Config
import org.firstinspires.ftc.teamcode.subsystems.Motor
import org.firstinspires.ftc.threedrd.nextftc.bindings.BindingsComponent
import org.firstinspires.ftc.threedrd.nextftc.config.ConfigComponent
import org.firstinspires.ftc.threedrd.nextftc.subsystems.SubsystemComponent
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryComponent

@TeleOp
@Suppress("unused")
class MotorDiagnostics : NextFTCOpMode() {
    init {
        configureHardwareTelemetry()
        addComponents(
            TelemetryComponent,
            BindingsComponent,
            BulkReadComponent,
            ConfigComponent(Config),
            SubsystemComponent(Config, Motor)
        )
    }
}
