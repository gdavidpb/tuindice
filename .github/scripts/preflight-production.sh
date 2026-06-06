#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

require_tool git
require_tool jq
require_tool curl

MISSING_VERSION_BUMP_FILE="${MISSING_VERSION_BUMP_FILE:-}"
E2E_ANDROID_CONTEXTS_FILE="${E2E_ANDROID_CONTEXTS_FILE:-}"
E2E_IOS_CONTEXTS_FILE="${E2E_IOS_CONTEXTS_FILE:-}"
REQUIRES_E2E_CERTIFICATION="${REQUIRES_E2E_CERTIFICATION:-false}"
HAS_RELEVANT_CHANGES="${HAS_RELEVANT_CHANGES:-true}"
APP_VERSION_CHANGED="${APP_VERSION_CHANGED:-true}"
HAS_RELEASE_IMPACT="${HAS_RELEASE_IMPACT:-true}"
SUMMARY_FILE="${SUMMARY_FILE:-${GITHUB_STEP_SUMMARY:-${RUNNER_TEMP:-/tmp}/preflight-production-summary.md}}"
TARGET_GIT_SHA="${TARGET_GIT_SHA:-${GITHUB_SHA:-$(git rev-parse HEAD)}}"
E2E_REUSE_BASE_SHA="${E2E_REUSE_BASE_SHA:-}"
E2E_REUSE_MAX_COMMITS="${E2E_REUSE_MAX_COMMITS:-50}"
E2E_FINGERPRINT_SCRIPT="${E2E_FINGERPRINT_SCRIPT:-${REPO_ROOT}/e2e/scripts/e2e-fingerprint.sh}"
MISSING_E2E_STATUSES_FILE="${MISSING_E2E_STATUSES_FILE:-${RUNNER_TEMP:-/tmp}/missing-e2e-statuses.txt}"

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

evidence_task_for_platform() {
	local platform="$1"

	case "$platform" in
		android)
			printf 'e2eMaestroEvidenceAndroid\n'
			;;
		ios)
			printf 'e2eMaestroEvidenceIos\n'
			;;
		*)
			die "Unsupported E2E evidence platform '${platform}'."
			;;
	esac
}

record_missing_e2e_status() {
	local platform="$1"
	local context="$2"

	printf '%s\t%s\n' "$platform" "$context" >>"$MISSING_E2E_STATUSES_FILE"
}

write_missing_e2e_guidance() {
	local platform
	local context
	local task

	file_has_entries "$MISSING_E2E_STATUSES_FILE" || return 0
	sort -u "$MISSING_E2E_STATUSES_FILE" -o "$MISSING_E2E_STATUSES_FILE"

	warn "Missing successful E2E statuses on ${TARGET_GIT_SHA}:"
	while IFS=$'\t' read -r platform context; do
		[[ -n "$platform" && -n "$context" ]] || continue
		task="$(evidence_task_for_platform "$platform")"
		warn " - ${context} (publish with: E2E_COMMIT_SHA=${TARGET_GIT_SHA} ./gradlew ${task})"
	done <"$MISSING_E2E_STATUSES_FILE"

	{
		printf '\n### Missing local E2E evidence\n\n'
		printf 'Publish every missing commit status for `%s` before rerunning preflight:\n\n' "$TARGET_GIT_SHA"
		while IFS=$'\t' read -r platform context; do
			[[ -n "$platform" && -n "$context" ]] || continue
			task="$(evidence_task_for_platform "$platform")"
			printf -- '- `%s` with `E2E_COMMIT_SHA=%s ./gradlew %s`\n' "$context" "$TARGET_GIT_SHA" "$task"
		done <"$MISSING_E2E_STATUSES_FILE"
		printf '\nOr run `E2E_COMMIT_SHA=%s ./gradlew e2eMaestroEvidenceLocal` to publish all required local evidence for this diff.\n' "$TARGET_GIT_SHA"
	} >>"$SUMMARY_FILE"
}

github_commit_status_state_at_sha() {
	local sha="$1"
	local context="$2"
	local repository="${GITHUB_REPOSITORY:?GITHUB_REPOSITORY is required to verify E2E commit statuses.}"
	local token="${GITHUB_TOKEN:-${GH_TOKEN:-}}"
	local api_url="${GITHUB_API_URL:-https://api.github.com}"

	[[ -n "$token" ]] || die "GITHUB_TOKEN or GH_TOKEN is required to verify E2E commit status '${context}'."

	curl --fail --silent --show-error \
		-H "Accept: application/vnd.github+json" \
		-H "Authorization: Bearer ${token}" \
		-H "X-GitHub-Api-Version: 2022-11-28" \
		"${api_url}/repos/${repository}/commits/${sha}/status" \
		| jq -r --arg context "$context" '.statuses[] | select(.context == $context) | .state' \
		| head -n 1
}

github_commit_status_state() {
	local context="$1"
	github_commit_status_state_at_sha "$TARGET_GIT_SHA" "$context"
}

status_context_succeeded_at_sha() {
	local sha="$1"
	local context="$2"
	local state

	state="$(github_commit_status_state_at_sha "$sha" "$context" || true)"
	[[ "$state" == "success" ]]
}

status_context_succeeded_at_sha_quiet() {
	local sha="$1"
	local context="$2"
	local state

	state="$(github_commit_status_state_at_sha "$sha" "$context" 2>/dev/null || true)"
	[[ "$state" == "success" ]]
}

status_context_succeeded() {
	local context="$1"
	status_context_succeeded_at_sha "$TARGET_GIT_SHA" "$context"
}

publish_github_commit_status() {
	local context="$1"
	local description="$2"
	local repository="${GITHUB_REPOSITORY:?GITHUB_REPOSITORY is required to publish E2E commit statuses.}"
	local token="${GITHUB_TOKEN:-${GH_TOKEN:-}}"
	local api_url="${GITHUB_API_URL:-https://api.github.com}"
	local payload
	local response
	local response_context
	local response_state

	[[ -n "$token" ]] || die "GITHUB_TOKEN or GH_TOKEN is required to publish reused E2E commit status '${context}'."

	payload="$(
		jq -n -c \
			--arg state "success" \
			--arg context "$context" \
			--arg description "$description" \
			'{state: $state, context: $context, description: $description}'
	)"

	response="$(
		curl --fail --silent --show-error \
			-X POST \
			-H "Accept: application/vnd.github+json" \
			-H "Authorization: Bearer ${token}" \
			-H "X-GitHub-Api-Version: 2022-11-28" \
			-H "Content-Type: application/json" \
			"${api_url}/repos/${repository}/statuses/${TARGET_GIT_SHA}" \
			--data "$payload"
	)" || return 1

	response_state="$(printf '%s\n' "$response" | jq -r '.state // empty')"
	response_context="$(printf '%s\n' "$response" | jq -r '.context // empty')"
	[[ "$response_state" == "success" && "$response_context" == "$context" ]]
}

publish_reused_github_commit_status() {
	local context="$1"
	local description="$2"

	if publish_github_commit_status "$context" "$description"; then
		return 0
	fi

	warn "Could not publish reused E2E status '${context}' on ${TARGET_GIT_SHA}."
	return 1
}

e2e_suite_from_context() {
	local context="$1"
	local platform="$2"
	local prefix="local-e2e/${platform}/"

	[[ "$context" == "${prefix}"* ]] || return 1
	printf '%s\n' "${context#"$prefix"}"
}

e2e_fingerprint() {
	local git_ref="$1"
	local platform="$2"
	local suite="$3"

	bash "${E2E_FINGERPRINT_SCRIPT}" "$platform" "$suite" "$git_ref"
}

e2e_reuse_candidate_commits() {
	if [[ -n "$E2E_REUSE_BASE_SHA" ]] && ! is_zero_sha "$E2E_REUSE_BASE_SHA" &&
		git merge-base --is-ancestor "$E2E_REUSE_BASE_SHA" "$TARGET_GIT_SHA" 2>/dev/null; then
		git rev-list "$TARGET_GIT_SHA" "^${E2E_REUSE_BASE_SHA}"
	else
		git rev-list --max-count="$E2E_REUSE_MAX_COMMITS" "$TARGET_GIT_SHA"
	fi | grep -v "^${TARGET_GIT_SHA}$" || true
}

reuse_successful_status_for_context() {
	local context="$1"
	local platform="$2"
	local suite
	local current_fingerprint
	local candidate_sha
	local candidate_fingerprint

	[[ "${E2E_REUSE_STATUS_BY_FINGERPRINT:-1}" == "1" ]] || return 1

	suite="$(e2e_suite_from_context "$context" "$platform" || true)"
	[[ -n "$suite" ]] || return 1

	current_fingerprint="$(e2e_fingerprint "$TARGET_GIT_SHA" "$platform" "$suite")"
	while IFS= read -r candidate_sha; do
		[[ -n "$candidate_sha" ]] || continue
		if ! status_context_succeeded_at_sha_quiet "$candidate_sha" "$context"; then
			continue
		fi

		candidate_fingerprint="$(e2e_fingerprint "$candidate_sha" "$platform" "$suite" || true)"
		if [[ "$candidate_fingerprint" != "$current_fingerprint" ]]; then
			continue
		fi

		publish_reused_github_commit_status \
			"$context" \
			"Reused E2E ${suite} from ${candidate_sha:0:7} fp ${current_fingerprint:0:12}." ||
			return 1
		info "Reused successful E2E status ${context} from ${candidate_sha} for fingerprint ${current_fingerprint}."
		return 0
	done < <(e2e_reuse_candidate_commits)

	return 1
}

verify_contexts_file() {
	local file="$1"
	local platform="$2"
	local context
	local fallback_context="local-e2e/${platform}/local-certification-suite"
	local missing_status=false

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

		if reuse_successful_status_for_context "$context" "$platform"; then
			continue
		fi

		if [[ "$context" != "$fallback_context" ]] && reuse_successful_status_for_context "$fallback_context" "$platform"; then
			info "Reused successful aggregate E2E status for ${context}: ${fallback_context}"
			continue
		fi

		record_missing_e2e_status "$platform" "$context"
		missing_status=true
	done <"$file"

	if [[ "$missing_status" == "true" ]]; then
		return 1
	fi

	return 0
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

if [[ "$APP_VERSION_CHANGED" == "true" || "$HAS_RELEASE_IMPACT" == "true" ]]; then
	bash "${SCRIPT_DIR}/validate-app-version.sh"
else
	info "Skipping app version validation because no app version or runtime release changes were detected."
fi

if file_has_entries "$MISSING_VERSION_BUMP_FILE"; then
	die "Runtime app changes or app version changes require both androidVersionCode and iosBuildNumber to change. Bump the missing build number(s) in $(app_version_file) before deploying."
fi

if [[ "${SKIP_E2E_STATUS_CHECK:-0}" != "1" && "$REQUIRES_E2E_CERTIFICATION" == "true" ]]; then
	: >"$MISSING_E2E_STATUSES_FILE"
	e2e_status_check_failed=false
	verify_contexts_file "$E2E_ANDROID_CONTEXTS_FILE" android || e2e_status_check_failed=true
	verify_contexts_file "$E2E_IOS_CONTEXTS_FILE" ios || e2e_status_check_failed=true
	if [[ "$e2e_status_check_failed" == "true" ]]; then
		write_missing_e2e_guidance
		die "Missing successful E2E status(es) on ${TARGET_GIT_SHA}. Publish all listed evidence statuses before rerunning preflight."
	fi
elif [[ "$REQUIRES_E2E_CERTIFICATION" == "true" ]]; then
	warn "Skipping E2E commit status verification because SKIP_E2E_STATUS_CHECK=1."
fi

write_summary
info "Production preflight checks passed."
