# Drive and Navigation

The pathing architecture separates reusable motion vocabulary from the coordinates,
tuning, and controls of one robot.

## Pedro Stack

`PedroComponent` owns a follower built by TeamCode `Constants.createFollower()`.
The reusable `DriveSubsystem` turns common follower operations into NextFTC commands.
The reusable `NavSubsystem` constructs robot-dimension-aware poses. Quickstart's
concrete `Drive` and `Nav` provide the basic season customization points.

## Localization

Pedro requires one localization source, but it does not require a dedicated
localizer device. Quickstart defaults to Pedro's drivetrain-encoder localizer so a
team can begin without GoBilda Pinpoint hardware. The drivetrain conversion
factors, dimensions, directions, and motor names are templates that still require
measurement and tuning.

Pedro's drivetrain and drive-encoder localizer constants default to motor names
`leftFront`, `leftRear`, `rightFront`, and `rightRear`. Teams using those exact
Robot Controller names can omit the motor-name configuration calls. Explicit names
are necessary only when the hardware configuration differs.

Teams with a dedicated device or odometry arrangement should replace the single
localizer constants type and `FollowerBuilder` call. Pedro 2.0.6 supports
Pinpoint, OTOS, two-wheel, three-wheel, and three-wheel-plus-IMU localizers. The
follower must configure exactly one of these options.

## Coordinates

Pedro uses its right-handed Cartesian field convention: +X is right on the standard
field drawing, +Y is up, heading zero points +X, and positive rotation is
counterclockwise. This differs from FTC SDK coordinate representations. Decode's
physical Driver Station placement makes the convention visually non-obvious, so
FTC/Limelight values must be deliberately converted and the drawing component must
use Pedro pose without an invented 90-degree rotation.

Alliance and side are sign-transform functions:

- `alliance(value)` mirrors values that change across red/blue;
- `side(value)` mirrors values that change across north/south.

The raw selected enum remains available as `Config.alliance` and `Config.side`.
Typed angle and distance arguments prevent radians/inches ambiguity.

## Units and Pose Alignment

The reusable helpers add:

- `number.tiles` as a 24-inch `Distance`;
- `number.pct` for path-completion fraction;
- `number.pctT` for Pedro curve parameter T;
- Axial/lateral pose transformations;
- Dimension-aware `pose()` with `FRONT/CENTER/BACK` and
  `LEFT/CENTER/RIGHT` alignment;
- Typed angles using NextFTC units.

`NavSubsystem.pose()` shifts the requested contact/alignment point by half the robot
length/width in the robot's local frame. This lets a season define “front of robot
at this field point” instead of repeating trigonometry.

## Paths Are Late-Bound

`DriveSubsystem.paths`, `to`, `curve`, and `curves` create deferred commands. The
start pose comes from `follower.pose` when execution begins. Decode helpers such as
`toSpike`, `toDeposit`, and `toParking` are delegated deferred properties for the
same reason.

Curved paths are the default for efficient travel. Straight line helpers remain
available where geometry demands them. Heading interpolation reaches the final
heading at the overridable `headingEnd` fraction.

## Typed `until`

Overloaded `until` methods communicate the meaning of otherwise ambiguous doubles:

```kotlin
Drive.until((-9).inches) // negative means distance remaining
Drive.until(50.pct)      // path completion
Drive.until(50.pctT)     // Bezier/path parameter T
```

Positive values measure progress from the beginning; negative values measure from
the end according to each overload. `untilNotBusy()` waits for the follower itself.

## Driver Control

`PedroDriverControlled` accepts live suppliers for forward, strafe, turn,
robot-centric mode, and heading offset. The included Drive maps left Y, left X,
and right X using the Pedro sign convention. It holds one reusable command instance
whose scalar changes between the included low/high power settings.

The command suppliers read NextFTC's lazy `gamepad1` ranges directly. Those ranges
resolve the current active OpMode and are sampled by NextBindings, so separate
`lateinit Range` aliases are unnecessary.

Robot-centric mode can change while the command is running because the supplier is
evaluated each update. A season may add an alliance-aware heading offset or drive
assist hooks without replacing the standard command.

## Starting Pose

`resetStartingPose(pose)` calls both Pedro's starting-pose API and current-pose API.
The former defines the localizer reference; the latter prevents the prior pose
offset from surviving a configuration change. A season should call it when a
starting-location setting changes during init. Auto should not reset pose again at
Start because teams may reposition the initialized robot and rely on Pedro tracking
that movement.

## Drawing

`PedroDrawingComponent` draws Pedro's robot pose and heading using the configured robot radius.
It is diagnostic only; it must never feed localization. Robot length and width live
with the Pedro constants because they configure both navigation alignment and field
representation.
