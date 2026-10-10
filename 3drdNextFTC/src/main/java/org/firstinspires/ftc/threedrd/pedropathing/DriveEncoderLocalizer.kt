package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.localization.*
import com.pedropathing.math.*
import com.pedropathing.revhub.localizers.*
import com.qualcomm.robotcore.hardware.*

/** Pedro 3 adapter for the four-drive-encoder option formerly supplied by Pedro 2. */
class DriveEncoderLocalizer(
    hardwareMap: HardwareMap,
    val config: DriveEncoderConfig,
    val nanoTime: () -> Long = System::nanoTime
) : Localizer {
    private val frontLeft = Encoder(hardwareMap.get(DcMotorEx::class.java, config.frontLeftName)).apply { setDirection(config.frontLeftDirection) }
    private val frontRight = Encoder(hardwareMap.get(DcMotorEx::class.java, config.frontRightName)).apply { setDirection(config.frontRightDirection) }
    private val backLeft = Encoder(hardwareMap.get(DcMotorEx::class.java, config.backLeftName)).apply { setDirection(config.backLeftDirection) }
    private val backRight = Encoder(hardwareMap.get(DcMotorEx::class.java, config.backRightName)).apply { setDirection(config.backRightDirection) }
    private val encoders = listOf(frontLeft, frontRight, backLeft, backRight)
    private var motion = MotionState.zero()
    private var previousTime = nanoTime()

    init {
        require(config.robotWidth + config.robotLength > 0.0) { "Drive encoder dimensions must have a positive sum" }
    }

    override fun state() = motion

    override fun setPose(pose: Pose) {
        encoders.forEach { it.reset() }
        motion = MotionState.ofVelocity(pose, Velocity.zero())
        previousTime = nanoTime()
    }

    override fun reset() = setPose(Pose.zero())

    override fun update() {
        val now = nanoTime()
        val seconds = (now - previousTime) / 1e9
        previousTime = now
        encoders.forEach { it.update() }
        val fl = frontLeft.deltaPosition
        val fr = frontRight.deltaPosition
        val bl = backLeft.deltaPosition
        val br = backRight.deltaPosition
        val delta = Twist(
            config.forwardTicksToInches * (fl + fr + bl + br),
            config.strafeTicksToInches * (-fl + fr + bl - br),
            config.turnTicksToInches * (-fl + fr - bl + br) / (config.robotWidth + config.robotLength)
        )
        val start = motion.pose()
        val end = start.exp(delta)
        val velocity = if (seconds > 0.0) Velocity(
            (end.x() - start.x()) / seconds,
            (end.y() - start.y()) / seconds,
            delta.omega / seconds
        ) else Velocity.zero()
        motion = MotionState.ofVelocity(end, velocity)
    }
}
