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

`AIM.minAngle` and `AIM.maxAngle` are measured output **degrees** at logical servo
positions 0 and 1, respectively. Include gearing when measuring them. They may
decrease for a mechanism that rotates the opposite way. Both default to zero;
`configured` remains false until the endpoints are finite and distinct. The
mutable `AIM` field lets Panels discover the nested calibration. Separate mapper
instances do not share settings; replacing the object also takes effect immediately.

For an example calibrated range of -90 to +90 degrees, a zero-degree target maps
to 0.5. With SDK `scaleRange(0.2, 0.8)`, the SDK maps that logical midpoint to 0.5
of full servo travel, and logical endpoints to 0.2 and 0.8. Do not apply those
physical limits again to the mapper's output.

```kotlin
fun aim(angle: Angle) {
    servo.scaleRange(POSITION_MIN, POSITION_MAX)
    servo.position = AIM.calculate(angle, servo.position) ?: servo.position
}
```

Use the existing degree/radian unit extensions for the typed input. The current
position is logical 0–1 **inside the scaled range**, not an SDK full-travel
position or measured physical angle. Every call reads the current calibration.

## Reachability and wrapping

`calculate(angle, currentPosition)` returns a nullable normalized position.
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
live/reversed calibration, invalid inputs and instance independence.
`ServoExTests` covers lazy acquisition, native identity, range-change writeback,
unchanged-range suppression and actual SDK scaling with reversal. Consuming
mechanisms must separately test geometry, lifecycle and their unreachable policy.
