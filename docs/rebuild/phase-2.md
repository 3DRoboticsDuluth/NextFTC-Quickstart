# Phase 2 — Reusable Platform

This phase creates the tested seasonal launch point. Every commit is neutral: it may
describe how 3DRD builds robots, but it must not describe Decode or Osiris.

## Ordered Changes

### 1. Module and Dependency Structure — `080cf185`

Create the Android library module `3drdNextFTC`, enable Kotlin, and make TeamCode
depend on it. Add shared test/coverage configuration with 100% line and branch
thresholds. Declare the versions in
[Modules and dependencies](../reference/modules-dependencies.md), including Next
Control, NextFTC, Panels, and Pedro repositories/artifacts.

Why first: every later reusable type and test needs a correct ownership boundary.

Quickstart creates only `3drdNextFTC`; reusable Quanomous infrastructure remains in
the separate Osiris implementation repository.

Checkpoint: all empty modules compile and Gradle can resolve every artifact.

### 2. Diagnostics — `9dedae09`

Implement `Diagnostics`, separate `LogLevel` and `TelemetryLevel`, `Logging`,
`Logger`, `LogEntry`, `Telemetry`, `Tel`, `TelemetryComponent`, and ActiveOpMode
mode helpers. Join Driver Station and Panels telemetry; keep RobotLog event output;
retain/rebuild filtered Driver Station log history; log command snapshot changes.

Why now: subsystem and hardware layers use source-scoped diagnostics, so the
destination contract must exist before them.

Checkpoint: diagnostics, logging, telemetry, component, and mode-helper tests pass.

### 3. Hardware Wrappers — `9f0b1860`

Implement lazy wrappers for servo, continuous servo, motor, IMU, and arbitrary
hardware. Each implements the common `Hardware` initialization contract. Provide
`update` extensions that mutate then call a configurable telemetry hook. Create a
hardware-independent test harness capable of providing devices by configured name.

Why: singleton subsystems need safe declaration before an active hardware map and a
uniform failure/testing seam.

Checkpoint: one test file per wrapper passes without a robot.

### 4. Commands and Subsystem Lifecycle — `9889ceaa`

Implement bindings cleanup, delegated/named/logged instant commands, deferred
command/factories, `alongWith`, repetition, base `Subsystem`, discovery, and
`SubsystemComponent`. Enforce command requirements. Initialize reflected hardware,
isolate disabled subsystems, run explicit start/controls/stop phases, schedule
defaults, and stop healthy subsystems in reverse order.

Why: this is the reusable execution model all student subsystems follow.

Checkpoint: binding, composition, instant, deferred, repeat, discovery, subsystem,
and component tests pass.

### 5. Configuration and Persistence — `e2ebfce8`

Implement FTC storage, debounced JSON persistence, `@Setting`, setting metadata,
type-driven setting construction, options providers, `ConfigComponent`, and
`ConfigSubsystem`. Preserve declaration order. Install init-time menu bindings and
support live/non-live values. Let the concrete `Config` singleton own its settings
directly, and restrict configuration persistence to fields annotated with `@Setting`.

Why: configuration should be declared once and consumed by Driver Station, Panels,
persistence, and diagnostics without a parallel menu table.

Checkpoint: storage, persistence, setting, diagnostics, config component, and config
subsystem tests pass.

### 6. Pedro Drive/Navigation Foundation — `115ebe3b`

Add generic `DriveSubsystem` and `NavSubsystem`, tile distances, typed path/T
progress, pose transforms, follower start-pose reset, Pedro driver command, and
field drawing component. Follow both `Path` and `PathChain`; build deferred paths;
support dimension-aware aligned poses and typed movement/waits.

Why: teams should specialize route coordinates and controls, not reimplement common
Pedro-to-NextFTC command mechanics.

Checkpoint: every Drive/Nav/Pedro helper test passes with neutral dimensions.

### 7. Sensor Debounce — `e2ec5037`

Add the stable-edge `Debounce` utility and exhaustive timing/reset tests.

Why: sensor chatter is cross-season behavior and does not belong in one intake.

### 8. Neutral TeamCode Scaffold — `e41cdb99`, `001609b3`, and `e34583a5`

Create:

- TeamCode hardware telemetry policy;
- Template Pedro constants with 18-inch placeholder dimensions, drivetrain-encoder
  localization, and defaults;
- Shared `OpMode` composition root;
- FTC-discoverable Teleop and an Auto that schedules a neutral example routine;
- Reusable driving/diagnostic `Config`;
- Concrete, customizable `Drive` and `Nav` subsystems with start/end poses;
- A small `Auto.execute` command that drives from the initialized start toward the
  example end pose;
- Hardware-free `Timing` subsystem;
- Tests for all scaffold code.

Why: a library is not a seasonal launch point until a minimal consumer compiles and
demonstrates correct integration.

The three commits establish the general scaffold first, add the concrete neutral
Drive/Nav example second, and add the minimal autonomous example third.

!!! danger "Template values are not robot values"
    The neutral Pedro constants exist to compile. They must be replaced and tuned
    before driving a physical robot.

### 9. Fast TeamCode Deployment — `978b8b3a`

Add Dairy Sloth's runtime and Load Gradle plugin at the TeamCode application
boundary. Replace the TeamCode runtime's standard FullPanels artifact with the
version packaged for Sloth, while leaving `3drdNextFTC` compiled against the
standard API. Add shared **Deploy Sloth** and **Deploy Full** run configurations and
matching Windows/Linux scripts. Add a shared deployment preflight that proves the
configured controller responds to an ADB shell round trip and, only when necessary,
repairs its TCP transport with a targeted disconnect/connect. Pass Gradle's
`--offline` option through every shared Sloth entry point so a primed computer never
waits for dependency repositories during fast iteration.

Do not cache discovered subsystem singletons across OpMode lifecycles. A hot load
can introduce a new TeamCode class loader, so each OpMode must discover the current
singleton instances through the active OpMode's class loader.

Checkpoint: Gradle exposes `deploySloth` and `ensureAdbConnection`; the shared IDE,
Windows, and Linux Sloth entry points specify `--offline`; dry-runs of both
deployment workflows include the ADB preflight; a dry-run of `installDebug` also
includes `removeSlothRemote`; dependency insight resolves only the Sloth-compatible
FullPanels runtime for TeamCode; repeated subsystem discovery tests pass. Complete
the physical checks in [Deploy robot code](../guides/deployment.md).

### 10. Conventions and Documented Endpoint — `c179c987`

Add `AGENTS.md`, the repository spelling dictionary, inspection profile, and ignore
rules. Verify the entire platform, then apply the Quickstart scope/documentation
commits so `main` is the season-neutral launch point.

## Exact Replay

```powershell
git cherry-pick 080cf185 9dedae09 9f0b1860 9889ceaa e2ebfce8 115ebe3b e2ec5037 e41cdb99 001609b3 e34583a5 978b8b3a c179c987
# Apply the Quickstart documentation commits from this history.
```

## Phase Gate

Run the exact command in [Verification](verification.md) at this commit. Also verify:

- `rg "teamcode|Osiris|Decode" 3drdNextFTC/src/main`
  finds no forbidden dependency or robot policy;
- Neutral TeamCode contains no real hardware-map names or tuned robot constants;
- Both OpModes are discoverable and the debug APK assembles;
- Quickstart `main` points to the verified commit.

## BIOBUZZ Upgrade Extension

After the historical reconstruction, apply the upstream SDK 12 manifest and
AprilTag sample changes and SDK 12.0.0 dependencies. Use Gradle 9.1.0, Kotlin
2.4.20, Mockito 5.24.0, Pedro FTC 2.1.2, Panels 1.0.13, and Sloth/Load 0.3.2.
Retain NextFTC v1 and the Pedro extension 1.0.0. Add read-only Drive motor
diagnostics from Pedro’s existing drivetrain motor collection using the common
TeamCode motor telemetry policy, without separate hardware declarations.

Traceability: REQ-FND-006 maps to SDK resolution, sample compilation, and APK
assembly. REQ-PLT-043 maps to DriveTests and hardware TelemetryTests;
REQ-PLT-006 maps to offline deployment dry runs and physical full-install checks.
Run the required verification command and strict documentation build. Full
installation, Driver Station compatibility, Panels, localization, autonomous,
and hot reload still require robot validation. No Pedro 3 or NextFTC v2 code is
part of this upgrade track.

Motor telemetry uses `(this as? Mecanum)?.motors?.tel()` and supports Pedro 2.1.2
`Mecanum`. Other drivetrains, including deprecated `MecanumEx`, Swerve, and custom
implementations, omit motor telemetry without interrupting driving.

## Team onboarding extension

After constructing the neutral platform, retain its reconstruction pages under
Platform reference, organize navigation into Get started / Our robot / Platform
reference, and add editable robot documentation plus the repository skill.
Traceability: REQ-SCF-010 maps to robot templates, workflow, navigation,
and strict MkDocs.

Extend the robot documentation templates with an implementation-readiness guide
and a worked hypothetical subsystem specification. Teach observable requirements,
test scenarios, lifecycle and event priority, and cross-subsystem interactions.
Add the same completeness review to the repository documentation skill.
Traceability: REQ-SCF-012 maps to these guides, robot requirement/interaction
templates, skill validation, and a strict MkDocs build.
