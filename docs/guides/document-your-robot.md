# Document your robot

Use [Our robot](../robot/index.md) as living design documentation. Before building
a mechanism, agree on its required behavior and acceptance checks. Then document
its subsystem owner, hardware, commands, controls, and safe Stop behavior. Update
the pages and meaningful tests when the implementation changes.

## Requirements that guide implementation

Follow [Write requirements someone can implement](implementable-requirements.md)
and the [worked roller-and-sensor specification](requirements-example.md). The
readiness standard is that another student can implement behavior and write tests
without inventing robot policy. Include whole-robot handoffs and resource conflicts,
not only standalone subsystem behavior.

## A useful review loop

1. Describe the match task and assign a stable robot requirement ID.
2. Decide which subsystem owns it and record the hardware configuration.
3. Implement the smallest complete behavior with tests.
4. Record desktop results and physical measurements separately.
5. Review the requirement, architecture, code, and evidence together.

Students can explain a design aloud or in rough notes. An agent can help organize
those notes, but measurements and team decisions must come from the team.

## Repository agent skill

The repository includes `.agents/skills/document-robot/SKILL.md`. In an agent that
supports repository skills, invoke `$document-robot`, for example:

> Use $document-robot to document our intake. It has one roller motor and a digital
> sensor. Ask about missing behavior, then update our robot requirements and architecture.

The skill reads the robot pages and relevant code, records unknowns explicitly,
keeps robot contracts separate from platform contracts, and checks documentation.
It also reviews implementation readiness and identifies unresolved behavior
choices separately from physical measurements that are still needed.
For an agent without skill discovery, ask it to read that file and follow the
workflow. It does not grant deployment or publishing authorization.

## Make the home page your robot

When the team overview is useful, move its content into `docs/index.md` and replace
Get started navigation's home link with the start-a-season guide. Keep the guides
and Platform reference available; the inherited rebuild contract remains valuable.
Update `site_name`, repository links, and the README for your team. Keep every
Markdown page in the explicit `mkdocs.yml` navigation and run `mkdocs build --strict`.
