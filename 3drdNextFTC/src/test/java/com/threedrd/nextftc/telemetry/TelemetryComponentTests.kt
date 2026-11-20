package com.threedrd.nextftc.telemetry

import com.bylazar.telemetry.*
import com.qualcomm.robotcore.eventloop.opmode.*
import com.qualcomm.robotcore.util.*
import dev.nextftc.core.commands.*
import dev.nextftc.core.commands.utility.*
import dev.nextftc.ftc.*
import org.firstinspires.ftc.robotcore.external.*
import com.threedrd.nextftc.logging.*
import com.threedrd.nextftc.logging.LogLevel.DEBUG
import com.threedrd.nextftc.logging.LogLevel.INFO
import com.threedrd.nextftc.logging.LogLevel.VERBOSE
import com.threedrd.nextftc.telemetry.Telemetry as TeamTelemetry
import org.junit.*
import org.mockito.*
import org.mockito.Mockito.*

class TelemetryComponentTests {
    private lateinit var telemetry: Telemetry
    private lateinit var telemetryLog: Telemetry.Log
    private lateinit var robotLog: MockedStatic<RobotLog>

    @Before
    fun setUp() {
        CommandManager.cancelAll()
        CommandManager.run()
        telemetry = mock(Telemetry::class.java)
        telemetryLog = mock(Telemetry.Log::class.java)
        `when`(telemetry.log()).thenReturn(telemetryLog)
        `when`(telemetryLog.capacity).thenReturn(9)
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }.apply { this.telemetry = this@TelemetryComponentTests.telemetry }
        Logging.DISPLAY_FILTER = ""
        Logging.FILTER = ""
        TeamTelemetry.DISPLAY_FILTER = ""
        TeamTelemetry.FILTER = ""
        PanelsTelemetry.telemetry.lines = mutableListOf()
        robotLog = mockStatic(RobotLog::class.java)
        TelemetryComponent.preInit()
        TeamTelemetry.output = telemetry
        Logging.LEVEL = DEBUG
        TeamTelemetry.LEVEL = com.threedrd.nextftc.telemetry.TelemetryLevel.INFO
        Logging.beginFrame()
        clearInvocations(telemetryLog)
    }

    @After
    fun tearDown() {
        verify(telemetryLog, never()).add(anyString())
        verify(telemetryLog, never()).clear()
        robotLog.close()
        CommandManager.cancelAll()
        CommandManager.run()
    }

    @Test
    fun componentRebuildsAndUpdatesBothLoopPhases() {
        TelemetryComponent.preWaitForStart()
        TelemetryComponent.preUpdate()
        TelemetryComponent.postWaitForStart()
        TelemetryComponent.postUpdate()

        verify(telemetry, times(2)).clear()
        verify(telemetry, times(2)).update()
    }

    @Test
    fun visibleHistoryGetsATitledLogSection() {
        Logging.add("Gate", INFO, "Opened")
        clearInvocations(telemetry)

        TelemetryComponent.postUpdate()

        verify(telemetry).addLine(TeamTelemetry.title("LOG"))
        verify(telemetry).update()
    }

    @Test
    fun commonAndSpecificFiltersRebuildHistory() {
        Logging.add("Gate", INFO, "Opened")
        Logging.add("Gate", INFO, "Closed")
        Logging.add("Deflector", INFO, "Up")
        clearInvocations(telemetryLog)
        Logging.DISPLAY_FILTER = "Gate"
        Logging.FILTER = "Open"

        TelemetryComponent.preUpdate()

        TelemetryComponent.postUpdate()
        verify(telemetryLog, never()).clear()
        verify(telemetry).addLine("I | Gate | Opened")
        verify(telemetry, never()).addLine("I | Gate | Closed")
        verify(telemetry, never()).addLine("I | Deflector | Up")
    }

    @Test
    fun changingOnlyTheSpecificFilterRebuildsHistoryAndHidesNewMismatches() {
        Logging.add("Gate", INFO, "Opened")
        clearInvocations(telemetryLog)
        Logging.FILTER = "Deflector"

        TelemetryComponent.preUpdate()
        Logging.add("Gate", INFO, "Closed")

        TelemetryComponent.postUpdate()
        verify(telemetryLog, never()).clear()
        verify(telemetry, never()).addLine("I | Gate | Opened")
        verify(telemetry, never()).addLine("I | Gate | Closed")
    }

    @Test
    fun levelChangesRebuildHistory() {
        Logging.LEVEL = VERBOSE
        TelemetryComponent.preUpdate()
        Logging.add("Gate", VERBOSE, "Position")
        Logging.add("Gate", INFO, "Opened")
        clearInvocations(telemetryLog)
        Logging.LEVEL = INFO

        TelemetryComponent.preUpdate()

        TelemetryComponent.postUpdate()
        verify(telemetryLog, never()).clear()
        verify(telemetry, never()).addLine("V | Gate | Position")
        verify(telemetry).addLine("I | Gate | Opened")
    }

    @Test
    fun historyUsesTheDriverStationCapacity() {
        `when`(telemetryLog.capacity).thenReturn(2)
        Logging.add("Test", INFO, "Event 1")
        Logging.add("Test", INFO, "Event 2")
        Logging.add("Test", INFO, "Event 3")
        clearInvocations(telemetryLog)

        TelemetryComponent.postUpdate()

        verify(telemetry, never()).addLine("I | Test | Event 1")
        verify(telemetry).addLine("I | Test | Event 2")
        verify(telemetry).addLine("I | Test | Event 3")
    }

    @Test
    fun commandSnapshotsLogChangesAndAssociateIdleWithThePreviousCommand() {
        val drive = LambdaCommand("Drive").setIsDone { false }
        CommandManager.scheduleCommand(drive)
        CommandManager.run()

        TelemetryComponent.preUpdate()
        TelemetryComponent.preUpdate()
        CommandManager.cancelCommand(drive)
        CommandManager.run()
        TelemetryComponent.preUpdate()

        TelemetryComponent.postUpdate()
        verify(telemetry).addLine("D | Commands | Running | Drive")
        verify(telemetry).addLine("D | Commands | Idle")
        val idle = Logging.history.last()
        org.junit.Assert.assertTrue(idle.matches("Drive"))
    }

    @Test
    fun commandSnapshotsExcludeNullCommands() {
        val nullCommand = LambdaCommand("NullCommand").setIsDone { false }
        CommandManager.scheduleCommand(nullCommand)
        CommandManager.run()

        TelemetryComponent.preUpdate()

        verify(telemetry, never()).addLine("D | Commands | Running | NullCommand")
    }
}
