#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

if ! is_macos; then
	printf 'iOS Maestro E2E requires macOS.\n' >&2
	exit 1
fi

require_command maestro
require_command xcrun

"${SCRIPT_DIR}/start-wiremock.sh"
reset_wiremock

APP_PATH="$("${SCRIPT_DIR}/build-ios-debug.sh" | tail -n 1)"
if [[ ! -d "${APP_PATH}" ]]; then
	printf 'iOS app bundle not found: %s\n' "${APP_PATH}" >&2
	exit 1
fi

"${SCRIPT_DIR}/reset-ios-app.sh"
log "Installing ${APP_PATH} on ${E2E_IOS_DEVICE_ID}."
xcrun simctl install "${E2E_IOS_DEVICE_ID}" "${APP_PATH}"

MAESTRO_IOS_DEVICE_ID="${E2E_IOS_DEVICE_ID}"
if [[ "${MAESTRO_IOS_DEVICE_ID}" == "booted" ]]; then
	MAESTRO_IOS_DEVICE_ID="$(xcrun simctl list devices booted | awk -F'[()]' '/Booted/ { print $2; exit }')"
fi

if [[ -z "${MAESTRO_IOS_DEVICE_ID}" ]]; then
	printf 'Unable to resolve a booted iOS simulator for Maestro.\n' >&2
	exit 1
fi

mkdir -p "${E2E_REPORT_DIR}"
log "Running Maestro iOS suite ${E2E_MAESTRO_SUITE} on ${MAESTRO_IOS_DEVICE_ID}."
maestro --device "${MAESTRO_IOS_DEVICE_ID}" test "${E2E_MAESTRO_SUITE}" | tee "${E2E_REPORT_DIR}/maestro-ios.log"
