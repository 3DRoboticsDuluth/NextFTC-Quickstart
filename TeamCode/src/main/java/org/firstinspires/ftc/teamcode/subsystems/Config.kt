package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.*
import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics.Level.INFO
import org.firstinspires.ftc.threedrd.nextftc.config.*
import org.firstinspires.ftc.threedrd.nextftc.subsystems.*

@Configurable
object Config : ConfigSubsystem() {
    @Setting(live = true)
    var robotCentric = true

    @Setting(live = true)
    var level = INFO

    @Transient
    var filter = ""
}
