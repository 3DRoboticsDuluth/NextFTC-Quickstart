package org.firstinspires.ftc.threedrd.nextftc.config

import com.bylazar.configurables.PanelsConfigurables
import dev.nextftc.core.components.Component
import org.firstinspires.ftc.threedrd.ftc.Persistence

class ConfigComponent<T : Any>(
    val config: T,
    val persistence: Persistence<T>,
    private val refresh: (T) -> Unit = PanelsConfig.refresher(
        PanelsConfigurables::class.java
    )
) : Component {
    companion object {
        var onChange: () -> Unit = {}

        fun changed() = onChange()
    }

    constructor(config: T) : this(
        config,
        Persistence(
            "${config.javaClass.simpleName.lowercase()}.json",
            config.javaClass,
            include = { it.getAnnotation(Setting::class.java) != null }
        )
    )

    override fun preInit() {
        onChange = {
            persistence.changed()
            refresh(config)
        }
        persistence.load(config)
        refresh(config)
    }

    override fun postWaitForStart() {
        persistence.update(config)
    }

    override fun postUpdate() {
        persistence.update(config)
    }
}
