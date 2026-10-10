# Choose drivetrain and localization

Choose the chassis first, then its controls and localization hardware. Tank is a
differential chassis; arcade control combines forward and turn inputs. Tank
control assigns a stick to each side. This guide uses arcade control.

| Chassis | Controls | Localization | Route |
|---|---|---|---|
| Mecanum | Forward, strafe, turn | Drive encoders | Default first-robot walkthrough |
| Mecanum | Forward, strafe, turn | Dedicated odometry | Keep drivetrain, replace localizer |
| Two-motor tank | Forward and turn, robot-centric | Drive encoders | Paired-slot recipe below |
| Two-motor tank | Forward and turn, robot-centric | Dedicated odometry | Same drive recipe, replace localizer; validate separately |

## Mecanum

Keep `MecanumConstants` and `.mecanumDrivetrain(driveConstants)` in TeamCode's
Pedro `Constants`. Configure names, motor/encoder directions, measured dimensions,
and conversion factors. The [first-robot guide](first-robot.md) supplies the complete
example. Verify heading and driver perspective before enabling field-centric control.

## Two-motor tank with arcade control

Pedro FTC 2.1.2 has no native tank drivetrain. The earlier arcade branch assigns
each physical motor to both mecanum slots on its side. This provides arcade
Teleop; general Pedro path following is not established for this chassis.

In TeamCode's `Constants`, use the same paired names in both objects:

```kotlin
var driveConstants = MecanumConstants()
    .leftFrontMotorName("leftMotor")
    .leftRearMotorName("leftMotor")
    .rightFrontMotorName("rightMotor")
    .rightRearMotorName("rightMotor")

var localizerConstants = DriveEncoderConstants()
    .leftFrontMotorName("leftMotor")
    .leftRearMotorName("leftMotor")
    .rightFrontMotorName("rightMotor")
    .rightRearMotorName("rightMotor")
    .forwardTicksToInches(MEASURED_FORWARD_FACTOR)
    .strafeTicksToInches(0.0)
    .turnTicksToInches(MEASURED_TURN_FACTOR)
    .robotWidth(MEASURED_LOCALIZER_WIDTH)
    .robotLength(MEASURED_LOCALIZER_LENGTH)
```

Capitalized values are measurement placeholders. Set motor and encoder directions
for the actual mounting, with matching directions for both slots on each side.
Retain `.mecanumDrivetrain(driveConstants)` and
`.driveEncoderLocalizer(localizerConstants)` in the follower builder.

Replace the included Drive's driver command with:

```kotlin
val driverControlled = PedroDriverControlled(
    gamepad1.leftStickY.negate(),
    { 0.0 },
    gamepad1.rightStickX.negate(),
    { true }
).apply { requires(this@Drive) }
```

Always use robot-centric input and zero strafe. Field-centric rotation can produce
lateral demand from forward input. The repeated same-side slots would then receive
different powers and the last write would win. The supplier above deliberately
ignores Config's centric-mode setting; remove that unused menu setting when
specializing a tank robot. Keep the Teleop-only default command and Stop lifecycle.

Pedro exposes four motor slots but two physical motors. TeamCode's collection
telemetry deduplicates the references so each motor is reported once.

### Calibrate drive encoders

Pedro multiplies ticks by the configured factors. The forward factor applies to
the four-slot sum, not a single wheel. Let `L` and `R` be direction-adjusted
encoder changes for the physical left and right motor:

```text
forward inches = forwardFactor * 2 * (L + R)
lateral inches = 0
heading radians = turnFactor * 2 * (R - L) / (width + length)
```

For measured straight travel `D`, set `forwardFactor = D / (2 * (L + R))`.
If both motors move `N` ticks, this is `D / (4 * N)`, a quarter of one wheel's
inches-per-tick value. Do not substitute ticks per inch.

For an in-place turn of measured `theta` radians, use
`turnFactor = theta * (width + length) / (2 * (R - L))`.
Use nonzero measured deltas, consistent signed heading, and positive geometry.
Despite its method name, `turnTicksToInches` feeds the heading calculation with
the geometry divisor above. Verify both turn directions and repeat measurements.

Matching same-side encoder expressions cancel lateral motion. Explicit zero
strafe conversion records that lateral motion is not estimated. Drive encoders
cannot detect sideways skid and wheel slip.

### Validation and limits

The coach reports the earlier setup was drivable after disabling strafe and
setting conversion factors. `TankDriveSetupTests` checks paired motor lookup,
same-side powers without lateral demand, forward/heading calculations, and zero
lateral estimates against Pedro 2.1.2. `PedroDriverControlledTests` checks the
robot-centric arcade inputs. These are desktop checks, not a new physical test.

Verify directions and Stop at low power with wheels lifted, then measure straight
distance and heading on the floor. Disable the neutral Auto path in a tank
specialization until a suitable differential autonomous routine is implemented
and physically verified. Arbitrary curves, strafing, and field-centric driving
are not promised by this adaptation.

## Dedicated odometry, with either chassis

A localizer supplies pose; it does not change the chassis's available motions.
For Pinpoint with two pods, replace `DriveEncoderConstants` with `PinpointConstants`
and select `.pinpointLocalizer(localizerConstants)` exactly once. Keep the
drivetrain and tank zero-strafe controls where applicable. The
[first-robot guide](first-robot.md) shows the Pinpoint builder recipe.

Measure offsets, select the correct resolution and encoder directions, and verify
axes and heading by moving the robot by hand. Follow the device's tuning process.
Two-wheel, three-wheel, three-wheel-plus-IMU, and OTOS options also need matching
hardware and configuration. No pods still means using a drive-encoder localizer.

Dedicated odometry on this tank adaptation has not been physically verified in
this upgrade. Record hardware and evidence in [Our robot](../robot/validation.md).

## Sources and traceability

- [Earlier arcade specialization](https://github.com/3DRoboticsDuluth/NextFTC-Quickstart/tree/codex/arcade-drivetrain)
- [Pedro 2.1.2 localizer calculations](https://github.com/Pedro-Pathing/PedroPathing/blob/v2.1.2/ftc/src/main/java/com/pedropathing/ftc/localization/localizers/DriveEncoderLocalizer.java)
- [Pedro 2.1.2 mecanum implementation](https://github.com/Pedro-Pathing/PedroPathing/blob/v2.1.2/ftc/src/main/java/com/pedropathing/ftc/drivetrains/Mecanum.java)

REQ-SCF-011 maps to this guide and the drivetrain tests; REQ-PLT-043 maps to
deduplicated motor telemetry and `TelemetryTests`. Physical readiness remains a
team validation result.
