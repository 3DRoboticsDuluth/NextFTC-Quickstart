# BIOBUZZ Upgrade Review

This local review batch is on `quickstart/develop`, based on Quickstart
`c0618015`, in the primary Biobuzz checkout. The original Pedro 3 draft is stashed.
No shared history has been rewritten and no team remotes have been pushed.

## Scope

| Component | Before | After |
|---|---|---|
| FTC SDK | 11.2.1 | 12.0.0 / version 12.0 manifest |
| Pedro FTC | 2.0.6 | 2.1.2 |
| Panels | 1.0.12 | 1.0.13 |
| Sloth and Load | 0.2.4 | 0.3.2 |
| Sloth Panels | 0.2.4+1.0.12 | 0.3.2+1.0.13 |
| Kotlin plugin and reflect | 2.4.10 | 2.4.20 |
| Mockito | 5.23.0 | 5.24.0 |
| Gradle | 8.14.4 | 9.1.0 |

NextFTC FTC/hardware 1.1.0, bindings 1.0.1, Control 1.0.0, and Pedro extension
1.0.0 remain in place. Drive-encoder localization remains upstream Pedro 2's
implementation. Pedro 3, its custom localizer/adapters, AutoTune, and NextFTC v2
are outside this batch. Predictive Braking is not enabled by a dependency update;
existing follower configuration is retained.

SDK 12 changes to the manifest and six AprilTag samples are taken directly from
FIRST's tagged release. AprilTag singleton/cluster handling changes in SDK 12;
season repositories with their own vision code need to review those callers.
Gradle 9.1.0 matches the SDK baseline and provides the process exception API
needed by Load 0.3.2; AGP stays 8.13.2 and the Java 17 daemon criteria remain.

## Drive Motor Diagnostics

Drive reports the four motors through the common TeamCode motor `tel` policy.
The collection comes from Pedro’s drivetrain, with configured names
resolved through the hardware map. Drive has no duplicate device declarations or
motor lookups. Reporting does not set power, reset encoders, or change modes. DEBUG shows power, velocity, and encoder position;
VERBOSE adds current, percentage velocity, and RPM. The common motor reporter
now retains raw numbers for Panels graphing, rather than preformatted strings.
These are periodic telemetry snapshots, not historical RobotLog events.

Motor telemetry uses `(this as? Mecanum)?.motors?.tel()` and supports Pedro 2.1.2
`Mecanum`. Other drivetrains, including deprecated `MecanumEx`, Swerve, and custom
implementations, omit motor telemetry without interrupting driving.

## Validation

The required command passes:

```text
gradlew.bat :3drdNextFTC:check :3drdNextFTC:unitTestCoverage :TeamCode:check :TeamCode:unitTestCoverage :TeamCode:assembleDebug
```

Debug and release each pass 127 library tests and 25 TeamCode tests. Both owned
modules retain 100% line and branch coverage (library: 923 lines / 335 branches;
TeamCode: 99 lines / 18 branches). Coverage thresholds and exclusions are unchanged.
Strict MkDocs and `git diff --check` pass. An offline dry run of
`:TeamCode:deploySloth :TeamCode:installDebug` passes with the shared ADB preflight
and full-install overlay removal present. No deployment tasks were executed.

The batch is organized into local commits and remains unpushed. The branch has
no remote upstream configured. Physical validation below is still outstanding.

A full robot installation is required for these dependency and SDK changes.
Confirm Driver Station software, initialization/Stop, motor names and directions,
drive-encoder motion, Teleop, simple paths, Panels configurables/graphs, and Sloth
hot reload after an online full build primes the computer. No robot deployment
has been performed during this review.

## Sources

- [FTC SDK version 12](https://github.com/FIRST-Tech-Challenge/FtcRobotController/tree/v12.0)
- [Pedro releases](https://github.com/Pedro-Pathing/PedroPathing/releases)
- [Sloth installation](https://github.com/Dairy-Foundation/Sloth)
- [Kotlin releases](https://github.com/JetBrains/kotlin/releases)
- [Mockito releases](https://github.com/mockito/mockito/releases)
