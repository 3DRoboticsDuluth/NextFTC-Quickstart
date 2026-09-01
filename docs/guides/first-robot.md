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

The base `Constants` intentionally contains 18-inch template dimensions and Pedro
defaults. Replace them in the upstream-recognizable Pedro shape:

```kotlin
object Constants {
    val robotLength = MEASURED_LENGTH.inches
    val robotWidth = MEASURED_WIDTH.inches
    val robotRadius = max(robotLength.inIn, robotWidth.inIn) / 2

    var followerConstants = FollowerConstants()
        .mass(MEASURED_MASS)
        // Add only values produced by the Pedro tuning process.

    var pathConstraints = PathConstraints(/* tuned values */)

    var driveConstants = MecanumConstants()
        .maxPower(LOW_BRINGUP_POWER)
        .leftFrontMotorName("<front-left>")
        .rightFrontMotorName("<front-right>")
        .leftRearMotorName("<back-left>")
        .rightRearMotorName("<back-right>")

    var localizerConstants = DriveEncoderConstants()
        .forwardTicksToInches(TUNED_FORWARD_TICKS_TO_INCHES)
        .strafeTicksToInches(TUNED_STRAFE_TICKS_TO_INCHES)
        .turnTicksToInches(TUNED_TURN_TICKS_TO_INCHES)
        .robotLength(robotLength.inIn)
        .robotWidth(robotWidth.inIn)
        .leftFrontMotorName("<front-left>")
        .rightFrontMotorName("<front-right>")
        .leftRearMotorName("<back-left>")
        .rightRearMotorName("<back-right>")

    fun createFollower(hardwareMap: HardwareMap): Follower =
        FollowerBuilder(followerConstants, hardwareMap)
            .pathConstraints(pathConstraints)
            .mecanumDrivetrain(driveConstants)
            .driveEncoderLocalizer(localizerConstants)
            .build()
}
```

The Quickstart uses Pedro's drivetrain-encoder localizer because it requires no
separate localization device. Its conversion factors, dimensions, motor names,
and encoder directions are still template values and must be configured or tuned.
Pedro defaults both the drivetrain and drive-encoder localizer motor names to
`leftFront`, `leftRear`, `rightFront`, and `rightRear`. If the Robot Controller
configuration uses those exact names, omit the corresponding motor-name calls
from both constants objects. Use explicit calls when the configured names differ.

A dedicated localizer is optional. If the robot has a GoBilda Pinpoint, replace
the localizer type and builder call while keeping Pedro's documented structure:

```kotlin
var localizerConstants = PinpointConstants()
    .hardwareMapName("<pinpoint>")
    .forwardPodY(MEASURED_FORWARD_POD_Y)
    .strafePodX(MEASURED_STRAFE_POD_X)

fun createFollower(hardwareMap: HardwareMap): Follower =
    FollowerBuilder(followerConstants, hardwareMap)
        .pathConstraints(pathConstraints)
        .mecanumDrivetrain(driveConstants)
        .pinpointLocalizer(localizerConstants)
        .build()
```

Pedro also supports OTOS, two-wheel, three-wheel, and three-wheel-plus-IMU
localizers. Select exactly one localizer builder method and follow the matching
Pedro tuning instructions.

Do not copy Osiris tuning into a different robot. Keep this integration surface
close to current Pedro documentation so the tuning output can be transferred
without translating through another abstraction.

Add constants tests that assert every measured name/value and that a follower can
be built with mocked hardware.

## 4. Customize the Included Nav Subsystem

Quickstart already includes `subsystems/Nav.kt` as the season-specific
specialization point:

```kotlin
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.Constants.robotLength
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.Constants.robotWidth

object Nav : NavSubsystem(robotLength, robotWidth) {
    val start = pose(0.inches, 0.inches, 0.deg)
    val test = pose(24.inches, 0.inches, 0.deg)
}
```

Before adding game poses, document the field axis, heading-zero direction, and
Driver Station viewpoint. Test `start`, `test`, and at least one dimension-aware
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
        { -gamepad1.leftStickY.get() },
        { -gamepad1.leftStickX.get() },
        { -gamepad1.rightStickX.get() },
        { Config.config.robotCentric }
    ).apply { requires(this@Drive) }

    val low by instant { driverControlled.scalar = POWER_LOW }
    val high by instant { driverControlled.scalar = POWER_HIGH }

    override val defaultCommand
        get() = if (state.teleop) driverControlled else super.defaultCommand

    override fun initialize() { driverControlled.scalar = POWER_HIGH }

    override fun controls() {
        gamepad1.dpadDown whenBecomesTrue low
        gamepad1.dpadUp whenBecomesTrue high
    }

    override fun periodic() {
        tel.info("Power", "%.2f".format(driverControlled.scalar))
        tel.debug("Pose", "%.1f, %.1f, %.1f°".format(
            follower.pose.x, follower.pose.y, Math.toDegrees(follower.pose.heading)
        ))
    }
}
```

The signs shown are the Osiris/Pedro precedent, not a universal promise. Verify
them on the new chassis. The default command is Teleop-only so Auto cannot accept
manual drive input. `SubsystemComponent` discovers the singleton; do not add a
manual subsystem list.

`Gamepads.gamepad1` and its ranges are lazy NextFTC wrappers around the current
active OpMode. Referencing the ranges inside the command suppliers keeps the input
live without separate `lateinit` aliases. The command is scheduled only after the
OpMode exists, so a null-to-zero fallback would hide a lifecycle error rather than
solve one.

Tests must prove the input mapping, speed commands, Teleop-only default command,
requirements, telemetry, and lifecycle stop.

## 6. Extend Configuration Only When Needed

The included `Config.Config` already provides a live `robotCentric` setting:

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

The base already provides `OpMode`, `Teleop`, and `Auto`. `OpMode` composes
telemetry, bindings, bulk reads, Pedro, drawing, config, and discovered subsystems.
Do not duplicate those components in each OpMode.

Teleop normally remains empty:

```kotlin
@TeleOp
class Teleop : OpMode()
```

Subsystem controls are registered once and activate after Teleop Start. The config
menu remains available during init. Auto must explicitly schedule its entry command
from `onStartButtonPressed()` when an autonomous routine exists.

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
