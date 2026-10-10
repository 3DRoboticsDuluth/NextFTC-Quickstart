package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.follower.*
import com.qualcomm.robotcore.hardware.*
import dev.nextftc.core.components.*
import dev.nextftc.ftc.*

class PedroComponent(val createFollower: (HardwareMap) -> Follower) : Component {
    override fun preInit() {
        activeFollower = createFollower(ActiveOpMode.hardwareMap)
        progress = PathProgress()
    }

    override fun preWaitForStart() = follower.update()
    override fun preUpdate() = follower.update()

    override fun postStop() {
        try {
            activeFollower?.stopNow()
        } finally {
            activeFollower = null
            progress = PathProgress()
        }
    }

    companion object {
        private var activeFollower: Follower? = null
        val follower: Follower
            get() = checkNotNull(activeFollower) { "Follower not initialized; add PedroComponent to the OpMode." }
        var progress = PathProgress()
            private set
    }
}
