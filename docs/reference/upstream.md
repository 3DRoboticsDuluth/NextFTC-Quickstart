# Upstream Resources

This repository records its integration choices but does not duplicate upstream
installation, tuning, or API manuals. Use documentation matching the pinned version
where APIs differ from the current site.

## Official Sources

- [FIRST Tech Challenge Robot Controller](https://github.com/FIRST-Tech-Challenge/FtcRobotController)
- [FIRST Tech Challenge documentation](https://ftc-docs.firstinspires.org/)
- [NextFTC documentation](https://nextftc.dev/)
- [NextFTC extensions and Pedro integration](https://nextftc.dev/extensions/)
- [Pedro Pathing documentation](https://pedropathing.com/docs)
- [Pedro Pathing coordinates](https://pedropathing.com/docs/pathing/reference/coordinates)
- [Pedro Pathing installation](https://pedropathing.com/docs/pathing/installation)
- [Pedro dashboard/Panels guidance](https://pedropathing.com/docs/pathing/dashboard)
- [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)
- [GitHub Pages custom workflows](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages)

## Version Caution

The current Pedro website may describe a version newer than the repository's pinned
2.0.6, while NextFTC's Pedro extension is pinned at 1.0.0. Follow this repository's
source/tests for the integration API and use upstream docs for underlying concepts.
Upgrade all related artifacts together only after checking compatibility.

Pedro tuning OpModes are intentionally not copied into `main`. The team may maintain
a separate tuning branch/workflow that tracks the Pedro tuning source without
mixing those files into the seasonal robot history.
