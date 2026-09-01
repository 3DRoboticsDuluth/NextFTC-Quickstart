# Seasonal scaffold requirements

The neutral `TeamCode` scaffold proves that the reusable platform can support a
new season before a mechanism, field, or real hardware constant is known.

| ID | Requirement | Why | Acceptance |
|---|---|---|---|
| `REQ-SCF-001` | A shared abstract `OpMode` MUST compose telemetry, bindings, bulk reads, Pedro, drawing, configuration, and all discovered subsystems. | Teleop and Auto should differ only in their behavior, not their infrastructure. | Neutral `OpModeTests` verify components. |
| `REQ-SCF-002` | Empty `Teleop` and `Auto` classes MUST be FTC-discoverable and compile without robot mechanisms. | A new season needs a deployable starting point from day one. | `TeamCode:assembleDebug` passes at the base tag. |
| `REQ-SCF-003` | Pedro constants MUST be structurally valid but MUST use documented neutral/template dimensions and defaults. | The base must compile without pretending template values are safe for a robot. | Constants state that they must be replaced; no Osiris device name or tuning is present. |
| `REQ-SCF-004` | The neutral config MUST include shared diagnostic level and filter behavior without season selections. | Diagnostics are platform behavior; alliance, side, and strategy are game behavior. | Only reusable diagnostics settings exist at the base tag. |
| `REQ-SCF-005` | A timing subsystem SHOULD demonstrate reusable subsystem discovery, loop telemetry, and a clearly documented template driver alert. | The scaffold needs a small, hardware-free subsystem that proves lifecycle operation, while alert timing remains season policy. | Timing tests and neutral OpMode tests pass; the 75-second threshold is identified as a value each season must review. |
| `REQ-SCF-006` | TeamCode MUST install its chosen hardware telemetry callbacks during OpMode construction. | Hardware wrappers remain reusable while each team controls visible diagnostic detail. | The scaffold's `configureHardwareTelemetry()` is called before components run. |
| `REQ-SCF-007` | Repository IDE conventions, dictionary, and agent guidance MUST be present at the seasonal endpoint. | New teams should inherit the same readable Kotlin, spelling, test, and history conventions. | `AGENTS.md` and committed `.idea` inspection files exist. |

The annotated `reusable-season-base` tag marks the commit that satisfies these
requirements. Its full mandated build and coverage verification passed before the
tag was published.
