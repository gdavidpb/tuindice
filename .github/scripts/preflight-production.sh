#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

require_tool git

MISSING_VERSION_BUMP_FILE="${MISSING_VERSION_BUMP_FILE:-}"
HAS_RELEVANT_CHANGES="${HAS_RELEVANT_CHANGES:-true}"
SUMMARY_FILE="${SUMMARY_FILE:-${GITHUB_STEP_SUMMARY:-${RUNNER_TEMP:-/tmp}/preflight-production-summary.md}}"
TARGET_GIT_SHA="${TARGET_GIT_SHA:-${GITHUB_SHA:-$(git rev-parse HEAD)}}"

file_has_entries() {
	local file="${1:-}"
	[[ -n "$file" && -s "$file" ]]
}

join_file_lines_as_csv() {
	local file="$1"

	if file_has_entries "$file"; then
		paste -sd, "$file"
	fi
}

write_summary() {
	{
		printf '## Production preflight summary\n\n'
		printf -- '- Target SHA: `%s`\n' "$TARGET_GIT_SHA"
		printf -- '- App version: `%s`\n' "$(get_app_version_name)"
		printf -- '- Android version code: `%s`\n' "$(get_android_version_code)"
		printf -- '- Reserved iOS build number: `%s`\n' "$(get_ios_build_number)"
		if file_has_entries "$MISSING_VERSION_BUMP_FILE"; then
			printf -- '- Missing version bump: `%s`\n' "$(join_file_lines_as_csv "$MISSING_VERSION_BUMP_FILE")"
		else
			printf -- '- Missing version bump: none\n'
		fi
		printf -- '- Local E2E certification: not required on the legacy Android production branch\n'
	} >>"$SUMMARY_FILE"
}

if [[ "$HAS_RELEVANT_CHANGES" != "true" ]]; then
	info "No relevant app release changes detected. Preflight exits successfully."
	write_summary
	exit 0
fi

bash "${SCRIPT_DIR}/validate-app-version.sh"

if file_has_entries "$MISSING_VERSION_BUMP_FILE"; then
	die "Runtime app changes were detected, but $(app_version_file) did not change. Bump versionName/androidVersionCode before deploying."
fi

write_summary
info "Production preflight checks passed."
