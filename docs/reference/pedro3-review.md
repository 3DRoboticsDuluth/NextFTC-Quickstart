# Pedro 3 migration

This branch upgrades Quickstart develop to Pedro REV Hub/core 3.0.1 and AutoTune
runtime 1.0.1. FTC SDK 12, Gradle 9.1, Sloth/Load 0.3.2, Panels 1.0.13, Kotlin
2.4.20, Mockito 5.24.0, and NextFTC v1 remain the baseline. NextFTC v2 is separate.

## Compatibility changes

Pedro 3 replaces the Pedro 2 follower builder, mutable pose API, path builders,
and command extension. The incompatible NextFTC Pedro extension is removed;
reusable NextFTC v1 adapters now own follower lifecycle, manual driving, follow,
hold, directed turns, and whole-path progress. Native path lambdas return `Path`;
compound paths use `Paths.path(...)`. Pose accessors are methods and headings normalize.
`follow(..., maxPower)` is removed: Pedro 3 speed constraints have a different contract.
Distance and completion waits account for compound-path segments.

Four-drive-encoder localization remains available through the reusable
`DriveEncoderLocalizer` and `DriveEncoderConfig`, preserving Pedro 2's sum-based
conversion factors. Tank arcade keeps paired slots and robot-centric zero-strafe
input; general differential autonomous path following is not established.

`MecanumDrive` extends Pedro's Mecanum and reads its private cached motor array
once during construction. Field metadata and raw references are cached; telemetry
performs no reflection, motor lookup, or output change each cycle. This adapter is
pinned to Pedro 3.0.1's internal field shape and is tested against its real class.
Other drivetrains omit motor telemetry safely. TeamCode retains telemetry policy.

## Tuning and validation

Foresight is a new tuning model. Zero controller gains and placeholder velocity,
braking, dimensions, and encoder factors are not robot calibration. Measure and
tune before autonomous use; do not transplant old follower gains.

The AutoTune artifact supplies a runtime, not registered tuning procedures.
Bring the appropriate procedure sources from Pedro's Quickstart into a deliberate
team tuning workflow, preserving upstream Java, and configure them for the selected
localizer. Dedicated odometry remains replaceable through native Pedro 3 localizers.

Desktop tests cover lifecycle, init localization, pose preservation at Start,
manual centric modes and heading offsets, path construction and progress, endpoint
holding, turns, cancellation, immediate Stop, motor access, and paired tank math.
Physical validation remains outstanding: lifted-wheel directions/Stop, measured
pose/distance/heading, tuning, simple and compound paths, Panels, and full install.
Re-prime each computer with an online full install before offline Sloth use.
No deployment or physical robot validation is performed by this migration.

## Sources

- [Pedro 3.0.1 source](https://github.com/Pedro-Pathing/PedroPathing/tree/v3.0.1)
- [Pedro tuning](https://pedropathing.com/docs/pathing/tuning)
- [Pedro Quickstart](https://github.com/Pedro-Pathing/Quickstart)

REQ-PLT-033 and REQ-PLT-036 map to lifecycle and command tests; REQ-PLT-038
maps to localizer and constants tests; REQ-PLT-043 maps to cached motor access and
read-only telemetry tests. REQ-SCF-011 maps to tank setup tests and its guide.
