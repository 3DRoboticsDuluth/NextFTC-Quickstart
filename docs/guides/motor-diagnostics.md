# Diagnose One Motor

Use the `MotorDiagnostics` Teleop to isolate one configured motor from Pedro and
the normal robot subsystems. Edit the single hardware name in `subsystems/Motor.kt`,
deploy the full APK, raise the driven mechanism safely, and begin with low power.

The OpMode manually registers only `Config` and `Motor`. The `Motor` subsystem is
marked `@ManuallyRegistered`, so it cannot activate in normal Teleop or Auto.

## Controls

| Button | Run mode | Power |
|---|---|---|
| A | `RUN_USING_ENCODER` | Positive `POWER` |
| B | `RUN_USING_ENCODER` | Negative `POWER` |
| X | `RUN_WITHOUT_ENCODER` | Positive `POWER` |
| Y | `RUN_WITHOUT_ENCODER` | Negative `POWER` |
| Left bumper | Unchanged | Zero |

The controls change power sign, not motor `Direction`. Each selection first writes
zero power, changes run mode, and then applies the selected power. Stopping the
OpMode also writes zero power directly.

Set the diagnostic level to `VERBOSE` to compare commanded power, encoder position,
velocity, current, run mode, direction, velocity percentage, and RPM. A motor that
runs steadily without encoder regulation but oscillates with it usually indicates
an encoder signal, polarity, configured motor type, cable, or controller-loop issue.

Always power off before changing encoder wiring. Verify power, ground, channel A,
and channel B against the motor and Hub documentation rather than relying only on
connector fit or wire color.
