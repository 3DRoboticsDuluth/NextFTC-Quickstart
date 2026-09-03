# Seasonal Scaffold Requirements

The neutral `TeamCode` scaffold proves that the reusable platform can support a
new season before a mechanism, field, or real hardware constant is known.

| ID | Requirement | Why | Acceptance |
|---|---|---|---|
| `REQ-SCF-001` | A shared abstract `OpMode` MUST compose telemetry, bindings, bulk reads, Pedro, drawing, configuration, and all discovered subsystems. | Teleop and Auto should differ only in their behavior, not their infrastructure. | Neutral `OpModeTests` verify components. |
| `REQ-SCF-002` | `Teleop` and `Auto` classes MUST be FTC-discoverable and compile without robot mechanisms. | A new season needs a deployable starting point from day one. | `TeamCode:assembleDebug` passes on Quickstart `main`. |
| `REQ-SCF-003` | Pedro constants MUST be structurally valid but MUST use documented neutral/template dimensions and defaults. The default localizer MUST require no dedicated localization device. | The base must compile without pretending template values are safe for a robot or requiring Pinpoint hardware. | Constants use Pedro's drivetrain-encoder localizer, state that all values must be replaced/tuned, and contain no Osiris device name or tuning. |
| `REQ-SCF-004` | The neutral config MUST include robot-centric mode plus shared diagnostic level and filter behavior without season selections. | Basic driving and diagnostics are platform behavior; alliance, side, and strategy are game behavior. | Only reusable driving/diagnostics settings exist on Quickstart `main`. |
| `REQ-SCF-005` | A timing subsystem SHOULD demonstrate reusable subsystem discovery and loop telemetry without game-specific alerts. | The scaffold needs a small, hardware-free subsystem that proves lifecycle operation; endgame warnings are season policy. | Timing tests and neutral OpMode tests pass, and Timing contains no gamepad or endgame behavior. |
| `REQ-SCF-006` | TeamCode MUST install its chosen hardware telemetry callbacks during OpMode construction. | Hardware wrappers remain reusable while each team controls visible diagnostic detail. | The scaffold's `configureHardwareTelemetry()` is called before components run. |
| `REQ-SCF-007` | Repository IDE conventions, dictionary, and agent guidance MUST be present at the seasonal endpoint. | New teams should inherit the same readable Kotlin, spelling, test, and history conventions. | `AGENTS.md` and committed `.idea` inspection files exist. |
| `REQ-SCF-008` | TeamCode MUST include basic concrete Drive and Nav implementations built on the reusable subsystems. | A Quickstart should be ready to customize rather than require every team to reconstruct the same drivetrain foundation. | Dedicated Drive/Nav tests verify inputs, commands, default ownership, telemetry, dimensions, and typed poses. |
| `REQ-SCF-009` | The neutral scaffold MUST include a small Auto subsystem that initializes a documented start pose and exposes Drive's deferred command to a documented end pose; the Auto OpMode MUST schedule it only after Start. | Teams need one complete, observable example of connecting Nav, Drive, command ownership, and the OpMode lifecycle without redundant deferred layers. | Auto subsystem and OpMode tests verify autonomous-only pose initialization, the exposed Drive command's requirements, and scheduling. |

Quickstart `main` is the maintained endpoint that satisfies these requirements. Its
full mandated build and coverage verification must pass before changes are
published.
