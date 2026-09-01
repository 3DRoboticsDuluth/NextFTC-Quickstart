# 3DRD NextFTC Quickstart

This is the season-neutral starting point for a 3D Robotics Duluth FTC robot. The
documentation is organized first around what a team needs to **do**, followed by
the architecture, requirements, reconstruction details, and exact reference data
that explain and constrain those steps.

## Start here

1. [Start a new season](guides/new-season.md) explains how to create the repository
   and identifies every template value that must be replaced.
2. [Build the first robot](guides/first-robot.md) takes the neutral scaffold through
   measured Pedro constants, Drive/Nav, one mechanism, deployment, and a safe
   physical test.
3. Complete the [hardware worksheet](guides/hardware-worksheet.md) before declaring
   devices, then follow [Add a subsystem](guides/subsystem.md) for each mechanism.

## Understand and reproduce it

- [Architecture](architecture/overview.md) explains how the reusable library,
  TeamCode scaffold, lifecycle, commands, hardware, diagnostics, and pathing fit
  together.
- [Requirements](requirements/index.md) defines the durable **what** and **why** of
  the platform with acceptance criteria.
- [Rebuild](rebuild/index.md) reconstructs the project from the official FTC Robot
  Controller foundation.
- [Reference](reference/modules-dependencies.md) records exact versions, settings,
  decisions, terminology, and upstream resources.

Use GitHub's **Use this template** button when a team wants a new independent
repository with the current files and no inherited commit history. Clone this
repository and change its remote when preserving the full FTC/platform history is
more useful.

## Documentation contract

The Markdown in `docs/` is canonical. The GitHub Pages site is a generated view of
the same files, not a second source of truth. A GitHub wiki is intentionally not
used as the canonical store because wiki content has separate history and review.
If a wiki is enabled, it should contain only a landing page that links here.

Every normative requirement has a stable ID. Architecture pages explain the
reasoning that does not fit cleanly in a requirement table.

## Baseline and scope

- Upstream: `FIRST-Tech-Challenge/FtcRobotController`
- FTC release: v11.2
- Local foundation: `26cd1fdd`
- Seasonal source: Quickstart `main`
- Android/Kotlin modules: `FtcRobotController`, `3drdNextFTC`, and `TeamCode`
- Required quality gate: 100% line and branch coverage in the two owned modules

This documentation does not replace FIRST, NextFTC, Pedro Pathing, or Panels
documentation. It records how and why those systems are assembled here.
