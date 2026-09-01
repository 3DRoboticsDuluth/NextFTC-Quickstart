# Settings

## Decode Driver Station settings

Settings appear in source declaration order.

| Setting | Type/default | Edit policy | Purpose |
|---|---|---|---|
| Alliance | `UNKNOWN` enum | Init only | Selects red/blue field mirroring and required Auto setup. |
| Side | `UNKNOWN` enum | Init only | Selects north/south start and required Auto setup. |
| Quanomous | stored program name / null | Init only | Selects required Auto strategy from reusable options provider. |
| Delay | `0.0`, 0.5 s increments, 0–30 s | Init only | Delays autonomous command execution. |
| Responsiveness | `1.00`, 0.05 increments, 0–1 | Live | Retained season tuning/config value. |
| Robot Centric | `true` | Live | Chooses robot-centric versus field-centric driver control. |
| Park Gate | `false` | Init only | Supplies the default parking strategy option. |
| Goal Distance Offset South | `0.0 in`, 6-inch increments | Live | Corrects south-side launch distance. |
| Goal Distance Offset North | `0.0 in`, 6-inch increments | Live | Corrects north-side launch distance. |
| Goal Angle Offset South | `0.0 deg`, 1-degree increments | Live | Corrects south-side goal heading. |
| Goal Angle Offset North | `0.0 deg`, 1-degree increments | Live | Corrects north-side goal heading. |
| Level | `INFO` enum | Live | Sets the shared default telemetry/log display level. |

`filter` is transient and not a Driver Station `@Setting`. Panels may edit it as a
temporary shared substring filter without persisting it into the next run.

## `@Setting` fields

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

## Panels configurables

Panels also exposes `@Configurable` objects such as mechanism tunables, `Logging`,
and `Telemetry`. Those values are debugging/tuning surfaces, not automatically
Driver Station menu settings or persisted config. This distinction is deliberate:
competition setup remains operable from the Driver Station.
