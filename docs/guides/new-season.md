# Start a new season

Use the verified seasonal tag as the branch point. Do not delete Decode code from
`main` and assume the result is equivalent; the tag proves exactly where robot
policy begins.

## Create the branch

```powershell
git fetch origin --tags
git switch --create season/<season-name> reusable-season-base
git config user.name 3drdProgramming
git config user.email programming@3droboticsduluth.com
```

Immediately run the full verification command from
[Verification](../rebuild/verification.md). A new season should begin from green,
not from an unverified dependency upgrade.

For a continuous implementation walkthrough, continue with
[Build the first robot](first-robot.md). It takes the neutral tag through hardware
inventory, Pedro specialization, a first mechanism, Teleop, deployment, and
physical validation. Use the [hardware worksheet](hardware-worksheet.md) before
writing hardware declarations.

## Replace the templates

1. **Pedro constants:** replace 18-inch template dimensions, follower defaults,
   drivetrain names/kinematics, localizer configuration, constraints, and tuning.
2. **Game model:** add the season's alliance/side/field concepts only when they are
   actually useful. Do not copy Decode coordinates by habit.
3. **Config:** put frequently selected values first. Keep diagnostic `level` and
   transient `filter` unless the season deliberately changes the convention.
4. **Drive:** specialize `DriveSubsystem` with the team's controls, scalar choices,
   default driver command, telemetry, and season assists.
5. **Nav:** specialize `NavSubsystem` with measured robot dimensions and named field
   poses. Prefer typed distance/angle and reusable mirror functions.
6. **Vision:** add only pipelines and coordinate conversions required by the game.
7. **Mechanisms:** add one subsystem and its test at a time.
8. **Autonomous:** add a top-level Auto subsystem when enough mechanism commands
   exist to compose a meaningful routine.
9. **Quanomous:** keep the generic module untouched; define this season's JSON step
   handlers in a TeamCode subsystem.
10. **Timing:** review the template 75-second rumble threshold against the new game
    and driver preference; change or remove it deliberately.

## Preserve module boundaries

- A reusable fix goes into `3drdNextFTC` with tests.
- Program parsing/compiler/storage improvements go into `3drdQuanomous` and must
  keep it independent of NextFTC.
- Hardware-map names, driver preference, diagnostic field choices, constants,
  paths, mechanisms, and game logic stay in TeamCode.

If multiple teams need a policy but may reasonably customize it, prefer a reusable
hook plus TeamCode implementation, as hardware telemetry does.

## Establish hardware safely

Build the hardware map table before enabling output. For each device record name,
type, hub/port, physical direction, safe init value, and safe stop value. Add the
wrapper declaration and unit test, deploy with the robot lifted or mechanism made
safe, then validate the direction at low output.

## Establish coordinates

Before building paths:

1. record the FTC/Pedro field axes and heading-zero convention;
2. measure robot length, width, localizer pod offsets, and mass;
3. tune Pedro using the season's separate tuning workflow/branch;
4. verify forward/strafe/turn signs in robot-centric mode;
5. verify field-centric behavior for both alliance perspectives;
6. verify follower pose and Panels drawing agree;
7. only then define named poses and curved paths.

## Commit progression

Build the season in dependency order:

1. robot constants and coordinate/game foundation;
2. drive/nav and base config;
3. independent mechanisms;
4. coordinating mechanisms and sensing;
5. vision/assists;
6. autonomous composition and strategy data;
7. field-proven behavior refinements.

Keep each feature's tests in the same conceptual commit. Fold later fixups into the
commit whose behavior they complete while the branch is still unpublished or when
a coordinated history rewrite is explicitly intended.
