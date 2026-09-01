# Phase 2 — reusable platform

This phase creates the tested seasonal launch point. Every commit is neutral: it may
describe how 3DRD builds robots, but it must not describe Decode or Osiris.

## Ordered changes

### 1. Module and dependency structure — `69db65e6`

Create the Android library module `3drdNextFTC`, enable Kotlin, and make TeamCode
depend on it. Add shared test/coverage configuration with 100% line and branch
thresholds. Declare the versions in
[Modules and dependencies](../reference/modules-dependencies.md), including Next
Control, NextFTC, Panels, and Pedro repositories/artifacts.

Why first: every later reusable type and test needs a correct ownership boundary.

The historical `69db65e6` commit also created `3drdQuanomous`. That optional module
is retained in the Osiris repository but removed from Quickstart by `571c2b2b`.
A clean implementation of Quickstart should create only `3drdNextFTC`.

Checkpoint: all empty modules compile and Gradle can resolve every artifact.

### 2. Diagnostics — `276be2ef`

Implement `Diagnostics`, separate `LogLevel` and `TelemetryLevel`, `Logging`,
`Logger`, `LogEntry`, `Telemetry`, `Tel`, `TelemetryComponent`, and ActiveOpMode
mode helpers. Join Driver Station and Panels telemetry; keep RobotLog event output;
retain/rebuild filtered Driver Station log history; log command snapshot changes.

Why now: subsystem and hardware layers use source-scoped diagnostics, so the
destination contract must exist before them.

Checkpoint: diagnostics, logging, telemetry, component, and mode-helper tests pass.

### 3. Hardware wrappers — `d498d2e0`

Implement lazy wrappers for servo, continuous servo, motor, IMU, and arbitrary
hardware. Each implements the common `Hardware` initialization contract. Provide
`update` extensions that mutate then call a configurable telemetry hook. Create a
hardware-independent test harness capable of providing devices by configured name.

Why: singleton subsystems need safe declaration before an active hardware map and a
uniform failure/testing seam.

Checkpoint: one test file per wrapper passes without a robot.

### 4. Commands and subsystem lifecycle — `c0b42867`

Implement bindings cleanup, delegated/named/logged instant commands, deferred
command/factories, `alongWith`, repetition, base `Subsystem`, discovery, and
`SubsystemComponent`. Enforce command requirements. Initialize reflected hardware,
isolate disabled subsystems, run explicit start/controls/stop phases, schedule
defaults, and stop healthy subsystems in reverse order.

Why: this is the reusable execution model all student subsystems follow.

Checkpoint: binding, composition, instant, deferred, repeat, discovery, subsystem,
and component tests pass.

### 5. Configuration and persistence — `8b70be56`

Implement FTC storage, debounced JSON persistence, `@Setting`, setting metadata,
type-driven setting construction, options providers, `ConfigComponent`, and
`ConfigSubsystem`. Preserve declaration order. Install init-time menu bindings and
support live/non-live values.

Why: configuration should be declared once and consumed by Driver Station, Panels,
persistence, and diagnostics without a parallel menu table.

Checkpoint: storage, persistence, setting, diagnostics, config component, and config
subsystem tests pass.

### 6. Pedro drive/navigation foundation — `19604a24`

Add generic `DriveSubsystem` and `NavSubsystem`, tile distances, typed path/T
progress, pose transforms, follower start-pose reset, Pedro driver command, and
field drawing component. Follow both `Path` and `PathChain`; build deferred paths;
support dimension-aware aligned poses and typed movement/waits.

Why: teams should specialize route coordinates and controls, not reimplement common
Pedro-to-NextFTC command mechanics.

Checkpoint: every Drive/Nav/Pedro helper test passes with neutral dimensions.

### 7. Sensor debounce — `4b3855f1`

Add the stable-edge `Debounce` utility and exhaustive timing/reset tests.

Why: sensor chatter is cross-season behavior and does not belong in one intake.

### 8. Neutral TeamCode scaffold — `29075d09`

Create:

- TeamCode hardware telemetry policy;
- template Pedro constants with 18-inch placeholder dimensions and defaults;
- shared `OpMode` composition root;
- empty FTC-discoverable Teleop and Auto;
- diagnostic-only `Config`;
- hardware-free `Timing` subsystem;
- tests for all scaffold code.

Why: a library is not a seasonal launch point until a minimal consumer compiles and
demonstrates correct integration.

!!! danger "Template values are not robot values"
    The neutral Pedro constants exist to compile. They must be replaced and tuned
    before driving a physical robot.

### 9. Conventions and documented endpoint — `3d35384f`

Add `AGENTS.md`, the repository spelling dictionary, inspection profile, and ignore
rules. Verify the entire platform, then apply the Quickstart scope/documentation
commits so `main` is the season-neutral launch point.

## Exact replay

```powershell
git cherry-pick 69db65e6 276be2ef d498d2e0 c0b42867 8b70be56 19604a24 4b3855f1 d7441bc7 29075d09 3d35384f
git cherry-pick 571c2b2b
# Apply the Quickstart documentation commits from this history.
```

## Phase gate

Run the exact command in [Verification](verification.md) at this commit. Also verify:

- `rg "teamcode|Osiris|Decode" 3drdNextFTC/src/main`
  finds no forbidden dependency or robot policy;
- neutral TeamCode contains no real hardware-map names or tuned robot constants;
- both OpModes are discoverable and the debug APK assembles;
- Quickstart `main` points to the verified commit.
