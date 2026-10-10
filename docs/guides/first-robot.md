# Build the First Robot From the Seasonal Base

This walkthrough starts at Quickstart `main` and ends with a small, tested,
deployable robot: configured Pedro construction, the included Drive/Nav pair, one servo
mechanism, Teleop, diagnostics, and a safe physical smoke test. It is the shortest
complete example of how a season should begin.

## Definition of Done

The first-robot milestone is complete when:

- The neutral base still passes before changes.
- The Control Hub configuration matches the documented hardware worksheet.
- Pedro constructs from measured and tuned robot values.
- Left-stick Y/X and right-stick X drive forward/strafe/turn correctly.
- Robot-centric and field-centric behavior are deliberately verified.
- One mechanism responds only after Teleop Start and stops safely.
- The follower pose agrees with Panels field drawing.
- Every owned line and branch remains covered.
- The full verification command and `assembleDebug` pass.

## 1. Branch and Prove the Baseline

```powershell
git config user.name 3drdProgramming
git config user.email programming@3droboticsduluth.com
./gradlew.bat :3drdNextFTC:check :3drdNextFTC:unitTestCoverage :TeamCode:check :TeamCode:unitTestCoverage :TeamCode:assembleDebug
```

Commit no robot code until this passes. A failure here belongs to the base,
toolchain, or local environment—not the new robot.

## 2. Inventory Hardware Before Coding

Complete the [hardware worksheet](hardware-worksheet.md). Create the Robot
Controller configuration with exactly those names. Photograph or export the
configuration if practical, and add its review date to the season documentation.

Keep hardware names in TeamCode. A name such as `arm` is a robot fact and must not
be added to `3drdNextFTC`.

## 3. Configure Pedro Constants

The template keeps 18-inch placeholder dimensions, explicit motor names/directions,
and a hardware-light drive-encoder localizer. Replace them with measured robot
values in `adaptations/pedropathing/Constants.kt`.

Pedro 3 constructs the follower from three parts:

```kotlin
fun createFollower(hardwareMap: HardwareMap): Follower {
    val drivetrain = MecanumDrive(hardwareMap, driveConfig)
    return Follower(DriveEncoderLocalizer(hardwareMap, localizerConfig), drivetrain, Foresight(foresightConfig))
}
```

Use native `MecanumConfig` for names/directions and native `ForesightConfig` for the
tuning output. The template's zero controller gains and placeholder velocity/brake
inputs are not autonomous calibration. See [Pedro 3 review](../reference/pedro3-review.md)
for the AutoTune procedure setup still needed in a team tuning workflow.

For drive encoders, fill `DriveEncoderConfig` with the configured motor names,
encoder signs, conversion factors, and measured width/length. Factors multiply
four-wheel sums, not averages; confirm forward, strafe, and rotation independently.
Construct the drivetrain before sampling encoders so motor directions are known.

For dedicated odometry, replace `DriveEncoderLocalizer(...)` with Pedro 3's
Pinpoint, OTOS, OctoQuad, two-wheel, three-wheel, or three-wheel-plus-IMU localizer
and its matching native configuration. Keep exactly one localization source.

Keep hardware names and tuning in TeamCode, preserve Pedro's documented shapes,
and test measured values and mocked follower construction. Do not reuse Osiris
calibration for a different robot.

For example, dedicated Pinpoint localization uses `PinpointConfig` and
`PinpointLocalizer` from `com.pedropathing.revhub.localizers`:

```kotlin
return Follower(PinpointLocalizer(hardwareMap, pinpointConfig), drivetrain, Foresight(foresightConfig))
```

Populate the native configuration from measured offsets, encoder resolution,
directions, and the configured device name using the upstream tuning guide.

## 4. Customize the Included Nav Subsystem

Quickstart already includes `subsystems/Nav.kt` as the season-specific
specialization point:

```kotlin
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.Constants.robotLength
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.Constants.robotWidth

object Nav : NavSubsystem(robotLength, robotWidth) {
    val start = pose(0.inches, 0.inches, 0.deg)
    val end = pose(24.inches, 0.inches, 0.deg)
}
```

Before adding game poses, document the field axis, heading-zero direction, and
Driver Station viewpoint. Test `start`, `end`, and at least one dimension-aware
`FRONT`/`LEFT` pose. Use typed distances and angles in every public navigation API.

## 5. Customize the Included Drive Subsystem

Quickstart already includes `subsystems/Drive.kt`. Retain its one long-lived driver
command and live suppliers so mode changes do not rebuild it; adjust its powers,
controls, telemetry, and assists for the robot:

```kotlin
@Configurable
object Drive : DriveSubsystem() {
    var POWER_LOW = 0.35
    var POWER_HIGH = 0.70

    val driverControlled = PedroDriverControlled(
        gamepad1.leftStickY.negate(),
        gamepad1.leftStickX.negate(),
        gamepad1.rightStickX.negate(),
        { Config.robotCentric }
    ).apply { requires(this@Drive) }

    val low by instant { driverControlled.scalar = POWER_LOW }
    val high by instant { driverControlled.scalar = POWER_HIGH }

    override val defaultCommand
        get() = if (state.teleop) driverControlled else super.defaultCommand

    override fun initialize() {
        driverControlled.scalar = POWER_HIGH
    }

    override fun controls() {
        val driving = !gamepad1.back
        (driving and gamepad1.dpadDown) whenBecomesTrue low
        (driving and gamepad1.dpadUp) whenBecomesTrue high
    }

    override fun periodic() {
        tel.info("Power", "%.2f".format(driverControlled.scalar))
        tel.debug("X", "%.1f".format(follower.pose.x))
        tel.debug("Y", "%.1f".format(follower.pose.y))
        tel.debug("Heading (deg)", "%.1f".format(Math.toDegrees(follower.pose.heading)))
    }
}
```

The signs shown follow the Pedro input convention, not a universal chassis promise.
Verify them on the new robot. The default command is Teleop-only so Auto cannot accept
manual drive input. `SubsystemComponent` discovers the singleton; do not add a
manual subsystem list.

`Gamepads.gamepad1` exposes NextFTC ranges that must be registered before
`BindingManager.update()` samples the loop's inputs. Constructing and retaining the
three ranges while `Drive` is initialized ensures the first command execution reads
an already-sampled value. Creating a range for the first time inside an input lambda
would register it after that loop's binding update and make its first `get()` fail.
The `negate()` transformation preserves the range and remains compatible with the
binding lifecycle; a null-to-zero fallback would not address the sampling problem.

Holding Back reserves the D-pad for configuration rather than changing drive power.
The robot-centric supplier remains a lambda because that setting may change while
the long-lived driver command is running.

Tests must prove the input mapping, speed commands, Teleop-only default command,
requirements, telemetry, and lifecycle stop.

## 6. Extend Configuration Only When Needed

The included `Config` object already provides a live `robotCentric` setting:

```kotlin
@Setting(live = true)
var robotCentric: Boolean = true
```

Settings appear in declaration order. Put competition setup choices first,
diagnostic `level` near the end, and keep `filter` transient. Do not add a setting
merely because Panels can expose a subsystem tuning value.

## 7. Add One Mechanism End to End

Use a harmless servo mechanism as the first complete vertical slice:

```kotlin
@Configurable
object Arm : Subsystem() {
    var MIN = 0.10
    var MAX = 0.90
    var DOWN = 0.0
    var UP = 1.0
    var POS = DOWN

    val servo = ServoEx("arm") { scaleRange(MIN, MAX) }

    val up by instant { POS = UP }
    val down by instant { POS = DOWN }

    override fun initialize() { POS = DOWN }

    override fun controls() {
        gamepad2.dpadUp whenBecomesTrue up
        gamepad2.dpadDown whenBecomesTrue down
    }

    override fun periodic() {
        servo.update { position = POS }
    }
}
```

Use `update` to mutate and report hardware in the same cycle. If the mechanism is
powered, implement an immediate, idempotent `stop()` that writes its safe output;
never assume another `periodic()` will run after stop. Add `ArmTests` in the same
package and cover configuration, commands, controls, initialization, periodic
output, hardware failure isolation, and stop behavior.

## 8. Use the Existing OpMode Scaffold

The base already provides `OpMode`, `Teleop`, and an intentionally small Auto
example. `OpMode` composes
telemetry, bindings, bulk reads, Pedro, drawing, config, and discovered subsystems.
Do not duplicate those components in each OpMode.

Teleop normally remains empty:

```kotlin
@TeleOp
class Teleop : OpMode()
```

Subsystem controls are registered once and activate after Teleop Start. The config
menu remains available during init. Quickstart's Auto subsystem establishes
`Nav.start` during autonomous initialization and exposes a named, deferred command:

```kotlin
object Auto : Subsystem() {
    val execute = Drive.to(Nav.end)

    override fun initialize() {
        if (ActiveOpMode.isAutonomous) follower.resetStartingPose(Nav.start)
    }
}
```

The Auto OpMode schedules that entry command only after Start:

```kotlin
@Autonomous
class Auto : OpMode() {
    override fun onStartButtonPressed() {
        execute.schedule()
    }
}
```

`Drive.to()` is already deferred and owns Drive, so another deferred wrapper and
duplicate requirement declaration would add no behavior. It reads the live follower
pose when execution begins. Replace `Nav.start`, `Nav.end`, and the command
composition with the new season's coordinates and routine rather than treating the
24-inch sample as tuned autonomous behavior.

## 9. Desktop and Robot Gates

Run the full verification command. Then deploy with drive wheels lifted and the
servo linkage disconnected or safely constrained.

Validate in this order:

1. Initialize and stop repeatedly; bindings must not duplicate.
2. Confirm controls do nothing before Teleop Start except the config menu.
3. Start and test forward, strafe, and turn at low power.
4. Move the robot by hand and compare the selected localizer pose with Panels drawing.
5. Test robot-centric and field-centric behavior deliberately.
6. Test each servo endpoint and direction.
7. Stop during motion and verify immediate safe output.
8. Reconnect mechanical loads and repeat at controlled power.

Record outcomes in the hardware worksheet. Commit in dependency order: constants
and coordinates, Drive/Nav/config, first mechanism, then field-proven corrections.

## Common Failures

| Symptom | Likely contract violation |
|---|---|
| Forward stick strafes | Forward/strafe suppliers are swapped or drivetrain/localizer axes are wrong. |
| Field-centric does nothing | Mode/heading suppliers were captured once instead of evaluated live. |
| Robot drawing is rotated | An FTC/Pedro coordinate conversion or invented drawing offset is present. |
| Auto responds to sticks | Driver command is not gated to Teleop or incorrectly scheduled. |
| Menu skips entries after re-init | Bindings were accumulated instead of cleared per lifecycle. |
| Mechanism remains powered after stop | `stop()` updates only target state and does not write hardware immediately. |
| Panels values exist but Driver Station menu does not | `@Configurable` and `@Setting` are different surfaces. |
