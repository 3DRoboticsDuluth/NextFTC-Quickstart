# Worked example: a roller and sensor

This is a hypothetical teaching specification, not an implemented Quickstart
mechanism or a claim about a team's robot. Its power and time limit are example
choices. A team must choose and physically validate its own values. The simpler
[motor tutorial](motor-subsystem.md) reports occupancy but does not implement the
automatic-stop and timeout behavior specified here.

## Goal, scope, and architecture

Goal: the operator can collect one game piece, stop promptly, and recognize when
the sensor detects it. Scope: Teleop roller operation only; autonomous intake,
ejection, jam detection, and mechanical retention are outside this example.

The `Roller` subsystem owns a `MotorEx("roller")` and a delegated
`Device("rollerSensor", DigitalChannel::class.java)` configured as input. Assume
active-low detection for the example; this must be confirmed on hardware.
`occupied` means sensor state is false, not a guarantee that a piece is retained.
The operator uses gamepad 2 A. Robot controls activate after Teleop Start.

Example tunables: `POWER = 0.3` and `MAX_RUN_SECONDS = 3.0`. The motor direction
must be recorded after testing. The subsystem has Stopped and Running states;
there is one owner of motor output and one elapsed timer started on entry to Running.
All events below are handled on the normal robot loop, except the direct Stop hook.

## Requirements and acceptance scenarios

These IDs belong to this example only. Assign your team's stable IDs when adopting
or changing the specification. Test names below are planned, not existing tests.

| ID | Required behavior | Acceptance scenario / planned test |
|---|---|---|
| EX-ROLLER-001 | Initialization resets the state and target to Stopped/zero and writes zero motor power. | Initialize from a previous Running state: state, target, and actual power are zero before initialization returns. `initializationStopsAndResets` |
| EX-ROLLER-002 | A new A press in active Teleop starts Running only if the sensor is clear and hardware is available. Entry resets the timer; periodic output uses the current POWER value. | Press with a clear sensor: Running; next periodic uses POWER. Change POWER while running: next output uses the new value. Press during init or Auto: no motor output. `pressStartsOnlyInTeleop` |
| EX-ROLLER-003 | Releasing A stops the roller and writes zero immediately in the release handler. | Release while Running: state/target/power become zero before the handler returns. Releasing while Stopped is harmless. `releaseStopsImmediately` |
| EX-ROLLER-004 | An occupied sensor rejects a start request. Detection while Running stops the roller during that periodic cycle. | Occupied before press: no start. Clear at press, occupied next loop: zero output that loop. `detectionPreventsOrEndsRun` |
| EX-ROLLER-005 | At elapsed time greater than or equal to MAX_RUN_SECONDS, periodic stops the roller. | Immediately before the limit it runs; exactly at and after the limit it is Stopped with zero power. `timeoutHasDefinedBoundary` |
| EX-ROLLER-006 | Detection or timeout does not restart the roller while A remains held. A release and a fresh press are required. | Detect, then clear sensor with A held: remains Stopped. Release and press with a clear sensor: starts a new timed run. `restartRequiresFreshPress` |
| EX-ROLLER-007 | Stop, cancellation of an active roller operation, and reinitialization stop output directly and reset Running state. Repeated Stop is harmless. | Stop while Running without another periodic cycle: zero actual power. Stop again: zero. Reinitialize: fresh state and no duplicated controls. `stopAndCancellationAreImmediate` |
| EX-ROLLER-008 | Missing motor or sensor prevents the subsystem from operating through inherited hardware-failure isolation. | Fail either hardware lookup: Roller disabled, failure reported, unrelated subsystems remain available; a start request cannot power the motor. `missingHardwareDisablesOwner` |
| EX-ROLLER-009 | Telemetry reports state and occupied status; historical events record entry into Running and its exit reason once per transition. Timeout is a warning. | Multiple unchanged periodic calls repeat the snapshot without adding repeated transition events. Detection, release, timeout, cancellation, and Stop have distinguishable exit reasons when ending a run. `diagnosticsDescribeTransitions` |

## Transition and priority rules

| Current state | Event | Result |
|---|---|---|
| Either | Stop or cancellation | Stopped, target/power zero immediately |
| Running | A release | Stopped, target/power zero immediately |
| Running | Sensor occupied | Stopped, zero this periodic cycle; reason Detection |
| Running | Elapsed time reaches limit | Stopped, zero this periodic cycle; reason Timeout |
| Stopped | Fresh A press, active Teleop, clear sensor | Running, timer reset; normal periodic applies POWER |
| Stopped | Held A, occupied sensor, or wrong mode | Remain Stopped |

If sensor detection and timeout occur in the same periodic cycle, Detection is the
recorded reason. Stop/cancellation and release take priority over a simultaneous
start; no request may restart the roller after OpMode Stop. State changes set the
target once; periodic writes normal output, and immediate-stop handlers write zero
directly. Commands that represent a sustained roller operation claim Roller;
their interruption path follows EX-ROLLER-007. No other subsystem writes its motor.

## Known limits and open hardware facts

This sensor contract has no debounce. A single observed occupied sample ends a
run; adding filtering would require a new timing decision and acceptance check.
A stuck-clear or disconnected digital input may look like an empty intake. The
example does not claim to detect that fault or a jam; the run timeout bounds the
powered attempt. Zero motor power does not guarantee zero physical movement.

Record mounting direction, measured sensor polarity, and the team's chosen power
and timeout before deployment. If those measurements invalidate the assumptions,
revise the specification and tests. No physical evidence is supplied here.

## Traceability and implementation readiness

| Requirement | Owner / intended code | Planned evidence |
|---|---|---|
| EX-ROLLER-001 through 009 | Roller in TeamCode; one corresponding RollerTests file | Scenarios above plus physical direction, detection, and stopping checks |

The example is ready to guide software implementation within its stated scope:
triggers, modes, outputs, timing boundaries, restart policy, priorities, and
failures are defined. It is not implemented or physically verified. Extending it
to autonomous requires a separate request/completion contract and arbitration
with operator controls. Whole-robot requirements must define its handoff to the
next mechanism; this example deliberately has no receiver.
