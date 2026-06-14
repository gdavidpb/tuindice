#!/usr/bin/env bash
set -euo pipefail

REQUESTED_WIREMOCK_PORT="${E2E_WIREMOCK_PORT:-}"
REQUESTED_TMP_DIR="${E2E_TMP_DIR:-}"
REQUESTED_ANDROID_API_BASE_URL="${E2E_ANDROID_API_BASE_URL:-}"
REQUESTED_ANDROID_WEB_BASE_URL="${E2E_ANDROID_WEB_BASE_URL:-}"
REQUESTED_IOS_API_BASE_URL="${E2E_IOS_API_BASE_URL:-}"
REQUESTED_IOS_WEB_BASE_URL="${E2E_IOS_WEB_BASE_URL:-}"
REQUESTED_E2E_MAESTRO_SUITE="${E2E_MAESTRO_SUITE:-}"

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

if [[ -n "${E2E_CERTIFICATION_DIR:-}" ]]; then
	printf 'E2E_CERTIFICATION_DIR cannot be shared by parallel local evidence. Use platform tasks directly or unset it.\n' >&2
	exit 1
fi

if [[ -n "${REQUESTED_WIREMOCK_PORT}" && ! "${REQUESTED_WIREMOCK_PORT}" =~ ^[0-9]+$ ]]; then
	printf 'E2E_WIREMOCK_PORT must be numeric when used by parallel local evidence.\n' >&2
	exit 1
fi

ANDROID_WIREMOCK_PORT="${E2E_ANDROID_WIREMOCK_PORT:-${REQUESTED_WIREMOCK_PORT:-18626}}"
if [[ -n "${E2E_IOS_WIREMOCK_PORT:-}" ]]; then
	IOS_WIREMOCK_PORT="${E2E_IOS_WIREMOCK_PORT}"
elif [[ -n "${REQUESTED_WIREMOCK_PORT}" ]]; then
	IOS_WIREMOCK_PORT="$((REQUESTED_WIREMOCK_PORT + 1))"
else
	IOS_WIREMOCK_PORT="18627"
fi

TMP_ROOT="${REQUESTED_TMP_DIR:-${E2E_TMP_DIR}}"
ANDROID_TMP_DIR="${E2E_ANDROID_TMP_DIR:-${TMP_ROOT}-android}"
IOS_TMP_DIR="${E2E_IOS_TMP_DIR:-${TMP_ROOT}-ios}"
android_pid=""
ios_pid=""
PARALLEL_STATUS_DIR="${TMP_ROOT}-parallel-status-$$"
ANDROID_STATUS_FILE="${PARALLEL_STATUS_DIR}/android.status"
IOS_STATUS_FILE="${PARALLEL_STATUS_DIR}/ios.status"

mkdir -p "${PARALLEL_STATUS_DIR}"

child_processes() {
	local parent_pid="$1"

	if command -v pgrep >/dev/null 2>&1; then
		pgrep -P "${parent_pid}" 2>/dev/null || true
	else
		ps -eo pid=,ppid= 2>/dev/null | awk -v parent_pid="${parent_pid}" '$2 == parent_pid { print $1 }' || true
	fi
}

terminate_process_tree() {
	local pid="$1"
	local child_pid

	for child_pid in $(child_processes "${pid}"); do
		terminate_process_tree "${child_pid}"
	done
	kill "${pid}" >/dev/null 2>&1 || true
}

stop_parallel_platform() {
	local pid="$1"
	local platform="$2"

	if [[ -z "${pid}" ]] || ! kill -0 "${pid}" >/dev/null 2>&1; then
		return 0
	fi

	log "Stopping ${platform} Maestro evidence process ${pid}."
	terminate_process_tree "${pid}"
	wait "${pid}" >/dev/null 2>&1 || true
}

cleanup_parallel_evidence() {
	local status="$?"
	trap - EXIT INT TERM

	stop_parallel_platform "${android_pid}" "Android"
	stop_parallel_platform "${ios_pid}" "iOS"

	E2E_TMP_DIR="${ANDROID_TMP_DIR}" \
	E2E_WIREMOCK_PORT="${ANDROID_WIREMOCK_PORT}" \
	E2E_WIREMOCK_URL="http://127.0.0.1:${ANDROID_WIREMOCK_PORT}" \
		stop_wiremock

	E2E_TMP_DIR="${IOS_TMP_DIR}" \
	E2E_WIREMOCK_PORT="${IOS_WIREMOCK_PORT}" \
	E2E_WIREMOCK_URL="http://127.0.0.1:${IOS_WIREMOCK_PORT}" \
		stop_wiremock

	exit "${status}"
}

trap cleanup_parallel_evidence EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

run_platform_evidence() {
	local platform="$1"
	local wiremock_port="$2"
	local tmp_dir="$3"
	local wiremock_url="http://127.0.0.1:${wiremock_port}"

	(
		set -o pipefail
		case "${platform}" in
			android)
				E2E_WIREMOCK_PORT="${wiremock_port}" \
				E2E_WIREMOCK_URL="${wiremock_url}" \
				E2E_ANDROID_API_BASE_URL="${REQUESTED_ANDROID_API_BASE_URL:-${wiremock_url}/}" \
				E2E_ANDROID_WEB_BASE_URL="${REQUESTED_ANDROID_WEB_BASE_URL:-${wiremock_url}}" \
				E2E_TMP_DIR="${tmp_dir}" \
					bash "${SCRIPT_DIR}/run-maestro-evidence.sh" android
				;;
			ios)
				E2E_WIREMOCK_PORT="${wiremock_port}" \
				E2E_WIREMOCK_URL="${wiremock_url}" \
				E2E_IOS_API_BASE_URL="${REQUESTED_IOS_API_BASE_URL:-http://localhost:${wiremock_port}/}" \
				E2E_IOS_WEB_BASE_URL="${REQUESTED_IOS_WEB_BASE_URL:-http://localhost:${wiremock_port}}" \
				E2E_TMP_DIR="${tmp_dir}" \
					bash "${SCRIPT_DIR}/run-maestro-evidence.sh" ios
				;;
		esac 2>&1 | awk -v prefix="[${platform}] " '{ print prefix $0; fflush(); }'
	)
}

start_platform_evidence_worker() {
	local pid_variable="$1"
	local platform="$2"
	local wiremock_port="$3"
	local tmp_dir="$4"
	local status_file="$5"
	local status

	(
		status=0
		run_platform_evidence "${platform}" "${wiremock_port}" "${tmp_dir}" || status="$?"
		printf '%s\n' "${status}" >"${status_file}"
		exit "${status}"
	) &
	printf -v "${pid_variable}" '%s' "$!"
}

wait_for_parallel_evidence() {
	local android_done=0
	local ios_done=0

	while true; do
		if [[ "${android_done}" == "0" && -n "${android_pid}" && -f "${ANDROID_STATUS_FILE}" ]]; then
			android_status="$(cat "${ANDROID_STATUS_FILE}")"
			android_done=1
			wait "${android_pid}" >/dev/null 2>&1 || true
			android_pid=""
			if [[ "${android_status}" != "0" ]]; then
				local ios_was_running=0
				log "Android evidence worker failed with exit ${android_status}; cancelling remaining platform workers."
				if [[ -n "${ios_pid}" ]]; then
					ios_was_running=1
				fi
				if [[ "${ios_done}" == "0" && -n "${ios_pid}" && -f "${IOS_STATUS_FILE}" ]]; then
					ios_status="$(cat "${IOS_STATUS_FILE}")"
					ios_done=1
					wait "${ios_pid}" >/dev/null 2>&1 || true
					ios_pid=""
				else
					stop_parallel_platform "${ios_pid}" "iOS"
					ios_pid=""
				fi
				if [[ "${ios_was_running}" == "1" && "${ios_done}" == "0" && "${ios_status}" == "0" ]]; then
					ios_status="cancelled"
				fi
				return 1
			fi
		fi

		if [[ "${ios_done}" == "0" && -n "${ios_pid}" && -f "${IOS_STATUS_FILE}" ]]; then
			ios_status="$(cat "${IOS_STATUS_FILE}")"
			ios_done=1
			wait "${ios_pid}" >/dev/null 2>&1 || true
			ios_pid=""
			if [[ "${ios_status}" != "0" ]]; then
				local android_was_running=0
				log "iOS evidence worker failed with exit ${ios_status}; cancelling remaining platform workers."
				if [[ -n "${android_pid}" ]]; then
					android_was_running=1
				fi
				if [[ "${android_done}" == "0" && -n "${android_pid}" && -f "${ANDROID_STATUS_FILE}" ]]; then
					android_status="$(cat "${ANDROID_STATUS_FILE}")"
					android_done=1
					wait "${android_pid}" >/dev/null 2>&1 || true
					android_pid=""
				else
					stop_parallel_platform "${android_pid}" "Android"
					android_pid=""
				fi
				if [[ "${android_was_running}" == "1" && "${android_done}" == "0" && "${android_status}" == "0" ]]; then
					android_status="cancelled"
				fi
				return 1
			fi
		fi

		if [[ -z "${android_pid}" || "${android_done}" == "1" ]] && [[ -z "${ios_pid}" || "${ios_done}" == "1" ]]; then
			return 0
		fi

		sleep 1
	done
}

if [[ -z "${REQUESTED_E2E_MAESTRO_SUITE}" && -z "${E2E_SCOPE_FILE:-}" ]]; then
	LOCAL_SCOPE_DIR="${E2E_SCOPE_STATE_DIR:-$(mktemp -d "${TMPDIR:-/tmp}/tuindice-e2e-local-scope.XXXXXX")}"
	LOCAL_SCOPE_FILE="${LOCAL_SCOPE_DIR}/e2e-scope.csv"
	mkdir -p "$LOCAL_SCOPE_DIR"
	bash "${SCRIPT_DIR}/resolve-e2e-scope.sh" all >"$LOCAL_SCOPE_FILE"
	export E2E_SCOPE_FILE="$LOCAL_SCOPE_FILE"
fi

platform_scope_required() {
	local platform="$1"
	local resolved_scope

	if [[ -n "${REQUESTED_E2E_MAESTRO_SUITE}" ]]; then
		return 0
	fi

	if ! resolved_scope="$(bash "${SCRIPT_DIR}/resolve-e2e-scope.sh" "$platform")"; then
		return 2
	fi

	[[ -n "$resolved_scope" ]]
}

scope_status() {
	local platform="$1"
	local status

	status=0
	platform_scope_required "$platform" || status="$?"
	if [[ "$status" == "0" ]]; then
		printf 'required\n'
		return 0
	fi

	case "$status" in
		1)
			printf 'not-required\n'
			return 0
			;;
		*)
			printf 'failed\n'
			return 1
			;;
	esac
}

android_scope="$(scope_status android)"
ios_scope="$(scope_status ios)"

if [[ -n "${REQUESTED_E2E_MAESTRO_SUITE}" ]]; then
	log "Local E2E evidence plan: requestedSuite=$(basename "${REQUESTED_E2E_MAESTRO_SUITE}"); android=${android_scope} tcp:${ANDROID_WIREMOCK_PORT}; ios=${ios_scope} tcp:${IOS_WIREMOCK_PORT}."
else
	log "Local E2E evidence plan: scope-aware suites; android=${android_scope} tcp:${ANDROID_WIREMOCK_PORT}; ios=${ios_scope} tcp:${IOS_WIREMOCK_PORT}."
fi

if [[ "${E2E_SKIP_ANDROID:-0}" == "1" ]]; then
	log "Skipping Android Maestro evidence because E2E_SKIP_ANDROID=1."
elif [[ "$android_scope" == "required" ]]; then
	log "Starting Android evidence worker."
	start_platform_evidence_worker android_pid android "${ANDROID_WIREMOCK_PORT}" "${ANDROID_TMP_DIR}" "${ANDROID_STATUS_FILE}"
else
	log "Skipping Android Maestro evidence because no Android E2E suites are required for this diff."
fi

if [[ "$ios_scope" != "required" ]]; then
	log "Skipping iOS Maestro evidence because no iOS E2E suites are required for this diff."
elif is_macos && command -v xcrun >/dev/null 2>&1; then
	log "Starting iOS evidence worker."
	start_platform_evidence_worker ios_pid ios "${IOS_WIREMOCK_PORT}" "${IOS_TMP_DIR}" "${IOS_STATUS_FILE}"
elif [[ "${E2E_STRICT_IOS:-0}" == "1" ]]; then
	printf 'iOS Maestro evidence is required but unavailable in this environment.\n' >&2
	exit 1
else
	log "Skipping iOS Maestro evidence because the local iOS simulator toolchain is unavailable."
fi

android_status=0
ios_status=0

if ! wait_for_parallel_evidence; then
	printf 'Local E2E evidence failed: android=%s ios=%s\n' "${android_status}" "${ios_status}" >&2
	exit 1
fi
