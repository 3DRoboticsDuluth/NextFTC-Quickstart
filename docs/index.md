# 3DRD NextFTC

This documentation defines **what** the repository must do, **why** each major
capability exists, and **how** to reconstruct the project from the official FTC
Robot Controller foundation.

This commit is the reusable seasonal endpoint. The annotated tag
`reusable-season-base` identifies this neutral starting point for a new robot and
FTC season.

## Choose a path

- **Understand the contract:** begin with [Requirements](requirements/index.md).
- **Learn how the pieces work:** read [Architecture](architecture/overview.md).
- **Recreate the repository:** follow [Rebuild](rebuild/index.md).
- **Start next season:** use [Start a new season](guides/new-season.md).
- **Build the first robot:** follow the end-to-end [first robot](guides/first-robot.md)
  walkthrough and [hardware worksheet](guides/hardware-worksheet.md).
- **Add robot behavior:** use [Add a subsystem](guides/subsystem.md).
- **Find exact values:** use the [Reference](reference/modules-dependencies.md).

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
- Reusable-base tag: `reusable-season-base`
- Android/Kotlin modules: `FtcRobotController`, `3drdNextFTC`,
  `3drdQuanomous`, and `TeamCode`
- Required quality gate: 100% line and branch coverage in the three owned modules

This documentation does not replace FIRST, NextFTC, Pedro Pathing, or Panels
documentation. It records how and why those systems are assembled here.
