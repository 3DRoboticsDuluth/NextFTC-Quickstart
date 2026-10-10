# Robot architecture

Describe your actual robot rather than copying a previous season's mechanisms.
List the drive, localization, game mechanisms, sensors, and autonomous coordinator.

| Subsystem | Responsibility | Hardware/configuration names | Commands and controls | Requirements | Code and tests |
|---|---|---|---|---|---|
| Choose an owner | One clear responsibility | From the hardware worksheet | User-visible actions | Stable IDs | Repository paths |

Explain how subsystems cooperate and which command owns each powered mechanism.
Record initialization and Stop behavior, sensor interpretation, coordinate axes,
and configuration settings that drivers select. Add a simple diagram when it
clarifies the movement of game pieces or the interactions between mechanisms.

Document the selected [drivetrain and localizer](../guides/first-robot.md), measured
geometry, tuning source, and limitations. Include the driver controls and
what the autonomous entry point calls. Keep hardware names and game policy in
TeamCode; link to [platform architecture](../architecture/overview.md) for inherited
lifecycle and scheduling behavior instead of duplicating those contracts here.

## Interactions between subsystems

| Requesting subsystem or mode | Receiving owner/resource | Request and preconditions | Completion signal | Interruption or unavailable behavior | Requirement IDs |
|---|---|---|---|---|---|
| Name requester | One owner of output | Agreed interface | Observable completion | Explicit outcome | Stable IDs |

Document manual/assist/autonomous priority, game-piece handoffs, initialization
selections, and Stop across owners. Review these with the
[implementation-readiness guide](../guides/implementable-requirements.md).
