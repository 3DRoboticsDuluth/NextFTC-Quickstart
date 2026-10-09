package org.firstinspires.ftc.threedrd.nextftc.hardware

import org.firstinspires.ftc.threedrd.testing.*
import org.junit.Assert.*
import org.junit.*
import org.mockito.Mockito.*

class IMUExTests : SubsystemTests() {
    @Test
    fun initializationResolvesAndConfiguresTheImu() {
        var configured = false
        val imu = IMUEx(configure = { configured = true })

        imu.initialize()

        assertNotNull(imu.imu)
        assertNotNull(imu.imuEx)
        assertTrue(configured)
        assertSame(imu, imu.zeroed())
        verify(imu.imu).resetYaw()
    }

    @Test
    fun updateAppliesTheActionAndReportsTheImu() {
        val imu = IMUEx("test")
        var actionApplied = false
        var reported: IMUEx? = null
        HardwareTelemetry.imu(imu)
        HardwareTelemetry.imu = { reported = this }

        imu.update { actionApplied = true }

        assertTrue(actionApplied)
        assertSame(imu, reported)
    }
}
