package com.threedrd.pedropathing

import com.pedropathing.follower.Follower
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.nextftc.core.components.Component
import dev.nextftc.extensions.pedro.PedroComponent as NextPedroComponent

interface PedroConfig {
    val robotRadius: Double
    fun createFollower(hardwareMap: HardwareMap): Follower
}

class PedroComponent(config: PedroConfig) : Component {
    companion object {
        private var follower: Follower? = null
        private var hardwareMap: HardwareMap? = null
    }

    val pedro = NextPedroComponent { map ->
        if (hardwareMap !== map) {
            follower = null
            hardwareMap = map
        }
        (follower ?: config.createFollower(map).also { follower = it }).apply {
            breakFollowing()
            update()
        }
    }

    val drawing = PedroDrawingComponent(config.robotRadius)

    override fun preInit() { pedro.preInit(); drawing.preInit() }
    override fun preWaitForStart() = pedro.preWaitForStart()
    override fun postWaitForStart() = drawing.postWaitForStart()
    override fun preUpdate() = pedro.preUpdate()
    override fun postUpdate() = drawing.postUpdate()
    override fun preStop() = NextPedroComponent.follower.breakFollowing()
    override fun postStop() = pedro.postStop()
}
