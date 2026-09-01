# Architecture overview

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

## Ownership rule

A practical test decides where code belongs:

> Could another 3DRD team use this next season without inheriting Osiris hardware,
> Decode coordinates, or our preferred telemetry fields?

- If yes, it belongs in `3drdNextFTC`.
- If it is a team-selectable policy, mechanism, field model, hardware map name, or
  tuning value, it belongs in `TeamCode`.
- Vendor code, such as the GoBilda Prism driver, stays recognizable and close to
  the vendor source in `TeamCode/adaptations`.

## The composition root

The shared TeamCode `OpMode` is the composition root. Its initializer installs
hardware telemetry policy and adds components in an intentional order:

1. `TelemetryComponent` starts and closes each display frame.
2. `BindingsComponent` prevents stale/duplicated bindings.
3. `BulkReadComponent` enables efficient hub reads.
4. `PedroComponent` owns the follower.
5. `PedroDrawingComponent` mirrors field state to Panels.
6. `ConfigComponent` restores and persists settings.
7. `SubsystemComponent.all()` discovers and runs subsystems.

Teleop inherits this root without extra code. Auto schedules its top-level command
in `onStartButtonPressed()`.

## Data and control flow

During a normal loop:

1. configuration and sensor state are current;
2. telemetry opens a fresh frame;
3. enabled subsystems run `periodic()` in deterministic order;
4. the scheduler runs active commands and supplies idle default commands;
5. hardware wrappers apply output and team-selected hardware telemetry;
6. current telemetry is updated to Driver Station and Panels;
7. event logs have already gone to RobotLog and, when visible, Driver Station log.

See [Lifecycle](lifecycle.md) for exact callback timing and
[Telemetry and logging](diagnostics.md) for destination behavior.

## Reuse boundary

Quickstart `main` contains real implementations of the generic classes and a
minimal compilable `TeamCode` scaffold. Its Pedro constants are explicitly
templates. A new season replaces those values and adds its field/robot
specializations. This makes the repository a real launch point rather than a
partially extracted library that requires cherry-picking later fixes.
