#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

bash "${SCRIPT_DIR}/sync-app-version.sh"

VERSION_NAME="$(get_app_version_name)"
ANDROID_VERSION_CODE="$(get_android_version_code)"
IOS_BUILD_NUMBER="$(get_ios_build_number)"
VERSION_XCCONFIG="iosApp/Config/Version.xcconfig"

validate_semver "$VERSION_NAME" || die "App versionName '${VERSION_NAME}' is invalid. Expected semantic version format X.Y.Z."
validate_positive_integer "$ANDROID_VERSION_CODE" || die "androidVersionCode '${ANDROID_VERSION_CODE}' is invalid. Expected a positive integer."
validate_positive_integer "$IOS_BUILD_NUMBER" || die "iosBuildNumber '${IOS_BUILD_NUMBER}' is invalid. Expected a positive integer."

EXPECTED_XCCONFIG="$(mktemp "${RUNNER_TEMP:-/tmp}/tuindice-version.XXXXXX")"
write_version_xcconfig_contents "$VERSION_NAME" "$IOS_BUILD_NUMBER" >"$EXPECTED_XCCONFIG"

if [[ ! -f "$VERSION_XCCONFIG" ]]; then
	die "Missing ${VERSION_XCCONFIG}. Run the app version sync step."
fi

if ! cmp -s "$EXPECTED_XCCONFIG" "$VERSION_XCCONFIG"; then
	{
		printf 'Expected %s to contain:\n' "$VERSION_XCCONFIG"
		cat "$EXPECTED_XCCONFIG"
		printf '\nActual contents:\n'
		cat "$VERSION_XCCONFIG"
	} >&2
	die "${VERSION_XCCONFIG} is out of sync with $(app_version_file)."
fi

if grep -qE '^[[:space:]]*(MARKETING_VERSION|CURRENT_PROJECT_VERSION)[[:space:]]*=' iosApp/TuIndiceHost.xcodeproj/project.pbxproj; then
	die "TuIndiceHost.xcodeproj still contains target-level iOS version values. Keep MARKETING_VERSION and CURRENT_PROJECT_VERSION in ${VERSION_XCCONFIG}."
fi

TAG_NAME="$(app_tag_name "$VERSION_NAME")"
TAG_TARGET="$(existing_tag_target "$TAG_NAME" || true)"

if [[ -n "$TAG_TARGET" && -n "${TARGET_GIT_SHA:-${GITHUB_SHA:-}}" ]]; then
	TARGET_SHA="${TARGET_GIT_SHA:-${GITHUB_SHA:-}}"
	if [[ "$TAG_TARGET" != "$TARGET_SHA" ]]; then
		die "Tag ${TAG_NAME} already exists at ${TAG_TARGET}. Bump $(app_version_file) before deploying."
	fi
fi

info "App version is valid: ${VERSION_NAME} androidVersionCode=${ANDROID_VERSION_CODE} iosBuildNumber=${IOS_BUILD_NUMBER} tag=${TAG_NAME}"
