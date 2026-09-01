# Glossary

**3DRD**
: 3D Robotics Duluth, the organization maintaining this platform.

**Adaptation**
: A bridge around a third-party API or vendor implementation. Reusable adaptations
  live in a library; team-selectable policy may remain in TeamCode.

**Command requirement**
: A subsystem/resource claimed by a command so the scheduler can prevent conflicting
  concurrent behavior.

**Component**
: A NextFTC lifecycle participant added to an OpMode.

**Deferred command**
: A reusable wrapper that creates its child when execution begins, using current
  pose/config/sensor state.

**Diagnostics**
: The shared convention that supplies level/filter defaults to telemetry and logging.

**Field-centric**
: Drive inputs interpreted relative to the field rather than robot heading.

**Hardware wrapper**
: A lazy, testable 3DRD/NextFTC device abstraction participating in subsystem
  initialization and telemetry hooks.

**Panels**
: The ByLazar web dashboard used for FTC tuning, configurables, telemetry, camera,
  and field visualization. It is not required as a competition control surface.

**Pedro Pathing**
: The FTC localization, path generation, follower, and field visualization library
used by Drive and Nav.

**Quanomous**
: 3DRD's stored autonomous program format and compiler approach. Strategy data is
translated into the current season's commands by TeamCode.

**Robot-centric**
: Drive inputs interpreted in the robot's current frame.

**Seasonal base**
: The verified neutral endpoint tagged `reusable-season-base`, suitable for branching
a different robot/season.

**Subsystem**
: A cohesive owner of robot state, hardware, commands, controls, periodic behavior,
and shutdown behavior.

**Telemetry**
: Replaceable current state displayed on Driver Station and Panels.

**RobotLog / Logcat**
: Persistent Android event output used for historical debugging.

**T value**
: Pedro's parameter along a path segment/curve; not necessarily equal to traveled
distance percentage.
