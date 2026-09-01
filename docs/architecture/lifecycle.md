# Lifecycle

NextFTC supplies the component and command scheduler model. The 3DRD layer adds
automatic subsystem discovery, staged hardware initialization, fault isolation,
bindings lifecycle, default scheduling, and explicit subsystem start/stop hooks.

## OpMode sequence

| FTC phase | Component action | Subsystem action | Reason |
|---|---|---|---|
| Construct | Add components | None | Establish one composition root. |
| Pre-init | Clear bindings; initialize diagnostics/config; discover subsystems | Initialize declared hardware, then `initialize()` if healthy | Hardware must exist before subsystem state uses it. |
| Wait for start loops | Begin/update telemetry frames | `periodic()` | Drivers can configure and inspect the robot during init. |
| Start pressed | Record mode state | `start()` for healthy subsystems; then `controls()` only in Teleop | Controls cannot move the robot during init or Auto. |
| Active loops | Begin/update telemetry; run scheduler | `periodic()` and idle default scheduling | Continuous state and commands cooperate through requirements. |
| Stop | Scheduler/framework shutdown | `stop()` in reverse subsystem order | Outputs are stopped directly even if no later loop occurs. |

## Discovery

`SubsystemComponent.all()` scans the installed APK for concrete NextFTC subsystem
implementations. Kotlin `object` instances are resolved and flattened through
NextFTC's subsystem composition. Results are sorted by `Subsystem.order` and kept
in a linked set.

Reflection is justified here because discovery occurs once per OpMode initialization,
removes a frequently forgotten registry, and is thoroughly tested. It is not used
inside the high-frequency periodic loop.

## Hardware failure isolation

The base `Subsystem` reflects over its own declared fields and finds reusable
`Hardware` wrappers. Each wrapper initializes independently. If any initialization
throws:

- the error is retained on the owning subsystem;
- the error is written to Logcat;
- subsystem `initialize()`, `start()`, `periodic()`, controls, commands, and `stop()`
  are skipped as appropriate;
- Driver Station telemetry reports that the subsystem is disabled.

This supports a partially assembled robot and makes a missing device diagnosable.
It does not claim that running a competition robot with a disabled critical
subsystem is safe; the OpMode or strategy can impose stricter readiness checks.

## Why `stop()` is explicit

Changing a target variable is not enough during shutdown because another periodic
cycle is not guaranteed. Mechanism stop hooks therefore update desired state and
write a safe actuator output directly. The subsystem component catches and logs a
stop failure, then continues stopping the remaining subsystems in reverse order.

## Binding lifecycle

Bindings may be declared during `initialize()` when they support init-time UI such
as the config menu. Robot controls belong in `controls()`, which is called once after
Teleop start. `BindingsComponent` clears global bindings when a new OpMode begins,
preventing the double-trigger behavior that otherwise appears after stop/re-init.

## Default commands

After a subsystem's active-loop `periodic()`, the component asks whether the
command manager has a command using it. If not, a non-null default is scheduled.
Drive uses this to make driver control the Teleop default without manually
rescheduling it every loop. Auto receives no manual default because its config state
is not Teleop.
