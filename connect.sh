#!/usr/bin/env bash
set -euo pipefail

readonly ADB="$(dirname "${BASH_SOURCE[0]}")/scripts/adb.sh"

"$ADB" disconnect
"$ADB" connect 192.168.43.1:5555
