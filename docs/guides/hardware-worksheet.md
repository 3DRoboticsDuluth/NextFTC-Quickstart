# Hardware worksheet

Complete this worksheet with the mechanical and electrical team before declaring
hardware in code. Names are contracts shared by the Control Hub configuration,
TeamCode, tests, and troubleshooting notes.

Copy the table into the season's documentation and replace every placeholder.

| Subsystem | Configuration name | SDK/wrapper type | Hub and port | Physical direction | Init state | Stop state | Verified |
|---|---|---|---|---|---|---|---|
| Drive | `<front-left>` | Pedro mecanum motor | `<hub:port>` | `<forward/reverse>` | zero | zero | no |
| Drive | `<front-right>` | Pedro mecanum motor | `<hub:port>` | `<forward/reverse>` | zero | zero | no |
| Drive | `<back-left>` | Pedro mecanum motor | `<hub:port>` | `<forward/reverse>` | zero | zero | no |
| Drive | `<back-right>` | Pedro mecanum motor | `<hub:port>` | `<forward/reverse>` | zero | zero | no |
| Localization | `<pinpoint>` | GoBilda Pinpoint | `<hub:i2c>` | `<pod directions>` | current pose | n/a | no |
| Example | `<arm>` | `MotorEx` | `<hub:port>` | `<forward/reverse>` | zero | zero | no |

For every motor also record run mode, zero-power behavior, encoder type, current
limits, and whether control is power-, velocity-, or position-based. For every
servo record the safe physical endpoints before assigning a logical `0.0`–`1.0`
range. For every sensor record units, active-high/active-low behavior, expected
range, and failure indication.

## Measurement worksheet

| Measurement | Value | Method/date |
|---|---:|---|
| Robot length | `<in>` | `<method>` |
| Robot width | `<in>` | `<method>` |
| Robot mass | `<kg>` | `<method>` |
| Forward odometry pod Y offset | `<in>` | `<method>` |
| Strafe odometry pod X offset | `<in>` | `<method>` |
| Wheel/encoder details | `<value>` | `<source>` |

## Physical bring-up record

For each output, lift the robot or disconnect the mechanism load, use a deliberately
low command, and record the result. Do not compensate for reversed hardware by
silently swapping driver axes; correct the owning motor/servo/localizer direction.

| Check | Expected | Actual | Pass/date |
|---|---|---|---|
| Positive forward | Robot moves forward |  |  |
| Positive strafe | Robot moves right |  |  |
| Positive turn | Counterclockwise by Pedro convention |  |  |
| Localizer forward/strafe/turn | Pose axes and heading agree |  |  |
| Stop OpMode | Powered actuators immediately safe |  |  |
| Servo endpoint | No binding at either endpoint |  |  |

The completed worksheet is part of the robot's requirements, not disposable setup
notes. Update it whenever hardware or Control Hub configuration changes.
