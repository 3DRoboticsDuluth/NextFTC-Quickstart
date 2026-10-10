# Robot validation

Record evidence as the robot develops. Distinguish planned, desktop-tested, and
physically verified behavior; a build success is not a robot driving test.

| Date | Requirement or milestone | Setup and revision | Expected result | Observed result | Status and next action |
|---|---|---|---|---|---|
| Record date | Stable ID or named milestone | Hardware, configuration, code revision | Measurable target | Actual measurement | Planned / passed / failed |

Start with motor directions and low-power driver control, then measured straight
travel and heading, mechanism limits, Stop behavior, and autonomous trials.
Record the surface, loads, power limits, and selected localization hardware so
another student can reproduce a result. Keep failed tests and unresolved issues.

Link desktop test reports or commands separately from physical observations.
Drive-encoder localization cannot measure sideways skid; dedicated odometry also
requires measurement and calibration. Record these practical limits for drivers.
