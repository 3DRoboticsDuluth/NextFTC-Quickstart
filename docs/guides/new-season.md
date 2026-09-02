# Start a New Season

Quickstart `main` is the verified season-neutral source. Preserve its history and
remote relationship so later Quickstart fixes can be incorporated cleanly.

## Create the Repository

Create an empty season repository on GitHub, then clone Quickstart. Rename its
original remote to `quickstart`, add the season repository as `origin`, and publish
the inherited history:

```powershell
git clone https://github.com/3DRoboticsDuluth/NextFTC-Quickstart.git <season-repository>
cd <season-repository>
git remote rename origin quickstart
git remote add origin https://github.com/<organization>/<season-repository>.git
git config user.name 3drdProgramming
git config user.email programming@3droboticsduluth.com
git push -u origin main
```

When Quickstart publishes a correction, incorporate it before adding unrelated
season work when practical:

```powershell
git fetch quickstart
git rebase quickstart/main
git push --force-with-lease
```

Rebasing published team work rewrites commit IDs. Coordinate with collaborators,
start from a clean working tree, and use `--force-with-lease`, never plain
`--force`. If teammates have unmerged work, finish or synchronize it before the
rebase.

Immediately run the full verification command from
[Verification](../rebuild/verification.md). A new season should begin from green,
not from an unverified dependency upgrade.

For a continuous implementation walkthrough, continue with
[Build the first robot](first-robot.md). It takes Quickstart `main` through hardware
inventory, Pedro specialization, a first mechanism, Teleop, deployment, and
physical validation. Use the [hardware worksheet](hardware-worksheet.md) before
writing hardware declarations. Contributors new to the language should first read
[Why Kotlin?](why-kotlin.md) for the small Kotlin vocabulary and conventions used by
the scaffold.

## Replace the Templates

1. **Pedro constants:** Replace 18-inch template dimensions, follower defaults,
   drivetrain names/kinematics, localizer configuration, constraints, and tuning.
   Keep Pedro's default motor names when they match the Robot Controller
   configuration; add explicit name calls only when they differ.
2. **Game model:** Add the season's alliance/side/field concepts only when they are
   actually useful. Do not copy Decode coordinates by habit.
3. **Config:** Put frequently selected values first. Keep diagnostic `level` and
   transient `filter` unless the season deliberately changes the convention.
4. **Drive:** Customize the included `Drive` subsystem with the team's controls,
   scalar choices, default driver command, telemetry, and season assists.
5. **Nav:** Customize the included `Nav` subsystem with measured robot dimensions
   and named field poses. Prefer typed distance/angle and reusable mirror functions.
6. **Vision:** Add only pipelines and coordinate conversions required by the game.
7. **Mechanisms:** Add one subsystem and its test at a time.
8. **Autonomous:** Replace the included start-to-end Auto example with the season's
   validated starting pose and command composition as mechanisms become available.

## Preserve Module Boundaries

- A reusable fix goes into `3drdNextFTC` with tests.
- Hardware-map names, driver preference, diagnostic field choices, constants,
  paths, mechanisms, and game logic stay in TeamCode.

If multiple teams need a policy but may reasonably customize it, prefer a reusable
hook plus TeamCode implementation, as hardware telemetry does.

## Establish Hardware Safely

Build the hardware map table before enabling output. For each device record name,
type, hub/port, physical direction, safe init value, and safe stop value. Add the
wrapper declaration and unit test, deploy with the robot lifted or mechanism made
safe, then validate the direction at low output.

## Establish Coordinates

Before building paths:

1. Record the FTC/Pedro field axes and heading-zero convention.
2. Measure robot length, width, mass, and the values required by the selected
   localizer.
3. Tune Pedro using the season's separate tuning workflow/branch.
4. Verify forward/strafe/turn signs in robot-centric mode.
5. Verify field-centric behavior for both alliance perspectives.
6. Verify follower pose and Panels drawing agree.
7. Only then define named poses and curved paths.

## Commit Progression

Build the season in dependency order:

1. Robot constants and coordinate/game foundation.
2. Drive/Nav and base config.
3. Independent mechanisms.
4. Coordinating mechanisms and sensing.
5. Vision/assists.
6. Autonomous composition and strategy data.
7. Field-proven behavior refinements.

Keep each feature's tests in the same conceptual commit. Fold later fixups into the
commit whose behavior they complete while the branch is still unpublished or when
a coordinated history rewrite is explicitly intended.
