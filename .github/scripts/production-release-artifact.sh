#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

require_tool jq

COMMAND="${1:-}"
TARGET_GIT_SHA="${TARGET_GIT_SHA:-${GITHUB_SHA:-$(git rev-parse HEAD)}}"
RELEASE_ARTIFACT_DIR="${RELEASE_ARTIFACT_DIR:-build/production-release}"
MANIFEST_PATH="${RELEASE_MANIFEST_PATH:-${RELEASE_ARTIFACT_DIR}/release-manifest.json}"
VERSION_NAME="$(get_app_version_name)"
TAG_NAME="$(app_tag_name "$VERSION_NAME")"
ANDROID_VERSION_CODE="$(get_android_version_code)"
IOS_BUILD_NUMBER="$(get_ios_build_number)"

sha256_file() {
	local file="$1"
	shasum -a 256 "$file" | awk '{ print $1 }'
}

relative_path() {
	local path="$1"
	local base="$2"

	case "$path" in
		"${base}"/*)
			printf '%s\n' "${path#"${base}/"}"
			;;
		*)
			die "${path} must be inside ${base}."
			;;
	esac
}

copy_into_artifact_dir() {
	local source_path="$1"
	local target_path="$2"

	[[ -s "$source_path" ]] || die "Release artifact input not found: ${source_path}"
	mkdir -p "$(dirname "$target_path")"
	cp "$source_path" "$target_path"
}

write_manifest() {
	local android_source="${ANDROID_AAB_PATH:?ANDROID_AAB_PATH is required.}"
	local ios_source="${IOS_IPA_PATH:?IOS_IPA_PATH is required.}"
	local android_target="${RELEASE_ARTIFACT_DIR}/android/$(basename "$android_source")"
	local ios_target="${RELEASE_ARTIFACT_DIR}/ios/$(basename "$ios_source")"
	local android_relative
	local ios_relative

	mkdir -p "$RELEASE_ARTIFACT_DIR"
	copy_into_artifact_dir "$android_source" "$android_target"
	copy_into_artifact_dir "$ios_source" "$ios_target"

	android_relative="$(relative_path "$android_target" "$RELEASE_ARTIFACT_DIR")"
	ios_relative="$(relative_path "$ios_target" "$RELEASE_ARTIFACT_DIR")"

	jq -n \
		--arg git_sha "$TARGET_GIT_SHA" \
		--arg version_name "$VERSION_NAME" \
		--arg tag_name "$TAG_NAME" \
		--arg android_version_code "$ANDROID_VERSION_CODE" \
		--arg android_aab_path "$android_relative" \
		--arg android_sha256 "$(sha256_file "$android_target")" \
		--arg ios_build_number "$IOS_BUILD_NUMBER" \
		--arg ios_ipa_path "$ios_relative" \
		--arg ios_sha256 "$(sha256_file "$ios_target")" \
		--arg run_id "${GITHUB_RUN_ID:-}" \
		--arg run_attempt "${GITHUB_RUN_ATTEMPT:-}" \
		'{
			gitSha: $git_sha,
			versionName: $version_name,
			tagName: $tag_name,
			android: {
				versionCode: ($android_version_code | tonumber),
				aabPath: $android_aab_path,
				sha256: $android_sha256
			},
			ios: {
				buildNumber: ($ios_build_number | tonumber),
				ipaPath: $ios_ipa_path,
				sha256: $ios_sha256
			},
			workflow: {
				runId: $run_id,
				runAttempt: $run_attempt
			}
		}' >"$MANIFEST_PATH"

	info "Production release manifest written to ${MANIFEST_PATH}."
}

manifest_value() {
	local jq_filter="$1"
	jq -r "$jq_filter" "$MANIFEST_PATH"
}

assert_manifest_value() {
	local label="$1"
	local actual="$2"
	local expected="$3"

	if [[ "$actual" != "$expected" ]]; then
		die "Invalid release manifest ${label}: expected '${expected}', got '${actual}'."
	fi
}

validate_checksum() {
	local label="$1"
	local relative_path_value="$2"
	local expected_sha="$3"
	local absolute_path="${RELEASE_ARTIFACT_DIR}/${relative_path_value}"
	local actual_sha

	[[ -s "$absolute_path" ]] || die "${label} artifact not found: ${absolute_path}"
	actual_sha="$(sha256_file "$absolute_path")"
	assert_manifest_value "${label} sha256" "$actual_sha" "$expected_sha"
}

validate_manifest() {
	local android_path
	local ios_path

	[[ -s "$MANIFEST_PATH" ]] || die "Release manifest not found: ${MANIFEST_PATH}"

	assert_manifest_value "gitSha" "$(manifest_value '.gitSha')" "$TARGET_GIT_SHA"
	assert_manifest_value "versionName" "$(manifest_value '.versionName')" "$VERSION_NAME"
	assert_manifest_value "tagName" "$(manifest_value '.tagName')" "$TAG_NAME"
	assert_manifest_value "android.versionCode" "$(manifest_value '.android.versionCode | tostring')" "$ANDROID_VERSION_CODE"
	assert_manifest_value "ios.buildNumber" "$(manifest_value '.ios.buildNumber | tostring')" "$IOS_BUILD_NUMBER"

	android_path="$(manifest_value '.android.aabPath')"
	ios_path="$(manifest_value '.ios.ipaPath')"

	validate_checksum "Android" "$android_path" "$(manifest_value '.android.sha256')"
	validate_checksum "iOS" "$ios_path" "$(manifest_value '.ios.sha256')"

	if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
		printf 'android_aab_path=%s\n' "${RELEASE_ARTIFACT_DIR}/${android_path}" >>"$GITHUB_OUTPUT"
		printf 'ios_ipa_path=%s\n' "${RELEASE_ARTIFACT_DIR}/${ios_path}" >>"$GITHUB_OUTPUT"
	fi

	info "Production release artifact validated for ${VERSION_NAME} (${TARGET_GIT_SHA})."
}

case "$COMMAND" in
	write-manifest)
		write_manifest
		;;
	validate-manifest)
		validate_manifest
		;;
	*)
		die "Usage: $0 write-manifest|validate-manifest"
		;;
esac
