package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.MotorControlAlgorithm
import com.qualcomm.robotcore.hardware.PIDFCoefficients
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.threedrd.testing.SubsystemTests
import org.junit.Assert.*
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

class MotorPIDFTests : SubsystemTests() {
    @Test
    fun gettersReadHubSettingsAndDeclarationsRemainLazy() {
        val map = ActiveOpMode.hardwareMap
        clearInvocations(map)
        val lazy = MotorEx("pidf")
        verifyNoInteractions(map)
        lazy.initialize()
        verify(map).get(DcMotorEx::class.java, "pidf")
        val sdk = mock(DcMotorEx::class.java)
        val velocity = PIDFCoefficients(1.0, 2.0, 3.0, 4.0)
        `when`(sdk.getPIDFCoefficients(RUN_TO_POSITION)).thenReturn(PIDFCoefficients(5.0, 0.0, 0.0, 0.0))
        `when`(sdk.getPIDFCoefficients(RUN_USING_ENCODER)).thenReturn(velocity)
        val motor = MotorEx(sdk)
        assertEquals(5.0, motor.positionP, 0.0)
        assertSame(velocity, motor.velocityPIDF)
        verify(sdk, never()).setMode(any())
        verify(sdk, never()).setPower(anyDouble())
    }

    @Test
    fun positionChangesAreCachedPerMotorAndReappliedAfterInitialization() {
        val sdk = mock(DcMotorEx::class.java)
        val motor = MotorEx(sdk)
        motor.positionP = 5.0
        motor.positionP = 5.0
        motor.positionP = 6.0
        verify(sdk).setPositionPIDFCoefficients(5.0)
        verify(sdk).setPositionPIDFCoefficients(6.0)
        motor.initialize()
        motor.positionP = 6.0
        verify(sdk, times(2)).setPositionPIDFCoefficients(6.0)
        val other = mock(DcMotorEx::class.java)
        MotorEx(other).positionP = 6.0
        verify(other).setPositionPIDFCoefficients(6.0)
    }

    @Test
    fun velocityTracksMutableValuesAndAlgorithmWithoutRetainingTheInputObject() {
        val sdk = mock(DcMotorEx::class.java)
        val motor = MotorEx(sdk)
        val gains = PIDFCoefficients(1.0, 2.0, 3.0, 4.0)
        motor.velocityPIDF = gains
        motor.velocityPIDF = PIDFCoefficients(gains)
        gains.p = 10.0
        motor.velocityPIDF = gains
        gains.i = 20.0
        motor.velocityPIDF = gains
        gains.d = 30.0
        motor.velocityPIDF = gains
        gains.f = 40.0
        motor.velocityPIDF = gains
        gains.algorithm = MotorControlAlgorithm.LegacyPID
        motor.velocityPIDF = gains
        motor.initialize()
        motor.velocityPIDF = gains
        val values = ArgumentCaptor.forClass(PIDFCoefficients::class.java)
        verify(sdk, times(7)).setPIDFCoefficients(eq(RUN_USING_ENCODER), values.capture())
        assertEquals(listOf(1.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0), values.allValues.map { it.p })
        assertEquals(2.0, values.allValues[1].i, 0.0)
        assertEquals(20.0, values.allValues[2].i, 0.0)
        assertEquals(30.0, values.allValues[3].d, 0.0)
        assertEquals(40.0, values.allValues[4].f, 0.0)
        assertEquals(MotorControlAlgorithm.LegacyPID, values.allValues.last().algorithm)
        assertNotSame(gains, values.allValues.last())
        verify(sdk, never()).setMode(any())
        verify(sdk, never()).setPower(anyDouble())
    }
}

