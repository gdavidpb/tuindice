#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

if ! is_macos; then
	printf 'iOS E2E build requires macOS.\n' >&2
	exit 1
fi

require_command xcrun

bash "${REPO_ROOT}/.github/scripts/materialize-firebase-configs.sh"

GOOGLE_SERVICE_INFO="${REPO_ROOT}/iosApp/Resources/GoogleService-Info.plist"
if [[ ! -s "${GOOGLE_SERVICE_INFO}" ]]; then
	log "Creating placeholder iOS Firebase configuration for debug build."
	mkdir -p "$(dirname "${GOOGLE_SERVICE_INFO}")"
	cat >"${GOOGLE_SERVICE_INFO}" <<'PLIST'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>API_KEY</key>
	<string>AIzaSyDebugOnlyPlaceholder</string>
	<key>BUNDLE_ID</key>
	<string>com.gdavidpb.tuindice.ios.debug</string>
	<key>GCM_SENDER_ID</key>
	<string>000000000000</string>
	<key>GOOGLE_APP_ID</key>
	<string>1:000000000000:ios:0000000000000000000000</string>
	<key>IS_ADS_ENABLED</key>
	<false/>
	<key>IS_ANALYTICS_ENABLED</key>
	<false/>
	<key>IS_APPINVITE_ENABLED</key>
	<false/>
	<key>IS_GCM_ENABLED</key>
	<false/>
	<key>IS_SIGNIN_ENABLED</key>
	<false/>
	<key>PROJECT_ID</key>
	<string>tu-indice-usb</string>
	<key>STORAGE_BUCKET</key>
	<string>tu-indice-usb.appspot.com</string>
</dict>
</plist>
PLIST
fi

log "Linking shared iOS simulator framework and syncing Compose resources."
CONFIGURATION="Debug" \
PLATFORM_NAME="iphonesimulator" \
ARCHS="arm64" \
BUILT_PRODUCTS_DIR="${E2E_IOS_DERIVED_DATA}/Build/Products/Debug-iphonesimulator" \
UNLOCALIZED_RESOURCES_FOLDER_PATH="TuIndiceHost.app" \
	"${REPO_ROOT}/gradlew" --console=plain \
	:maincore:linkDebugFrameworkIosSimulatorArm64 \
	:maincore:syncComposeResourcesForIos

MAINCORE_FRAMEWORK_BINARY="${REPO_ROOT}/maincore/build/bin/iosSimulatorArm64/debugFramework/maincore.framework/maincore"
if [[ ! -f "${MAINCORE_FRAMEWORK_BINARY}" ]]; then
	printf 'Expected linked maincore framework was not produced: %s\n' "${MAINCORE_FRAMEWORK_BINARY}" >&2
	exit 1
fi

log "Building iOS debug host."
CONFIGURATION="Debug" \
DERIVED_DATA_PATH="${E2E_IOS_DERIVED_DATA}" \
REQUIRE_SIMULATOR="1" \
SKIP_FRAMEWORK_BUILD="1" \
TUINDICE_API_BASE_URL="${E2E_IOS_API_BASE_URL}" \
TUINDICE_PRIVACY_POLICY_URL="${E2E_IOS_WEB_BASE_URL}/e2e/privacy.html" \
TUINDICE_TERMS_AND_CONDITIONS_URL="${E2E_IOS_WEB_BASE_URL}/e2e/terms.html" \
TUINDICE_SUPPORT_URL="${E2E_IOS_WEB_BASE_URL}/e2e/support.html" \
	"${REPO_ROOT}/iosApp/scripts/ci-build-ios-host.sh"

printf '%s\n' "${E2E_IOS_DERIVED_DATA}/Build/Products/Debug-iphonesimulator/TuIndiceHost.app"
