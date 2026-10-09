package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.DigitalChannel.Mode.INPUT
import org.firstinspires.ftc.threedrd.testing.*
import org.junit.Assert.*
import org.junit.*

class DeviceTests : SubsystemTests() {
    @Test
    fun initializationResolvesAndConfiguresAnArbitraryDevice() {
        var configured = false
        val device = Device("laser", DigitalChannel::class.java) {
            mode = INPUT
            configured = true
        }

        device.initialize()

        assertEquals("laser", device.name)
        assertEquals(DigitalChannel::class.java, device.type)
        assertNotNull(device.device)
        assertTrue(configured)
    }

    @Test
    fun delegatesToTheResolvedDevice() {
        val device = Device("laser", DigitalChannel::class.java)
        device.initialize()
        val owner = object {
            val laser by device
        }

        assertSame(device.device, owner.laser)
    }
}
