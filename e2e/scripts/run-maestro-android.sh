#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

require_command adb
require_command maestro

"${SCRIPT_DIR}/start-wiremock.sh"
reset_wiremock

APK_PATH="$("${SCRIPT_DIR}/build-android-debug.sh" | tail -n 1)"
if [[ ! -f "${APK_PATH}" ]]; then
	printf 'Android APK not found: %s\n' "${APK_PATH}" >&2
	exit 1
fi

log "Installing ${APK_PATH}."
adb install -r "${APK_PATH}" >/dev/null
"${SCRIPT_DIR}/reset-android-app.sh"

mkdir -p "${E2E_REPORT_DIR}"
log "Running Maestro Android suite ${E2E_MAESTRO_SUITE}."
maestro test "${E2E_MAESTRO_SUITE}" | tee "${E2E_REPORT_DIR}/maestro-android.log"
