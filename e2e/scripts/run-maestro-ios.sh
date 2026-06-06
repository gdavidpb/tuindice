#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

if ! is_macos; then
	printf 'iOS Maestro E2E requires macOS.\n' >&2
	exit 1
fi

require_command maestro
require_command xcrun

register_wiremock_cleanup
"${SCRIPT_DIR}/start-wiremock.sh"
reset_wiremock

capture_streamed_last_line \
	APP_PATH \
	"${E2E_TMP_DIR}/ios-build-output.log" \
	"${SCRIPT_DIR}/build-ios-debug.sh"
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
MAESTRO_LOG_FILE="${E2E_MAESTRO_LOG_FILE:-${E2E_REPORT_DIR}/maestro-ios.log}"
MAESTRO_HOME="${E2E_MAESTRO_HOME:-${E2E_TMP_DIR}/maestro-home/ios}"
mkdir -p "${MAESTRO_HOME}"
E2E_MAESTRO_SUITE="$("${SCRIPT_DIR}/prepare-maestro-suite.sh" "${E2E_MAESTRO_SUITE}")"
declare -a maestro_args=(
	test
)
if [[ -n "${E2E_MAESTRO_FORMAT:-}" ]]; then
	maestro_args+=(--format "${E2E_MAESTRO_FORMAT}")
fi
if [[ -n "${E2E_MAESTRO_REPORT_FILE:-}" ]]; then
	maestro_args+=(--output "${E2E_MAESTRO_REPORT_FILE}")
fi
if [[ -n "${E2E_MAESTRO_TEST_OUTPUT_DIR:-}" ]]; then
	maestro_args+=(--test-output-dir "${E2E_MAESTRO_TEST_OUTPUT_DIR}")
fi
if [[ -n "${E2E_MAESTRO_DEBUG_OUTPUT_DIR:-}" ]]; then
	maestro_args+=(--debug-output "${E2E_MAESTRO_DEBUG_OUTPUT_DIR}")
fi
maestro_args+=("${E2E_MAESTRO_SUITE}")

log "Running Maestro iOS suite ${E2E_MAESTRO_SUITE} on ${MAESTRO_IOS_DEVICE_ID}."
run_maestro_with_progress \
	"iOS" \
	"${MAESTRO_LOG_FILE}" \
	"${E2E_MAESTRO_SUITE}" \
	"${E2E_MAESTRO_TEST_OUTPUT_DIR:-}" \
	"${E2E_MAESTRO_DEBUG_OUTPUT_DIR:-}" \
	env HOME="${MAESTRO_HOME}" maestro --device "${MAESTRO_IOS_DEVICE_ID}" "${maestro_args[@]}"
