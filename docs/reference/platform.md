# Platform reference

This section explains and reconstructs the inherited starter. For daily team work,
use [Get started](../index.md) and [Our robot](../robot/index.md).

- [Architecture](../architecture/overview.md): module ownership, lifecycle,
  commands, hardware, configuration, diagnostics, and navigation.
- [Requirements](../requirements/index.md): stable foundation, reusable-platform,
  and neutral-scaffold contracts with acceptance criteria.
- [Reconstruction](../rebuild/index.md): rebuild from the official FTC foundation.
- [Verification](../rebuild/verification.md): desktop gates and physical validation.
- [Dependencies](modules-dependencies.md), [decisions](decisions.md), and
  [upstream resources](upstream.md): exact integration facts and rationale.

## Baseline

The project preserves FTC Robot Controller v11.2 history and adopts SDK 12,
Pedro 2.1.2, and NextFTC v1 in the first upgrade track. Its Android modules are
`FtcRobotController`, `3drdNextFTC`, and `TeamCode`. The neutral scaffold defaults
to mecanum with drive-encoder localization and untuned template dimensions.
It is a starting point, not calibrated robot code. Pedro 3 and NextFTC v2 remain
separate future evaluations. See the [upgrade review](season-upgrades-review.md).

The team repository retains Quickstart as a remote and preserves its history.
Follow the [new-season guide](../guides/new-season.md) when incorporating fixes.
This reference complements FIRST and library documentation; it records the
choices and contracts specific to this project.
