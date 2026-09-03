#!/usr/bin/env bash
set -euo pipefail

find_adb() {
    if command -v adb >/dev/null 2>&1; then
        command -v adb
        return
    fi

    local sdk_root
    for sdk_root in "${ANDROID_SDK_ROOT:-}" "${ANDROID_HOME:-}" "$HOME/Android/Sdk"; do
        if [[ -n "$sdk_root" && -x "$sdk_root/platform-tools/adb" ]]; then
            printf '%s\n' "$sdk_root/platform-tools/adb"
            return
        fi
    done

    printf '%s\n' 'ADB was not found. Add platform-tools to PATH or set ANDROID_SDK_ROOT.' >&2
    return 1
}

readonly ADB="$(find_adb)"
exec "$ADB" "$@"
