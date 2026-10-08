# Architectural Decisions

These short decision records preserve the alternatives considered and the reason
the current direction is defensible.

## Documentation Is Repository-Owned Markdown

**Decision:** Keep canonical documentation in `docs/`, publish it with MkDocs
Material and GitHub Pages, and use the root README as the landing page.

**Why:** Documentation changes receive the same review, branch, tag, and history as
code. MkDocs adds navigation and search without copying content. A GitHub wiki would
create separate history and can drift; if enabled, it should link to the canonical
site.

## Reuse Is Separated From Season Policy

**Decision:** Use `3drdNextFTC` as the reusable module, with TeamCode as the
consumer/policy layer.

**Why:** Multiple teams/seasons can share fixes without inheriting a field model or
hardware. Optional strategy systems remain in the robot repository until they are
deliberately adopted as part of the Quickstart contract.

## Reflection Is Allowed at Initialization Boundaries

**Decision:** Use reflection for subsystem discovery, hardware-field discovery,
setting metadata, and one-time diagnostics field lookup.

**Why:** These remove error-prone registries and duplicated menu declarations. The
work occurs during initialization or is cached, not in the high-frequency loop, and
has exhaustive tests.

Subsystem class discovery intentionally repeats for each OpMode lifecycle. This is
still initialization-bound work, and it prevents a Sloth hot reload from retaining
singleton instances created by the previous TeamCode class loader. Discovery loads
the classes through the active OpMode so Panels and the subsystem lifecycle share
the same live TeamCode class identity.

## Sloth Is Development Acceleration, Not Robot Architecture

**Decision:** Integrate Dairy Sloth at the TeamCode application boundary and expose
two named workflows: **Deploy Sloth** for ordinary TeamCode iteration and **Deploy
Full** for a clean APK install. Full deployment automatically removes the remote
Sloth overlay.

**Why:** Hot reload shortens the edit/test loop without moving Sloth into reusable
robot APIs. A clean full deployment remains the authoritative integration and
competition baseline because dependency, manifest, resource, and library changes
cannot be proven by replacing TeamCode bytecode alone.

Every shared Sloth entry point runs Gradle offline. Sloth is used only after a full
installation has primed the computer, so repository access adds delay and failure
risk without providing a valid Sloth workflow. Missing cached artifacts instead fail
promptly and identify that the computer needs an online full build.

## Probe ADB Before Refreshing Its Transport

**Decision:** Before transferring code, both deployment workflows perform a bounded
ADB shell round trip. A healthy connection is preserved. A failed or timed-out
probe triggers a disconnect and reconnect of only the configured Robot Controller,
followed by another round-trip verification.

**Why:** ADB's recorded transport state can outlive a physical controller power or
network interruption, so a state query alone is not sufficient proof that the
robot responds. An unconditional disconnect/reconnect reliably clears that stale
transport but needlessly interrupts healthy Logcat sessions and adds delay to every
Sloth deployment. The bounded probe preserves the fast healthy path while retaining
the teams' proven disconnect-before-connect recovery behavior.

## Controls Activate at Teleop Start

**Decision:** Separate `controls()` from `initialize()` and clear bindings for every
OpMode.

**Why:** Robot motion during init is unsafe, Auto must not receive manual controls,
and global binding accumulation caused double actions after re-init. Config menu
bindings remain an intentional init-time exception.

## Telemetry and Logging Are Separate

**Decision:** Keep `tel` for current state and `log` for events, with separate
levels/filters and a convenient shared config default.

**Why:** Driver/Panels state is replaced every frame; Logcat/history is cumulative.
Duplicating high-rate state into logs creates noise and cost.

## Hardware Update Hooks Are Split Between Library and TeamCode

**Decision:** Keep mutation-then-hook `update` in the reusable hardware API and
specific `tel()` fields/levels in TeamCode.

**Why:** Ordering and wrapper integration are universal; what students want to see
for a motor or sensor is a team policy.

## Commands Use Delegated Naming and Deferred Construction

**Decision:** Infer names for property-delegated instant/deferred commands and build
live-state command children at start.

**Why:** This removes repeated strings/log calls while preserving accurate pose,
config, and vision decisions and strict requirement ownership.

## Pedro Constants Are Neutral in Quickstart

**Decision:** Keep a structurally valid constants object in the seasonal base using
obvious template values and Pedro's drivetrain-encoder localizer. Dedicated
localizers such as Pinpoint remain documented, optional substitutions; introduce
Osiris tuning only in phase 3.

**Why:** The base must compile and demonstrate integration without making unsafe
claims about another robot or requiring a particular localization device. A new
season has one clear replacement point that still resembles Pedro documentation.

## Vendor GoBilda Code Remains Java

**Decision:** Import the supplied Prism implementation without a Kotlin rewrite.

**Why:** It is third-party code the team does not intend to maintain. Source fidelity
improves upstream comparison and replacement.

## Embedded Versus Robot-Level Control

**Decision:** Use hub embedded velocity PIDF for flywheel motor speed and Next
Control for drive/robot-level corrections.

**Why:** Embedded motor loops run close to the encoder and at high frequency; robot
assists combine pose, vision, and multiple axes and belong in the OpMode loop.

## Separate Seasonal Upgrades from Framework Migrations

Upgrade FTC SDK 12, Panels, Sloth/Load, Pedro 2.1.2, Kotlin, Mockito, and drive
motor diagnostics in one review branch while retaining NextFTC v1 and upstream
drive-encoder localization. Gradle 9.1.0 follows the SDK baseline and supplies
the API required by Load 0.3.2. Keep Pedro 3 and NextFTC v2 in a later track.
Preserve shared history; eventual upgrade commits append to it.
