#!/usr/bin/env bash
set -euo pipefail

readonly ADB="$(dirname "${BASH_SOURCE[0]}")/scripts/adb.sh"

"$ADB" start-server
