package org.firstinspires.ftc.threedrd.pedropathing

import com.bylazar.field.*
import com.pedropathing.api.*
import com.pedropathing.math.*
import org.junit.Assert.*
import org.junit.*
import org.mockito.Mockito.*
import org.firstinspires.ftc.threedrd.testing.*

class PedroDrawingComponentTests : SubsystemTests() {
    @Test
    fun initializesDrawsNativePathsAndKeepsBoundedHistory() {
        val follower = mockFollower()
        val component = PedroComponent { follower }.apply { preInit() }
        try {
            val defaults = PedroDrawingComponent()
            assertSame(PanelsField.field, defaults.field)
            assertEquals(9.0, defaults.robotRadius, 0.0)
            assertSame(follower, defaults.getFollower())
            val field = FieldManager()
            val drawing = PedroDrawingComponent(field, { follower }, 7.125)
            drawing.preInit()
            `when`(follower.pose()).thenReturn(Pose(5.0, 6.0, 0.0))
            field.lastUpdate = 0
            drawing.postWaitForStart()
            assertEquals(PanelsField.presets.DEFAULT_FTC, field.lastCanvas.preset)
            assertEquals(2, field.lastCanvas.items.size)
            assertEquals(7.125, (field.lastCanvas.items[0] as Circle).r, 0.0)
            assertTrue(field.lastCanvas.items[1] is Line)
            val path = Paths.line(Pose(10.0, 40.0), Pose(30.0, 60.0)).constant(0.0)
            `when`(follower.currentPath()).thenReturn(path)
            `when`(follower.closestPose()).thenReturn(Pose(20.0, 50.0))
            field.lastUpdate = 0
            drawing.postUpdate()
            val items = field.lastCanvas.items
            assertEquals(45, items.size)
            assertEquals(drawing.targetStyle, (items[0] as Line).style)
            assertEquals(drawing.targetStyle, (items[40] as Circle).style)
            assertEquals(drawing.robotStyle, (items[42] as Line).style)
            assertEquals(Style("#666", "#4CAF50", 2.0), drawing.robotStyle)
            `when`(follower.currentPath()).thenReturn(null)
            repeat(101) {
                field.lastUpdate = 0
                drawing.postUpdate()
            }
            assertEquals(101, field.lastCanvas.items.size)
            drawing.preInit()
            field.lastUpdate = 0
            drawing.draw()
            assertEquals(2, field.lastCanvas.items.size)
        } finally {
            component.postStop()
        }
    }
}
