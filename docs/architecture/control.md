# Velocity profiles and software current limiting

`3drdNextFTC` supplies two velocity-only implementations of NextControl's
`InterpolatorElement`, plus a separate experimental `CurrentLimiter`. They have no
robot hardware names, scheduling, telemetry policy, or motor ownership. Adding
these optional utilities does not change existing robot behavior.

## Choose a velocity profile

```kotlin
import org.firstinspires.ftc.threedrd.nextftc.control.interpolators.*

val controller = controlSystem {
    velPid(PID)
    basicFF(FF)
    interpolator(VelocityRamp(maxAcceleration = 1.6))
}
```

Alternatively, replace the `interpolator` argument with:

```kotlin
VelocitySmoother(timeConstant = 0.1)
```

| Interpolator | Setting | Behavior |
|---|---|---|
| `VelocityRamp` | Positive finite `maxAcceleration`, velocity units per second | Limits both increasing and decreasing velocity, including reversing; reaches the target without overshoot |
| `VelocitySmoother` | Positive finite `timeConstant`, seconds | Covers about 63% of the remaining change in one time constant; approaches the target asymptotically |

Keep assigning the **final** desired velocity, not an already-ramped value:

```kotlin
controller.goal = KineticState(velocity = targetVelocity)
```

Use the same velocity units for target and measurement. With normalized motor
speed, `1.6` means 1.6 full-scale speed units per second: zero to `0.4` takes
0.25 seconds. With encoder ticks/second, acceleration is ticks/second squared.
Settings are mutable properties. Valid positive finite edits take effect without
resetting history. Profiles first advance to the edit time with the old setting,
then use the new setting for subsequent elapsed time. Current threshold and rates
apply on the next `limit` call, preserving the existing power cap. Invalid edits
throw and retain the previous value.

The shorter names replace `AccelerationLimitedVelocityInterpolator` with
`VelocityRamp` and `ExponentialVelocityInterpolator` with `VelocitySmoother`.
This is a source-level rename: update imports and constructor calls.

Create a separate interpolator for each controller. Each starts from zero velocity.
Setting a new goal first advances the old trajectory to the current time, then
changes the goal without a velocity discontinuity. Reassigning the same goal every
loop does not restart the trajectory. Time elapsed before the first use does not
cause an initial jump. After a running loop pauses, the trajectory does catch up
to elapsed time; explicitly reset when stopping or beginning a new lifecycle.

`currentReference` supplies velocity and instantaneous reference acceleration,
with position fixed at zero. The linear profile reports zero acceleration once
the target is reached; the exponential profile reports remaining velocity error
divided by the time constant. A changed goal's acceleration is reflected on the
next positive time advance. These are not position profiles. Supply finite
velocity goals (nonfinite velocity is rejected); acceleration and position fields
of the goal are not profiled.

Both profiles advance by elapsed seconds rather than by a fixed step per read.
Reads at the same timestamp return the same state, so NextControl's feedback and
feedforward reference reads do not double the ramp rate. Small real elapsed time
between reads advances the profile normally. The exponential result is independent
of how many intermediate reads occur for an unchanged goal.

`reset()` clears the goal, reference, and clock history. Set the desired goal again
after reset. Keep readiness tied to the final target and both measured motor
speeds, rather than declaring readiness at an intermediate reference speed.

## Add optional current feedback

Current feedback operates **after** PID and feedforward, on requested power:

```text
Velocity goal → Interpolator → PID + feedforward → Current limiter → Motor power
```

```kotlin
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit.AMPS
import org.firstinspires.ftc.threedrd.nextftc.control.CurrentLimiter
import org.firstinspires.ftc.threedrd.nextftc.hardware.update
import org.firstinspires.ftc.threedrd.nextftc.hardware.velocityPercentage

val limiter = CurrentLimiter(maxCurrent = 5.0, reductionRate = 2.0, recoveryRate = 0.5)

fun updateMotor() {
    controller.goal = KineticState(velocity = targetVelocity)
    launcher.update {
        val requestedPower = controller.calculate(KineticState(velocity = velocityPercentage))
        power = limiter.limit(requestedPower, motor.getCurrent(AMPS))
    }
}
```

This snippet assumes `launcher` is a native NextFTC `MotorEx`, with a controller and
target owned by the subsystem. Inside `launcher.update`, `motor.getCurrent(AMPS)`
accesses that instance's SDK motor. Hardware telemetry remains the TeamCode hook.
The example settings illustrate units, not a recommended motor rating.

Give each motor its own limiter and one fresh current sample per update.
The limiter starts with a `powerLimit` of `1.0`. Above `maxCurrent` amperes,
the cap falls by `reductionRate` power units per second. At or below the threshold,
it rises by `recoveryRate`, bounded to `0..1`. Returned power is clipped
symmetrically to that cap, retaining smaller requests and either direction.
Zero requested power always returns zero. Finite power and nonnegative finite
current are required; invalid samples throw rather than silently disabling limiting.

The first sample establishes time without reducing the cap; subsequent samples
apply their rate over the elapsed interval. Repeat calls at the same timestamp
do not change the cap again. `reset()` restores full available power and discards
elapsed time. Call it at lifecycle initialization/shutdown, not every periodic loop.
Direct emergency/lifecycle stops must still write zero power immediately,
independently of profiles and limiters.

This is sampled software feedback, **not a guaranteed current ceiling**. It cannot
prevent a transient that happens before a sample or react while the loop is stalled.
It limits power magnitude without distinguishing acceleration from active braking
and has no built-in PID anti-windup. Integrating controllers may wind up while output
is capped; start with zero integral gain or deliberately provide an anti-windup
strategy. A sustained overload can drive the cap to zero, with recovery beginning
when measured current drops. It does not latch a fault or manage stalled mechanisms.

## Timing and validation

All three constructors accept an optional `seconds: () -> Double` monotonic clock.
Production defaults to `System.nanoTime() / 1e9`; tests advance a fake clock without
sleeping. Nonadvancing or backward clock readings produce no negative time step.
The utilities are stateful and intended for one OpMode control thread.

`VelocityInterpolatorTests` verifies linear and exponential trajectories, reversal,
goal changes, repeated reads, reset/idle behavior, invalid settings, and actual
NextControl feedback/feedforward integration. `CurrentLimiterTests` verifies
reduction, recovery, saturation, both power signs, independent motor state, reset,
and invalid samples. These desktop tests establish the software contract, not
measured current reduction on a robot.
