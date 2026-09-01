# Settings

## Quickstart Driver Station Settings

Settings appear in source declaration order.

| Setting | Type/default | Edit policy | Purpose |
|---|---|---|---|
| Robot Centric | `true` Boolean | Live | Chooses robot-centric or field-centric driver control. |
| Level | `INFO` enum | Live | Sets the shared default telemetry/log display level. |

The scaffold intentionally contains no game, robot, or autonomous-strategy
settings. Add only the settings the new season actually requires, keeping required
init-time selections first.

`filter` is transient and not a Driver Station `@Setting`. Panels may edit it as a
temporary shared substring filter without persisting it into the next run.

## `@Setting` Fields

| Attribute | Default | Meaning |
|---|---|---|
| `name` | Humanized property name | Optional display-name override |
| `inc` | `NaN` | Numeric increment; a supported default is inferred when absent |
| `min` | negative infinity | Numeric lower bound |
| `max` | positive infinity | Numeric upper bound |
| `format` | empty | `String.format` pattern used for display |
| `live` | `false` | Whether Driver Station may change it after Start |
| `options` | none | Class/object exposing `options(): List<String>` for a string setting |

Supported setting types are Boolean, enum, numeric fields supported by the
reflection factory, and string fields with an options provider. Invalid increments,
ranges, empty enums/providers, and unsupported types fail explicitly.

## Panels Configurables

Panels also exposes `@Configurable` objects such as mechanism tunables, `Logging`,
and `Telemetry`. Those values are debugging/tuning surfaces, not automatically
Driver Station menu settings or persisted config. This distinction is deliberate:
competition setup remains operable from the Driver Station.
