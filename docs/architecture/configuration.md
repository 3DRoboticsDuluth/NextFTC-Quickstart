# Configuration

Configuration serves three distinct clients: the Driver Station menu, Panels
configurables, and persisted Robot Controller state. The system keeps one ordinary
singleton object as the source of truth rather than maintaining parallel definitions
or a nested data object.

## Declaring Settings

```kotlin
@Configurable
object Config : ConfigSubsystem() {
    @Setting
    var alliance: Alliance = Alliance.UNKNOWN

    @Setting(inc = 0.5, min = 0.0, max = 30.0, format = "%.1fs")
    var delay: Double = 0.0

    @Setting(live = true)
    var level: Diagnostics.Level = Diagnostics.Level.INFO

    @Transient
    var filter: String = ""
}
```

The build-time `3drdSettingsProcessor` uses KSP to generate a small `Config$SettingOrder`
class beside each settings owner. The menu reads its field names once through the
owner's class loader, then uses reflection to access the real settings. This makes
singleton menu order follow source declarations on Android as well as desktop,
without numeric priorities, a manual list, or changes to Panels discovery. The property's type
selects Boolean, enum, numeric, or provider-backed behavior. Metadata supplies
increment, range, format, whether a started OpMode may edit it, and an optional
dynamic string-options provider.

Because the subsystem is also the settings owner, other robot code can use concise,
live references such as `Config.alliance` or statically import an individual setting.
There is no separate `Config.config` identity for Panels and the Driver Station to
fall out of sync over.

`@Transient` keeps `filter` out of JSON persistence while Panels can still expose
the field. This is useful for temporary diagnostic text.

## Driver Station Editing

Either gamepad can hold Back to enter config editing:

- D-pad up/down selects the previous/next setting;
- D-pad left/right changes the value;
- `>` marks the editable selection;
- `x` marks a selected setting that cannot change after start.

Init-time menu bindings are installed by `ConfigSubsystem.initialize()`, not
`controls()`, because Auto setup must work before Start and does not enable manual
robot controls.

After restoring persisted values and after each Driver Station edit,
`ConfigComponent` refreshes the concrete configuration class in Panels. This makes
the two control surfaces display the same live values without polling or rescanning
configuration every robot loop. A cached compatibility adapter supports both
standard Panels and the Sloth-aware Panels runtime.

## Persistence

`ConfigComponent` loads `{config-class-name}.json` during init. It serializes only
fields marked `@Setting`, so inherited subsystem state and unannotated Panels-only
values cannot accidentally enter the configuration file. A successful menu value
change marks persistence dirty. Periodic calls compare and debounce state;
serialization occurs only after a change has remained eligible, avoiding a JSON
write on every robot loop.

Panels is intentionally not wired into the persistence dirty notification path.
Panels is a debugging tool rather than a competition control surface, so browser
edits are live but are not guaranteed to be written to the Robot Controller JSON.
A future requirement can add that path if persistent Panels edits become important.

## Diagnostics Convention

The reusable `Diagnostics` adapter looks for `level` and `filter` by convention.
No base config interface is required. If the fields are absent or incompatible,
diagnostics safely default off/empty. Field metadata is resolved once and later
reads use cached accessors, so periodic sampling does not repeat field discovery.

The shared config level maps to both telemetry and logging defaults. Panels may
override `Telemetry.LEVEL`, `Telemetry.FILTER`, `Logging.LEVEL`, and `Logging.FILTER`
individually. If an override remains different from the configured value, live
sampling preserves it until it once again matches the configured value.

## Season-Specific Validation

The reusable subsystem does not know about alliance, side, or autonomous strategy
selection. A season may add those settings and validate them before Auto starts.
That policy correctly remains in TeamCode rather than the reusable configuration
library.


## Source-order generation and deployment

Both TeamCode and 3drdNextFTC apply KSP and depend on the JVM-only
`3drdSettingsProcessor` module. Its processor is not packaged in the robot APK.
Each generated Java class depends only on its owner's source file, so moving,
adding or removing settings updates the metadata during normal variant compilation.
The processor also handles nested owners and ignores unannotated properties.
Generated files stay under each module's `build/generated/ksp` directory.

If metadata is absent (for example, an older external library), constructor
parameter ordering remains supported, followed by the remaining reflected fields.
Source order of singleton fields is not promised without generation. The menu
still works; it does not silently disable settings or require a central registry.

The runtime uses the settings owner's class loader rather than a global registry
or process-wide cache. A full install is required when first introducing this
processor or adding/removing settings because the generated class is structural
code. Reordering settings changes its static metadata; use Deploy Full to verify
that change on hardware. Sloth's offline build includes KSP, but a dry-run or a
successful APK build does not prove hot-loaded class replacement or Android menu
ordering. No robot deployment has been performed for this change.

New development computers must run an online full build to cache KSP and its
processor dependencies before the offline Deploy Sloth workflow is used.
