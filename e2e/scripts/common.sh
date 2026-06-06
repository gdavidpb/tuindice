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

elapsed_label() {
	local elapsed_seconds="$1"
	printf '%dm %02ds' "$((elapsed_seconds / 60))" "$((elapsed_seconds % 60))"
}

maestro_direct_flow_plan() {
	local suite_path="$1"
	local suite_dir
	local resolved_dir
	local flow
	local flow_count=0

	[[ -f "${suite_path}" ]] || return 0
	suite_dir="$(cd "$(dirname "${suite_path}")" && pwd -P)"
	log "Maestro suite plan for $(basename "${suite_path}"):"
	while IFS= read -r flow; do
		[[ -n "${flow}" ]] || continue
		flow_count=$((flow_count + 1))
		case "${flow}" in
			/*)
				log "  ${flow_count}. ${flow}"
				;;
			*)
				if resolved_dir="$(cd "${suite_dir}" && cd "$(dirname "${flow}")" && pwd -P 2>/dev/null)"; then
					log "  ${flow_count}. ${flow} (${resolved_dir}/$(basename "${flow}"))"
				else
					log "  ${flow_count}. ${flow} (${suite_dir}/${flow})"
				fi
				;;
		esac
	done < <(
		awk '
			/^[[:space:]]*-[[:space:]]*runFlow:[[:space:]]*[^[:space:]]/ {
				sub(/^[[:space:]]*-[[:space:]]*runFlow:[[:space:]]*/, "")
				gsub(/^["'\''[:space:]]+|["'\''[:space:]]+$/, "")
				print
			}
		' "${suite_path}"
	)

	if [[ "${flow_count}" == "0" ]]; then
		log "  inline commands only"
	fi
}

latest_modified_file() {
	local path
	local file
	local timestamp

	for path in "$@"; do
		[[ -n "${path}" && -d "${path}" ]] || continue
		while IFS= read -r -d '' file; do
			timestamp="$(stat -f '%m' "${file}" 2>/dev/null || stat -c '%Y' "${file}" 2>/dev/null || printf '0')"
			printf '%s\t%s\n' "${timestamp}" "${file}"
		done < <(find "${path}" -type f -print0 2>/dev/null)
	done | sort -rn | head -n 1 | cut -f2-
}

latest_wiremock_request() {
	local wiremock_log="${E2E_TMP_DIR}/wiremock.log"
	[[ -f "${wiremock_log}" ]] || return 0
	awk '
		/Request received:/ {
			request = $0
			if (getline endpoint) {
				last = request " " endpoint
			}
		}
		END {
			if (last != "") {
				print last
			}
		}
	' "${wiremock_log}"
}

log_maestro_progress() {
	local platform="$1"
	local started_at="$2"
	local test_output_dir="$3"
	local debug_output_dir="$4"
	local log_file="$5"
	local elapsed
	local latest_file
	local latest_line=""
	local wiremock_request=""

	elapsed="$(elapsed_label "$(($(date +%s) - started_at))")"
	latest_file="$(latest_modified_file "${test_output_dir}" "${debug_output_dir}")"

	if [[ -n "${latest_file}" ]]; then
		case "${latest_file}" in
			*.json|*.log|*.txt|*.xml|*.yaml)
				latest_line="$(tail -n 1 "${latest_file}" 2>/dev/null | tr -d '\r' | cut -c1-220)"
				;;
		esac
		if [[ -n "${latest_line}" ]]; then
			log "${platform} Maestro still running after ${elapsed}; latest artifact $(basename "${latest_file}"): ${latest_line}"
		else
			log "${platform} Maestro still running after ${elapsed}; latest artifact $(basename "${latest_file}") updated."
		fi
		return 0
	fi

	wiremock_request="$(latest_wiremock_request)"
	if [[ -n "${wiremock_request}" ]]; then
		log "${platform} Maestro still running after ${elapsed}; latest WireMock request: ${wiremock_request}"
		return 0
	fi

	if [[ -f "${log_file}" ]]; then
		latest_line="$(tail -n 1 "${log_file}" 2>/dev/null | tr -d '\r' | cut -c1-220)"
	fi
	if [[ -n "${latest_line}" ]]; then
		log "${platform} Maestro still running after ${elapsed}; latest log line: ${latest_line}"
	else
		log "${platform} Maestro still running after ${elapsed}; waiting for Maestro output."
	fi
}

run_maestro_with_progress() {
	local platform="$1"
	local log_file="$2"
	local suite_path="$3"
	local test_output_dir="$4"
	local debug_output_dir="$5"
	shift 5
	local interval_seconds="${E2E_MAESTRO_PROGRESS_INTERVAL_SECONDS:-30}"
	local started_at
	local command_pid
	local monitor_pid
	local status=0

	maestro_direct_flow_plan "${suite_path}"
	started_at="$(date +%s)"
	(
		set -o pipefail
		"$@" 2>&1 | tee "${log_file}"
	) &
	command_pid="$!"

	(
		trap 'exit 0' INT TERM
		while kill -0 "${command_pid}" >/dev/null 2>&1; do
			sleep "${interval_seconds}"
			kill -0 "${command_pid}" >/dev/null 2>&1 || break
			log_maestro_progress "${platform}" "${started_at}" "${test_output_dir}" "${debug_output_dir}" "${log_file}"
		done
	) &
	monitor_pid="$!"

	wait "${command_pid}" || status="$?"
	kill "${monitor_pid}" >/dev/null 2>&1 || true
	wait "${monitor_pid}" >/dev/null 2>&1 || true
	return "${status}"
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
