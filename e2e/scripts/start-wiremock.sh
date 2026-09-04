#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

require_command curl

mkdir -p "${E2E_TMP_DIR}"

WIREMOCK_LOG="${E2E_TMP_DIR}/wiremock.log"
WIREMOCK_PID_FILE="${E2E_TMP_DIR}/wiremock.pid"
STARTED_WIREMOCK=0

cleanup_failed_wiremock_start() {
	local status="$?"
	trap - EXIT
	if [[ "${status}" != "0" && "${STARTED_WIREMOCK}" == "1" ]]; then
		stop_wiremock
	fi
	exit "${status}"
}

trap cleanup_failed_wiremock_start EXIT

if curl --fail --silent --output /dev/null "${E2E_WIREMOCK_URL}/__admin"; then
	if [[ -f "${WIREMOCK_PID_FILE}" ]]; then
		WIREMOCK_PID="$(cat "${WIREMOCK_PID_FILE}")"

		if [[ -n "${WIREMOCK_PID}" ]] && kill -0 "${WIREMOCK_PID}" >/dev/null 2>&1; then
			log "Restarting owned WireMock at ${E2E_WIREMOCK_URL} to reload mappings."
			kill "${WIREMOCK_PID}" >/dev/null 2>&1 || true

			for _ in 1 2 3 4 5 6 7 8 9 10; do
				if ! curl --fail --silent --output /dev/null "${E2E_WIREMOCK_URL}/__admin"; then
					break
				fi

				sleep 1
			done

			if curl --fail --silent --output /dev/null "${E2E_WIREMOCK_URL}/__admin"; then
				kill -9 "${WIREMOCK_PID}" >/dev/null 2>&1 || true

				for _ in 1 2 3 4 5; do
					if ! curl --fail --silent --output /dev/null "${E2E_WIREMOCK_URL}/__admin"; then
						break
					fi

					sleep 1
				done
			fi
		fi
	fi

	if curl --fail --silent --output /dev/null "${E2E_WIREMOCK_URL}/__admin" &&
		command -v lsof >/dev/null 2>&1; then
		while IFS= read -r LISTENER_PID; do
			[[ -z "${LISTENER_PID}" ]] && continue
			log "Stopping WireMock listener ${LISTENER_PID} on tcp:${E2E_WIREMOCK_PORT}."
			kill "${LISTENER_PID}" >/dev/null 2>&1 || true
		done < <(wiremock_listener_pids)

		for _ in 1 2 3 4 5; do
			if ! curl --fail --silent --output /dev/null "${E2E_WIREMOCK_URL}/__admin"; then
				break
			fi

			sleep 1
		done

		if curl --fail --silent --output /dev/null "${E2E_WIREMOCK_URL}/__admin"; then
			while IFS= read -r LISTENER_PID; do
				[[ -z "${LISTENER_PID}" ]] && continue
				log "Force stopping WireMock listener ${LISTENER_PID} on tcp:${E2E_WIREMOCK_PORT}."
				kill -9 "${LISTENER_PID}" >/dev/null 2>&1 || true
			done < <(wiremock_listener_pids)

			for _ in 1 2 3 4 5; do
				if ! curl --fail --silent --output /dev/null "${E2E_WIREMOCK_URL}/__admin"; then
					break
				fi

				sleep 1
			done
		fi
	fi
fi

if ! curl --fail --silent --output /dev/null "${E2E_WIREMOCK_URL}/__admin" &&
	command -v lsof >/dev/null 2>&1; then
	while IFS= read -r LISTENER_PID; do
		[[ -z "${LISTENER_PID}" ]] && continue
		log "Stopping stale listener ${LISTENER_PID} on tcp:${E2E_WIREMOCK_PORT}."
		kill "${LISTENER_PID}" >/dev/null 2>&1 || true
	done < <(wiremock_listener_pids)

	for _ in 1 2 3 4 5; do
		if [[ -z "$(wiremock_listener_pids)" ]]; then
			break
		fi

		sleep 1
	done

	while IFS= read -r LISTENER_PID; do
		[[ -z "${LISTENER_PID}" ]] && continue
		log "Force stopping stale listener ${LISTENER_PID} on tcp:${E2E_WIREMOCK_PORT}."
		kill -9 "${LISTENER_PID}" >/dev/null 2>&1 || true
	done < <(wiremock_listener_pids)
fi

if curl --fail --silent --output /dev/null "${E2E_WIREMOCK_URL}/__admin"; then
	log "WireMock is already running at ${E2E_WIREMOCK_URL}."
	reset_wiremock
	exit 0
fi

log "Starting WireMock at ${E2E_WIREMOCK_URL}."
PORT="${E2E_WIREMOCK_PORT}" \
	WIREMOCK_DELAY_PROFILE="${E2E_WIREMOCK_DELAY_PROFILE}" \
	WIREMOCK_GENERATED_ROOT="${WIREMOCK_GENERATED_ROOT:-${E2E_TMP_DIR}/wiremock-generated}" \
	"${REPO_ROOT}/mocks/start-mock-enviroment.sh" \
	> "${WIREMOCK_LOG}" 2>&1 &
printf '%s\n' "$!" > "${WIREMOCK_PID_FILE}"
STARTED_WIREMOCK=1

# A timeout here reads like a busy port, but the usual cause is that the mock
# environment never came up -- most often because the Kotlin extensions failed
# to compile. The log holds the compiler error; without it the next stop is a
# fruitless hunt through ports and stale processes.
if ! wait_for_url "${E2E_WIREMOCK_URL}/__admin" 45; then
	printf 'WireMock never became ready. Last lines of %s:\n' "${WIREMOCK_LOG}" >&2
	tail -40 "${WIREMOCK_LOG}" >&2 || true
	exit 1
fi

reset_wiremock
log "WireMock ready. Log: ${WIREMOCK_LOG}"
trap - EXIT
