#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

MAX_ATTEMPTS="${GRADLE_RETRY_MAX_ATTEMPTS:-2}"
SLEEP_SECONDS="${GRADLE_RETRY_SLEEP_SECONDS:-20}"
LOG_DIR="${GRADLE_RETRY_LOG_DIR:-${RUNNER_TEMP:-/tmp}/gradle-retry-logs}"

is_positive_integer() {
	local value="$1"
	[[ "$value" =~ ^[1-9][0-9]*$ ]]
}

is_transient_gradle_failure() {
	local log_file="$1"

	grep -E \
		"Received status code (429|50[0-9])|Bad Gateway|Service Unavailable|Gateway Timeout|Could not GET|Read timed out|Connection reset|Connection timed out|Temporary failure in name resolution|Remote host terminated the handshake" \
		"$log_file" >/dev/null 2>&1
}

command_display() {
	printf '%q ' "$@"
}

is_positive_integer "$MAX_ATTEMPTS" || die "GRADLE_RETRY_MAX_ATTEMPTS must be a positive integer."
is_positive_integer "$SLEEP_SECONDS" || die "GRADLE_RETRY_SLEEP_SECONDS must be a positive integer."
[[ "$#" -gt 0 ]] || die "Usage: $0 <gradle command> [args...]"

mkdir -p "$LOG_DIR"

attempt=1
while [[ "$attempt" -le "$MAX_ATTEMPTS" ]]; do
	log_file="${LOG_DIR}/gradle-attempt-${attempt}.log"
	info "Running Gradle attempt ${attempt}/${MAX_ATTEMPTS}: $(command_display "$@")"

	set +e
	"$@" 2>&1 | tee "$log_file"
	status="${PIPESTATUS[0]}"
	set -e

	if [[ "$status" -eq 0 ]]; then
		exit 0
	fi

	if [[ "$attempt" -ge "$MAX_ATTEMPTS" ]]; then
		error "Gradle failed after ${attempt} attempt(s). Last log: ${log_file}"
		exit "$status"
	fi

	if ! is_transient_gradle_failure "$log_file"; then
		error "Gradle failed with a non-transient error. Log: ${log_file}"
		exit "$status"
	fi

	warn "Gradle failed with a transient dependency/network error. Retrying in ${SLEEP_SECONDS}s. Log: ${log_file}"
	sleep "$SLEEP_SECONDS"
	attempt="$((attempt + 1))"
done
