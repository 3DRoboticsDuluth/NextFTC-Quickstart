package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.extensions.pedro.*
import dev.nextftc.ftc.*
import dev.nextftc.ftc.components.*
import com.threedrd.nextftc.bindings.*
import com.threedrd.nextftc.config.*
import com.threedrd.nextftc.subsystems.*
import com.threedrd.nextftc.telemetry.*
import com.threedrd.pedropathing.*
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
