#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

require_command curl

mkdir -p "${E2E_TMP_DIR}"

if curl --fail --silent --output /dev/null "${E2E_WIREMOCK_URL}/__admin"; then
	log "WireMock is already running at ${E2E_WIREMOCK_URL}."
	reset_wiremock
	exit 0
fi

WIREMOCK_LOG="${E2E_TMP_DIR}/wiremock.log"
WIREMOCK_PID_FILE="${E2E_TMP_DIR}/wiremock.pid"

log "Starting WireMock at ${E2E_WIREMOCK_URL}."
PORT="${E2E_WIREMOCK_PORT}" \
	"${REPO_ROOT}/mocks/start-mock-enviroment.sh" \
	> "${WIREMOCK_LOG}" 2>&1 &
printf '%s\n' "$!" > "${WIREMOCK_PID_FILE}"

wait_for_url "${E2E_WIREMOCK_URL}/__admin" 45
reset_wiremock
log "WireMock ready. Log: ${WIREMOCK_LOG}"
