# Project context

This page is the concise handoff for mentors, contributors, and coding agents. The
linked pages are canonical when more detail is needed.

## Purpose

The repository begins with FTC Robot Controller v11.2 and adds a reusable 3DRD
Kotlin/NextFTC platform plus a neutral TeamCode scaffold. Current `main` is the
season template. Decode/Osiris behavior and Quanomous remain in the separate
`LeastOne/NextFTC` implementation repository.

## Read first

- [Start a new season](guides/new-season.md)
- [Build the first robot](guides/first-robot.md)
- [Architecture overview](architecture/overview.md)
- [Requirements](requirements/index.md)
- [Rebuild guide](rebuild/index.md)
- [Architectural decisions](reference/decisions.md)

## Non-negotiable boundaries

- Reusable NextFTC behavior belongs in `3drdNextFTC`.
- Hardware names, constants, game concepts, controls, mechanisms, vision, and
  strategy belong in TeamCode.
- Telemetry is current state; logging is event history.
- Robot controls activate only after Teleop Start; config menu works during init.
- Hardware failures isolate their subsystem; stop hooks directly safe actuators.
- Commands claim requirements; deferred commands declare all possible child owners.
- Owned production code maintains 100% line and branch coverage.

## Style

Prefer concise, readable Kotlin and established local patterns. Keep comparable
members visually consistent; avoid clever one-liners that hide lifecycle or command
ownership. Use typed units for public distance/angle/path-progress APIs. Use
qualified subsystem names for common verbs in coordinating code.

## Completion

Run the exact command in [Verification](rebuild/verification.md). Keep tests with
the behavior they test, organize commits by conceptual dependency, and update the
requirement/architecture/rebuild/traceability documentation when behavior changes.
