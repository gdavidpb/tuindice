#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

cd "$REPO_ROOT"

VERSION_NAME="$(get_app_version_name)"
ANDROID_VERSION_CODE="$(get_android_version_code)"
IOS_BUILD_NUMBER="$(get_ios_build_number)"
VERSION_XCCONFIG="${VERSION_XCCONFIG:-iosApp/Config/Version.xcconfig}"

validate_semver "$VERSION_NAME" || die "App versionName '${VERSION_NAME}' is invalid. Expected semantic version format X.Y.Z."
validate_positive_integer "$ANDROID_VERSION_CODE" || die "androidVersionCode '${ANDROID_VERSION_CODE}' is invalid. Expected a positive integer."
validate_positive_integer "$IOS_BUILD_NUMBER" || die "iosBuildNumber '${IOS_BUILD_NUMBER}' is invalid. Expected a positive integer."

EXPECTED_XCCONFIG="$(mktemp "${RUNNER_TEMP:-/tmp}/tuindice-version.XXXXXX")"
trap 'rm -f "$EXPECTED_XCCONFIG"' EXIT

write_version_xcconfig_contents "$VERSION_NAME" "$IOS_BUILD_NUMBER" >"$EXPECTED_XCCONFIG"
mkdir -p "$(dirname "$VERSION_XCCONFIG")"

if [[ -f "$VERSION_XCCONFIG" ]] && cmp -s "$EXPECTED_XCCONFIG" "$VERSION_XCCONFIG"; then
	info "${VERSION_XCCONFIG} is already in sync with $(app_version_file)."
	exit 0
fi

cp "$EXPECTED_XCCONFIG" "$VERSION_XCCONFIG"
info "Synced ${VERSION_XCCONFIG} from $(app_version_file)."
