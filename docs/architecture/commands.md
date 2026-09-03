# Commands

The command layer aims for autonomous code that reads as a composition of robot
intent while preserving NextFTC requirements and scheduler behavior.

## Delegated Instant Commands

```kotlin
val open by instant { POS = OPEN }
```

Using `by` lets the command infer `Gate.open`. It also:

- Requires the owning subsystem;
- Prevents execution when the subsystem is disabled;
- Logs `Executed | Gate.open` at debug level;
- Gives command snapshots a useful name without a string literal.

Use an ordinary method when inputs change per call. Use a delegated property for a
stable reusable command.

## Deferred Commands

```kotlin
val toScore by deferred {
    curve(Nav.score.axial(-0.5.tiles), Nav.score)
}
```

A deferred command creates its child in `start()`, not during object construction.
That is essential for paths and decisions that use the current follower pose,
alliance, side, target, or configuration. The wrapper declares requirements up
front, verifies the child does not escape those requirements, forwards lifecycle
methods, and can be reused after stop.

Top-level factory overloads support values that are supplied when a routine is
assembled. Named property delegates still infer the property name.

## Composition Vocabulary

| Expression | Meaning |
|---|---|
| `a.then(b, c)` | Run commands sequentially using NextFTC composition. |
| `a.alongWith(b, c)` | Run commands together in a parallel group. |
| `a.raceWith(b)` | Complete when the first branch completes. |
| `command.times(4)` | Re-run one command lifecycle four times. |
| `command.endAfter(1.5)` | Interrupt the command after the time limit. |
| `thenWait(0.8)` | Insert a readable delay between actions. |

## Requirements Are the Concurrency Contract

Every command that changes a subsystem should require it. Parallel groups are then
able to reject or coordinate conflicting ownership. A deferred command must list
all requirements its future child may use. This is why a complex helper such as
`Auto.deposit()` declares Drive, Intake, Conveyor, Flywheel, Gate, and Vision at
construction even though the exact child is generated later.

## Naming and Logging Boundaries

Named delegated instant commands log execution automatically. Command-manager
snapshots log transitions between idle and running command names. Higher-order
method calls are not automatically inferred from local variable names; Kotlin has
no general `nameof` facility for that use. Names should be added where snapshots
materially improve debugging, not repeated mechanically on every group.

## Failure Behavior

A deferred command refuses to:

- Start twice without stopping;
- Wrap a child already scheduled elsewhere;
- Create a child with undeclared requirements.

If child start fails, internal state is cleared so the wrapper remains reusable.
Stop forwards the interruption flag and always releases the current child.
