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
E2E_WIREMOCK_DELAY_PROFILE="${E2E_WIREMOCK_DELAY_PROFILE:-fast}"
E2E_MAESTRO_OPTIMIZE_SETUP="${E2E_MAESTRO_OPTIMIZE_SETUP:-1}"

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

wiremock_pid_file() {
	printf '%s/wiremock.pid\n' "${E2E_TMP_DIR}"
}

wiremock_listener_pids() {
	if ! command -v lsof >/dev/null 2>&1; then
		return 0
	fi

	lsof -tiTCP:"${E2E_WIREMOCK_PORT}" -sTCP:LISTEN 2>/dev/null || true
}

wait_for_process_exit() {
	local pid="$1"
	local attempts="${2:-10}"

	for _ in $(seq 1 "${attempts}"); do
		if ! kill -0 "${pid}" >/dev/null 2>&1; then
			return 0
		fi

		sleep 1
	done

	return 1
}

stop_wiremock() {
	local restore_errexit=0
	case "$-" in
		*e*) restore_errexit=1 ;;
	esac
	set +e

	local pid_file
	local had_pid_file=0
	local wiremock_pid=""
	local listener_pid

	pid_file="$(wiremock_pid_file)"
	if [[ -f "${pid_file}" ]]; then
		had_pid_file=1
		wiremock_pid="$(cat "${pid_file}" 2>/dev/null || true)"
	fi

	if [[ -n "${wiremock_pid}" ]] && kill -0 "${wiremock_pid}" >/dev/null 2>&1; then
		log "Stopping owned WireMock ${wiremock_pid} on tcp:${E2E_WIREMOCK_PORT}."
		kill "${wiremock_pid}" >/dev/null 2>&1 || true
		if ! wait_for_process_exit "${wiremock_pid}" 10; then
			log "Force stopping owned WireMock ${wiremock_pid} on tcp:${E2E_WIREMOCK_PORT}."
			kill -9 "${wiremock_pid}" >/dev/null 2>&1 || true
			wait_for_process_exit "${wiremock_pid}" 5 >/dev/null 2>&1 || true
		fi
	fi

	if [[ "${had_pid_file}" == "1" ]]; then
		while IFS= read -r listener_pid; do
			[[ -n "${listener_pid}" ]] || continue
			if [[ "${listener_pid}" == "${wiremock_pid}" ]]; then
				continue
			fi

			log "Stopping WireMock listener ${listener_pid} on tcp:${E2E_WIREMOCK_PORT}."
			kill "${listener_pid}" >/dev/null 2>&1 || true
		done < <(wiremock_listener_pids)

		for _ in 1 2 3 4 5; do
			if [[ -z "$(wiremock_listener_pids)" ]]; then
				break
			fi

			sleep 1
		done

		while IFS= read -r listener_pid; do
			[[ -n "${listener_pid}" ]] || continue
			log "Force stopping WireMock listener ${listener_pid} on tcp:${E2E_WIREMOCK_PORT}."
			kill -9 "${listener_pid}" >/dev/null 2>&1 || true
		done < <(wiremock_listener_pids)

		rm -f "${pid_file}"
	fi

	if [[ "${restore_errexit}" == "1" ]]; then
		set -e
	fi
	return 0
}

register_wiremock_cleanup() {
	trap 'status=$?; trap - EXIT INT TERM; stop_wiremock; exit "${status}"' EXIT
	trap 'exit 130' INT
	trap 'exit 143' TERM
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
