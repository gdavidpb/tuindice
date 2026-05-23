#!/usr/bin/env bash
set -euo pipefail

REQUESTED_WIREMOCK_PORT="${E2E_WIREMOCK_PORT:-}"
REQUESTED_TMP_DIR="${E2E_TMP_DIR:-}"
REQUESTED_ANDROID_API_BASE_URL="${E2E_ANDROID_API_BASE_URL:-}"
REQUESTED_ANDROID_WEB_BASE_URL="${E2E_ANDROID_WEB_BASE_URL:-}"
REQUESTED_IOS_API_BASE_URL="${E2E_IOS_API_BASE_URL:-}"
REQUESTED_IOS_WEB_BASE_URL="${E2E_IOS_WEB_BASE_URL:-}"

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

android_pid=""
ios_pid=""

if [[ "${E2E_SKIP_ANDROID:-0}" != "1" ]]; then
	log "Starting Android Maestro evidence in parallel on WireMock tcp:${ANDROID_WIREMOCK_PORT}."
	run_platform_evidence android "${ANDROID_WIREMOCK_PORT}" "${ANDROID_TMP_DIR}" &
	android_pid="$!"
else
	log "Skipping Android Maestro evidence because E2E_SKIP_ANDROID=1."
fi

if is_macos && command -v xcrun >/dev/null 2>&1; then
	log "Starting iOS Maestro evidence in parallel on WireMock tcp:${IOS_WIREMOCK_PORT}."
	run_platform_evidence ios "${IOS_WIREMOCK_PORT}" "${IOS_TMP_DIR}" &
	ios_pid="$!"
elif [[ "${E2E_STRICT_IOS:-0}" == "1" ]]; then
	printf 'iOS Maestro evidence is required but unavailable in this environment.\n' >&2
	exit 1
else
	log "Skipping iOS Maestro evidence because the local iOS simulator toolchain is unavailable."
fi

android_status=0
ios_status=0

if [[ -n "${android_pid}" ]]; then
	wait "${android_pid}" || android_status="$?"
fi

if [[ -n "${ios_pid}" ]]; then
	wait "${ios_pid}" || ios_status="$?"
fi

if [[ "${android_status}" != "0" || "${ios_status}" != "0" ]]; then
	printf 'Local E2E evidence failed: android=%s ios=%s\n' "${android_status}" "${ios_status}" >&2
	exit 1
fi
