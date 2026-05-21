#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

require_command curl

mkdir -p "${E2E_TMP_DIR}"

WIREMOCK_LOG="${E2E_TMP_DIR}/wiremock.log"
WIREMOCK_PID_FILE="${E2E_TMP_DIR}/wiremock.pid"

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
		done < <(lsof -ti "tcp:${E2E_WIREMOCK_PORT}" || true)

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
			done < <(lsof -ti "tcp:${E2E_WIREMOCK_PORT}" || true)

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
	done < <(lsof -ti "tcp:${E2E_WIREMOCK_PORT}" || true)

	for _ in 1 2 3 4 5; do
		if [[ -z "$(lsof -ti "tcp:${E2E_WIREMOCK_PORT}" || true)" ]]; then
			break
		fi

		sleep 1
	done

	while IFS= read -r LISTENER_PID; do
		[[ -z "${LISTENER_PID}" ]] && continue
		log "Force stopping stale listener ${LISTENER_PID} on tcp:${E2E_WIREMOCK_PORT}."
		kill -9 "${LISTENER_PID}" >/dev/null 2>&1 || true
	done < <(lsof -ti "tcp:${E2E_WIREMOCK_PORT}" || true)
fi

if curl --fail --silent --output /dev/null "${E2E_WIREMOCK_URL}/__admin"; then
	log "WireMock is already running at ${E2E_WIREMOCK_URL}."
	reset_wiremock
	exit 0
fi

log "Starting WireMock at ${E2E_WIREMOCK_URL}."
PORT="${E2E_WIREMOCK_PORT}" \
	"${REPO_ROOT}/mocks/start-mock-enviroment.sh" \
	> "${WIREMOCK_LOG}" 2>&1 &
printf '%s\n' "$!" > "${WIREMOCK_PID_FILE}"

wait_for_url "${E2E_WIREMOCK_URL}/__admin" 45
reset_wiremock
log "WireMock ready. Log: ${WIREMOCK_LOG}"
