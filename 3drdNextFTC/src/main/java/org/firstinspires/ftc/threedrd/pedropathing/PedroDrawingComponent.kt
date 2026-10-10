package org.firstinspires.ftc.threedrd.pedropathing

import com.bylazar.field.*
import com.pedropathing.follower.*
import com.pedropathing.math.*
import com.pedropathing.paths.*
import dev.nextftc.core.components.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import org.firstinspires.ftc.threedrd.pedropathing.PedroComponent.Companion.follower

class PedroDrawingComponent(
    val field: FieldManager = PanelsField.field,
    val getFollower: () -> Follower = { follower },
    val robotRadius: Double = 9.0
) : Component {
    val targetStyle = Style("#666", "#3F51B5", 2.0)
    val robotStyle = Style("#666", "#4CAF50", 2.0)
    private val history = ArrayDeque<Pose>()

    override fun preInit() {
        history.clear()
        field.setOffsets(PanelsField.presets.DEFAULT_FTC)
    }

    override fun postWaitForStart() = draw()
    override fun postUpdate() = draw()

    fun draw() {
        val follower = getFollower()
        follower.currentPath()?.let { path ->
            draw(path, targetStyle)
            draw(follower.closestPose(), targetStyle)
        }
        history.addLast(follower.pose())
        if (history.size > 100) history.removeFirst()
        drawHistory()
        draw(follower.pose(), robotStyle)
        field.update()
    }

    fun draw(path: Path, style: Style) {
        field.setStyle(style)
        val start = path.get(0.0)
        field.moveCursor(start.x(), start.y())
        for (index in 1..40) {
            val point = path.get(index / 40.0)
            field.line(point.x(), point.y())
        }
    }

    private fun drawHistory() {
        field.setStyle(robotStyle)
        val poses = history.iterator()
        var previous = poses.next()
        while (poses.hasNext()) {
            val pose = poses.next()
            field.moveCursor(previous.x(), previous.y())
            field.line(pose.x(), pose.y())
            previous = pose
        }
    }

    fun draw(pose: Pose, style: Style) {
        field.setStyle(style)
        field.moveCursor(pose.x(), pose.y())
        field.circle(robotRadius)
        val x = cos(pose.heading()) * robotRadius
        val y = sin(pose.heading()) * robotRadius
        field.moveCursor(pose.x() + x / 2, pose.y() + y / 2)
        field.line(pose.x() + x, pose.y() + y)
    }
}
