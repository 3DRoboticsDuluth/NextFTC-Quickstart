# Project Context

This page is the concise handoff for mentors, contributors, and coding agents. The
linked pages are canonical when more detail is needed.

## Purpose

The repository begins with FTC Robot Controller v11.2 and adds a reusable 3DRD
Kotlin/NextFTC platform plus a neutral TeamCode scaffold. This branch builds on
Quickstart develop's FTC SDK 12, Panels/Sloth, Kotlin, Mockito, and telemetry
upgrades, migrating Pedro to REV Hub/core 3.0.1 with reusable NextFTC v1 adapters
and retained drive-encoder localization. NextFTC v2 remains a separate evaluation.
Quickstart `main` remains the published season template. Decode/Osiris behavior and Quanomous remain in the separate
`LeastOne/NextFTC` implementation repository.

## Read First

- [Get started](index.md)
- [Our robot templates](robot/index.md)
- [Choose drivetrain and localization](guides/drivetrain.md)
- [Document your robot](guides/document-your-robot.md)
- [Start a new season](guides/new-season.md)
- [Why Kotlin?](guides/why-kotlin.md)
- [Build the first robot](guides/first-robot.md)
- [Architecture overview](architecture/overview.md)
- [Requirements](requirements/index.md)
- [Rebuild guide](rebuild/index.md)
- [Architectural decisions](reference/decisions.md)

## Non-Negotiable Boundaries

- Reusable NextFTC behavior belongs in `3drdNextFTC`.
- Hardware names, constants, game concepts, controls, mechanisms, vision, and
  strategy belong in TeamCode.
- Telemetry is current state; logging is event history.
- Robot controls activate only after Teleop Start; config menu works during init.
- Hardware failures isolate their subsystem; stop hooks directly safe actuators.
- Commands claim requirements; deferred commands declare all possible child owners.
- Deploy Sloth accelerates ordinary TeamCode iteration and runs Gradle offline after
  an online full build has primed the computer; Deploy Full removes the overlay and
  remains the authoritative release/competition validation. Both prove
  and, when necessary, repair the configured Robot Controller ADB connection first.
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

## Team-facing documentation

Navigation separates Get started, editable Our robot pages, and Platform reference.
Use `.agents/skills/document-robot/SKILL.md` to assist team documentation. The
neutral scaffold remains mecanum. The tank/arcade guide reviews the earlier paired
motor mapping against Pedro 3.0.1 and the reusable localizer adapter, requires robot-centric zero-strafe input, and
records desktop evidence separately from the coach's earlier driving report.
Current upgraded tank hardware, dedicated odometry, and differential autonomous
behavior still require physical validation.

Robot requirements follow the implementation-readiness guide and worked example:
expected behavior, test scenarios, and whole-robot interactions must be explicit.
The documentation skill reports unresolved policy separately from missing hardware
measurements. Teaching examples are not implemented robot features.
