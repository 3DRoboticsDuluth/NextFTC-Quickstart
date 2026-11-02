# Architecture Overview

The architecture separates stable mechanisms for building a robot from the policy
of one robot and game.

```text
Official FTC Robot Controller
        │
        ├── FtcRobotController     Android application and FTC SDK host
        │
        ├── 3drdNextFTC            reusable lifecycle, commands, hardware,
        │                           diagnostics, config, and Pedro helpers
        │
        └── TeamCode               neutral seasonal scaffold
```

Dependencies point downward toward more reusable behavior:

```text
TeamCode ───────► 3drdNextFTC ─────► NextFTC / FTC / Panels / Pedro
```

## Reusable packages and migration

Reusable NextFTC integrations use `com.threedrd.nextftc`; Pedro integrations use
`com.threedrd.pedropathing`. Team robot code remains in its existing package.

This is a source-breaking package rename from `org.firstinspires.ftc.threedrd`.
Replace that prefix with `com.threedrd` in imports and fully qualified references,
including tests and reflection configuration. Gradle module names and upstream
NextFTC, Pedro and FTC SDK packages are unchanged. Perform a full APK deployment
after updating; Sloth alone cannot replace the library classes.

## Ownership Rule

A practical test decides where code belongs:

> Could another 3DRD team use this next season without inheriting Osiris hardware,
> Decode coordinates, or our preferred telemetry fields?

- If yes, it belongs in `3drdNextFTC`.
- If it is a team-selectable policy, mechanism, field model, hardware map name, or
  tuning value, it belongs in `TeamCode`.
- Vendor code, such as the GoBilda Prism driver, stays recognizable and close to
  the vendor source in `TeamCode/adaptations`.

## The Composition Root

The shared TeamCode `OpMode` is the composition root. Its initializer installs
hardware telemetry policy and adds components in an intentional order:

1. `TelemetryComponent` starts and closes each display frame.
2. `BindingsComponent` prevents stale/duplicated bindings.
3. `BulkReadComponent` enables efficient hub reads.
4. `PedroComponent` owns the follower.
5. `PedroDrawingComponent` mirrors field state to Panels.
6. `ConfigComponent` restores and persists settings.
7. `SubsystemComponent.all()` discovers and runs subsystems.

Teleop inherits this root without extra code. The sample Auto subsystem establishes
the neutral starting pose during autonomous initialization, and the Auto OpMode
schedules its top-level deferred command in `onStartButtonPressed()`.

## Data and Control Flow

During a normal loop:

1. Configuration and sensor state are current;
2. Telemetry opens a fresh frame;
3. Enabled subsystems run `periodic()` in deterministic order;
4. The scheduler runs active commands and supplies idle default commands;
5. Hardware wrappers apply output and team-selected hardware telemetry;
6. Current telemetry is updated to Driver Station and Panels;
7. Event logs have already gone to RobotLog and, when visible, Driver Station log.

See [Lifecycle](lifecycle.md) for exact callback timing and
[Telemetry and logging](diagnostics.md) for destination behavior.

## Reuse Boundary

Quickstart `main` contains real implementations of the generic classes and a
minimal compilable `TeamCode` scaffold. Its Pedro constants are explicitly
templates. A new season replaces those values and adds its field/robot
specializations. This makes the repository a real launch point rather than a
partially extracted library that requires cherry-picking later fixes.
