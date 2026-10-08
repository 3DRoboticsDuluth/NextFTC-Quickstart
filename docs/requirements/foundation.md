# FTC Foundation Requirements

The foundation preserves the official FTC v11.2 history and applies only the
small, generally useful changes needed before adding Kotlin or third-party robot
architecture.

| ID | Requirement | Why | Acceptance |
|---|---|---|---|
| `REQ-FND-001` | The project MUST retain the upstream FTC Robot Controller v11.2 history and module structure. | FIRST's samples, manifests, packaging, and release lineage remain auditable and updateable. | `26cd1fdd` is present as the foundation commit. |
| `REQ-FND-002` | Gradle MUST select a compatible daemon JVM through the Foojay toolchain resolver and committed daemon criteria. | Android Studio upgrades must not silently switch Gradle to an incompatible JDK. | Gradle sync and `gradlew --version` succeed on a clean machine. |
| `REQ-FND-003` | Expected upstream source/target and 16 KiB alignment warnings SHOULD be suppressed in repository-owned configuration without hiding unrelated warnings. | Known non-actionable warnings distract students from actionable build failures. | Build output omits the targeted warnings; lint remains enabled. |
| `REQ-FND-004` | Common ADB server and robot connection operations MUST be available as repository-root scripts for Windows and Linux, backed by platform-specific ADB discovery wrappers. | Mentors and students need repeatable operations without memorizing ADB syntax, assuming a single SDK location, or changing operating systems. | Run the `.cmd` helpers on Windows and the corresponding executable `.sh` helpers on Linux; invoke `scripts/adb.cmd version` and `scripts/adb.sh version` to verify discovery. |
| `REQ-FND-005` | The upstream TeamCode placeholder README MUST be removed once real TeamCode is introduced. | Android Studio should expose the actual project structure without an obsolete instructional file. | The placeholder is absent from TeamCode. |
| `REQ-FND-006` | The BIOBUZZ upgrade MUST use FTC SDK 12.0.0 with the upstream version 12 manifest and AprilTag sample API changes while preserving earlier history. | The seasonal foundation must expose the game tag API and compile the supplied examples. | SDK artifacts resolve, upstream samples compile, and the full APK assembles; robot and Driver Station validation follow. |

## Boundary

This phase intentionally does **not** add Kotlin, NextFTC, Panels, Pedro Pathing,
or 3DRD modules. Those changes belong to the reusable platform so the FTC
foundation remains easy to compare with upstream.
