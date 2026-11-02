package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.*
import com.threedrd.nextftc.config.Diagnostics.Level.INFO
import com.threedrd.nextftc.config.*
import com.threedrd.nextftc.subsystems.*

@Configurable
object Config : ConfigSubsystem() {
    @Setting(live = true)
    var robotCentric = true

    @Setting(live = true)
    var level = INFO

    @Transient
    var filter = ""
}
