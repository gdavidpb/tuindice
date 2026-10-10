#!/usr/bin/env bash
# Builds the iOS host app and the TuIndiceUITests bundle for the simulator with one `xcodebuild build-for-testing`.
#   build.sh [--port PORT] [--udid UDID] [--for-testing-only]
# Without --udid the destination is any simulator. --for-testing-only is the entry of CI and of verifyIosUiTestsBuild:
# after the build it requires the symbol check of the app (no ScenarioKit in the app binaries) instead of skipping it.
# stdout: {"app", "appId", "executable", "derivedData"}; everything else goes to stderr.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
source "${SCRIPT_DIR}/../shared/lib.sh"
source "${SCRIPT_DIR}/../shared/layout.env"

port="${E2E_IOS_WIREMOCK_PORT:-18627}"
udid=""
for_testing_only=0
while [[ $# -gt 0 ]]; do
	case "$1" in
		--port) port="${2:?--port needs a number}"; shift 2 ;;
		--udid) udid="${2:?--udid needs a simulator UDID}"; shift 2 ;;
		--for-testing-only) for_testing_only=1; shift ;;
		*) printf 'Unknown argument: %s\n' "$1" >&2; exit 2 ;;
	esac
done

if ! is_macos; then
	printf 'The iOS build requires macOS.\n' >&2
	exit 1
fi
require_command xcodebuild

# Derived data belongs to one checkout: the hash of the repository root keeps a build or a parity run from another
# worktree from replacing the binaries an evidence run is installing (the Swift sources also bake in their path).
repo_hash="$(printf '%s' "${REPO_ROOT}" | shasum -a 256 | cut -c1-12)"
ios_state="${E2E_TMP_ROOT:-${TMPDIR:-/tmp}/tuindice-e2e}/ios/${repo_hash}"
derived_data="${ios_state}/derived-data"
base_url="http://localhost:${port}"
app="${derived_data}/Build/Products/Debug-iphonesimulator/TuIndiceHost.app"
build_log="${ios_state}/build.log"
destination="generic/platform=iOS Simulator"
if [[ -n "${udid}" ]]; then
	destination="platform=iOS Simulator,id=${udid}"
fi

bash "${REPO_ROOT}/.github/scripts/materialize-firebase-configs.sh" >&2
google_service_info="${REPO_ROOT}/iosApp/Resources/GoogleService-Info.plist"
if [[ ! -s "${google_service_info}" ]]; then
	log "Creating the placeholder iOS Firebase configuration for the debug build."
	mkdir -p "$(dirname "${google_service_info}")"
	cat > "${google_service_info}" << 'PLIST'
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
bash "${REPO_ROOT}/.github/scripts/sync-app-version.sh" >&2

log "Building the iOS host and ${E2E_IOS_UITEST_SCHEME} (API ${base_url}/, destination ${destination}); the log is ${build_log}."
mkdir -p "$(dirname "${build_log}")"
if ! xcodebuild build-for-testing \
	-workspace "${REPO_ROOT}/iosApp/TuIndiceHost.xcworkspace" -scheme "${E2E_IOS_UITEST_SCHEME}" \
	-configuration Debug -sdk iphonesimulator -destination "${destination}" -derivedDataPath "${derived_data}" \
	CODE_SIGNING_ALLOWED=NO \
	TUINDICE_API_BASE_URL="${base_url}/" TUINDICE_PRIVACY_POLICY_URL="${base_url}/e2e/privacy.html" \
	TUINDICE_TERMS_AND_CONDITIONS_URL="${base_url}/e2e/terms.html" TUINDICE_SUPPORT_URL="${base_url}/e2e/support.html" \
	> "${build_log}" 2>&1; then
	tail -n 60 "${build_log}" >&2
	printf 'xcodebuild failed; the full log is %s\n' "${build_log}" >&2
	exit 1
fi

[[ -d "${app}" ]] || { printf 'The build did not produce %s\n' "${app}" >&2; exit 1; }
if [[ "${for_testing_only}" == "1" ]]; then
	bash "${REPO_ROOT}/iosApp/scripts/verify-ui-test-target.sh" --app "${app}" --require-app >&2
fi

emit_json "app=s:${app}" "appId=s:$(plutil -extract CFBundleIdentifier raw -o - "${app}/Info.plist")" \
	"executable=s:$(plutil -extract CFBundleExecutable raw -o - "${app}/Info.plist")" "derivedData=s:${derived_data}"
