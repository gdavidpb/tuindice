#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

BEFORE_SHA="${1:-${GIT_BEFORE_SHA:-}}"
AFTER_SHA="${2:-${GIT_AFTER_SHA:-${GITHUB_SHA:-HEAD}}}"
STATE_DIR="${STATE_DIR:-$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-changes.XXXXXX")}"

CHANGED_FILES_FILE="${CHANGED_FILES_FILE:-${STATE_DIR}/changed-files.txt}"
MISSING_VERSION_BUMP_FILE="${MISSING_VERSION_BUMP_FILE:-${STATE_DIR}/missing-version-bump.txt}"
ANDROID_TASKS_FILE="${ANDROID_TASKS_FILE:-${STATE_DIR}/android-gradle-tasks.txt}"
RELEASE_IMPACTED_FILE="${RELEASE_IMPACTED_FILE:-${STATE_DIR}/release-impacted.txt}"

mkdir -p "$STATE_DIR"
: >"$CHANGED_FILES_FILE"
: >"$MISSING_VERSION_BUMP_FILE"
: >"$ANDROID_TASKS_FILE"
: >"$RELEASE_IMPACTED_FILE"

APP_VERSION_TOUCHED=false
APP_VERSION_CHANGED=false
CI_CONFIG_TOUCHED=false
HAS_RELEVANT_CHANGES=false
HAS_RELEASE_IMPACT=false

append_android_task() {
	append_unique_line "$ANDROID_TASKS_FILE" "$1"
}

mark_app_tested() {
	append_android_task ":app:testDebugUnitTest"
}

mark_release_impacted() {
	HAS_RELEASE_IMPACT=true
	append_unique_line "$RELEASE_IMPACTED_FILE" app
	mark_app_tested
	append_android_task ":app:bundleRelease"
}

classify_changed_file() {
	local file="$1"

	case "$file" in
		''|.DS_Store|*/.DS_Store)
			return 0
			;;
		.github/workflows/*|.github/scripts/*|.github/actions/*)
			CI_CONFIG_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			return 0
			;;
		"$(app_version_file)")
			APP_VERSION_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			mark_release_impacted
			return 0
			;;
		AGENTS.md|README.md|LICENSE|docs/*|.codex/*)
			return 0
			;;
		settings.gradle|settings.gradle.kts|build.gradle|build.gradle.kts|gradle.properties|gradlew|gradlew.bat|gradle/*)
			HAS_RELEVANT_CHANGES=true
			mark_release_impacted
			return 0
			;;
		app/src/test/*)
			HAS_RELEVANT_CHANGES=true
			mark_app_tested
			return 0
			;;
		app/*)
			HAS_RELEVANT_CHANGES=true
			mark_release_impacted
			return 0
			;;
		mocks/*)
			HAS_RELEVANT_CHANGES=true
			mark_app_tested
			return 0
			;;
		*)
			HAS_RELEVANT_CHANGES=true
			mark_release_impacted
			return 0
			;;
	esac
}

changed_files_between_refs "$BEFORE_SHA" "$AFTER_SHA" >"$CHANGED_FILES_FILE"

while IFS= read -r changed_file; do
	[[ -n "$changed_file" ]] || continue
	classify_changed_file "$changed_file"
done <"$CHANGED_FILES_FILE"

if [[ "$APP_VERSION_TOUCHED" == "true" ]]; then
	base_version="$(get_app_version_property_at_git_ref versionName "$BEFORE_SHA")"
	head_version="$(get_app_version_property_at_git_ref versionName "$AFTER_SHA")"
	base_android_code="$(get_app_version_property_at_git_ref androidVersionCode "$BEFORE_SHA")"
	head_android_code="$(get_app_version_property_at_git_ref androidVersionCode "$AFTER_SHA")"
	base_ios_build="$(get_app_version_property_at_git_ref iosBuildNumber "$BEFORE_SHA")"
	head_ios_build="$(get_app_version_property_at_git_ref iosBuildNumber "$AFTER_SHA")"

	if [[ "$base_version" != "$head_version" || "$base_android_code" != "$head_android_code" || "$base_ios_build" != "$head_ios_build" ]]; then
		APP_VERSION_CHANGED=true
	fi
fi

if [[ "$HAS_RELEASE_IMPACT" == "true" && "$APP_VERSION_CHANGED" != "true" ]]; then
	append_unique_line "$MISSING_VERSION_BUMP_FILE" app
fi

if [[ "$APP_VERSION_CHANGED" == "true" ]]; then
	HAS_RELEVANT_CHANGES=true
	mark_release_impacted
fi

if [[ "$CI_CONFIG_TOUCHED" == "true" ]]; then
	append_android_task "verifyAppVersionSync"
fi

sort_file_if_present "$MISSING_VERSION_BUMP_FILE"
sort_file_if_present "$ANDROID_TASKS_FILE"
sort_file_if_present "$RELEASE_IMPACTED_FILE"

info "Changed files: $(file_to_csv "$CHANGED_FILES_FILE" || true)"
info "App version touched: ${APP_VERSION_TOUCHED}"
info "App version changed: ${APP_VERSION_CHANGED}"
info "Missing version bump: $(file_to_csv "$MISSING_VERSION_BUMP_FILE" || true)"
info "CI/CD configuration touched: ${CI_CONFIG_TOUCHED}"
info "Android Gradle tasks: $(file_to_space_list "$ANDROID_TASKS_FILE" || true)"

if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
	{
		printf 'state_dir=%s\n' "$STATE_DIR"
		printf 'changed_files_file=%s\n' "$CHANGED_FILES_FILE"
		printf 'missing_version_bump_file=%s\n' "$MISSING_VERSION_BUMP_FILE"
		printf 'missing_version_bump_csv=%s\n' "$(file_to_csv "$MISSING_VERSION_BUMP_FILE" || true)"
		printf 'release_impacted_file=%s\n' "$RELEASE_IMPACTED_FILE"
		printf 'release_impacted_csv=%s\n' "$(file_to_csv "$RELEASE_IMPACTED_FILE" || true)"
		printf 'android_tasks=%s\n' "$(file_to_space_list "$ANDROID_TASKS_FILE" || true)"
		printf 'android_tasks_file=%s\n' "$ANDROID_TASKS_FILE"
		printf 'app_version_touched=%s\n' "$APP_VERSION_TOUCHED"
		printf 'app_version_changed=%s\n' "$APP_VERSION_CHANGED"
		printf 'ci_config_touched=%s\n' "$CI_CONFIG_TOUCHED"
		printf 'has_relevant_changes=%s\n' "$HAS_RELEVANT_CHANGES"
		printf 'has_release_impact=%s\n' "$HAS_RELEASE_IMPACT"
	} >>"$GITHUB_OUTPUT"
fi
