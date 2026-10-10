---
name: document-robot
description: Create or update this FTC team's robot requirements, subsystem architecture, hardware and control descriptions, and validation records from design discussions and repository evidence.
---

# Document the team's robot

Read `docs/project-context.md`, `docs/robot/`, and relevant TeamCode files before
editing. Use `docs/guides/document-your-robot.md` for the team workflow and
`docs/guides/first-robot.md` when drivetrain or localization choices are involved.
For requirement creation or readiness review, read
`docs/guides/implementable-requirements.md` and the teaching example in
`docs/guides/requirements-example.md`. Example policy and numbers are not team decisions.
Resolve these paths from the repository root, three directories above this skill.

Turn the user's design discussion into concrete, student-readable documentation.
Ask about missing decisions that affect the design; work on known facts while
waiting. Label unresolved choices as proposed or not yet recorded. Never invent
team identity, hardware names, tuning, match goals, or measured results.

Keep team requirements under `docs/robot/requirements.md`, using stable team IDs
and observable acceptance checks. Link each to its owning subsystem, code, tests,
and physical evidence when available. Record architecture and subsystem ownership
under `docs/robot/architecture.md`; update the overview and validation pages when
facts change. Explain triggers, controls, lifecycle/Stop behavior, and limitations
where they matter. Separate desktop-tested behavior from physical verification.

Review whether another student could implement each behavior and write its tests
without inventing robot policy. Cover triggers/modes, input meaning and units,
preconditions, state/outputs, completion/restart, competing requests, lifecycle,
Stop/interruption, failure/recovery, diagnostics, and acceptance boundaries.
Check whole-robot handoffs, command/resource arbitration, required selections,
and unavailable receiving subsystems as well as individual mechanisms.

Keep behavior requirements distinct from implementation choices. Derive planned
test scenarios from agreed requirements, including event priority and boundary
cases; never label them as passing tests without evidence. Do not transplant the
worked example's automatic-stop policy into the simpler motor tutorial or team
code unless the user chooses it.

Report readiness by affected subsystem or interaction: proposed, ready to
implement, implemented, or physically verified. List unresolved behavior choices
with affected IDs and focused questions; separately list missing physical
measurements and validation. An unresolved behavior decision blocks only its
affected scope. Parameterized software can be implemented before tuning is
measured when the expected behavior is settled; deployment values remain unknown.

Inherited platform contracts remain under `docs/requirements/` and
`docs/architecture/`. Link to them rather than mixing team policy into reusable
requirements. A documentation task alone does not authorize changing robot code,
deploying, or publishing; respect the user's requested scope.

When asked to make the site about the robot, adapt `docs/index.md` from the robot
overview and retain the Get started and Platform reference navigation. Keep all
Markdown pages in `mkdocs.yml`, then run `mkdocs build --strict`. Report missing
facts and validation limits rather than presenting a draft as a proven design.
