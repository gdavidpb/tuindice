#!/usr/bin/env bash
set -euo pipefail

# Every runtime data file under testkit/e2e must be part of the E2E fingerprint.
#
# Those files change what a Maestro flow does — the quarantine list decides which
# flows even execute — so a change to one must invalidate published evidence. The
# fingerprint is what does that, and it lists them one by one rather than by
# directory, because validators and docs in the same folder deliberately stay out.
#
# That split is only safe while the list stays complete. A new data file that
# nobody adds to the fingerprint would change what runs while leaving the
# fingerprint unmoved, and stale evidence would be reused as if nothing happened.
#
# Usage: e2e/scripts/verify-e2e-fingerprint-coverage.sh

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
FINGERPRINT_SCRIPT="${REPO_ROOT}/e2e/scripts/e2e-fingerprint.sh"

issues=0

while IFS= read -r file; do
	case "${file}" in
		testkit/e2e/validate-*.sh | *.md) continue ;;
	esac

	if ! grep -qF "\"${file}\"" "${FINGERPRINT_SCRIPT}"; then
		printf '%s is read at runtime but missing from the fingerprint in e2e/scripts/e2e-fingerprint.sh; evidence would be reused across a change that alters what the suite does.\n' \
			"${file}" >&2
		issues=1
	fi
done < <(git -C "${REPO_ROOT}" ls-files 'testkit/e2e/*')

if [[ "${issues}" != "0" ]]; then
	exit 1
fi

printf 'E2E fingerprint coverage is valid.\n'
