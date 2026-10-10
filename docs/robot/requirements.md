# Robot requirements

Write observable goals before choosing code structure. Use a team-specific prefix
such as `REQ-ROBOT`; preserve IDs as requirements change and mark retired ones.
These are robot contracts, separate from inherited platform requirements.

| ID | Required behavior | Reason | Acceptance check | Owning subsystem | Status |
|---|---|---|---|---|---|
| Assign an ID | Describe the agreed behavior | Match or driver need | Measurable pass condition | Choose an owner | Proposed |

For each new requirement record the control or event that triggers it, the result,
limits, and behavior on Stop or missing hardware. Separate an agreed requirement
from a design idea. Avoid inventing performance targets the team has not chosen.

Link the implementation and test paths once they exist. A passing unit test proves
its asserted software behavior; a measured physical result needs its own record
on the [validation page](validation.md).

## Implementation readiness

Use [Write requirements someone can implement](../guides/implementable-requirements.md)
and the [worked example](../guides/requirements-example.md) to expand the table
into behavior contracts. For each behavior record triggers/modes, inputs and
units, preconditions, states and outputs, completion/restart, competing requests,
lifecycle/Stop, failures, diagnostics, and measurable acceptance scenarios.

Record the owner, related requirements, intended code, and planned tests before
implementation. Update those links with actual evidence afterward. A ready spec
lets another student implement and test it without inventing robot policy.

| Scope | Specification status | Unresolved choices | Hardware measurements still needed | Evidence links |
|---|---|---|---|---|
| Name subsystem or interaction | Proposed / ready to implement / implemented / physically verified | Decisions that affect expected behavior | Values and physical checks | Planned or completed evidence |

| Open item | Affected IDs | Decision or measurement owner | Work that can proceed | Readiness impact |
|---|---|---|---|---|
| State the unknown | Stable IDs | Team role | Settled independent behavior | Behavior blocked or hardware verification pending |

Review interactions too: driver/autonomous arbitration, mechanism handoffs,
required configuration, unavailable receivers, interruption, and whole-robot Stop.
Do not treat a collection of complete subsystem specs as proof those interactions
are defined. Keep unknown tuning visibly unknown; do not invent deployed values.
