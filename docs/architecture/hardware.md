# Hardware

Hardware adapters make lazy FTC device acquisition, subsystem fault isolation,
configuration, telemetry hooks, and tests consistent across device types.

## Adapter families

| Adapter | Underlying device | Added behavior |
|---|---|---|
| `ServoEx` | Native NextFTC servo (type alias) | Lazy configure factory, name/initialization extensions; upstream API and caching |
| `CRServoEx` | Native NextFTC continuous servo (type alias) | Lazy configure factory, name/initialization extensions; upstream API and caching |
| `MotorEx` | Native NextFTC motor (type alias) | Lazy configure factory, velocity-percentage extension; full upstream API and caching |
| `IMUEx` | Native NextFTC IMU (type alias) | Lazy configure factory, initialization/name extensions; full upstream API |
| `Device<T>` | Any FTC `HardwareDevice` | Generic lazy lookup for sensors/vendor hardware |

Declarations remain close to the mechanism:

```kotlin
val motor = MotorEx("intake") {
    direction = REVERSE
    mode = RUN_USING_ENCODER
    zeroPowerBehavior = FLOAT
}

val laser by Device("laser2", DigitalChannel::class.java) {
    mode = INPUT
}
```

The project factories and the `Device` constructor put the hardware name first.
`Device` accepts an optional
configuration block; `by` exposes its underlying FTC device through `getValue()`.
Motor and servo properties retain their enhanced objects using `=`.

The adapters do not require an active hardware map at construction. Actual acquisition
happens during subsystem hardware initialization, which keeps singleton subsystem
construction safe in unit tests and before an OpMode is active.

## Native NextFTC hardware

`MotorEx`, `IMUEx`, `ServoEx`, and `CRServoEx` are type aliases for the actual
NextFTC 1.1.0 classes.
The constructor-shaped configuration factories return those exact types; there is
no containing `motorEx` / `imuEx` object and no hand-maintained forwarding API.
All upstream constructors, properties, methods, and fluent return types remain
available directly. There are no custom servo subclasses either, so fluent
methods return the same native type recognized by discovery and telemetry.

### Aliases, factories, and constructors

These declarations serve different roles:

```kotlin
typealias MotorEx = dev.nextftc.hardware.impl.MotorEx

val motor: dev.nextftc.hardware.impl.MotorEx = MotorEx("intake") {
    zeroPowerBehavior = BRAKE
}
```

The type alias gives the upstream type a local name. It creates no runtime class,
wrapper, subclass, or extra constructors. The project's `MotorEx("intake")` and
`MotorEx("intake") { ... }` calls resolve to **factory functions** named `MotorEx`.
Those functions construct and return the exact upstream object, supplying lazy
hardware lookup and configuration. The same pattern applies to `ServoEx`,
`CRServoEx`, and `IMUEx`.

Their uppercase names deliberately make object creation read like construction;
the factory names remain part of the supported API. This does not mean the
configured overloads are upstream constructors. Kotlin resolves a factory or an
upstream constructor from the available declarations and argument types; IDE
Go to Declaration shows the selected implementation. Use the project's hardware
package when its factory overloads and extensions are wanted.

`Device(name, type)`, in contrast, calls the real constructor of the project's
generic delegated `Device<T>` class. `by` exposes its SDK device after framework
initialization. Keeping this distinction does not require renaming either API.
This clarification introduces no source or runtime compatibility change.

### SDK configuration blocks and native fluent methods

The factory configuration block receives the **FTC SDK device**, while the factory
returns a **NextFTC enhanced object**:

| Declaration | Configuration block receiver | Returned object |
|---|---|---|
| `MotorEx(name) { ... }` | SDK `DcMotorEx` | Native NextFTC `MotorEx` |
| `ServoEx(name) { ... }` | SDK `Servo` | Native NextFTC `ServoEx` |
| `CRServoEx(name) { ... }` | SDK `CRServo` | Native NextFTC `CRServoEx` |
| `IMUEx(name) { ... }` | SDK `IMU` | Native NextFTC `IMUEx` |
| `Device(name, type) { ... }` | SDK/vendor device of type `T` | Project `Device<T>` delegate; `by` exposes `T` |

```kotlin
val configured = MotorEx("intake") {
    zeroPowerBehavior = BRAKE // this is the SDK DcMotorEx
}
val fluent = MotorEx("launcher").floatMode().reversed()
```

The second line uses native NextFTC fluent methods on the returned `MotorEx`.
In particular, NextFTC `reversed()` reverses its logical power/encoder interpretation;
setting SDK `direction` inside the configuration block changes the SDK device.
Access through `.motor` reaches the SDK device directly and bypasses NextFTC's
logical reversal. Use one intentional reversal convention for a device.

The block is stored for first hardware acquisition, not executed when the subsystem
singleton is constructed. Framework initialization normally triggers acquisition;
early access to a native object's underlying SDK device can also trigger its lazy
lookup. `Device<T>` is initialized explicitly by the framework before delegated use.

### Fluent servo range configuration

```kotlin
val servo = ServoEx("deflector").scaleRange(MIN, MAX).reversed()
```

`scaleRange(min, max)` is a project extension returning the **same native ServoEx**;
`reversed()` is the upstream fluent method. Import `scaleRange` explicitly or use
the project hardware package wildcard. No wrapper, subclass, or reflection is used.

The factory registers a private configuration queue under a weak key in
`configurations`. Its SDK initializer applies the original configuration block
first, then queued range requests in order. The queue is cleared only after success,
so initialization failure retains pending actions for retry. Framework initialization
and early `.servo` access both trigger the queue through the native initializer.
Upstream lazy callbacks such as `reversed()` run after that SDK initializer.
After successful acquisition, a new `scaleRange` call updates the SDK servo
immediately, without another hardware lookup. It still returns the original object.

The extension supports **project-factory-created instances**. A servo built directly
through an upstream constructor has no registered configuration queue. Calling
this extension on it throws `IllegalArgumentException` with a factory-required
message, without forcing SDK acquisition. Existing upstream constructors remain
available; they are simply outside this optional lazy extension's scope.

Numeric `MIN` and `MAX` arguments are evaluated when the chain is declared. They
are not live Panels bindings. Retain the SDK configuration block when values must
be evaluated at acquisition time instead:

```kotlin
val servo = ServoEx("deflector") { scaleRange(MIN, MAX) }.reversed()
```

REQ-PLT-049 and `ServoExTests` cover native identity, construction without an active
OpMode, lazy lookup, queued order, early access, immediate later calls, retry, and
safe rejection of unregistered instances. The extension is additive; existing
factory blocks and calls do not need migration.

```kotlin
val motor = MotorEx("intake") { zeroPowerBehavior = BRAKE }
val imu = IMUEx("imu") { initialize(imuParameters) }

motor.reverse()             // upstream method, directly available
motor.velocityPercentage = 0.5 // local extension
imu.zero()                  // upstream method, directly available
```

`imuParameters` represents the team's configured SDK parameters, not a template
orientation. Configuration lambdas receive the underlying FTC SDK device and run
on first acquisition. Short `MotorEx("name")`, `ServoEx("name")`, and
`CRServoEx("name")` factories use the same lazy, typed lookup path. Their configured
factory overloads require a lambda, avoiding conflicts with upstream constructors'
default arguments. Original constructors accepting SDK objects, suppliers, or
explicit cache tolerance remain available and return the same native types.

Subsystem initialization recognizes all four native NextFTC types in addition
to `Hardware` adapters. It deduplicates object references, resolves each device
before the owning subsystem's `initialize()`, and isolates/retries failures as
before. Native failure messages identify the subsystem field: obtaining a display
name must not trigger another failed hardware lookup. Servo and IMU telemetry use registered
hardware-map names, falling back to the SDK device name for an unregistered instance.

### Migration and imports

This removes the four custom adapter runtime classes and is a binary compatibility
change: rebuild consuming code and perform a full APK installation before Sloth.
Existing configured declarations and hardware names remain valid. Replace
`motor.motorEx.someMember` with `motor.someMember` and `imu.imuEx.someMember` with
`imu.someMember`. The SDK devices still live at `.motor` and `.imu`, as upstream
intends. These four types no longer implement our `Hardware` interface; use subsystem
discovery or the initialization extensions rather than casting them to it.

A package wildcard import includes the aliases, factories, and extensions:

```kotlin
import org.firstinspires.ftc.threedrd.nextftc.hardware.*
```

With explicit imports, add the extension imports you use (`initialize`,
`velocityPercentage`, `scaleRange`, `update`, or servo/IMU `name`). Importing just the class/type alias
does not import its extensions. Native members win over same-signature extensions.
Type aliases are Kotlin-only: Java consumers import the original `dev.nextftc.hardware.impl`
classes and call extension/factory helpers through their generated static methods
when needed. REQ-PLT-044 defines this direct-API contract; REQ-PLT-011 and 030–032
retain the initialization, device coverage, and telemetry contracts.

## Breaking change: generic device declarations

The lowercase `device(type, name)` factory has been removed. This is a
source compatibility break: existing calls and imports will no longer compile.
Use `Device(name, type)` instead, reversing the argument order so the hardware
name comes first. Keep `by` and the existing configuration block:

```kotlin
// Before
val laser by device(DigitalChannel::class.java, "laser2") { mode = INPUT }

// After
val laser by Device("laser2", DigitalChannel::class.java) { mode = INPUT }
```

Replace an explicit import of
`org.firstinspires.ftc.threedrd.nextftc.hardware.device` with
`org.firstinspires.ftc.threedrd.nextftc.hardware.Device`. An existing wildcard
import of that package needs no change. Calls without a configuration block
also use `Device(name, type)`; its configuration block defaults to empty.
Hardware names, initialization timing, configuration, and delegated access to
the underlying device remain unchanged.

## Update Hook

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

## Vendor Devices

`Device<T>` supports types such as Limelight, digital channels, and the GoBilda
Prism. Vendor source is not mechanically converted to Kotlin merely for uniformity.
Keeping GoBilda's Java implementation intact makes upstream comparison and future
replacement easier.

## Typed Hub motor targets

REQ-PLT-050 adds opt-in `distancePerTick(Distance)` / `anglePerTick(Angle)`
calibration and `targetDistance: Distance` / `targetAngle: Angle` properties on
native MotorEx. Import them explicitly or use the hardware package wildcard.
For example (illustrative calibration, not a measured mechanism):

```kotlin
import dev.nextftc.core.units.*
import org.firstinspires.ftc.threedrd.nextftc.hardware.*

val slide = MotorEx("slide").brakeMode().distancePerTick(0.01.inches)
val turret = MotorEx("turret").anglePerTick(1.deg)

// After hardware initialization and deliberate Hub mode/power configuration:
slide.targetDistance = 6.inches
turret.targetAngle = 90.deg
```

The fluent calibration functions return the same native instance, acquire no
hardware, and work with both project factories and upstream constructors.
Weak-keyed per-instance metadata stores positive finite millimeters/tick or
radians/tick; no reflection, wrapper, subclass, or robot-specific calibration is
added. Distance and angle scales are independent. Configure only the dimensions
you use. Gear ratios and output circumference/lead belong in the team's measured
calibration, not an assumption about motor encoder counts.

Targets are absolute in native MotorEx's logical encoder frame: logical position
zero corresponds to zero distance/angle. Native `zero()`, `atPosition()`, writable
`currentPosition`, and logical reversal are respected. The adapter maps the
logical target to an SDK encoder target using the current logical/raw position
and direction. SDK direction remains part of the SDK's encoder coordinate frame.
Native position and raw encoder reads use the normal per-cycle bulk-read snapshot.
Use targets relative to that established zero; a positive distance is not an
incremental move. Angles are unwrapped: two revolutions means two revolutions,
not zero or a shortest-path turn.

Setters round to the nearest encoder tick (ties toward positive infinity). Missing
calibration, invalid scale, nonfinite converted targets, and targets outside the
SDK Int range fail clearly. Missing calibration and nonfinite targets fail before
hardware lookup; overflow fails without writing a target. A rejected scale leaves
any previous calibration intact. Getters read the SDK's currently commanded target,
convert it to the current logical frame, and return the configured physical quantity.
They report the rounded target, not a separately cached requested value. If the
logical zero or calibration changes, the getter reflects that coordinate change;
it does not move the motor or rewrite an existing target.

These properties only read/write SDK targetPosition. They do not select mode,
change power, wait for arrival, add limits/homing, or replace a controller.
For Hub position control, deliberately set SDK target, RUN_TO_POSITION, and
movement power in the owning subsystem, and directly zero SDK power at shutdown.
Do not use native MotorEx.power in that mode: it selects RUN_WITHOUT_ENCODER.
For NextControl, use the same calibration to compute a goal separately; these
properties specifically address the Hub target, not a NextControl controller.

`MotorTargetsTests` verifies native identity and lazy configuration, conversions,
rounding, signed/multi-turn targets, logical offsets/reversal, factory/native
constructor compatibility, independent calibrations, and failures without actuation.

### Separate ticks and travel

REQ-PLT-051 adds separate `.ticks(counts)` and `.travel(Distance)` or
`.travel(Angle)` calls. Both quantities describe **the same shaft revolution**:

```kotlin
val motor = MotorEx("staging").ticks(384.5).travel(336.mm).brakeMode()
val rotary = MotorEx("rotary").travel(360.deg).ticks(384.5)
```

Calls may appear in either order and always return the same native motor without
hardware acquisition. Each supplied count/travel must be positive and finite.
Partial configuration is allowed for motors that are still being configured or
used only for power/velocity. A typed target read/write requires travel for that
dimension and ticks; missing data or an unrepresentable ratio fails before SDK
lookup. There is no per-cycle configuration work, reflection, or wrapper.

Distance and angular travel are independent but share ticks per reference
revolution. Include external gearing in the travel for that same reference
revolution. Changing ticks affects all travel-based dimensions the next time a
target is read/written; it does not rewrite an existing SDK target.

The latest `travel(Distance)` or `distancePerTick()` call selects the distance
calibration, independently of the angle equivalent. A direct per-tick calibration
does not require ticks and is unaffected by later ticks changes; a subsequent
travel call uses the stored ticks value again. These rules preserve the original
per-tick APIs without silently mixing their meanings. `MotorTargetsTests` covers
partial construction, both orders/dimensions, invalid and extreme values,
per-motor isolation, and calibration precedence.
