#!/usr/bin/env bash
# Evidence scope of HEAD against the base branch: one `<platform>,<suite>,<reason>` line per
# platform the diff requires. Prints nothing when no evidence is required.
#   resolve-e2e-scope.sh [all|android|ios]
# E2E_BASE_SHA overrides the merge base; E2E_SCOPE_FILE replays a scope file instead of running the detector.
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

# shellcheck source=.github/scripts/common.sh
source "${REPO_ROOT}/.github/scripts/common.sh"

PLATFORM="${1:-all}"
case "$PLATFORM" in
	all|android|ios) ;;
	*)
		printf 'Usage: %s [all|android|ios]\n' "$0" >&2
		exit 1
		;;
esac

rev_parse_commit() {
	git -C "$REPO_ROOT" rev-parse --verify --quiet "${1}^{commit}"
}

filter_scope_file() {
	[[ -f "$1" ]] || { printf 'Missing E2E scope file: %s\n' "$1" >&2; exit 1; }
	awk -F, -v platform_filter="$PLATFORM" '
		$1 != "" && $2 != "" && (platform_filter == "all" || $1 == platform_filter) { print $1 "," $2 "," $3 }
	' "$1" | sort -u
}

if [[ -n "${E2E_SCOPE_FILE:-}" ]]; then
	filter_scope_file "$E2E_SCOPE_FILE"
	exit 0
fi

HEAD_SHA="$(rev_parse_commit HEAD)" || { printf 'Unable to resolve HEAD.\n' >&2; exit 1; }

if [[ -n "${E2E_BASE_SHA:-}" ]]; then
	BASE_SHA="$(rev_parse_commit "$E2E_BASE_SHA")" || { printf 'Unable to resolve E2E_BASE_SHA: %s\n' "$E2E_BASE_SHA" >&2; exit 1; }
else
	base_ref="$(e2e_base_ref "$REPO_ROOT")" || {
		printf 'Unable to resolve origin/production or production. Run git fetch or pass E2E_BASE_SHA.\n' >&2
		exit 1
	}
	BASE_SHA="$(git -C "$REPO_ROOT" merge-base "$HEAD_SHA" "$base_ref")" || {
		printf 'Unable to resolve merge-base between %s and %s. Pass E2E_BASE_SHA to override.\n' "$HEAD_SHA" "$base_ref" >&2
		exit 1
	}
fi

STATE_DIR="$(mktemp -d "${TMPDIR:-/tmp}/tuindice-e2e-scope.XXXXXX")"
trap 'rm -rf "$STATE_DIR"' EXIT

STATE_DIR="$STATE_DIR" E2E_SCOPE_FILE="${STATE_DIR}/e2e-scope.csv" \
	bash "${REPO_ROOT}/.github/scripts/detect-changed-app.sh" "$BASE_SHA" "$HEAD_SHA" >/dev/null
filter_scope_file "${STATE_DIR}/e2e-scope.csv"
