#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

require_command adb

log "Preparing Android package ${E2E_APP_ID}."
adb reverse "tcp:${E2E_WIREMOCK_PORT}" "tcp:${E2E_WIREMOCK_PORT}" >/dev/null
disable_android_keyboard_helpers
adb shell pm clear "${E2E_APP_ID}" >/dev/null || true
