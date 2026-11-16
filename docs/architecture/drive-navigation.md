# Drive and Navigation

The pathing architecture separates reusable motion vocabulary from the coordinates,
tuning, and controls of one robot.

## Pedro Stack

`PedroComponent` owns a follower built by TeamCode `Constants.createFollower()`.
The reusable `DriveSubsystem` turns common follower operations into NextFTC commands.
The reusable `NavSubsystem` constructs robot-dimension-aware poses. Quickstart's
concrete `Drive` and `Nav` provide the basic season customization points.

## Coordinate vocabulary and migration

Use axial and lateral consistently for signed robot-relative movement and offsets:

| Name | Meaning | Positive | Negative |
|---|---|---|---|
| `Drive.axial(distance)` / `Pose.axial(distance)` | Movement along the robot's forward axis | Forward | Backward |
| `Drive.lateral(distance)` / `Pose.lateral(distance)` | Movement along its left axis | Left | Right |
| `ao` | Axial offset for a destination pose | Forward | Backward |
| `lo` | Lateral offset for a destination pose | Left | Right |
| Pose `x`, `y` | Field coordinates | Along the corresponding field axis | Opposite that axis |

Drive commands use the follower pose at execution time, keep its heading and
retain Drive ownership. Typed distances are preferred; Double drive distances
remain inches. `Nav.pose` rotates ao/lo using the destination heading, not the
current robot heading. `axial` (FRONT/CENTER/BACK) and `lateral` (LEFT/CENTER/RIGHT)
still choose robot reference points; they are not offset distances.

```kotlin
Drive.axial((-6).inches)
Drive.lateral(3.inches)
Nav.pose(24.inches, 48.inches, 90.deg, ao = 6.inches, lo = (-3).inches)
```

Migration: rename drive `forward` to `axial` and `strafe` to `lateral`. The old
methods remain deprecated aliases with unchanged signs and requirements; their
command names now use the canonical axis vocabulary. Rename Kotlin named
arguments `axialOffset` to `ao` and `lateralOffset` to `lo` for both heading
overloads of Nav.pose. Those old parameter names are a source compatibility
break; positional calls and coordinate calculations remain unchanged. No field
xo/yo offsets or additional directional helpers are introduced by this rename.

## Optional driver-command shaping

`PedroDriverControlled` accepts an optional `slew: DriverSlewParameters` argument.
Omitting it preserves the original transfer and stop behavior. The neutral
Quickstart Drive remains unshaped; a robot opts in with its own visible tuning:

```kotlin
val DRIVER_SLEW = DriverSlewParameters(
    translation = SCurveParameters(maxAcceleration = 4.0, maxJerk = 40.0),
    turn = SCurveParameters(maxAcceleration = 8.0, maxJerk = 80.0),
)

val driverControlled = PedroDriverControlled(
    gamepad1.leftStickY.negate(),
    gamepad1.leftStickX.negate(),
    gamepad1.rightStickX.negate(),
    { Config.robotCentric },
    slew = DRIVER_SLEW,
)
```

Import `DriverSlewParameters` from `com.threedrd.pedropathing`
and `SCurveParameters` from the library's `nextftc.control.interpolators` package.
Keep `DRIVER_SLEW` on the owning `@Configurable` subsystem: Panels discovers both
mutable nested tuning groups. Parameters may be shared; command state must not be.
Constructor defaults above are examples, not measured robot tuning.

The pipeline is:

```text
Joystick → selected scalar → manual command shaping → drive/strafe/turn callbacks
         → Pedro setTeleOpDrive → Pedro normalization and motor mixing
```

The callbacks remain the existing assist boundary. Chase/goal-lock corrections
replace shaped manual inputs exactly where they replaced raw inputs before;
their outputs are not smoothed or capped by this integration. Do not opt standalone
autonomous/chase-only commands into shaping unless a separate behavior change is
intended. Path following and NextControl position/heading assists remain unchanged.

Translation uses one coordinated two-dimensional tracker. Its slew vector has
bounded Euclidean magnitude, and changes in that vector have a shared jerk budget;
diagonals do not receive one full budget per axis. A critically damped target tracker
requests jerk from command error and current slew. Vector projection limits jerk,
slew magnitude, and the change in slew. It preserves both vector states on retargeting
and live edits; a lowered slew ceiling is approached gradually at the jerk limit.
The response converges to held input rather than using a fixed-duration profile.
Turning uses a separate scalar `SCurve`, sharing only its own tuning group.

`maxAcceleration` means normalized command change/second, and `maxJerk` means
change in that slew/second. They do not specify chassis acceleration, torque, or
current. The limits describe the manual reference before assists and Pedro's
existing saturation/mixing, not final wheel powers. No new input normalization,
wheel-current controllers, or electrical cap are introduced. Shaping may soften
load peaks; it cannot guarantee a current ceiling.

Translation integrates in small bounded steps (at most 5 ms, smaller for high
tracking frequency). Tracking frequency is derived from jerk/slew limits, capped
at 100/s for numerical stability. Both translation and turn process at most 100 ms
per call, deliberately discarding older elapsed time after a stalled loop instead
of catching up with a full-demand jump. Production timing is internal. Tests use
fake clocks outside ordinary subsystem declarations.

Lifecycle and operator behavior:

- Start/restart clears command and slew history; the first update starts at rest.
- Stick release and scalar reductions smoothly approach the new target. A selected
  scalar is a target multiplier, not an immediate ceiling during ramp-down.
- Reversal preserves command and slew state. Temporary continuation in the previous
  direction is possible while removing slew; there is no instantaneous sign flip.
- Positive finite live parameter edits preserve state. Invalid inputs/settings are
  rejected before elapsed time advances. Reset does not read settings.
- Changing robot-/field-centric mode or heading offset rotates stored translation
  **and slew** into the new frame. Pedro's convention is field vector = input
  rotated by `(robotCentric ? currentHeading : 0) + headingOffset`. The conversion
  preserves the existing manual vector's field direction; subsequent inputs are
  followed in the newly selected frame. Turn state is unchanged. Normal robot
  rotation does not trigger this conversion while the selected frame is unchanged.
- Ending an opted-in command, whether interrupted or completed, immediately resets
  shaping and calls `breakFollowing()`. Lifecycle/emergency stops continue to stop
  the follower directly; they never wait for stick-release deceleration. A subsequent
  command start resets state again, so no demand is replayed after idle time.

`DriverCommandSlewTests` verifies rotational symmetry, vector slew/jerk bounds,
reversal/release, independent turn, live tuning, frame-state rotation, reset, timing
caps, and validation. `PedroDriverControlledTests` verifies scaling before shaping,
unshaped assist outputs, legacy compatibility, centric transitions, and immediate
stop/restart against the actual command integration. These are deterministic
desktop contracts; they do not measure battery sag or establish hardware handling.

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
localizer constants type and `FollowerBuilder` call. Pedro 2.1.2 supports
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

`DriveSubsystem.paths`, `to`, `curve`, and `through` create deferred commands. The
start pose comes from `follower.pose` when execution begins. Decode helpers such as
`toSpike`, `toDeposit`, and `toParking` are delegated deferred properties for the
same reason.

Curved paths are the default for efficient travel. Straight line helpers remain
available where geometry demands them. Heading interpolation reaches the final
heading at the overridable `headingEnd` fraction.

### Curve control points and through waypoints

`Drive.curve(vararg poses, holdEnd = true)` uses the supplied intermediate poses
as Bezier control points. `Drive.through(vararg poses, holdEnd = true)` visits
each supplied X/Y waypoint using a chain of cubic Bezier segments. Both acquire
the start pose when the command begins and reserve Drive throughout the command.
Through accepts one or more destinations and holds only at the final endpoint
when requested. Passing an intermediate waypoint does not add a stop command.

```kotlin
Drive.through(Nav.pose(1.tiles, 2.tiles), Nav.pose(3.tiles, 1.tiles, 90.deg))
```

Through uses one global start-to-final heading interpolation across the chain,
reaching the final heading at headingEnd of total chain distance. Intermediate
pose headings do not define heading constraints. The global interpolation avoids
restarting heading interpolation on every segment.

The PathBuilder.through helper uses Pedro's curveThrough cubic-chain routine at
tension 0.5. It supplies the execution-time start explicitly and extrapolates a
previous point from the first destination. Adjacent segments have matching
first derivatives at their shared waypoint. This produces Catmull-Rom
interpolation with the number of segments proportional to the number of
destinations. It avoids Pedro 2.1.2's general BezierCurve.through vararg factory,
which failed a four-point numerical waypoint test. PathBuilderTests checks the
real path geometry and join derivatives, one-destination straight travel,
repeated-point finiteness, and rejection of missing destinations.
DriveSubsystemTests checks live-start forwarding, global heading, ownership,
naming and both hold choices. Physical tracking accuracy remains robot validation.

The former `curves(...)` method has been removed. Replace those calls with
`through(...)` to visit the same destinations with smooth segment joins. Use
`paths` for explicitly chosen control points and segment shapes.

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

`resetStartingPose(pose)` calls both Pedro's starting-pose API and current-pose API.
The former defines the localizer reference; the latter prevents the prior pose
offset from surviving a configuration change. A season should call it when a
starting-location setting changes during init. Auto should not reset pose again at
Start because teams may reposition the initialized robot and rely on Pedro tracking
that movement.

## Drawing

`PedroDrawingComponent` draws Pedro's robot pose and heading using the configured robot radius.
Its constructor order is `robotRadius`, `field`, then `getFollower`, with defaults
for all three. OpMode uses `PedroDrawingComponent(Constants.robotRadius)`.
For existing positional calls, move the radius first. Named arguments keep their
names, and the follower supplier can be a trailing lambda.
PedroDrawingComponentTests covers the radius-first constructor and defaults
(REQ-PLT-036).
It is diagnostic only; it must never feed localization. Robot length and width live
with the Pedro constants because they configure both navigation alignment and field
representation.

## Drive Motor Diagnostics

Drive reads Pedro’s existing drivetrain motor collection with
`follower.drivetrain.tel()`. The collection extension resolves
configured names through the hardware map without acquiring motors again. Pedro retains control of motor power and
configuration. During each periodic snapshot, the four devices use the same
TeamCode `tel` policy as other motors: DEBUG power, velocity, and position; VERBOSE
current, velocity percentage, and RPM. Motor values remain numeric for Panels
graphs. This is current telemetry, not a periodic historical RobotLog event.

Motor telemetry uses `(this as? Mecanum)?.motors?.tel()` and supports Pedro 2.1.2
`Mecanum`. Other drivetrains, including deprecated `MecanumEx`, Swerve, and custom
implementations, omit motor telemetry without interrupting driving.

## Chassis setup routes

The [drivetrain guide](../guides/drivetrain.md) separates mecanum from two-motor
tank/arcade and drive encoders from dedicated localization. The paired-slot tank
recipe requires robot-centric zero-strafe input; it does not make the chassis
holonomic. Collection telemetry reports each distinct physical motor once.
REQ-SCF-011 is verified by TankDriveSetupTests and PedroDriverControlledTests;
REQ-PLT-043 retains read-only telemetry checks in TelemetryTests.

## Pose direction helpers

REQ-PLT-056 adds the `Pose.directionTo(other)` extension in
`com.threedrd.pedropathing`. It returns a fresh native Pedro
Vector of length one pointing from this pose's XY position to the other pose's
XY position, in their shared field coordinate frame. Headings do not affect it;
neither pose is mutated. Coincident positions return a zero vector.

```kotlin
val pose = follower.pose
val distance = pose.distanceFrom(goal)
val direction = pose.directionTo(goal)
val towardVelocity = follower.velocity.dot(direction)
val towardAcceleration = follower.acceleration.dot(direction)
```

Pedro already supplies distanceFrom and Vector.dot. Positive projections point
toward the other pose, negative projections away. Use the same coordinate frame
for both poses and input vectors. The helper constructs Cartesian components
directly, keeping axis-aligned zero components exact rather than reconstructing
them from a polar angle. This avoids tiny orthogonal projection values affecting
sign-based calculations. It does not model shooter offsets or prescribe robot
control policy. PoseTests cover axes, diagonal/reverse directions, nonzero origins,
heading independence, pose preservation and independent zero vectors.

## Point-to-point and absolute turns

`Drive.turnTo(heading)` targets an absolute heading; `turn(angle)` remains relative.
Both accept typed angles or degrees and claim Drive. Adapted `TurnTo` computes
signed shortest target-minus-current heading at command start and calls Pedro's
relative turn, avoiding Pedro 2.1.2's unsigned turnTo calculation. It waits for
Pedro's turning flag and cancels following on interruption.

`Drive.to(pose, reverse = false, holdEnd = true)` turns toward destination X/Y,
travels straight at constant heading, then turns in place to the final heading.
Reverse offsets only the travel heading by 180 degrees. Already-reached positions
skip travel using Pose.distanceFrom and the follower path constraints' translational
tolerance. Pose.face provides the bearing. Apply hold/stop after the final turn.

`Drive.line(pose, holdEnd = true)` creates a live-start BezierLine with constant
supplied heading, without initial/final turns. Tank callers must align first.
`curve(pose)` retains the former continuous linear-heading `to` behavior.
Axial/lateral now use curve, preserving signed movement and forward/strafe aliases.
Lateral movement still requires a holonomic chassis. The deferred sequence claims
Drive throughout; these helpers do not establish general differential path following.

Use **Deploy Full** for these library changes; Sloth alone cannot install TurnTo.
The originating tank checkout reported working absolute-turn and reverse maneuvers,
but setup, accuracy and repeatability were not fully recorded. DriveSubsystemTests
now verifies signed turns, forward/reverse point-to-point sequencing, nearby
destinations, constant-heading lines, interruption, and hold/stop outcomes.
Validate each robot's encoder geometry and floor tracking.

## Tuning lifecycle

The Pedro tuning launcher is vendor Java under TeamCode's
`adaptations/pedropathing` package. It uses Pedro's SelectableOpMode lifecycle,
with Utility registration, and builds its follower from the existing Constants.
This preserves the upstream tuning workflow and avoids running the robot's
NextFTC subsystem discovery or default driver command alongside the selected
tuner. Competition OpModes continue to use the shared robot OpMode lifecycle.
See REQ-SCF-016 and the [tuning guide](../guides/drivetrain.md#pedro-tuning-utility).


## Follower lifetime across OpModes

TeamCode's constants object implements PedroConfig, supplying createFollower and
robotRadius. The shared OpMode registers PedroComponent(Constants) alongside its
other components. Each OpMode gets its own component; a single shared follower
inside the library component is created on first successful initialization and
reused while the incoming hardware map is the same object. A different map (for
example after a Sloth hardware-map rebuild) invalidates the cached follower, so the
factory creates fresh hardware references. Development reloads may reset the pose;
no pose transfer is attempted. Normal Auto-to-Teleop transitions sharing the map
retain localization. This assumes one robot configuration; changing constants
without replacing the map requires restarting the Robot Controller.
The component combines the upstream NextFTC PedroComponent and PedroDrawingComponent.
Initialization cancels following and refreshes localization without setting the pose.
Init and active loops update before drawing, and preStop immediately breaks following
before postStop clears the upstream active reference. Drive.stop remains immediate
and idempotent.
No background update loop runs between OpModes. Pinpoint tracks onboard while
powered; the next update reads movement that occurred during that interval.
This avoids reconstructing Pedro's Pinpoint localizer, whose constructor writes
its starting pose into the device. TeamCode remains responsible for intentional starting-pose resets; the component
never assigns a starting pose on its own.

The cached follower and hardware references survive normal OpMode transitions,
but a new hardware map invalidates them. A Robot Controller restart also clears
them. Use a full deployment for this
library/OpMode structural change; do not assume a Sloth deployment refreshes the
retained factory or changed constants. Non-Pinpoint localizers may have different
between-OpMode tracking behavior. Hardware validation must cover stopping during
a path, pushing the robot after Auto stops, Teleop initialization, Teleop-first
startup, repeated OpModes, and Sloth reload followed by Teleop without the empty-name
telemetry failure. Desktop tests verify lifecycle reuse, map invalidation and fresh reads,
not Android hardware-reference validity or physical odometry.

Reconstruction: add the combined PedroComponent in the reusable
Pedro package, implement PedroConfig on the team's constants, register
PedroComponent(Constants) in the shared OpMode, and preserve Drive lifecycle stop.
REQ-PLT-060 and PedroComponentTests track this contract.
