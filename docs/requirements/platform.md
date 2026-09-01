# Reusable platform requirements

The platform is the architecture shared by teams and seasons. It ends before any
Osiris motor name, Decode field coordinate, or season strategy is introduced.

## Modules and dependencies

| ID | Requirement | Why | Acceptance |
|---|---|---|---|
| `REQ-PLT-001` | The build MUST support Kotlin in Android library and TeamCode modules. | Kotlin makes subsystem and command declarations concise enough for student robot code. | Kotlin sources compile in all owned modules. |
| `REQ-PLT-002` | `3drdNextFTC` MUST contain reusable 3DRD extensions around NextFTC, Panels, Pedro Pathing, and FTC hardware. | Shared fixes and conventions should be implemented once across teams. | No `org.firstinspires.ftc.teamcode` dependency exists in the module. |
| `REQ-PLT-004` | `TeamCode` MUST depend on the reusable module and contain robot policy. | Dependency direction must prevent reusable code from importing a season. | Module dependency graph matches [Architecture overview](../architecture/overview.md). |
| `REQ-PLT-005` | NextFTC FTC, hardware, bindings, control, and Pedro extension artifacts MUST be declared centrally enough to reproduce the build. | A fresh clone must resolve the same architecture without manual IDE installation. | Versions are recorded in [Modules and dependencies](../reference/modules-dependencies.md). |

## Lifecycle and commands

| ID | Requirement | Why | Acceptance |
|---|---|---|---|
| `REQ-PLT-010` | Subsystems MUST be discoverable automatically and ordered deterministically. | Adding a subsystem must not create a central registration hotspot. | Discovery finds singleton subsystems; tests verify ordering. |
| `REQ-PLT-011` | Hardware fields MUST initialize before subsystem `initialize()` and a device failure MUST disable only its owning subsystem. | One missing mechanism should not make the entire robot unavailable for diagnosis. | Hardware failure tests verify isolation and disabled telemetry. |
| `REQ-PLT-012` | `initialize()`, `start()`, `periodic()`, `controls()`, and `stop()` MUST have distinct lifecycle meanings. | Explicit phases prevent bindings during init and guarantee a last direct shutdown action. | Component tests verify callback order in Teleop and Auto. |
| `REQ-PLT-013` | Driver bindings MUST be cleared per OpMode and activated only after Teleop start. | Reinitializing an OpMode must not duplicate bindings; Auto must not enable manual robot controls. | Binding and repeated-init tests pass. |
| `REQ-PLT-014` | Subsystems MUST support default commands that are scheduled only when no active command requires the subsystem. | Continuous driving should be declarative and command ownership must prevent conflicts. | Subsystem component tests cover scheduling and `NullCommand`. |
| `REQ-PLT-015` | Delegated instant commands MUST infer `Subsystem.property`, claim the subsystem, skip disabled owners, and log execution. | Students get useful naming and safety without repeating strings or logging calls. | Instant command tests verify naming, requirements, disabled behavior, and logs. |
| `REQ-PLT-016` | Deferred commands MUST construct children at execution time, forward lifecycle calls, and validate child requirements. | Paths and decisions depend on live pose/config, but command ownership must remain predictable. | Deferred command tests cover creation, reuse, failures, and requirements. |
| `REQ-PLT-017` | Reusable composition MUST include sequential NextFTC composition, `alongWith`, and finite repetition. | Autonomous routines should read as a compact chain of intentions. | Composition and repeat tests pass. |

## Configuration and diagnostics

| ID | Requirement | Why | Acceptance |
|---|---|---|---|
| `REQ-PLT-020` | `@Setting` metadata MUST infer Boolean, enum, numeric, and provider-backed menu behavior from declared fields. | Students should add one plainly declared field instead of editing parallel menu tables. | Setting reflection tests cover supported and invalid declarations. |
| `REQ-PLT-021` | Menu order SHOULD follow source declaration order and preserve numeric format, range, increment, and live-edit policy. | Alliance/side and other important choices must appear where students wrote them. | Settings tests verify order and metadata. |
| `REQ-PLT-022` | Configuration MUST persist to Robot Controller storage only after changes, with debounce, and restore on init. | Persistence is convenient, but per-loop JSON serialization creates avoidable pauses. | Persistence tests verify change detection, delay, load, and invalid data handling. |
| `REQ-PLT-023` | Telemetry and logging MUST be separate APIs with independent levels and filters, while a config diagnostics level/filter may feed both. | Repeated live state and historical events have different volume and destinations. | Diagnostics, telemetry, and logging tests pass. |
| `REQ-PLT-024` | Telemetry MUST render structured `CONFIG`, `TEL`, and conditional `LOG` sections to Driver Station and Panels. | Drivers need a readable hierarchy while Panels needs the same current values. | Telemetry tests verify sections, filtering, clearing, and mirroring. |
| `REQ-PLT-025` | Logging MUST write to RobotLog/Logcat and retain a filtered Driver Station history, including command snapshot changes. | Detailed evidence must survive beyond the current telemetry frame without flooding live state. | Logging tests cover levels, filters, history, RobotLog routing, and rebuild. |
| `REQ-PLT-026` | Panels MUST expose configurable values without becoming required for competition operation. | Panels accelerates tuning and visualization, but Driver Station operation must remain complete. | Robot operates with Panels absent; configurables appear when connected. |

## Hardware and pathing

| ID | Requirement | Why | Acceptance |
|---|---|---|---|
| `REQ-PLT-030` | Reusable wrappers MUST cover servo, continuous servo, motor, IMU, and arbitrary FTC hardware devices. | Subsystem hardware should share initialization, fault handling, and test seams. | Wrapper tests pass for each type. |
| `REQ-PLT-031` | Hardware `update` extensions MUST apply an action and then invoke a configurable telemetry hook. | Mechanism code should not repeat mutation plus telemetry boilerplate. | Hardware tests verify action-before-hook behavior. |
| `REQ-PLT-032` | Concrete hardware telemetry policy MUST remain in TeamCode. | Teams may choose different fields and diagnostic levels without forking the shared library. | The reusable module exposes hooks; TeamCode configures `tel()` extensions. |
| `REQ-PLT-033` | The OpMode foundation MUST enable bulk reads, Pedro follower lifecycle, field drawing, configuration, bindings, diagnostics, and subsystem lifecycle. | Every OpMode needs the same correctly ordered base services. | Neutral OpMode tests verify the component list. |
| `REQ-PLT-034` | Reusable drive helpers MUST follow paths and chains, build deferred curves, move by units, turn by angles, wait on typed progress, and stop safely. | Common navigation operations should not be reimplemented in each season. | Drive subsystem tests cover overloads and boundaries. |
| `REQ-PLT-035` | Reusable navigation helpers MUST construct dimension-aware poses using typed distance and angle inputs. | Robot center/front/back/left/right geometry otherwise causes repeated sign and half-width mistakes. | Nav subsystem tests verify all axial/lateral alignments. |
| `REQ-PLT-036` | Pedro helpers MUST provide tile distances, pose transformations, field drawing, driver control, start-pose reset, and typed path/T progress. | These bridge common gaps between Pedro's API and readable FTC intent. | Pedro helper tests pass. |
| `REQ-PLT-037` | Next Control MUST be available for robot-level closed-loop controllers while embedded motor velocity control remains available for high-rate motor loops. | The correct controller depends on where feedback is sampled and applied. | Control dependency resolves; TeamCode controller tests pass. |

## Reusable utilities

| ID | Requirement | Why | Acceptance |
|---|---|---|---|
| `REQ-PLT-042` | A reusable debounce utility MUST detect stable boolean transitions using elapsed time. | Sensors often chatter; behavior should trigger once on a stable edge. | Debounce tests cover timing and reset. |
