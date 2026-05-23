#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

require_tool git
require_tool jq

MISSING_VERSION_BUMP_FILE="${MISSING_VERSION_BUMP_FILE:-}"
E2E_ANDROID_CONTEXTS_FILE="${E2E_ANDROID_CONTEXTS_FILE:-}"
E2E_IOS_CONTEXTS_FILE="${E2E_IOS_CONTEXTS_FILE:-}"
REQUIRES_E2E_CERTIFICATION="${REQUIRES_E2E_CERTIFICATION:-false}"
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

github_commit_status_state() {
	local context="$1"
	local repository="${GITHUB_REPOSITORY:?GITHUB_REPOSITORY is required to verify E2E commit statuses.}"
	local token="${GITHUB_TOKEN:-${GH_TOKEN:-}}"
	local api_url="${GITHUB_API_URL:-https://api.github.com}"

	[[ -n "$token" ]] || die "GITHUB_TOKEN or GH_TOKEN is required to verify E2E commit status '${context}'."

	curl --fail --silent --show-error \
		-H "Accept: application/vnd.github+json" \
		-H "Authorization: Bearer ${token}" \
		-H "X-GitHub-Api-Version: 2022-11-28" \
		"${api_url}/repos/${repository}/commits/${TARGET_GIT_SHA}/status" \
		| jq -r --arg context "$context" '.statuses[] | select(.context == $context) | .state' \
		| head -n 1
}

status_context_succeeded() {
	local context="$1"
	local state

	state="$(github_commit_status_state "$context" || true)"
	[[ "$state" == "success" ]]
}

verify_contexts_file() {
	local file="$1"
	local platform="$2"
	local context
	local fallback_context="local-e2e/${platform}/local-certification-suite"

	file_has_entries "$file" || return 0

	while IFS= read -r context; do
		[[ -n "$context" ]] || continue
		if status_context_succeeded "$context"; then
			info "Found successful E2E status: ${context}"
			continue
		fi

		if [[ "$context" != "$fallback_context" ]] && status_context_succeeded "$fallback_context"; then
			info "Found successful aggregate E2E status for ${context}: ${fallback_context}"
			continue
		fi

		die "Missing successful E2E status '${context}' on ${TARGET_GIT_SHA}. Run the local evidence task and publish the GitHub status before merging."
	done <"$file"
}

write_summary() {
	{
		printf '## Production preflight summary\n\n'
		printf -- '- Target SHA: `%s`\n' "$TARGET_GIT_SHA"
		printf -- '- App version: `%s`\n' "$(get_app_version_name)"
		printf -- '- Android version code: `%s`\n' "$(get_android_version_code)"
		printf -- '- iOS build number: `%s`\n' "$(get_ios_build_number)"
		if file_has_entries "$MISSING_VERSION_BUMP_FILE"; then
			printf -- '- Missing version bump: `%s`\n' "$(join_file_lines_as_csv "$MISSING_VERSION_BUMP_FILE")"
		else
			printf -- '- Missing version bump: none\n'
		fi
		if [[ "$REQUIRES_E2E_CERTIFICATION" == "true" ]]; then
			printf -- '- Local E2E certification: required and validated\n'
		else
			printf -- '- Local E2E certification: not required for this diff\n'
		fi
	} >>"$SUMMARY_FILE"
}

if [[ "$HAS_RELEVANT_CHANGES" != "true" ]]; then
	info "No relevant app release changes detected. Preflight exits successfully."
	write_summary
	exit 0
fi

bash "${SCRIPT_DIR}/validate-app-version.sh"

if file_has_entries "$MISSING_VERSION_BUMP_FILE"; then
	die "Runtime app changes were detected, but $(app_version_file) did not change. Bump versionName/androidVersionCode/iosBuildNumber before deploying."
fi

if [[ "${SKIP_E2E_STATUS_CHECK:-0}" != "1" && "$REQUIRES_E2E_CERTIFICATION" == "true" ]]; then
	verify_contexts_file "$E2E_ANDROID_CONTEXTS_FILE" android
	verify_contexts_file "$E2E_IOS_CONTEXTS_FILE" ios
elif [[ "$REQUIRES_E2E_CERTIFICATION" == "true" ]]; then
	warn "Skipping E2E commit status verification because SKIP_E2E_STATUS_CHECK=1."
fi

write_summary
info "Production preflight checks passed."
