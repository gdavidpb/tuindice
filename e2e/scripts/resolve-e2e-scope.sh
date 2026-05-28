#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

PLATFORM="${1:-all}"
case "$PLATFORM" in
	all|android|ios)
		;;
	*)
		printf 'Usage: %s [all|android|ios]\n' "$0" >&2
		exit 1
		;;
esac

require_git() {
	if ! command -v git >/dev/null 2>&1; then
		printf 'Missing required command: git\n' >&2
		exit 1
	fi
}

rev_parse_commit() {
	local ref="$1"
	git -C "$REPO_ROOT" rev-parse --verify --quiet "${ref}^{commit}"
}

default_base_ref() {
	if rev_parse_commit production >/dev/null; then
		printf 'production\n'
		return 0
	fi

	if rev_parse_commit origin/production >/dev/null; then
		printf 'origin/production\n'
		return 0
	fi

	printf 'Unable to resolve production or origin/production. Run git fetch or pass E2E_BASE_SHA.\n' >&2
	return 1
}

filter_scope_file() {
	local scope_file="$1"
	local platform_filter="$2"

	if [[ ! -f "$scope_file" ]]; then
		printf 'Missing E2E scope file: %s\n' "$scope_file" >&2
		exit 1
	fi

	awk -F, -v platform_filter="$platform_filter" '
		$1 == "" || $2 == "" {
			next
		}
		platform_filter == "all" || $1 == platform_filter {
			line = $1 "," $2 "," $3
			lines[++count] = line
			if ($2 == "local-certification-suite") {
				hasLocal[$1] = 1
			}
		}
		END {
			for (idx = 1; idx <= count; idx++) {
				split(lines[idx], fields, ",")
				if (!hasLocal[fields[1]] || fields[2] == "local-certification-suite") {
					print lines[idx]
				}
			}
		}
	' "$scope_file" | sort -u
}

require_git

if [[ -n "${E2E_SCOPE_FILE:-}" ]]; then
	filter_scope_file "$E2E_SCOPE_FILE" "$PLATFORM"
	exit 0
fi

HEAD_REF="${E2E_HEAD_SHA:-${E2E_COMMIT_SHA:-HEAD}}"
if ! HEAD_SHA="$(rev_parse_commit "$HEAD_REF")"; then
	printf 'Unable to resolve E2E head ref: %s\n' "$HEAD_REF" >&2
	exit 1
fi

if [[ -n "${E2E_BASE_SHA:-}" ]]; then
	if ! BASE_SHA="$(rev_parse_commit "$E2E_BASE_SHA")"; then
		printf 'Unable to resolve E2E_BASE_SHA: %s\n' "$E2E_BASE_SHA" >&2
		exit 1
	fi
else
	BASE_REF="$(default_base_ref)"
	if ! BASE_SHA="$(git -C "$REPO_ROOT" merge-base "$HEAD_SHA" "$BASE_REF")"; then
		printf 'Unable to resolve merge-base between %s and %s. Pass E2E_BASE_SHA to override.\n' "$HEAD_SHA" "$BASE_REF" >&2
		exit 1
	fi
fi

STATE_DIR="${E2E_SCOPE_STATE_DIR:-$(mktemp -d "${TMPDIR:-/tmp}/tuindice-e2e-scope.XXXXXX")}"
SCOPE_FILE="${STATE_DIR}/e2e-scope.csv"
mkdir -p "$STATE_DIR"

STATE_DIR="$STATE_DIR" \
	E2E_SCOPE_FILE="$SCOPE_FILE" \
	bash "${REPO_ROOT}/.github/scripts/detect-changed-app.sh" "$BASE_SHA" "$HEAD_SHA" >/dev/null

filter_scope_file "$SCOPE_FILE" "$PLATFORM"
