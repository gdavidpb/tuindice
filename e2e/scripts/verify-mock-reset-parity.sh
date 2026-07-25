#!/usr/bin/env bash
set -euo pipefail

# Every WireMock transformer that keeps a mutable dataset must be resettable, and
# reset_wiremock must actually call it.
#
# WireMock's own scenario reset does not touch a custom transformer's in-memory
# state. evaluations solved this with a reset endpoint and left a comment saying
# so; record was written later without one, so a suite that failed after creating
# a synthetic term left it in the dataset and every retry got term_already_exists.
# The retry could never pass, and the whole rotation was lost. Nothing caught it
# because the gap lives between three files that are each individually fine.
#
# For every stateful transformer this checks:
#   - it handles a reset request,
#   - a stub mapping exposes that path,
#   - reset_wiremock curls it.
#
# Usage: e2e/scripts/verify-mock-reset-parity.sh

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TRANSFORMER_DIR="${REPO_ROOT}/mocks/extensions/src/com/gdavidpb/tuindice/mocks"
MAPPINGS_DIR="${REPO_ROOT}/mocks/mappings"
COMMON_SH="${REPO_ROOT}/e2e/scripts/common.sh"

issues=0

report() {
	printf '%s\n' "$1" >&2
	issues=1
}

reset_path_of() {
	# isResetRequest matches on pathSegments == listOf("record", "v5", "reset")
	sed -n '/fun isResetRequest/,/^$/p' "$1" |
		grep -o 'listOf([^)]*)' |
		head -n 1 |
		grep -o '"[^"]*"' |
		tr -d '"' |
		paste -sd/ - |
		sed 's|^|/|'
}

for transformer in "${TRANSFORMER_DIR}"/*ResponseTransformerFactory.kt; do
	[[ -f "${transformer}" ]] || continue

	name="$(basename "${transformer}")"
	if ! grep -qE '^[[:space:]]*private (var|val) state' "${transformer}"; then
		continue
	fi

	if ! grep -q 'fun isResetRequest' "${transformer}"; then
		report "${name} keeps a mutable dataset but handles no reset request; its state will leak across cases and retries."
		continue
	fi

	reset_path="$(reset_path_of "${transformer}")"
	if [[ -z "${reset_path}" || "${reset_path}" == "/" ]]; then
		report "${name} declares isResetRequest but its path could not be read; keep the pathSegments == listOf(...) form."
		continue
	fi

	if ! grep -rqF "\"urlPath\": \"${reset_path}\"" "${MAPPINGS_DIR}"; then
		report "${name} resets on ${reset_path} but no stub mapping under mocks/mappings exposes it, so the request never reaches the transformer."
	fi

	if ! sed -n '/^reset_wiremock()/,/^}/p' "${COMMON_SH}" | grep -qF "${reset_path}"; then
		report "${name} resets on ${reset_path} but reset_wiremock never calls it, so the dataset survives between cases and retries."
	fi
done

if [[ "${issues}" != "0" ]]; then
	exit 1
fi

printf 'Mock reset parity is valid.\n'
