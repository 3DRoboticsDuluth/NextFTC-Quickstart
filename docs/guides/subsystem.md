# Add a subsystem

This is the established student-facing pattern. Start with the smallest complete
subsystem and add abstraction only when behavior requires it.

## 1. Declare a singleton

```kotlin
@Configurable
object Example : Subsystem() {
    var MIN = 0.0
    var MAX = 1.0
    var POS = 0.5

    val servo = ServoEx("example") { scaleRange(MIN, MAX) }

    val activate by instant { POS = 1.0 }
    val reset by instant { POS = 0.0 }

    override fun controls() {
        val selected = !gamepad2.start and gamepad2.a
        (selected and gamepad2.dpadUp) whenBecomesTrue activate
        (selected and gamepad2.dpadDown) whenBecomesTrue reset
    }

    override fun periodic() {
        servo.update { position = POS }
    }
}
```

Discovery means no central registration edit is necessary.

## 2. Assign lifecycle responsibilities

- Declare wrapper fields near tunables.
- Use `initialize()` to reset internal state after hardware is ready.
- Use `controls()` for robot-motion bindings that should activate only after
  Teleop Start.
- Use `periodic()` to read sensors, calculate current state, write normal outputs,
  and report telemetry.
- Use `start()` for state that changes exactly when Start is pressed.
- Use `stop()` to write direct safe outputs that cannot wait for another loop.

The config menu is the important exception: its init-time bindings belong in the
reusable Config subsystem because setup must work before Start and in Auto.

## 3. Choose command shape

- `val open by instant { ... }` for a stable reusable no-argument command.
- `fun set(value: Double) = instant { ... }` for a command factory with an input.
- `val path by deferred { ... }` when child behavior needs live execution state.
- A regular method for a calculation/query that is not itself a command.

Avoid putting the word `Command` in property names; the type and usage already
communicate that detail.

## 4. Report diagnostics

- Use `tel.info` for useful default current state.
- Use `tel.debug` for developer state.
- Use `tel.verbose` for high-volume or hardware detail.
- Use `log` for transitions, decisions, warnings, and failures—not repeated state.
- Use `hardware.update { ... }` so TeamCode hardware telemetry follows writes.

## 5. Handle failure and stop

Do not catch hardware lookup exceptions in the subsystem. The base lifecycle records
the device name, disables only the owner, and keeps the rest of the robot available
for diagnosis. Commands delegated from the disabled subsystem do not execute.

For a motor subsystem, `stop()` should normally update its target and set motor
power/velocity directly. A servo may intentionally remain at its last position and
need no stop hook.

## 6. Add one dedicated test file

Place `ExampleTests.kt` in the matching TeamCode subsystem test package. Extend the
shared subsystem test harness when hardware-map behavior is needed. Cover:

- initial/reset state;
- hardware name and configuration;
- each command;
- each binding edge/chord;
- periodic calculations, output, and telemetry;
- boundary/error branches;
- direct stop output when applicable.

Run TeamCode tests/coverage, then the complete repository verification command.
Finally test physical direction and safety on the robot.
