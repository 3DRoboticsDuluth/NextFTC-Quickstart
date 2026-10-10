# Start your robot season

Welcome to your team's robot repository. Begin with a small working robot, then
make this site your team's handbook as the robot develops.

## Get started

1. [Create the season repository](guides/new-season.md) and prove the baseline.
2. [Record the hardware](guides/hardware-worksheet.md): names, ports, directions,
   and safe outputs before writing mechanism code.
3. [Configure the first robot](guides/first-robot.md). Start with the
   included mecanum scaffold and its localization configuration.
4. [Build and test the first robot](guides/first-robot.md).
5. Add a [servo subsystem](guides/subsystem.md) or a
   [motor and sensor subsystem](guides/motor-subsystem.md), one mechanism at a time.
6. [Deploy](guides/deployment.md) and record what actually worked on hardware.

New to Kotlin? Start with [Why Kotlin?](guides/why-kotlin.md).

## Make this your robot's handbook

[Our robot](robot/index.md) is an editable starting place for your team's design.
Record the [requirements](robot/requirements.md), [subsystems and architecture](robot/architecture.md),
and [validation results](robot/validation.md) alongside the implementation.
Unknown facts should stay visibly unknown until the team decides or measures them.

When your team has a robot overview, replace this page with that overview and
keep the Get started links available in navigation. Update the site title in
`mkdocs.yml` and the repository README to identify your team, season, and robot.
The [documentation workflow](guides/document-your-robot.md) includes an agent skill
that helps students develop these pages from design conversations and code.

## Platform reference

The reusable starter still has a complete [platform reference](reference/platform.md):
architecture, stable requirements, reconstruction steps, and dependency details.
Use it when explaining an inherited behavior or rebuilding the platform.
Your robot's requirements belong under Our robot; platform contracts retain their
existing IDs and remain separate.

Markdown in `docs/` is the source of truth. The generated site presents those same
files; document changes belong in code review with their associated implementation.
