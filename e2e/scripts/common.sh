#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
E2E_TMP_DIR="${E2E_TMP_DIR:-/tmp/tuindice-e2e}"
E2E_REPORT_DIR="${E2E_REPORT_DIR:-${REPO_ROOT}/build/e2e}"
E2E_APP_ID="${E2E_APP_ID:-com.gdavidpb.tuindice.debug}"
E2E_IOS_BUNDLE_ID="${E2E_IOS_BUNDLE_ID:-com.gdavidpb.tuindice.debug}"
E2E_WIREMOCK_PORT="${E2E_WIREMOCK_PORT:-8080}"
E2E_WIREMOCK_URL="${E2E_WIREMOCK_URL:-http://127.0.0.1:${E2E_WIREMOCK_PORT}}"
E2E_ANDROID_API_BASE_URL="${E2E_ANDROID_API_BASE_URL:-http://127.0.0.1:${E2E_WIREMOCK_PORT}/}"
E2E_ANDROID_WEB_BASE_URL="${E2E_ANDROID_WEB_BASE_URL:-http://127.0.0.1:${E2E_WIREMOCK_PORT}}"
E2E_IOS_API_BASE_URL="${E2E_IOS_API_BASE_URL:-http://localhost:${E2E_WIREMOCK_PORT}/}"
E2E_IOS_WEB_BASE_URL="${E2E_IOS_WEB_BASE_URL:-http://localhost:${E2E_WIREMOCK_PORT}}"
E2E_IOS_DERIVED_DATA="${E2E_IOS_DERIVED_DATA:-${E2E_TMP_DIR}/ios-derived-data}"
E2E_IOS_DEVICE_ID="${E2E_IOS_DEVICE_ID:-booted}"
E2E_MAESTRO_SUITE="${E2E_MAESTRO_SUITE:-${REPO_ROOT}/e2e/maestro/flows/suites/local-certification-suite.yaml}"
E2E_DISABLE_KEYBOARD_HELPERS="${E2E_DISABLE_KEYBOARD_HELPERS:-1}"

log() {
	printf '[tuindice-e2e] %s\n' "$*"
}

require_command() {
	local command_name="$1"

	if ! command -v "${command_name}" >/dev/null 2>&1; then
		printf 'Missing required command: %s\n' "${command_name}" >&2
		exit 1
	fi
}

capture_streamed_last_line() {
	local result_variable="$1"
	local output_file="$2"
	shift 2

	mkdir -p "$(dirname "${output_file}")"
	set +e
	"$@" 2>&1 | tee "${output_file}"
	local command_status="${PIPESTATUS[0]}"
	set -e
	if [[ "${command_status}" != "0" ]]; then
		return "${command_status}"
	fi

	printf -v "${result_variable}" '%s' "$(tail -n 1 "${output_file}")"
}

is_macos() {
	[[ "$(uname -s)" == "Darwin" ]]
}

wait_for_url() {
	local url="$1"
	local timeout_seconds="${2:-30}"
	local start_seconds
	start_seconds="$(date +%s)"

	while true; do
		if curl --fail --silent --output /dev/null "${url}"; then
			return 0
		fi

		if (( "$(date +%s)" - start_seconds >= timeout_seconds )); then
			printf 'Timed out waiting for %s\n' "${url}" >&2
			return 1
		fi

		sleep 1
	done
}

reset_wiremock() {
	require_command curl
	curl --fail --silent --output /dev/null \
		--request POST "${E2E_WIREMOCK_URL}/__admin/scenarios/reset"
	curl --fail --silent --output /dev/null \
		--request DELETE "${E2E_WIREMOCK_URL}/__admin/requests"
}

disable_android_keyboard_helpers() {
	if [[ "${E2E_DISABLE_KEYBOARD_HELPERS}" != "1" ]]; then
		return 0
	fi

	log "Disabling Android keyboard helpers for deterministic text input."
	adb shell settings put secure spell_checker_enabled 0 >/dev/null 2>&1 || true
	adb shell settings put secure selected_spell_checker "" >/dev/null 2>&1 || true
	adb shell settings put secure autofill_service null >/dev/null 2>&1 || true
	adb shell settings put secure show_ime_with_hard_keyboard 0 >/dev/null 2>&1 || true
	adb shell cmd autofill disable >/dev/null 2>&1 || true
}

disable_ios_keyboard_helpers() {
	if [[ "${E2E_DISABLE_KEYBOARD_HELPERS}" != "1" ]]; then
		return 0
	fi

	log "Disabling iOS simulator keyboard helpers for deterministic text input."
	xcrun simctl spawn "${E2E_IOS_DEVICE_ID}" defaults write -g KeyboardAutocorrection -bool NO >/dev/null 2>&1 || true
	xcrun simctl spawn "${E2E_IOS_DEVICE_ID}" defaults write -g KeyboardPrediction -bool NO >/dev/null 2>&1 || true
	xcrun simctl spawn "${E2E_IOS_DEVICE_ID}" defaults write -g KeyboardShowPrediction -bool NO >/dev/null 2>&1 || true
	xcrun simctl spawn "${E2E_IOS_DEVICE_ID}" defaults write -g NSAutomaticSpellingCorrectionEnabled -bool NO >/dev/null 2>&1 || true
	xcrun simctl spawn "${E2E_IOS_DEVICE_ID}" defaults write -g NSAutomaticTextCompletionEnabled -bool NO >/dev/null 2>&1 || true
	xcrun simctl spawn "${E2E_IOS_DEVICE_ID}" defaults write -g NSUseSpellCheckerForCompletions -bool NO >/dev/null 2>&1 || true
}
