# Drive and Navigation

The pathing architecture separates reusable motion vocabulary from the coordinates,
tuning, and controls of one robot.

## Pedro Stack

The local NextFTC v1 `PedroComponent` owns a follower built by TeamCode `Constants.createFollower()`.
The reusable `DriveSubsystem` turns common follower operations into NextFTC commands.
The reusable `NavSubsystem` constructs robot-dimension-aware poses. Quickstart's
concrete `Drive` and `Nav` provide the basic season customization points.

## Localization

Quickstart keeps four-drive-encoder localization without requiring an additional
sensor. Pedro 3.0.1 no longer supplies that localizer, so the reusable module supplies
`DriveEncoderLocalizer` with robot-supplied `DriveEncoderConfig`. Conversion factors
multiply the **sum of four encoders**, as in Pedro 2, not their average. The turn
factor multiplies the signed wheel sum divided by robot width plus length. Physical
inches-per-tick cannot be substituted without accounting for that convention.

`Constants.localizerConfig` supplies names, encoder directions, dimensions, and
conversion factors. `Constants.driveConfig` uses native `MecanumConfig` with explicit
motor names/directions. Construct the drivetrain first so encoder sampling observes
its configured motor directions. Pose resets establish a new encoder reference and
zero velocity without relying on a subsequent update.

Teams may substitute Pedro 3's Pinpoint, OTOS, OctoQuad, two-wheel, three-wheel,
or three-wheel-plus-IMU localizer. Exactly one localizer feeds the follower.

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

The command retains NextFTC's `gamepad1` ranges directly. Constructing those ranges
before the first binding update registers them in time for NextBindings to sample
the loop's input before the default command reads it. Do not first create the ranges
inside the command suppliers: on the first loop that would happen after the binding
update, and `get()` would correctly report that the new variable has not yet been
sampled. Range transformations such as `negate()` keep the declaration concise
without introducing `lateinit` aliases or hiding lifecycle errors with fallback
values.

Robot-centric mode can change while the command is running because the supplier is
evaluated each update. A season may add an alliance-aware heading offset or drive
assist hooks without replacing the standard command.

## Starting Pose

`resetStartingPose(pose)` calls Pedro 3's `setPose(pose)`, which establishes the
localizer reference and exact pose in one operation. A season should call it when a
starting-location setting changes during init. Auto should not reset pose again at
Start because teams may reposition the initialized robot and rely on Pedro tracking
that movement.

## Drawing

`PedroDrawingComponent` draws Pedro's robot pose and heading using the configured robot radius.
It is diagnostic only; it must never feed localization. Robot length and width live
with the Pedro constants because they configure both navigation alignment and field
representation.

## Pedro 3 APIs and Safety

`paths { ... }` now returns a native Pedro `Path`; combine segments with
`Paths.path(...)`. `PathBuilder` and `PathChain` no longer exist. The old
`follow(..., maxPower)` argument is removed: native speed constraints are not a
motor-power cap. Pose imports change to `com.pedropathing.math.Pose`; its accessors
are `x()`, `y()`, and `heading()`, with headings normalized by Pedro.

`FollowPath` waits until the follower leaves FOLLOW mode, preserves endpoint
holding on normal completion, restores the prior `holdEnd` option, and stops motors
on cancellation. Whole-path distances/completion use segment lengths resolved once
at execution. T refers to the current segment. Turns use successive short hold
targets to preserve direction across heading wrap and multiple revolutions, with
`turnTolerance` defining completion. Heading sampling must occur more frequently
than one half-revolution.

`stopNow()` calls both `Follower.stop()` and `Drivetrain.stop()`: the former only
changes mode until a later update. OpMode cleanup also releases the follower even
if actuator cleanup throws. Drawing samples native paths and retains at most 100
recent poses, resetting its history on initialization.

See [Pedro 3 review](../reference/pedro3-review.md) for tuning and validation limits.
