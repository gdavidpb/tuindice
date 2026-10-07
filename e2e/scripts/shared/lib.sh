#!/usr/bin/env bash
# Small helpers shared by the platform adapters. The orchestrator itself is Python.

log() {
	printf '[tuindice-e2e] %s\n' "$*" >&2
}

# emit_json key=s:string key=j:raw-json ...
emit_json() {
	python3 -c '
import json, sys
out = {}
for item in sys.argv[1:]:
    key, _, rest = item.partition("=")
    kind, _, value = rest.partition(":")
    out[key] = json.loads(value) if kind == "j" else value
print(json.dumps(out))' "$@"
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
