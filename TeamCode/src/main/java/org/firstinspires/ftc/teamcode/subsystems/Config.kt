package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics.Level
import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics.Level.INFO
import org.firstinspires.ftc.threedrd.nextftc.config.Setting
import org.firstinspires.ftc.threedrd.nextftc.subsystems.ConfigSubsystem

@Configurable
object Config : ConfigSubsystem() {
    override var config = Config()
        private set

    data class Config(
        @Setting(live = true)
        var robotCentric: Boolean = true,

        @Setting(live = true)
        var level: Level = INFO,

        @Transient
        var filter: String = ""
    )
}
