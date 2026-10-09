package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.extensions.pedro.*
import dev.nextftc.ftc.*
import dev.nextftc.ftc.components.*
import org.firstinspires.ftc.threedrd.nextftc.bindings.*
import org.firstinspires.ftc.threedrd.nextftc.config.*
import org.firstinspires.ftc.threedrd.nextftc.subsystems.*
import org.firstinspires.ftc.threedrd.nextftc.telemetry.*
import org.firstinspires.ftc.threedrd.pedropathing.*
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.*
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.*
import org.firstinspires.ftc.teamcode.subsystems.*

abstract class OpMode : NextFTCOpMode() {
    init {
        configureHardwareTelemetry()
        addComponents(
            TelemetryComponent,
            BindingsComponent,
            BulkReadComponent,
            PedroComponent(Constants::createFollower),
            PedroDrawingComponent(robotRadius = Constants.robotRadius),
            ConfigComponent(Config),
            SubsystemComponent.all()
        )
    }
}
