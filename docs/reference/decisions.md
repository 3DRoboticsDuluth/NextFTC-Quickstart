# Architectural decisions

These short decision records preserve the alternatives considered and the reason
the current direction is defensible.

## Documentation is repository-owned Markdown

**Decision:** Keep canonical documentation in `docs/`, publish it with MkDocs
Material and GitHub Pages, and use the root README as the landing page.

**Why:** Documentation changes receive the same review, branch, tag, and history as
code. MkDocs adds navigation and search without copying content. A GitHub wiki would
create separate history and can drift; if enabled, it should link to the canonical
site.

## Reuse is separated from season policy

**Decision:** Use `3drdNextFTC` as the reusable module, with TeamCode as the
consumer/policy layer.

**Why:** Multiple teams/seasons can share fixes without inheriting a field model or
hardware. Optional strategy systems remain in the robot repository until they are
deliberately adopted as part of the Quickstart contract.

## Reflection is allowed at initialization boundaries

**Decision:** Use reflection for subsystem discovery, hardware-field discovery,
setting metadata, and one-time diagnostics field lookup.

**Why:** These remove error-prone registries and duplicated menu declarations. The
work occurs during initialization or is cached, not in the high-frequency loop, and
has exhaustive tests.

## Controls activate at Teleop start

**Decision:** Separate `controls()` from `initialize()` and clear bindings for every
OpMode.

**Why:** Robot motion during init is unsafe, Auto must not receive manual controls,
and global binding accumulation caused double actions after re-init. Config menu
bindings remain an intentional init-time exception.

## Telemetry and logging are separate

**Decision:** Keep `tel` for current state and `log` for events, with separate
levels/filters and a convenient shared config default.

**Why:** Driver/Panels state is replaced every frame; Logcat/history is cumulative.
Duplicating high-rate state into logs creates noise and cost.

## Hardware update hooks are split between library and TeamCode

**Decision:** Keep mutation-then-hook `update` in the reusable hardware API and
specific `tel()` fields/levels in TeamCode.

**Why:** Ordering and wrapper integration are universal; what students want to see
for a motor or sensor is a team policy.

## Commands use delegated naming and deferred construction

**Decision:** Infer names for property-delegated instant/deferred commands and build
live-state command children at start.

**Why:** This removes repeated strings/log calls while preserving accurate pose,
config, and vision decisions and strict requirement ownership.

## Pedro constants are neutral at the base tag

**Decision:** Keep a structurally valid constants object in the seasonal base using
obvious template values; introduce Osiris tuning only in phase 3.

**Why:** The base must compile and demonstrate integration without making unsafe
claims about another robot. A new season has one clear replacement point that still
resembles Pedro documentation.

## Vendor GoBilda code remains Java

**Decision:** Import the supplied Prism implementation without a Kotlin rewrite.

**Why:** It is third-party code the team does not intend to maintain. Source fidelity
improves upstream comparison and replacement.

## Embedded versus robot-level control

**Decision:** Use hub embedded velocity PIDF for flywheel motor speed and Next
Control for drive/robot-level corrections.

**Why:** Embedded motor loops run close to the encoder and at high frequency; robot
assists combine pose, vision, and multiple axes and belong in the OpMode loop.
