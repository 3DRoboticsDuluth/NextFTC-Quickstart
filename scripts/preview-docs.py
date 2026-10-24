"""Prepare the ignored docs environment and serve this checkout's documentation."""

import hashlib
import os
from pathlib import Path
import subprocess
import sys
import venv


def main():
    root = Path(__file__).resolve().parent.parent
    environment = root / ".venv-docs"
    python = environment / ("Scripts/python.exe" if os.name == "nt" else "bin/python")
    requirements = root / "requirements-docs.txt"
    stamp = environment / ".requirements-sha256"
    fingerprint = hashlib.sha256(requirements.read_bytes()).hexdigest()

    if not python.exists():
        venv.EnvBuilder(with_pip=True).create(environment)
    if not stamp.exists() or stamp.read_text() != fingerprint:
        subprocess.run([str(python), "-m", "pip", "install", "-r", str(requirements)], check=True)
        stamp.write_text(fingerprint)

    try:
        return subprocess.call([str(python), "-m", "mkdocs", "serve", *sys.argv[1:]], cwd=root)
    except KeyboardInterrupt:
        return 0


if __name__ == "__main__":
    sys.exit(main())
