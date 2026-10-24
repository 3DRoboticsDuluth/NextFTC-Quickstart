# Preview the documentation

Install Python 3 with pip and virtual-environment support. On Linux, your
distribution may package that support as `python3-venv`.

From the repository, run:

```powershell
.\docs.cmd
```

On Linux:

```bash
./docs.sh
```

Both scripts also work when invoked by path from another directory. They create
the ignored `.venv-docs` environment and install `requirements-docs.txt` on first
use or when that file changes. Those setup runs require package access; unchanged
subsequent launches reuse the installed environment. Keep generated `site/` and
the environment out of commits.

In Android Studio, select **Preview Docs** from the run configurations and click
Run. This starts the same launcher through Gradle's `docs` task; it does
not build or deploy robot code. Sync Gradle if the configuration is not visible.

Open the address printed by MkDocs in the terminal. Its path follows the team's
`site_url` in `mkdocs.yml`. Saved documentation changes rebuild automatically.
Stop with Ctrl+C in the terminal, or the IDE's Stop button.

To use another port:

```powershell
.\docs.cmd --dev-addr 127.0.0.1:8001
```

The Linux script accepts the same arguments. Keep the default loopback address
for a preview on your own computer. Previewing is separate from publishing the
GitHub Pages site and from the required `mkdocs build --strict` validation.

REQ-SCF-013 maps to these scripts, the shared Python launcher, and the Preview Docs
run configuration. Validate Windows launch, Linux shell syntax, argument forwarding,
and the strict documentation build when changing them.
