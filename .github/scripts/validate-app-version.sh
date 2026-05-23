#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

APP_BUILD_GRADLE="app/build.gradle"
VERSION_NAME="$(get_app_version_name)"
ANDROID_VERSION_CODE="$(get_android_version_code)"
IOS_BUILD_NUMBER="$(get_ios_build_number)"

validate_semver "$VERSION_NAME" || die "App versionName '${VERSION_NAME}' is invalid. Expected semantic version format X.Y.Z."
validate_positive_integer "$ANDROID_VERSION_CODE" || die "androidVersionCode '${ANDROID_VERSION_CODE}' is invalid. Expected a positive integer."
validate_positive_integer "$IOS_BUILD_NUMBER" || die "iosBuildNumber '${IOS_BUILD_NUMBER}' is invalid. Expected a positive integer."

[[ -f "$APP_BUILD_GRADLE" ]] || die "Missing ${APP_BUILD_GRADLE}."

grep -q 'rootProject.file("gradle/app-version.properties")' "$APP_BUILD_GRADLE" \
	|| die "${APP_BUILD_GRADLE} must read version values from $(app_version_file)."
grep -qE '^[[:space:]]*versionCode[[:space:]]+androidVersionCode[[:space:]]*$' "$APP_BUILD_GRADLE" \
	|| die "${APP_BUILD_GRADLE} must use 'versionCode androidVersionCode'."
grep -qE '^[[:space:]]*versionName[[:space:]]+appVersionName[[:space:]]*$' "$APP_BUILD_GRADLE" \
	|| die "${APP_BUILD_GRADLE} must use 'versionName appVersionName'."

if grep -qE '^[[:space:]]*versionCode[[:space:]]+[0-9]+[[:space:]]*$' "$APP_BUILD_GRADLE"; then
	die "${APP_BUILD_GRADLE} still contains a hard-coded Android versionCode."
fi

if grep -qE '^[[:space:]]*versionName[[:space:]]+"[^"]+"[[:space:]]*$' "$APP_BUILD_GRADLE"; then
	die "${APP_BUILD_GRADLE} still contains a hard-coded Android versionName."
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
