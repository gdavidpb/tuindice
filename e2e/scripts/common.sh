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
E2E_IOS_DERIVED_DATA="${E2E_IOS_DERIVED_DATA:-${E2E_TMP_DIR}/ios-derived-data}"
E2E_IOS_DEVICE_ID="${E2E_IOS_DEVICE_ID:-booted}"
E2E_MAESTRO_SUITE="${E2E_MAESTRO_SUITE:-${REPO_ROOT}/e2e/maestro/flows/suites/local-certification-suite.yaml}"

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
