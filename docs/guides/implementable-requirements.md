# Write requirements someone can implement

A useful robot specification lets another student implement behavior and write
its tests without inventing robot policy. It need not predict every class or make
a one-shot build possible. It must make the important behavior decisions explicit.

## Separate goals, requirements, and design

“Collect a game piece” is a goal. “Releasing the intake button stops the roller”
is a requirement. “Use a singleton subsystem with a delegated instant command”
is an implementation choice governed by this project's architecture.

Write requirements in terms of observable behavior. Record the reason and the
acceptance check. Put necessary design decisions, such as subsystem ownership and
hardware configuration, in the robot architecture page. Link inherited lifecycle
contracts instead of copying them into every mechanism requirement.

Avoid requiring a particular helper or class unless that choice is itself a real
integration constraint. Requirements and architecture together guide the code.

## Describe a behavior completely

For each behavior answer these questions. Use “not applicable” with a reason when
a question truly does not apply; silence leaves the implementer guessing.

| Topic | What to record |
|---|---|
| Trigger and mode | Button edge, held input, sensor event, or autonomous request; when it is accepted |
| Inputs | Hardware name/type, units, sensor polarity, and how unavailable data is handled |
| Preconditions | What must already be true; what happens when it is false |
| State and outputs | Resulting state, motor/servo behavior, limits, and tunable values |
| Completion | When the action ends, its resulting state, and whether it can restart |
| Competing requests | Which request wins; cancellation, replacement, and command ownership |
| Lifecycle | Initialization, Start, interruption, Stop, and reinitialization behavior |
| Failure | Missing hardware, timeout, impossible input, and recovery policy where applicable |
| Diagnostics | Useful current telemetry and significant historical events |
| Acceptance | Given a setup, when an event happens, then a measurable result must follow |

Do not bury these decisions in a sentence such as “handle errors appropriately.”
Specify what happens and how the driver recognizes it. If a sensor cannot detect
a failure, say so rather than promising detection the hardware cannot provide.

Prefer small requirements with stable IDs. A state/transition table is useful for
a mechanism with several modes. It supplements the requirements instead of
becoming a separate, inconsistent behavior specification.

## Connect requirements to tests

For each ID give at least one normal acceptance scenario and the boundary or
failure scenarios that matter. Include input combinations and event order when
they can change the result. State expected output and when it must be observable.

For example: given a running roller, when the release event is processed, its
target and physical motor power are zero before that event handler returns.
This is more implementable than “stops quickly.” A physical stopping distance
would require a separate measured criterion because zero power does not prove
the mechanism has stopped moving.

Link the ID to its owner, implementation, tests, and physical validation once they
exist. Keep planned test scenarios distinct from passing test evidence. See the
[worked roller example](requirements-example.md).

## Review subsystem readiness

A subsystem is ready to implement when another student can answer:

- What inputs and requests exist, and in which modes are they accepted?
- What output follows each request, state transition, and conflicting input?
- What completes or interrupts an action, and what happens afterward?
- What happens on init, Stop, unavailable hardware, and repeated initialization?
- Which component owns each actuator, and what assumptions come from elsewhere?
- Can meaningful tests be derived without inventing a target or expected result?

Mark the specification **proposed**, **ready to implement**, **implemented**, and
**physically verified** separately. A software-ready mechanism can still need
physical calibration. An unresolved behavior decision makes the affected behavior
not ready; do not block unrelated settled work merely because one choice is open.

## Review the whole robot

Complete subsystem specs alone are not a complete robot specification. Record:

- Driver roles, control combinations, initialization selections, and mode changes.
- Hardware configuration, measured dimensions, coordinates, localization, and units.
- Mechanism handoffs: who starts them, what signals completion, what waits, and
  what happens when a receiving mechanism is unavailable or interrupted.
- Arbitration when manual controls, assists, and autonomous request the same resource.
- Autonomous sequence, required selections, entry conditions, failure outcomes,
  and whether Stop immediately stops all powered outputs.
- Which behavior is inherited from the platform and which policy the team chooses.
- Performance goals with acceptance evidence, not unsupported claims.

Use one interaction table or diagram in the architecture page when it clarifies
these relationships. Check for conflicting requirements across subsystem boundaries.

## Keep open decisions actionable

| Open item | Affected requirement | Who decides or measures | What work can proceed | Readiness impact |
|---|---|---|---|---|
| Sensor polarity not measured | Assign an ID | Hardware owner | Write both polarity scenarios | Hardware verification pending |
| Detection should stop or only report? | Assign an ID | Drivers and mechanism owner | Hardware initialization | Behavior not ready |

An agent should ask focused questions for behavior choices and record measured
values as unknown until evidence exists. Parameterized tests can verify use of a
tunable before its physical value is known; deployed settings still need review.
Document the agreed answer and update the tests when a choice changes.

REQ-SCF-012 maps to this guide, the worked example, robot requirements template,
and the documentation skill's readiness review.
