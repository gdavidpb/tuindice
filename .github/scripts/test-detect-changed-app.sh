#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

require_tool() {
	command -v "$1" >/dev/null 2>&1 || {
		printf 'Required tool %s is not available.\n' "$1" >&2
		exit 1
	}
}

require_tool git

HEAD_SHA="$(git -C "${REPO_ROOT}" rev-parse HEAD)"

assert_file_empty() {
	local file="$1"
	local label="$2"

	if [[ -s "$file" ]]; then
		printf 'Expected %s to be empty, got:\n' "$label" >&2
		cat "$file" >&2
		exit 1
	fi
}

assert_file_contains_line() {
	local file="$1"
	local expected="$2"
	local label="$3"

	if ! grep -Fxq "$expected" "$file"; then
		printf 'Expected %s to contain "%s", got:\n' "$label" "$expected" >&2
		cat "$file" >&2 || true
		exit 1
	fi
}

run_detector_fixture() {
	local name="$1"
	local changed_path="$2"
	local temp_dir
	local changed_files_file
	local output_file
	local github_output_file

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-detect-test.XXXXXX")"
	changed_files_file="${temp_dir}/changed-files.txt"
	output_file="${temp_dir}/output.log"
	github_output_file="${temp_dir}/github-output.txt"
	printf '%s\n' "$changed_path" >"$changed_files_file"

	(
		cd "${REPO_ROOT}"
		STATE_DIR="${temp_dir}/state" \
		GITHUB_OUTPUT="$github_output_file" \
		DETECT_CHANGED_APP_CHANGED_FILES_FILE="$changed_files_file" \
			bash ./.github/scripts/detect-changed-app.sh "$HEAD_SHA" "$HEAD_SHA"
	) >"$output_file" 2>&1

	case "$name" in
		ios-script-tooling)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/missing-version-bump.txt" "missing version bump"
			assert_file_empty "${temp_dir}/state/e2e-suites.txt" "E2E suites"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyAppVersionSync" "Android tasks"
			assert_file_empty "${temp_dir}/state/ios-gradle-tasks.txt" "iOS tasks"
			assert_file_contains_line "$github_output_file" "ios_ci_scripts_touched=true" "GitHub output"
			;;
		ios-host-runtime)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "iosApp" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "iosApp" "release impacted modules"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "app" "missing version bump"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,local-certification-suite,ios-host-runtime" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostBuildRelease" "iOS tasks"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostTypecheck" "iOS tasks"
			assert_file_contains_line "$github_output_file" "ios_ci_scripts_touched=false" "GitHub output"
			;;
		*)
			printf 'Unknown detector fixture: %s\n' "$name" >&2
			exit 1
			;;
	esac
}

run_detector_fixture ios-script-tooling iosApp/scripts/ci-upload-ios-appstore.sh
run_detector_fixture ios-host-runtime iosApp/Sources/TuIndiceHost/TuIndiceAppBootstrap.swift

printf 'Detect changed app shell fixtures passed.\n'
