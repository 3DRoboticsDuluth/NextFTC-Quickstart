# 3drdNextFTC

Reusable NextFTC foundations shared by 3D Robotics Duluth teams.

This Android library contains generic command, configuration, hardware, logging,
telemetry, subsystem, and Pedro Pathing integrations. Robot hardware tuning,
field geometry, autonomous routines, and season-specific subsystems belong in
`TeamCode`.

The module is currently consumed as an in-repository Gradle dependency while
its public API is validated across robot projects.

See the canonical [reusable platform requirements](../docs/requirements/platform.md),
[architecture overview](../docs/architecture/overview.md), and
[phase-2 reconstruction guide](../docs/rebuild/phase-2.md) for the module contract
and design rationale.
