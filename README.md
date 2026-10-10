# 3DRD NextFTC Quickstart

This repository is 3D Robotics Duluth's requirements-driven FTC robot platform.
It preserves the official FIRST Tech Challenge Robot Controller v11.2 history and
updates the platform to SDK 12 for BIOBUZZ
and adds a reusable Kotlin and NextFTC platform for starting a robot season.

## Documentation

The canonical documentation lives in [`docs/`](docs/index.md), organized around
starting a team season and maintaining the robot's design:

- [Get started](docs/index.md): repository setup, hardware inventory,
  drivetrain/localization choices, first subsystems, and deployment.
- [Our robot](docs/robot/index.md): editable team requirements, subsystem
  architecture, and validation evidence.
- [Platform reference](docs/reference/platform.md): inherited architecture,
  stable requirements, reconstruction steps, and exact dependencies.

Use the [robot documentation workflow](docs/guides/document-your-robot.md) and
repository-local `$document-robot` skill to keep design and code aligned. Promote
the robot overview to the site home page as the team's design matures.

The same Markdown is published as a searchable, navigable GitHub Pages site by the
repository's documentation workflow. GitHub Pages must be configured to use
**GitHub Actions** as its source before the first deployment.

For local preview:

```powershell
python -m venv .venv-docs
.\.venv-docs\Scripts\python -m pip install -r requirements-docs.txt
.\.venv-docs\Scripts\mkdocs serve
```

## Start a Season

Clone Quickstart and retain it as the `quickstart` remote so a season preserves the
complete FTC/platform history and can rebase onto future Quickstart corrections.
See [Start a new season](docs/guides/new-season.md) for the exact workflow,
collaboration cautions, and replacement checklist.

## Verification

All reusable modules and TeamCode enforce 100% line and branch coverage. Run the
repository verification command before merging:

```powershell
.\gradlew :3drdNextFTC:check :3drdNextFTC:unitTestCoverage :TeamCode:check :TeamCode:unitTestCoverage :TeamCode:assembleDebug
```

The upstream FTC release notes remain available in the Git history at the
[FTC foundation commit](../../tree/26cd1fdd).
