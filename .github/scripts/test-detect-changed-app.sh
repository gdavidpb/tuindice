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

assert_file_lines() {
	local file="$1"
	local label="$2"
	shift 2
	local expected
	expected="$(printf '%s\n' "$@")"

	if [[ "$(cat "$file")" != "$expected" ]]; then
		printf 'Expected %s to hold exactly:\n%s\nGot:\n' "$label" "$expected" >&2
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

create_ios_release_runtime_commit() {
	local name="$1"
	local file_path="iosApp/Config/Release.xcconfig"
	local temp_dir
	local release_config_file

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-ios-runtime-detect-test.XXXXXX")"
	release_config_file="${temp_dir}/Release.xcconfig"
	git -C "${REPO_ROOT}" show "${HEAD_SHA}:${file_path}" \
		| sed 's/^SWIFT_VERSION = .*/SWIFT_VERSION = 6.0/' \
		>"$release_config_file"

	create_file_commit "$name" "$file_path" "$release_config_file"
}

# A commit that moves a file without changing it, so `git diff` sees a rename unless renames are turned off.
create_rename_commit() {
	local name="$1"
	local from_path="$2"
	local to_path="$3"
	local temp_dir
	local index_file
	local blob_sha
	local tree_sha

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-rename-detect-test.XXXXXX")"
	index_file="${temp_dir}/index"

	GIT_INDEX_FILE="$index_file" git -C "${REPO_ROOT}" read-tree "$HEAD_SHA"
	blob_sha="$(git -C "${REPO_ROOT}" rev-parse "${HEAD_SHA}:${from_path}")"
	GIT_INDEX_FILE="$index_file" git -C "${REPO_ROOT}" update-index --force-remove "$from_path"
	GIT_INDEX_FILE="$index_file" git -C "${REPO_ROOT}" update-index --add --cacheinfo "100644,${blob_sha},${to_path}"
	tree_sha="$(GIT_INDEX_FILE="$index_file" git -C "${REPO_ROOT}" write-tree)"

	GIT_AUTHOR_NAME="TuIndice CI Test" \
		GIT_AUTHOR_EMAIL="tuindice-ci-test@example.invalid" \
		GIT_COMMITTER_NAME="TuIndice CI Test" \
		GIT_COMMITTER_EMAIL="tuindice-ci-test@example.invalid" \
		git -C "${REPO_ROOT}" commit-tree "$tree_sha" -p "$HEAD_SHA" -m "test ${name}"
}

# Runs the detector over a real diff (no changed-files override); the results stay in RENAME_SCOPE / RENAME_CHANGED.
run_rename_fixture() {
	local before_sha="$1"
	local after_sha="$2"
	local temp_dir

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-rename-run.XXXXXX")"
	(
		cd "${REPO_ROOT}"
		STATE_DIR="${temp_dir}/state" bash ./.github/scripts/detect-changed-app.sh "$before_sha" "$after_sha"
	) >"${temp_dir}/output.log" 2>&1
	RENAME_SCOPE="${temp_dir}/state/e2e-scope.csv"
	RENAME_CHANGED="${temp_dir}/state/changed-files.txt"
}

join_changed_paths() {
	printf '%s\n' "$@"
}

run_detector_fixture() {
	local name="$1"
	local changed_paths="$2"
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
	printf '%s\n' "$changed_paths" >"$changed_files_file"

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
			assert_file_empty "${temp_dir}/state/ios-test-gradle-tasks.txt" "iOS test tasks"
			assert_file_empty "${temp_dir}/state/ios-host-gradle-tasks.txt" "iOS host tasks"
			assert_file_contains_line "$github_output_file" "app_version_changed=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "ios_uitest_build_required=false" "GitHub output"
			;;
		e2e-loose-script)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eContract" "Android tasks"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			;;
		certification-skill)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eContract" "Android tasks"
			assert_file_contains_line "$github_output_file" "e2e_contract_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_relevant_changes=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "ios_uitest_build_required=false" "GitHub output"
			;;
		skill-docs)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/android-gradle-tasks.txt" "Android tasks"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "$github_output_file" "has_relevant_changes=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			# A skill is a document the vocabulary gate reads: it runs alone (no harness suite, no Gradle task).
			assert_file_contains_line "$github_output_file" "vocabulary_gate_required=true" "GitHub output"
			;;
		docs-vocabulary)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/android-gradle-tasks.txt" "Android tasks"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "$github_output_file" "vocabulary_gate_required=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_relevant_changes=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "e2e_contract_touched=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			;;
		license-only)
			assert_file_contains_line "$github_output_file" "vocabulary_gate_required=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_relevant_changes=false" "GitHub output"
			;;
		launch-contract-script)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eContract" "Android tasks"
			assert_file_contains_line "$github_output_file" "e2e_contract_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_relevant_changes=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			;;
		ci-shared-script)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eContract" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyAppVersionSync" "Android tasks"
			assert_file_contains_line "$github_output_file" "e2e_contract_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "ci_config_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			;;
		e2e-shared-ci-library)
			# ZD-4: the scripts of CI take info/die, the version readers and the trust list from this file, which lives under
			# the fingerprint: a change to it runs the CI configuration checks (and their fixtures) as well as the contract.
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eContract" "Android tasks"
			assert_file_contains_line "$github_output_file" "ci_config_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "e2e_contract_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "android,local-certification-suite,e2e-harness-shared" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,local-certification-suite,e2e-harness-shared" "E2E scope"
			;;
		ci-other-script)
			assert_file_not_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eContract" "Android tasks"
			assert_file_contains_line "$github_output_file" "e2e_contract_touched=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "ci_config_touched=true" "GitHub output"
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
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "versionName" "missing version bump"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,local-certification-suite,ios-host-runtime" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS tasks"
			assert_file_not_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostTypecheck" "iOS tasks"
			assert_file_contains_line "${temp_dir}/state/ios-host-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS host tasks"
			assert_file_empty "${temp_dir}/state/ios-test-gradle-tasks.txt" "iOS test tasks"
			assert_file_contains_line "$github_output_file" "ios_ci_scripts_touched=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "ios_uitest_build_required=false" "GitHub output"
			# The guard that reads the host Swift runs on the change it protects, not only on a change of the E2E contract.
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyLaunchArgumentContract" "Android tasks"
			assert_file_contains_line "$github_output_file" "vocabulary_gate_required=false" "GitHub output"
			;;
		ios-host-tests-group)
			# A group named like a test target inside the host sources is host runtime, not a unit test.
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "iosApp" "release impacted modules"
			assert_file_lines "${temp_dir}/state/e2e-scope.csv" "E2E scope" "ios,local-certification-suite,ios-host-runtime"
			assert_file_contains_line "$github_output_file" "has_release_impact=true" "GitHub output"
			;;
		ios-unit-tests-dir)
			# The directory iosApp/<name>Tests/ really is a unit test source: no runtime impact, no evidence.
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "iosApp" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			;;
		kmp-test-named-package)
			# A package ending in Test deeper in a runtime source set is runtime of both platforms.
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "auth" "release impacted modules"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "android,local-certification-suite,module-runtime" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,local-certification-suite,module-runtime" "E2E scope"
			assert_file_contains_line "$github_output_file" "has_release_impact=true" "GitHub output"
			;;
		kmp-tests-named-package)
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "android,local-certification-suite,module-runtime" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,local-certification-suite,module-runtime" "E2E scope"
			assert_file_contains_line "$github_output_file" "has_release_impact=true" "GitHub output"
			;;
		product-ui-tags)
			# The tags the catalog is generated from: the committed artifacts must be compared, not only regenerated.
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "scenarios" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":scenarios:testAndroidHostTest" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eArtifactsFresh" "Android tasks"
			;;
		ios-version-xcconfig)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/missing-version-bump.txt" "missing version bump"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyAppVersionSync" "Android tasks"
			assert_file_empty "${temp_dir}/state/ios-gradle-tasks.txt" "iOS tasks"
			assert_file_contains_line "$github_output_file" "app_version_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "app_version_changed=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			;;
		ios-release-signing)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/missing-version-bump.txt" "missing version bump"
			# The fingerprint of iOS reads the file whole, so a signing-only change still asks for iOS evidence (and only iOS).
			assert_file_lines "${temp_dir}/state/e2e-scope.csv" "E2E scope" "ios,local-certification-suite,ios-signing-config"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyAppVersionSync" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS tasks"
			assert_file_contains_line "${temp_dir}/state/ios-host-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS host tasks"
			assert_file_empty "${temp_dir}/state/ios-test-gradle-tasks.txt" "iOS test tasks"
			assert_file_contains_line "$github_output_file" "ci_config_touched=true" "GitHub output"
			;;
		unclassified-path)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/missing-version-bump.txt" "missing version bump"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "$github_output_file" "has_relevant_changes=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			;;
		ios-release-runtime)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "iosApp" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "iosApp" "release impacted modules"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "iosBuildNumber" "missing version bump"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,local-certification-suite,ios-host-runtime" "E2E scope"
			assert_file_contains_line "$github_output_file" "has_release_impact=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		android-version-code)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "app" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "app" "release impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":app:testDebugUnitTest" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":app:bundleRelease" "Android tasks"
			assert_file_empty "${temp_dir}/state/ios-gradle-tasks.txt" "iOS tasks"
			assert_file_empty "${temp_dir}/state/ios-test-gradle-tasks.txt" "iOS test tasks"
			assert_file_empty "${temp_dir}/state/ios-host-gradle-tasks.txt" "iOS host tasks"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "iosBuildNumber" "missing version bump"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "versionName" "missing version bump"
			assert_file_not_contains_line "${temp_dir}/state/missing-version-bump.txt" "androidVersionCode" "missing version bump"
			assert_file_contains_line "$github_output_file" "app_version_changed=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "app_release_build_numbers_changed=false" "GitHub output"
			;;
		ios-build-number)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "iosApp" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "iosApp" "release impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyAppVersionSync" "Android tasks"
			assert_file_not_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":app:bundleRelease" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS tasks"
			assert_file_not_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostTypecheck" "iOS tasks"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "androidVersionCode" "missing version bump"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "versionName" "missing version bump"
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
			assert_file_not_contains_line "${temp_dir}/state/missing-version-bump.txt" "versionName" "missing version bump"
			assert_file_contains_line "$github_output_file" "app_version_changed=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "app_release_build_numbers_changed=false" "GitHub output"
			;;
		release-build-numbers)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "app" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "iosApp" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "app" "release impacted modules"
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "iosApp" "release impacted modules"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "versionName" "missing version bump"
			assert_file_not_contains_line "${temp_dir}/state/missing-version-bump.txt" "androidVersionCode" "missing version bump"
			assert_file_not_contains_line "${temp_dir}/state/missing-version-bump.txt" "iosBuildNumber" "missing version bump"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":app:testDebugUnitTest" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":app:bundleRelease" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS tasks"
			assert_file_not_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostTypecheck" "iOS tasks"
			assert_file_contains_line "$github_output_file" "app_version_changed=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "app_release_build_numbers_changed=true" "GitHub output"
			;;
		scenarios-test-only)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "scenarios" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":scenarios:testAndroidHostTest" "Android tasks"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			;;
		scenarios-sources)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "scenarios" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/missing-version-bump.txt" "missing version bump"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "android,local-certification-suite,e2e-scenarios" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,local-certification-suite,e2e-scenarios" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":scenarios:testAndroidHostTest" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eContract" "Android tasks"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			assert_file_lines "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts" "local-e2e/android/local-certification-suite"
			assert_file_lines "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts" "local-e2e/ios/local-certification-suite"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		scenariokit-test-only)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "scenariokit" "impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":scenariokit:testAndroidHostTest" "Android tasks"
			assert_file_not_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":scenariorunner:compileAndroidMain" "Android tasks"
			# A kit test source does not reach the runner: nothing to assemble.
			assert_file_not_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":scenariorunner:assembleDebug" "Android tasks"
			assert_file_not_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" ":scenariorunner:compileKotlinIosSimulatorArm64" "iOS tasks"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			;;
		scenariokit-common)
			assert_file_contains_line "$github_output_file" "ios_uitest_build_required=true" "GitHub output"
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "scenariokit" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "scenarios" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "android,local-certification-suite,e2e-scenariokit" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,local-certification-suite,e2e-scenariokit" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":scenariokit:testAndroidHostTest" "Android tasks"
			assert_file_not_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":scenariorunner:compileAndroidMain" "Android tasks"
			# ZD-14: a runtime change of the kit reaches the runner, so the runner is assembled (the loop of impacted modules gives it).
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":scenariorunner:assembleDebug" "Android tasks"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			assert_file_lines "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts" "local-e2e/android/local-certification-suite"
			assert_file_lines "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts" "local-e2e/ios/local-certification-suite"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		scenariokit-android-source-set)
			assert_file_contains_line "$github_output_file" "ios_uitest_build_required=false" "GitHub output"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "android,local-certification-suite,e2e-scenariokit" "E2E scope"
			assert_file_not_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,local-certification-suite,e2e-scenariokit" "E2E scope"
			assert_file_lines "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts" "local-e2e/android/local-certification-suite"
			assert_file_empty "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		scenariokit-ios-source-set)
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,local-certification-suite,e2e-scenariokit" "E2E scope"
			assert_file_not_contains_line "${temp_dir}/state/e2e-scope.csv" "android,local-certification-suite,e2e-scenariokit" "E2E scope"
			assert_file_empty "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts"
			assert_file_lines "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts" "local-e2e/ios/local-certification-suite"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		scenariorunner)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "scenariorunner" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":scenariorunner:assembleDebug" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eContract" "Android tasks"
			assert_file_empty "${temp_dir}/state/ios-gradle-tasks.txt" "iOS tasks"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			assert_file_lines "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts" "local-e2e/android/local-certification-suite"
			assert_file_empty "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		persistence-runtime)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "persistence" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "wizard" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "subjects" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "android,local-certification-suite,module-runtime" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,local-certification-suite,module-runtime" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":wizard:testAndroidHostTest" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyLaunchArgumentContract" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eArtifactsFresh" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/ios-test-gradle-tasks.txt" ":persistence:iosSimulatorArm64Test" "iOS test tasks"
			assert_file_contains_line "${temp_dir}/state/ios-test-gradle-tasks.txt" ":wizard:compileKotlinIosSimulatorArm64" "iOS test tasks"
			assert_file_contains_line "${temp_dir}/state/ios-host-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS host tasks"
			assert_file_not_contains_line "${temp_dir}/state/ios-test-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS test tasks"
			assert_file_lines "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts" "local-e2e/android/local-certification-suite"
			assert_file_lines "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts" "local-e2e/ios/local-certification-suite"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		feature-module-dependents)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "record" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "wizard" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "maincore" "impacted modules"
			assert_file_lines "${temp_dir}/state/e2e-scope.csv" "E2E scope" \
				"android,local-certification-suite,module-runtime" \
				"ios,local-certification-suite,module-runtime"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":wizard:testAndroidHostTest" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":record:detekt" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":wizard:detekt" "Android tasks"
			assert_file_contains_line "${temp_dir}/state/ios-test-gradle-tasks.txt" ":record:compileKotlinIosSimulatorArm64" "iOS test tasks"
			assert_file_contains_line "${temp_dir}/state/ios-test-gradle-tasks.txt" ":record:iosSimulatorArm64Test" "iOS test tasks"
			assert_file_contains_line "${temp_dir}/state/ios-host-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS host tasks"
			assert_file_not_contains_line "${temp_dir}/state/ios-host-gradle-tasks.txt" ":record:iosSimulatorArm64Test" "iOS host tasks"
			assert_file_contains_line "$github_output_file" "semgrep_required=true" "GitHub output"
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
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" ":record:detekt" "Android tasks"
			assert_file_contains_line "$github_output_file" "module_graph_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=true" "GitHub output"
			;;
		ios-build-script)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS tasks"
			assert_file_contains_line "${temp_dir}/state/ios-host-gradle-tasks.txt" "verifyIosHostBuildDeviceRelease" "iOS host tasks"
			assert_file_empty "${temp_dir}/state/ios-test-gradle-tasks.txt" "iOS test tasks"
			assert_file_contains_line "$github_output_file" "ios_ci_scripts_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			# It builds the framework the E2E app links: iOS evidence and the UI test build job (D-3, D-9).
			assert_file_lines "${temp_dir}/state/e2e-scope.csv" "E2E scope" "ios,local-certification-suite,e2e-ios-build-scripts"
			assert_file_contains_line "$github_output_file" "ios_uitest_build_required=true" "GitHub output"
			;;
		ios-build-script-uitest|ios-firebase-script|ios-sync-version-script)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_lines "${temp_dir}/state/e2e-scope.csv" "E2E scope" "ios,local-certification-suite,e2e-ios-build-scripts"
			assert_file_empty "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts"
			assert_file_lines "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts" "local-e2e/ios/local-certification-suite"
			assert_file_contains_line "$github_output_file" "ios_uitest_build_required=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			;;
		ios-harness-build-sh)
			assert_file_lines "${temp_dir}/state/e2e-scope.csv" "E2E scope" "ios,local-certification-suite,e2e-harness-ios"
			assert_file_contains_line "$github_output_file" "ios_uitest_build_required=true" "GitHub output"
			;;
		root-build-file)
			assert_file_lines "${temp_dir}/state/e2e-scope.csv" "E2E scope" \
				"android,local-certification-suite,root-build" \
				"ios,local-certification-suite,root-build"
			assert_file_contains_line "$github_output_file" "has_release_impact=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "module_graph_touched=true" "GitHub output"
			;;
		e2e-tasks-script)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eContract" "Android tasks"
			assert_file_contains_line "$github_output_file" "e2e_contract_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			;;
		gradle-unread-file)
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "$github_output_file" "has_release_impact=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			;;
		app-proguard-rules)
			assert_file_contains_line "${temp_dir}/state/release-impacted-modules.txt" "app" "release impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "$github_output_file" "has_release_impact=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			;;
		app-test-debug-source)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "app" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			;;
		scenariokit-appledir-source-set)
			assert_file_lines "${temp_dir}/state/e2e-scope.csv" "E2E scope" "ios,local-certification-suite,e2e-scenariokit"
			assert_file_contains_line "$github_output_file" "ios_uitest_build_required=true" "GitHub output"
			;;
		scenarios-ios-source-set)
			assert_file_lines "${temp_dir}/state/e2e-scope.csv" "E2E scope" "ios,local-certification-suite,e2e-scenarios"
			assert_file_contains_line "$github_output_file" "ios_uitest_build_required=false" "GitHub output"
			;;
		ios-typecheck-script)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/ios-gradle-tasks.txt" "verifyIosHostTypecheck" "iOS tasks"
			assert_file_contains_line "${temp_dir}/state/ios-host-gradle-tasks.txt" "verifyIosHostTypecheck" "iOS host tasks"
			assert_file_empty "${temp_dir}/state/ios-test-gradle-tasks.txt" "iOS test tasks"
			assert_file_contains_line "$github_output_file" "ios_ci_scripts_touched=true" "GitHub output"
			;;
		detekt-config)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "detekt" "Android tasks"
			assert_file_empty "${temp_dir}/state/ios-gradle-tasks.txt" "iOS tasks"
			assert_file_contains_line "$github_output_file" "semgrep_required=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			;;
		semgrep-config)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_empty "${temp_dir}/state/android-gradle-tasks.txt" "Android tasks"
			assert_file_empty "${temp_dir}/state/ios-gradle-tasks.txt" "iOS tasks"
			assert_file_contains_line "$github_output_file" "semgrep_config_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "semgrep_required=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_relevant_changes=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			;;
		detekt-baseline)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "detekt" "Android tasks"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			;;
		e2e-catalog|e2e-harness-shared|mocks-all)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/missing-version-bump.txt" "missing version bump"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eContract" "Android tasks"
			assert_file_contains_line "$github_output_file" "e2e_contract_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			assert_file_contains_line "$github_output_file" "semgrep_required=false" "GitHub output"
			assert_file_lines "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts" "local-e2e/android/local-certification-suite"
			assert_file_lines "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts" "local-e2e/ios/local-certification-suite"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			# The catalog is what ScenarioKit and the XCUITest class list are built from; the harness and the mocks are not.
			if [[ "$name" == "e2e-catalog" ]]; then
				assert_file_contains_line "$github_output_file" "ios_uitest_build_required=true" "GitHub output"
			else
				assert_file_contains_line "$github_output_file" "ios_uitest_build_required=false" "GitHub output"
			fi
			;;
		e2e-harness-android)
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eContract" "Android tasks"
			assert_file_not_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,local-certification-suite,e2e-harness-android" "E2E scope"
			assert_file_lines "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts" "local-e2e/android/local-certification-suite"
			assert_file_empty "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		e2e-harness-ios)
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eContract" "Android tasks"
			assert_file_not_contains_line "${temp_dir}/state/e2e-scope.csv" "android,local-certification-suite,e2e-harness-ios" "E2E scope"
			assert_file_empty "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts"
			assert_file_lines "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts" "local-e2e/ios/local-certification-suite"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		ios-uitests)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_empty "${temp_dir}/state/release-impacted-modules.txt" "release impacted modules"
			assert_file_empty "${temp_dir}/state/missing-version-bump.txt" "missing version bump"
			assert_file_empty "${temp_dir}/state/ios-gradle-tasks.txt" "iOS tasks"
			assert_file_contains_line "$github_output_file" "ios_uitest_build_required=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_relevant_changes=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "has_release_impact=false" "GitHub output"
			assert_file_empty "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts"
			assert_file_lines "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts" "local-e2e/ios/local-certification-suite"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		ios-xcodeproj-scheme|ios-podfile)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "iosApp" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/e2e-scope.csv" "ios,local-certification-suite,ios-host-runtime" "E2E scope"
			assert_file_contains_line "$github_output_file" "ios_uitest_build_required=true" "GitHub output"
			assert_file_empty "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts"
			assert_file_lines "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts" "local-e2e/ios/local-certification-suite"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		ios-ui-test-xcconfig)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_contains_line "$github_output_file" "ios_uitest_build_required=true" "GitHub output"
			assert_file_empty "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts"
			assert_file_lines "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts" "local-e2e/ios/local-certification-suite"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		e2e-tools)
			assert_file_empty "${temp_dir}/state/impacted-modules.txt" "impacted modules"
			assert_file_contains_line "${temp_dir}/state/android-gradle-tasks.txt" "verifyE2eContract" "Android tasks"
			assert_file_contains_line "$github_output_file" "e2e_contract_touched=true" "GitHub output"
			assert_file_contains_line "$github_output_file" "ios_uitest_build_required=false" "GitHub output"
			assert_file_empty "${temp_dir}/state/e2e-scope.csv" "E2E scope"
			assert_file_empty "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts"
			assert_file_empty "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=false" "GitHub output"
			;;
		kmp-android-source-set)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "auth" "impacted modules"
			assert_file_contains_line "$github_output_file" "has_release_impact=true" "GitHub output"
			assert_file_lines "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts" "local-e2e/android/local-certification-suite"
			assert_file_empty "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		kmp-ios-source-set)
			assert_file_contains_line "${temp_dir}/state/impacted-modules.txt" "auth" "impacted modules"
			assert_file_contains_line "$github_output_file" "has_release_impact=true" "GitHub output"
			assert_file_empty "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts"
			assert_file_lines "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts" "local-e2e/ios/local-certification-suite"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			;;
		platform-asymmetric-scope)
			assert_file_lines "${temp_dir}/state/e2e-scope.csv" "E2E scope" \
				"android,local-certification-suite,module-runtime" \
				"ios,local-certification-suite,ios-host-runtime"
			assert_file_lines "${temp_dir}/state/e2e-android-contexts.txt" "E2E Android contexts" "local-e2e/android/local-certification-suite"
			assert_file_lines "${temp_dir}/state/e2e-ios-contexts.txt" "E2E iOS contexts" "local-e2e/ios/local-certification-suite"
			assert_file_contains_line "$github_output_file" "requires_e2e_certification=true" "GitHub output"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "versionName" "missing version bump"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "androidVersionCode" "missing version bump"
			assert_file_contains_line "${temp_dir}/state/missing-version-bump.txt" "iosBuildNumber" "missing version bump"
			assert_file_contains_line "$github_output_file" "has_release_impact=true" "GitHub output"
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
ios_release_runtime_commit="$(
	create_ios_release_runtime_commit ios-release-runtime
)"

run_detector_fixture e2e-runner e2e/tools/tests/support.py
run_detector_fixture e2e-loose-script e2e/scripts/stray.sh
run_detector_fixture certification-skill .codex/skills/certify-tuindice-pr/scripts/run_preflight_parity_checks.sh
run_detector_fixture skill-docs .codex/skills/implement-tuindice-module/SKILL.md
run_detector_fixture docs-vocabulary docs/release-pipeline.md
run_detector_fixture docs-vocabulary README.md
run_detector_fixture docs-vocabulary AGENTS.md
# ZD-11: the instructions for the assistants and the versioned .claude entries are documents the gate reads too.
run_detector_fixture docs-vocabulary CLAUDE.md
run_detector_fixture docs-vocabulary .claude/skills/certify-tuindice-pr/SKILL.md
run_detector_fixture license-only LICENSE
run_detector_fixture launch-contract-script scripts/verify-launch-argument-contract.sh
run_detector_fixture launch-contract-script scripts/verify-e2e-artifacts.sh
run_detector_fixture ci-shared-script .github/scripts/common.sh
run_detector_fixture ci-shared-script .github/scripts/detect-changed-app.sh
run_detector_fixture ci-other-script .github/scripts/deploy-production.sh
run_detector_fixture ios-host-tests-group iosApp/Sources/FooTests/Probe.swift
run_detector_fixture ios-unit-tests-dir iosApp/TuIndiceHostTests/Probe.swift
run_detector_fixture kmp-test-named-package auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/abTest/Probe.kt
run_detector_fixture kmp-tests-named-package auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/fooTests/Probe.kt
run_detector_fixture product-ui-tags auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/AuthUiTags.kt
run_detector_fixture ios-script-tooling iosApp/scripts/ci-upload-ios-appstore.sh
run_detector_fixture scenarios-test-only scenarios/src/androidHostTest/kotlin/com/gdavidpb/tuindice/scenarios/CatalogShapeTest.kt
run_detector_fixture scenarios-sources scenarios/src/commonMain/kotlin/com/gdavidpb/tuindice/scenarios/catalog/E2eCatalog.kt
run_detector_fixture scenariokit-test-only scenariokit/src/commonTest/kotlin/com/gdavidpb/tuindice/scenariokit/StepTest.kt
run_detector_fixture scenariokit-common scenariokit/src/commonMain/kotlin/com/gdavidpb/tuindice/scenariokit/model/Step.kt
run_detector_fixture scenariokit-android-source-set scenariokit/src/androidMain/kotlin/com/gdavidpb/tuindice/scenariokit/Platform.android.kt
run_detector_fixture scenariokit-ios-source-set scenariokit/src/iosMain/kotlin/com/gdavidpb/tuindice/scenariokit/Platform.ios.kt
run_detector_fixture scenariorunner scenariorunner/src/main/kotlin/com/gdavidpb/tuindice/scenariorunner/ScenarioSuiteTest.kt
run_detector_fixture persistence-runtime persistence/src/commonMain/kotlin/com/gdavidpb/tuindice/persistence/data/repository/MutationRepository.kt
run_detector_fixture feature-module-dependents record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/presentation/RecordScreen.kt
run_detector_fixture module-graph-config scripts/module-graph.txt
run_detector_fixture module-build-file record/build.gradle.kts
run_detector_fixture ios-build-script iosApp/scripts/build-kmp-framework.sh
run_detector_fixture ios-typecheck-script iosApp/scripts/ci-typecheck-ios-host.sh
run_detector_fixture ios-build-script-uitest iosApp/scripts/build-scenario-kit.sh
run_detector_fixture ios-build-script-uitest iosApp/scripts/verify-ui-test-target.sh
run_detector_fixture ios-build-script-uitest iosApp/scripts/add-ui-test-target.rb
run_detector_fixture ios-firebase-script .github/scripts/materialize-firebase-configs.sh
run_detector_fixture ios-sync-version-script .github/scripts/sync-app-version.sh
run_detector_fixture ios-harness-build-sh e2e/scripts/ios/build.sh
run_detector_fixture root-build-file build.gradle.kts
run_detector_fixture root-build-file gradle/libs.versions.toml
run_detector_fixture root-build-file gradle/gradle-daemon-jvm.properties
run_detector_fixture root-build-file gradle/wrapper/gradle-wrapper.jar
run_detector_fixture root-build-file gradlew
run_detector_fixture e2e-tasks-script gradle/e2e-tasks.gradle.kts
run_detector_fixture gradle-unread-file gradle/some-unread-file.txt
run_detector_fixture app-proguard-rules app/proguard-rules.pro
run_detector_fixture app-test-debug-source app/src/testDebug/kotlin/com/gdavidpb/tuindice/Sample.kt
run_detector_fixture scenariokit-appledir-source-set scenariokit/src/appleMain/kotlin/com/gdavidpb/tuindice/scenariokit/Platform.apple.kt
run_detector_fixture scenarios-ios-source-set scenarios/src/iosMain/kotlin/com/gdavidpb/tuindice/scenarios/Platform.ios.kt
run_detector_fixture detekt-config config/detekt/detekt.yml
run_detector_fixture detekt-baseline evaluations/detekt-baseline.xml
run_detector_fixture semgrep-config config/semgrep/rules/layering.yaml
run_detector_fixture ios-host-runtime iosApp/Sources/TuIndiceHost/TuIndiceAppBootstrap.swift
run_detector_fixture ios-uitests iosApp/UITests/XCUIScenarioDriver.swift
run_detector_fixture ios-ui-test-xcconfig iosApp/Config/UITests.xcconfig
run_detector_fixture ios-xcodeproj-scheme iosApp/TuIndiceHost.xcodeproj/xcshareddata/xcschemes/TuIndiceUITests.xcscheme
run_detector_fixture ios-podfile iosApp/Podfile
run_detector_fixture e2e-catalog e2e/catalog/scenarios.json
run_detector_fixture e2e-harness-shared e2e/scripts/shared/e2e.py
run_detector_fixture e2e-shared-ci-library e2e/scripts/shared/ci-common.sh
run_detector_fixture e2e-shared-ci-library e2e/scripts/shared/layout.env
run_detector_fixture e2e-harness-android "$(join_changed_paths e2e/scripts/android/adapter.sh e2e/toolchain/android.lock)"
run_detector_fixture e2e-harness-ios "$(join_changed_paths e2e/scripts/ios/adapter.sh e2e/toolchain/ios.lock)"
run_detector_fixture e2e-tools "$(join_changed_paths e2e/tools/tests/test_runner.py e2e/platform/android/README.md e2e/README.md)"
run_detector_fixture mocks-all mocks/mappings/login/auth-email-success.json
run_detector_fixture kmp-android-source-set auth/src/androidMain/kotlin/com/gdavidpb/tuindice/auth/Android.kt
run_detector_fixture kmp-ios-source-set auth/src/iosMain/kotlin/com/gdavidpb/tuindice/auth/Ios.kt
run_detector_fixture ios-version-xcconfig iosApp/Config/Version.xcconfig
run_detector_fixture ios-release-signing iosApp/Config/Release.xcconfig "$HEAD_SHA" "$ios_release_signing_commit"
run_detector_fixture ios-release-runtime iosApp/Config/Release.xcconfig "$HEAD_SHA" "$ios_release_runtime_commit"
run_detector_fixture unclassified-path some-unmapped-top-level/nested/file.txt
run_detector_fixture android-version-code "$APP_VERSION_FILE" "$HEAD_SHA" "$android_version_commit"
run_detector_fixture ios-build-number "$APP_VERSION_FILE" "$HEAD_SHA" "$ios_build_commit"
run_detector_fixture release-build-numbers "$APP_VERSION_FILE" "$HEAD_SHA" "$release_build_numbers_commit"
run_detector_fixture version-name "$APP_VERSION_FILE" "$HEAD_SHA" "$version_name_commit"

run_detector_fixture platform-asymmetric-scope "$(
	join_changed_paths \
		record/src/androidMain/kotlin/com/gdavidpb/tuindice/record/Android.kt \
		iosApp/Sources/TuIndiceHost/TuIndiceAppBootstrap.swift
)"

# D-1: a file moved out of a runtime path is a deletion there. A plain diff folds a rename into its destination
# and the destination alone (a test source set) asks for no evidence.
renamed_commit="$(
	create_rename_commit rename-out-of-runtime \
		auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/data/model/BootstrapTokensResponse.kt \
		auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/data/model/BootstrapTokensResponse.kt
)"
run_rename_fixture "$HEAD_SHA" "$renamed_commit"
assert_file_contains_line "$RENAME_CHANGED" "auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/data/model/BootstrapTokensResponse.kt" "changed files of a rename"
assert_file_contains_line "$RENAME_SCOPE" "android,local-certification-suite,module-runtime" "E2E scope of a rename out of runtime"
assert_file_contains_line "$RENAME_SCOPE" "ios,local-certification-suite,module-runtime" "E2E scope of a rename out of runtime"

# D-18: a name with a space and a non-ASCII character is listed unquoted whatever core.quotePath says; quoted, it would
# match no path pattern and ask for nothing.
odd_source="$(mktemp "${RUNNER_TEMP:-/tmp}/tuindice-odd-path.XXXXXX")"
printf 'let x = 1\n' >"$odd_source"
odd_commit="$(create_file_commit odd-path 'iosApp/Sources/TuIndiceHost/Café menú.swift' "$odd_source")"
for quote_path in true false; do
	GIT_CONFIG_COUNT=1 GIT_CONFIG_KEY_0=core.quotePath GIT_CONFIG_VALUE_0="$quote_path" run_rename_fixture "$HEAD_SHA" "$odd_commit"
	assert_file_lines "$RENAME_CHANGED" "changed files with core.quotePath=${quote_path}" 'iosApp/Sources/TuIndiceHost/Café menú.swift'
	assert_file_contains_line "$RENAME_SCOPE" "ios,local-certification-suite,ios-host-runtime" "E2E scope of a non-ASCII path with core.quotePath=${quote_path}"
done
rm -f "$odd_source"

printf 'Detect changed app shell fixtures passed.\n'
