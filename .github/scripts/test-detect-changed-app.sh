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
APP_VERSION_FILE="gradle/app-version.properties"

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

assert_file_not_contains_line() {
	local file="$1"
	local unexpected="$2"
	local label="$3"

	if grep -Fxq "$unexpected" "$file"; then
		printf 'Expected %s not to contain "%s", got:\n' "$label" "$unexpected" >&2
		cat "$file" >&2 || true
		exit 1
	fi
}

app_version_property() {
	local property_name="$1"

	git -C "${REPO_ROOT}" show "${HEAD_SHA}:${APP_VERSION_FILE}" \
		| awk -F= -v property_name="$property_name" '
		$1 == property_name {
			value = $2
			gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
			print value
		}
	'
}

increment_patch_version() {
	local version="$1"
	local major
	local minor
	local patch

	IFS=. read -r major minor patch <<<"$version"
	printf '%s.%s.%s\n' "$major" "$minor" "$((patch + 1))"
}

create_app_version_commit() {
	local name="$1"
	local version_name="$2"
	local android_version_code="$3"
	local ios_build_number="$4"
	local temp_dir
	local index_file
	local version_file
	local blob_sha
	local tree_sha

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-version-detect-test.XXXXXX")"
	index_file="${temp_dir}/index"
	version_file="${temp_dir}/app-version.properties"

	printf 'versionName=%s\nandroidVersionCode=%s\niosBuildNumber=%s\n' \
		"$version_name" \
		"$android_version_code" \
		"$ios_build_number" >"$version_file"

	GIT_INDEX_FILE="$index_file" git -C "${REPO_ROOT}" read-tree "$HEAD_SHA"
	blob_sha="$(git -C "${REPO_ROOT}" hash-object -w "$version_file")"
	GIT_INDEX_FILE="$index_file" git -C "${REPO_ROOT}" update-index --add --cacheinfo "100644,${blob_sha},${APP_VERSION_FILE}"
	tree_sha="$(GIT_INDEX_FILE="$index_file" git -C "${REPO_ROOT}" write-tree)"

	GIT_AUTHOR_NAME="TuIndice CI Test" \
		GIT_AUTHOR_EMAIL="tuindice-ci-test@example.invalid" \
		GIT_COMMITTER_NAME="TuIndice CI Test" \
		GIT_COMMITTER_EMAIL="tuindice-ci-test@example.invalid" \
		git -C "${REPO_ROOT}" commit-tree "$tree_sha" -p "$HEAD_SHA" -m "test ${name}"
}

create_file_commit() {
	local name="$1"
	local file_path="$2"
	local source_file="$3"
	local temp_dir
	local index_file
	local blob_sha
	local tree_sha

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-file-detect-test.XXXXXX")"
	index_file="${temp_dir}/index"

	GIT_INDEX_FILE="$index_file" git -C "${REPO_ROOT}" read-tree "$HEAD_SHA"
	blob_sha="$(git -C "${REPO_ROOT}" hash-object -w "$source_file")"
	GIT_INDEX_FILE="$index_file" git -C "${REPO_ROOT}" update-index --add --cacheinfo "100644,${blob_sha},${file_path}"
	tree_sha="$(GIT_INDEX_FILE="$index_file" git -C "${REPO_ROOT}" write-tree)"

	GIT_AUTHOR_NAME="TuIndice CI Test" \
		GIT_AUTHOR_EMAIL="tuindice-ci-test@example.invalid" \
		GIT_COMMITTER_NAME="TuIndice CI Test" \
		GIT_COMMITTER_EMAIL="tuindice-ci-test@example.invalid" \
		git -C "${REPO_ROOT}" commit-tree "$tree_sha" -p "$HEAD_SHA" -m "test ${name}"
}

create_ios_release_signing_commit() {
	local name="$1"
	local file_path="iosApp/Config/Release.xcconfig"
	local temp_dir
	local release_config_file

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-ios-signing-detect-test.XXXXXX")"
	release_config_file="${temp_dir}/Release.xcconfig"
	git -C "${REPO_ROOT}" show "${HEAD_SHA}:${file_path}" \
		| sed 's/^TUINDICE_CODE_SIGN_STYLE = .*/TUINDICE_CODE_SIGN_STYLE = Manual/' \
		>"$release_config_file"

	create_file_commit "$name" "$file_path" "$release_config_file"
}

run_detector_fixture() {
	local name="$1"
	local changed_path="$2"
	local before_sha="${3:-$HEAD_SHA}"
	local after_sha="${4:-$HEAD_SHA}"
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
			bash ./.github/scripts/detect-changed-app.sh "$before_sha" "$after_sha"
	) >"$output_file" 2>&1

	case "$name" in
		e2e-runner)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/missing-version-bump.txt" "missing version bump"
			assert_file_empty "${temp_dir}/state/e2e-suites.txt" "E2E suites"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eContract" "Android tasks"
			assert_file_empty "${temp_dir}/state/ios-gradle-tasks.txt" "iOS tasks"
			assert_file_contains_line "$github_output_file" "app_version_changed=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			;;
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
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "androidVersionCode" "missing version bump"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "iosBuildNumber" "missing version bump"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,local-certification-suite,ios-host-runtime" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS tasks"
			assert_file_not_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostTypecheck" "iOS tasks"
			assert_file_contains_line "$github_output_file" "ios_ci_scripts_touched=false" "GitHub output"
			;;
		ios-version-xcconfig)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/missing-version-bump.txt" "missing version bump"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_empty "${temp_dir}/state/android-gradle-tasks.txt" "Android tasks"
			assert_file_empty "${temp_dir}/state/ios-gradle-tasks.txt" "iOS tasks"
			assert_file_contains_line "$github_output_file" "app_version_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "app_version_changed=false" "GitHub output"
			;;
		ios-release-signing)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/missing-version-bump.txt" "missing version bump"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyAppVersionSync" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS tasks"
			assert_file_contains_line "$github_output_file" "ci_config_touched=true" "GitHub output"
			;;
		android-version-code)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "app" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "app" "release impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":app:testDebugUnitTest" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":app:bundleRelease" "Android tasks"
			assert_file_empty "${temp_dir}/state/ios-gradle-tasks.txt" "iOS tasks"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "iosBuildNumber" "missing version bump"
			assert_file_not_contains_line "${temp_dir}/state/missing-version-bump.txt" "androidVersionCode" "missing version bump"
			assert_file_contains_line "$github_output_file" "app_version_changed=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "app_release_build_numbers_changed=false" "GitHub output"
			;;
		ios-build-number)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "iosApp" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "iosApp" "release impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_empty "${temp_dir}/state/android-gradle-tasks.txt" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS tasks"
			assert_file_not_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostTypecheck" "iOS tasks"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "androidVersionCode" "missing version bump"
			assert_file_not_contains_line "${temp_dir}/state/missing-version-bump.txt" "iosBuildNumber" "missing version bump"
			assert_file_contains_line "$github_output_file" "app_version_changed=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "app_release_build_numbers_changed=false" "GitHub output"
			;;
		version-name)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "app" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "iosApp" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "app" "release impacted modules"
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "iosApp" "release impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":app:testDebugUnitTest" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":app:bundleRelease" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS tasks"
			assert_file_not_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostTypecheck" "iOS tasks"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "androidVersionCode" "missing version bump"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "iosBuildNumber" "missing version bump"
			assert_file_contains_line "$github_output_file" "app_version_changed=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "app_release_build_numbers_changed=false" "GitHub output"
			;;
		release-build-numbers)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "app" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "iosApp" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "app" "release impacted modules"
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "iosApp" "release impacted modules"
			assert_file_empty "${temp_dir}/state/missing-version-bump.txt" "missing version bump"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":app:testDebugUnitTest" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":app:bundleRelease" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS tasks"
			assert_file_not_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostTypecheck" "iOS tasks"
			assert_file_contains_line "$github_output_file" "app_version_changed=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "app_release_build_numbers_changed=true" "GitHub output"
			;;
		persistence-runtime)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "persistence" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "wizard" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "subjects" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "android,wizard-suite,persistence-runtime" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,summary-suite,persistence-runtime" "E2E scope"
			assert_file_not_contains_line "${temp_dir}/state/e2e-scope.csv" "android,maincore-suite,persistence-bootstrap" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":wizard:testAndroidHostTest" "Android tasks"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		persistence-bootstrap)
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "android,maincore-suite,persistence-bootstrap" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "android,wizard-suite,persistence-runtime" "E2E scope"
			;;
		feature-module-dependents)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "record" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "wizard" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "maincore" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "android,record-suite,module-runtime" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "android,wizard-suite,module-runtime" "E2E scope"
			assert_file_not_contains_line "${temp_dir}/state/e2e-scope.csv" "android,subjects-suite,module-runtime" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":wizard:testAndroidHostTest" "Android tasks"
			;;
		module-graph-config)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyModuleGraph" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyAppVersionSync" "Android tasks"
			assert_file_contains_line "$github_output_file" "module_graph_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			;;
		module-build-file)
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyModuleGraph" "Android tasks"
			assert_file_contains_line "$github_output_file" "module_graph_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=true" "GitHub output"
			;;
		ios-build-script)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS tasks"
			assert_file_contains_line "$github_output_file" "ios_ci_scripts_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			;;
		ios-typecheck-script)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostTypecheck" "iOS tasks"
			assert_file_contains_line "$github_output_file" "ios_ci_scripts_touched=true" "GitHub output"
			;;
		*)
			printf 'Unknown detector fixture: %s\n' "$name" >&2
			exit 1
			;;
	esac
}

current_version_name="$(app_version_property versionName)"
current_android_version_code="$(app_version_property androidVersionCode)"
current_ios_build_number="$(app_version_property iosBuildNumber)"

android_version_commit="$(
	create_app_version_commit \
		android-version-code \
		"$current_version_name" \
		"$((current_android_version_code + 1))" \
		"$current_ios_build_number"
)"
ios_build_commit="$(
	create_app_version_commit \
		ios-build-number \
		"$current_version_name" \
		"$current_android_version_code" \
		"$((current_ios_build_number + 1))"
)"
release_build_numbers_commit="$(
	create_app_version_commit \
		release-build-numbers \
		"$current_version_name" \
		"$((current_android_version_code + 1))" \
		"$((current_ios_build_number + 1))"
)"
version_name_commit="$(
	create_app_version_commit \
		version-name \
		"$(increment_patch_version "$current_version_name")" \
		"$current_android_version_code" \
		"$current_ios_build_number"
)"
ios_release_signing_commit="$(
	create_ios_release_signing_commit ios-release-signing
)"

run_detector_fixture e2e-runner e2e/scripts/common.sh
run_detector_fixture ios-script-tooling iosApp/scripts/ci-upload-ios-appstore.sh
run_detector_fixture persistence-runtime persistence/src/commonMain/kotlin/com/gdavidpb/tuindice/persistence/data/repository/MutationRepository.kt
run_detector_fixture persistence-bootstrap persistence/src/commonMain/kotlin/com/gdavidpb/tuindice/persistence/di/PersistenceModule.kt
run_detector_fixture feature-module-dependents record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/presentation/RecordScreen.kt
run_detector_fixture module-graph-config scripts/module-graph.txt
run_detector_fixture module-build-file record/build.gradle.kts
run_detector_fixture ios-build-script iosApp/scripts/build-kmp-framework.sh
run_detector_fixture ios-typecheck-script iosApp/scripts/ci-typecheck-ios-host.sh
run_detector_fixture ios-host-runtime iosApp/Sources/TuIndiceHost/TuIndiceAppBootstrap.swift
run_detector_fixture ios-version-xcconfig iosApp/Config/Version.xcconfig
run_detector_fixture ios-release-signing iosApp/Config/Release.xcconfig "$HEAD_SHA" "$ios_release_signing_commit"
run_detector_fixture android-version-code "$APP_VERSION_FILE" "$HEAD_SHA" "$android_version_commit"
run_detector_fixture ios-build-number "$APP_VERSION_FILE" "$HEAD_SHA" "$ios_build_commit"
run_detector_fixture release-build-numbers "$APP_VERSION_FILE" "$HEAD_SHA" "$release_build_numbers_commit"
run_detector_fixture version-name "$APP_VERSION_FILE" "$HEAD_SHA" "$version_name_commit"

printf 'Detect changed app shell fixtures passed.\n'
