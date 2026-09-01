# Start a new season

Quickstart `main` is the verified season-neutral source. Choose whether the new
season benefits more from a fresh repository or retained history.

## Create the repository

For the simplest team workflow, open the Quickstart repository, select **Use this
template**, choose the organization/name/visibility, and create the new repository.
Clone the generated repository and configure the maintainer identity:

```powershell
git clone https://github.com/<organization>/<season-repository>.git
cd <season-repository>
git config user.name 3drdProgramming
git config user.email programming@3droboticsduluth.com
```

The generated repository contains the current files but starts with fresh history
and has no fork relationship. To retain the FTC/platform commit history instead,
clone Quickstart, replace `origin`, and push to an empty season repository:

```powershell
git clone https://github.com/3DRoboticsDuluth/NextFTC-Quickstart.git <season-repository>
cd <season-repository>
git remote set-url origin https://github.com/<organization>/<season-repository>.git
git push -u origin main
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
   Keep Pedro's default motor names when they match the Robot Controller
   configuration; add explicit name calls only when they differ.
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

## Preserve module boundaries

- A reusable fix goes into `3drdNextFTC` with tests.
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
