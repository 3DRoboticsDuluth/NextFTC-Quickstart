# Drive and navigation

The pathing architecture separates reusable motion vocabulary from the coordinates,
tuning, and controls of one robot.

## Pedro stack

`PedroComponent` owns a follower built by TeamCode `Constants.createFollower()`.
The reusable `DriveSubsystem` turns common follower operations into NextFTC commands.
The reusable `NavSubsystem` constructs robot-dimension-aware poses. Decode's `Drive`
and `Nav` add Osiris controls, assists, path shapes, and field locations.

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

## Units and pose alignment

The reusable helpers add:

- `number.tiles` as a 24-inch `Distance`;
- `number.pct` for path-completion fraction;
- `number.pctT` for Pedro curve parameter T;
- axial/lateral pose transformations;
- dimension-aware `pose()` with `FRONT/CENTER/BACK` and
  `LEFT/CENTER/RIGHT` alignment;
- typed angles using NextFTC units.

`NavSubsystem.pose()` shifts the requested contact/alignment point by half the robot
length/width in the robot's local frame. This lets a season define “front of robot
at this field point” instead of repeating trigonometry.

## Paths are late-bound

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

## Driver control

`PedroDriverControlled` accepts live suppliers for forward, strafe, turn,
robot-centric mode, and heading offset. Decode maps left Y, left X, and right X with
the signs verified on Osiris. It holds one reusable command instance whose scalar
changes for intake/low/medium/high/auto power.

Field-centric heading offset changes with alliance. Robot-centric mode can change
while the command is running because the suppliers are evaluated each update.
Drive assist hooks can transform each axis without replacing the standard command.

## Starting pose

`resetStartingPose(pose)` calls both Pedro's starting-pose API and current-pose API.
The former defines the localizer reference; the latter prevents the prior pose
offset from surviving a config change. It is called when Alliance or Side changes
during init. Auto does not reset pose again at Start because teams may reposition
the initialized robot and rely on Pedro tracking that movement.

## Drawing

`PedroDrawingComponent` draws Pedro's robot pose and heading using Osiris radius.
It is diagnostic only; it must never feed localization. Robot length and width live
with the Pedro constants because they configure both navigation alignment and field
representation.
