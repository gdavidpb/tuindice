#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

require_command adb
require_command maestro

log "Android Maestro setup: WireMock=${E2E_WIREMOCK_URL}; appId=${E2E_APP_ID}."
register_wiremock_cleanup
"${SCRIPT_DIR}/start-wiremock.sh"
reset_wiremock

capture_streamed_last_line \
	APK_PATH \
	"${E2E_TMP_DIR}/android-build-output.log" \
	"${SCRIPT_DIR}/build-android-debug.sh"
if [[ ! -f "${APK_PATH}" ]]; then
	printf 'Android APK not found: %s\n' "${APK_PATH}" >&2
	exit 1
fi

log "Installing Android APK: $(display_path "${APK_PATH}")."
adb install -r "${APK_PATH}" >/dev/null
"${SCRIPT_DIR}/reset-android-app.sh"

mkdir -p "${E2E_REPORT_DIR}"
MAESTRO_LOG_FILE="${E2E_MAESTRO_LOG_FILE:-${E2E_REPORT_DIR}/maestro-android.log}"
MAESTRO_HOME="${E2E_MAESTRO_HOME:-${E2E_TMP_DIR}/maestro-home/android}"
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

run_maestro_with_progress \
	"Android" \
	"${MAESTRO_LOG_FILE}" \
	"${E2E_MAESTRO_SUITE}" \
	"${E2E_MAESTRO_TEST_OUTPUT_DIR:-}" \
	"${E2E_MAESTRO_DEBUG_OUTPUT_DIR:-}" \
	env HOME="${MAESTRO_HOME}" maestro "${maestro_args[@]}"
