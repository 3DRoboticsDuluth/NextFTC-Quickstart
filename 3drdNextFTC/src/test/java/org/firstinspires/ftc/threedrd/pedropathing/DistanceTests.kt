package org.firstinspires.ftc.threedrd.pedropathing

import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceTests {
    @Test
    fun convertsTilesToFieldDistance() {
        assertEquals(23.5, TILE_WIDTH.inIn, 0.0)
        assertEquals(23.5, 1.tile.inIn, 0.0)
        assertEquals(47.0, 2.tiles.inIn, 0.0)
    }
}
