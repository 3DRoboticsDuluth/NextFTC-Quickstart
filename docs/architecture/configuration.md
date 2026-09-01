# Configuration

Configuration serves three distinct clients: the Driver Station menu, Panels
configurables, and persisted Robot Controller state. The system keeps one ordinary
data object as the source of truth rather than maintaining parallel definitions.

## Declaring settings

```kotlin
data class Config(
    @Setting
    var alliance: Alliance = Alliance.UNKNOWN,

    @Setting(inc = 0.5, min = 0.0, max = 30.0, format = "%.1fs")
    var delay: Double = 0.0,

    @Setting(live = true)
    var level: Diagnostics.Level = Diagnostics.Level.INFO,

    @Transient
    var filter: String = ""
)
```

Reflection finds `@Setting` fields in source declaration order. The property's type
selects Boolean, enum, numeric, or provider-backed behavior. Metadata supplies
increment, range, format, whether a started OpMode may edit it, and an optional
dynamic string-options provider.

`@Transient` keeps `filter` out of JSON persistence while Panels can still expose
the field. This is useful for temporary diagnostic text.

## Driver Station editing

Either gamepad can hold Back to enter config editing:

- D-pad up/down selects the previous/next setting;
- D-pad left/right changes the value;
- `>` marks the editable selection;
- `x` marks a selected setting that cannot change after start.

Init-time menu bindings are installed by `ConfigSubsystem.initialize()`, not
`controls()`, because Auto setup must work before Start and does not enable manual
robot controls.

## Persistence

`ConfigComponent` loads `{config-class-name}.json` during init. A successful menu
value change marks persistence dirty. Periodic calls compare and debounce state;
serialization occurs only after a change has remained eligible, avoiding a JSON
write on every robot loop.

Panels is intentionally not wired into the dirty notification path. Panels is a
debugging tool rather than a competition control surface. A future requirement can
add that path if persistent Panels edits become important.

## Diagnostics convention

The reusable `Diagnostics` adapter looks for `level` and `filter` by convention.
No base config interface is required. If the fields are absent or incompatible,
diagnostics safely default off/empty. Field metadata is resolved once and later
reads use cached accessors, so periodic sampling does not repeat field discovery.

The shared config level maps to both telemetry and logging defaults. Panels may
override `Telemetry.LEVEL`, `Telemetry.FILTER`, `Logging.LEVEL`, and `Logging.FILTER`
individually. If an override remains different from the configured value, live
sampling preserves it until it once again matches the configured value.

## Season-specific validation

The reusable subsystem does not know about alliance, side, or autonomous strategy
selection. A season may add those settings and validate them before Auto starts.
That policy correctly remains in TeamCode rather than the reusable configuration
library.
