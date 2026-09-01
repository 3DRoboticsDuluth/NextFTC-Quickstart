# Hardware

Hardware wrappers make lazy FTC device acquisition, subsystem fault isolation,
configuration, telemetry hooks, and tests consistent across device types.

## Wrapper families

| Wrapper | Underlying device | Added behavior |
|---|---|---|
| `ServoEx` | NextFTC servo wrapper / FTC Servo | Lazy map lookup, configure lambda, `scaleRange`, reverse helper, caching |
| `CRServoEx` | NextFTC continuous servo | Lazy map lookup, configure lambda, caching |
| `MotorEx` | NextFTC motor / `DcMotorEx` | Lazy lookup, configure lambda, velocity-percentage access, caching |
| `IMUEx` | NextFTC IMU wrapper | Lazy lookup and FTC IMU parameters |
| `Device<T>` | Any FTC `HardwareDevice` | Generic lazy lookup for sensors/vendor hardware |

Declarations remain close to the mechanism:

```kotlin
val motor = MotorEx("intake") {
    direction = REVERSE
    mode = RUN_USING_ENCODER
    zeroPowerBehavior = FLOAT
}

val laser by device(DigitalChannel::class.java, "laser2") {
    mode = INPUT
}
```

The wrapper constructor does not require an active hardware map. Actual acquisition
happens during subsystem hardware initialization, which keeps singleton subsystem
construction safe in unit tests and before an OpMode is active.

## Update hook

```kotlin
servo.update { position = POS }
```

`update` applies the receiver lambda, then invokes the configured telemetry hook.
The shared library owns the reliable ordering; TeamCode decides what `tel()` means
for each hardware type. Current policy reports:

| Hardware | Debug | Verbose |
|---|---|---|
| Servo | Position | Reversed |
| Continuous servo | Power | Reversed |
| Motor | Power, velocity, encoder position | current, velocity percentage, RPM |
| IMU | Yaw, pitch, roll | — |

Values are formatted to stable, sensible precision. Teams may change this policy
without changing the wrappers.

## Caching

NextFTC wrappers cache actuator writes according to their supported tolerance. This
reduces repeated bus traffic when a periodic loop assigns the same target. A target
increment equal to the cache tolerance may not produce a new write, so tunable
mechanism increments should be chosen deliberately rather than forcing tolerance to
zero by default.

## Shutdown

Periodic target assignment and direct shutdown serve different contracts. During
normal loops, `update` is the single place where desired state reaches hardware and
telemetry. During `stop()`, motor mechanisms directly set a safe output because the
framework may not call `periodic()` again.

## Vendor devices

`Device<T>` supports types such as Limelight, digital channels, and the GoBilda
Prism. Vendor source is not mechanically converted to Kotlin merely for uniformity.
Keeping GoBilda's Java implementation intact makes upstream comparison and future
replacement easier.
