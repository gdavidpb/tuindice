#!/usr/bin/env bash
# Small helpers shared by the platform adapters. The orchestrator itself is Python.

log() {
	printf '[tuindice-e2e] %s\n' "$*" >&2
}

# The environment is not right (exit 3): the message goes to stderr, where the harness keeps it.
fail() {
	printf '%s\n' "$*" >&2
	exit 3
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

# load_lock: reads ${LOCK_FILE} (KEY=VALUE lines) into variables and requires every name in LOCK_KEYS.
load_lock() {
	local key line value
	[[ -f "${LOCK_FILE}" ]] || { printf 'Missing lock %s\n' "${LOCK_FILE}" >&2; exit 2; }
	while IFS= read -r line || [[ -n "${line}" ]]; do
		[[ -z "${line}" || "${line}" == \#* ]] && continue
		[[ "${line%%=*}" =~ ^[A-Z_]+$ ]] || { printf 'Bad line in %s: %s\n' "${LOCK_FILE}" "${line}" >&2; exit 2; }
		printf -v "${line%%=*}" '%s' "${line#*=}"
	done < "${LOCK_FILE}"
	for key in "${LOCK_KEYS[@]}"; do
		value="${!key:-}"
		[[ -n "${value}" ]] || { printf 'The lock %s has no %s\n' "${LOCK_FILE}" "${key}" >&2; exit 2; }
	done
}

# read_build jsonKey=VARIABLE ...: the build the build verb recorded in ${BUILD_STATE}, into the named variables.
read_build() {
	[[ -f "${BUILD_STATE}" ]] || fail "No build is recorded in ${BUILD_STATE}; run the build verb first"
	eval "$(python3 -c '
import json, shlex, sys
state = json.load(open(sys.argv[1]))
for pair in sys.argv[2:]:
    key, name = pair.split("=")
    print("%s=%s" % (name, shlex.quote(state[key])))' "${BUILD_STATE}" "$@")"
}
