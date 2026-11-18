# Servo angle mapping

`com.threedrd.nextftc.control.ServoAngle` converts a desired output angle into a
logical servo position within 0–1. It does not own hardware, estimate actual
position, or run a feedback controller. Geometry and mechanism policy stay with
the consuming subsystem.

## Live calibration

Expose a mutable mapper on a Panels-configurable object or nested mechanism:

```kotlin
var AIM = ServoAngle()
var POSITION_MIN = 0.0
var POSITION_MAX = 1.0
val servo = ServoEx("mechanism").scaleRange(POSITION_MIN, POSITION_MAX)
```

The live `angle1` / `position1` and `angle2` / `position2` fields describe two
measured output angles in **degrees** and their corresponding **unscaled** servo
positions. They need not be mechanical endpoints. For example, forward at 0 degrees
and position 0.27, then backward at +180 degrees and position 0.72:

```kotlin
var AIM = ServoAngle(angle1 = 0.0, angle2 = 180.0, position1 = 0.27, position2 = 0.72)
```

Measure with the full 0–1 SDK range, in the servo's configured direction. Include
gearing in the measured output angles. Either pair may come first, and increasing
position may increase or decrease angle. Both angles default to zero, leaving the
mapper unconfigured. Positions default to 0 and 1. Panels discovers all four
mutable fields through the mutable `AIM` object; instances remain independent.

Safe physical limits are separate from the calibration pairs. Pass the same limits
to the mapper and SDK bridge. The mapper derives the angles at those limits, then
returns a logical 0–1 demand inside that safe range:

```kotlin
fun aim(angle: Angle) {
    servo.scaleRange(POSITION_MIN, POSITION_MAX)
    servo.position = AIM.calculate(angle, servo.position, POSITION_MIN, POSITION_MAX) ?: servo.position
}
```

For the example above and safe range 0.2–0.8, forward maps to logical 0.116667;
the SDK converts that to unscaled 0.27. Backward maps to logical 0.866667,
which becomes unscaled 0.72. Do not apply the physical limits again to the result.
Omitting the limits assumes full 0–1 travel. Changing safe limits does not require
remeasuring the two calibration points.

Use degree/radian unit extensions for the typed input. Current position is logical
0–1 **inside the scaled range**, not a measured output angle. Every call reads
current calibration and limits. The mapping assumes linear angle versus position.

## Reachability and wrapping

`calculate(angle, currentPosition, minPosition, maxPosition)` returns a nullable normalized position.
It considers equivalent bearings separated by full turns and selects a reachable
one nearest the angle represented by the current position. A range crossing the
usual heading wrap, reversed endpoints, or calibrated multi-turn travel therefore
does not require mechanism-specific wrapping code.

Invalid calibration, nonfinite inputs, and angles with no reachable equivalent
return null. The caller decides whether to hold, signal a fault, or take another
action. A finite current position outside 0–1 is clamped only as the reference for
choosing the nearest turn; unreachable angles are not silently sent to an endpoint.
Moving a positional servo toward a new target is managed by its own electronics;
this mapper adds no motion profile or physical alignment/readiness guarantee.

## SDK range updates

Hardware initialization restores a previously acquired servo's fluent range and
cached logical position each OpMode, because the SDK resets its own range between
OpModes. Repeated periodic calls still skip unchanged ranges. This lifecycle
restoration is regression-tested against the SDK reset; robot validation remains pending.

The project ServoEx factory and fluent `scaleRange` bridge retain the native
NextFTC type and lazy acquisition. Repeating the same fluent range does no SDK
work. Changing it after acquisition applies the new SDK range and reapplies the
native cached logical position, even when the normal position cache would skip
an unchanged demand. This is necessary for live physical endpoint tuning.

The range must satisfy the SDK's increasing 0–1 endpoint rules. Call the helper
with the current limits during updates; changing the original variables alone
cannot rerun a constructor expression. Avoid mixing direct SDK range writes with
the cached fluent bridge. Native servo reversal remains available and is applied
by the SDK exactly once; calibrate endpoints in that configured direction.

`ServoAngleTests` covers interpolation, units, wrapping, multi-turn selection,
live/reversed calibration, interior measurement pairs, separate safe limits, invalid inputs and instance independence.
`ServoExTests` covers lazy acquisition, native identity, range-change writeback,
unchanged-range suppression, OpMode range restoration and actual SDK scaling with reversal. Consuming
mechanisms must separately test geometry, lifecycle and their unreachable policy.
