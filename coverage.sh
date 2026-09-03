#!/usr/bin/env bash
set -euo pipefail

readonly ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly REPORT="$ROOT/TeamCode/build/reports/jacoco/unitTestCoverage/html/index.html"
readonly MODE="${1:-refresh}"

run() {
    "$ROOT/gradlew" :TeamCode:unitTestCoverage
}

open() {
    if [[ ! -f "$REPORT" ]]; then
        printf 'Coverage report not found:\n%s\n\nGenerate it with:\n  ./coverage.sh run\n' "$REPORT" >&2
        return 1
    fi

    if command -v xdg-open >/dev/null 2>&1; then
        xdg-open "$REPORT"
    elif command -v gio >/dev/null 2>&1; then
        gio open "$REPORT"
    else
        printf 'Coverage report:\n%s\n' "$REPORT"
    fi
}

case "$MODE" in
    run) run ;;
    open) open ;;
    refresh) run && open ;;
    *)
        printf '%s\n' 'Usage: ./coverage.sh [run|open|refresh]' >&2
        exit 1
        ;;
esac

