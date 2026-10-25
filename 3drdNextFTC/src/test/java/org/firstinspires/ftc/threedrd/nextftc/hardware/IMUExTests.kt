package org.firstinspires.ftc.threedrd.nextftc.hardware

import org.firstinspires.ftc.threedrd.testing.*
import org.junit.Assert.*
import org.junit.*
import org.mockito.Mockito.*

class IMUExTests : SubsystemTests() {
    @Test
    fun initializationResolvesAndConfiguresTheImu() {
        var configured = false
        clearInvocations(dev.nextftc.ftc.ActiveOpMode.hardwareMap)
        val imu = IMUEx(configure = { configured = true })
        assertFalse(configured)
        verify(dev.nextftc.ftc.ActiveOpMode.hardwareMap, never()).get(
            com.qualcomm.robotcore.hardware.IMU::class.java, "imu"
        )

        imu.initialize()

        assertNotNull(imu.imu)
        assertEquals(dev.nextftc.hardware.impl.IMUEx::class.java, imu.javaClass)
        assertTrue(configured)
        `when`(dev.nextftc.ftc.ActiveOpMode.hardwareMap.getNamesOf(imu.imu)).thenReturn(setOf("imu"))
        assertEquals("imu", imu.name)
        imu.zero()
        verify(imu.imu).resetYaw()
        clearInvocations(imu.imu)
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
    @Test
    fun originalConstructorAndUnregisteredDeviceNameRemainAvailable() {
        val sdk = mock(com.qualcomm.robotcore.hardware.IMU::class.java)
        val imu = IMUEx(sdk)
        `when`(dev.nextftc.ftc.ActiveOpMode.hardwareMap.getNamesOf(sdk)).thenReturn(emptySet())
        `when`(sdk.deviceName).thenReturn("Standalone IMU")
        assertSame(sdk, imu.imu)
        assertEquals("Standalone IMU", imu.name)
        imu.zero()
        verify(sdk).resetYaw()
    }

}
