#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

require_tool jq

TARGET_SHA="$(git -C "$REPO_ROOT" rev-parse HEAD)"

assert_fails() {
	local label="$1"
	shift

	if "$@" >/tmp/tuindice-release-artifact-test.out 2>/tmp/tuindice-release-artifact-test.err; then
		printf 'Expected failure for %s.\n' "$label" >&2
		cat /tmp/tuindice-release-artifact-test.out >&2 || true
		cat /tmp/tuindice-release-artifact-test.err >&2 || true
		exit 1
	fi
}

create_fixture() {
	local temp_dir="$1"

	mkdir -p "${temp_dir}/inputs"
	printf 'android-aab\n' >"${temp_dir}/inputs/app-release.aab"
	printf 'ios-ipa\n' >"${temp_dir}/inputs/TuIndiceHost.ipa"
}

write_fixture_manifest() {
	local temp_dir="$1"

	(
		cd "$REPO_ROOT"
		TARGET_GIT_SHA="$TARGET_SHA" \
		RELEASE_ARTIFACT_DIR="${temp_dir}/artifact" \
		ANDROID_AAB_PATH="${temp_dir}/inputs/app-release.aab" \
		IOS_IPA_PATH="${temp_dir}/inputs/TuIndiceHost.ipa" \
			bash ./.github/scripts/production-release-artifact.sh write-manifest
	)
}

validate_fixture_manifest() {
	local temp_dir="$1"
	local target_sha="${2:-$TARGET_SHA}"

	(
		cd "$REPO_ROOT"
		TARGET_GIT_SHA="$target_sha" \
		RELEASE_ARTIFACT_DIR="${temp_dir}/artifact" \
			bash ./.github/scripts/production-release-artifact.sh validate-manifest
	)
}

run_valid_manifest_fixture() {
	local temp_dir

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-release-artifact-valid.XXXXXX")"
	create_fixture "$temp_dir"
	write_fixture_manifest "$temp_dir"
	validate_fixture_manifest "$temp_dir"
}

run_wrong_sha_fixture() {
	local temp_dir

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-release-artifact-sha.XXXXXX")"
	create_fixture "$temp_dir"
	write_fixture_manifest "$temp_dir"
	assert_fails "wrong sha" validate_fixture_manifest "$temp_dir" "0000000000000000000000000000000000000000"
}

run_wrong_version_fixture() {
	local temp_dir

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-release-artifact-version.XXXXXX")"
	create_fixture "$temp_dir"
	write_fixture_manifest "$temp_dir"
	jq '.versionName = "0.0.0"' "${temp_dir}/artifact/release-manifest.json" >"${temp_dir}/manifest.tmp"
	mv "${temp_dir}/manifest.tmp" "${temp_dir}/artifact/release-manifest.json"
	assert_fails "wrong version" validate_fixture_manifest "$temp_dir"
}

run_wrong_checksum_fixture() {
	local temp_dir

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-release-artifact-checksum.XXXXXX")"
	create_fixture "$temp_dir"
	write_fixture_manifest "$temp_dir"
	printf 'mutated\n' >>"${temp_dir}/artifact/android/app-release.aab"
	assert_fails "wrong checksum" validate_fixture_manifest "$temp_dir"
}

run_missing_file_fixture() {
	local temp_dir

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-release-artifact-missing.XXXXXX")"
	create_fixture "$temp_dir"
	write_fixture_manifest "$temp_dir"
	rm "${temp_dir}/artifact/ios/TuIndiceHost.ipa"
	assert_fails "missing ipa" validate_fixture_manifest "$temp_dir"
}

run_valid_manifest_fixture
run_wrong_sha_fixture
run_wrong_version_fixture
run_wrong_checksum_fixture
run_missing_file_fixture

printf 'Production release artifact fixtures passed for %s.\n' "$TARGET_SHA"
