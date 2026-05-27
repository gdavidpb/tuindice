#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
REPO_ROOT="$(cd "${ROOT_DIR}/.." && pwd)"
PROJECT_PATH="$ROOT_DIR/TuIndiceHost.xcodeproj"
WORKSPACE_PATH="$ROOT_DIR/TuIndiceHost.xcworkspace"
SCHEME_NAME="TuIndiceHost"
CONFIGURATION_NAME="${CONFIGURATION:-Release}"
ARCHIVE_PATH="${ARCHIVE_PATH:-${REPO_ROOT}/build/ios/archive/TuIndiceHost.xcarchive}"
EXPORT_PATH="${EXPORT_PATH:-${REPO_ROOT}/build/ios/export}"
API_KEY_PATH="${APP_STORE_CONNECT_API_KEY_PATH:-}"
API_KEY_ID="${APP_STORE_CONNECT_KEY_ID:-}"
API_ISSUER_ID="${APP_STORE_CONNECT_ISSUER_ID:-}"
APPLE_TEAM_ID_VALUE="${APPLE_TEAM_ID:-94BBJ5X5GL}"
KEYCHAIN_PATH="${APPLE_KEYCHAIN_PATH:-${RUNNER_TEMP:-/tmp}/tuindice-signing.keychain-db}"
KEYCHAIN_PASSWORD="${APPLE_KEYCHAIN_PASSWORD:-tuindice-ci-keychain}"

log() {
	printf '[tuindice-ios-upload] %s\n' "$*"
}

die() {
	printf '[tuindice-ios-upload] ERROR: %s\n' "$*" >&2
	exit 1
}

decode_base64_to_file() {
	local value="$1"
	local target_file="$2"

	mkdir -p "$(dirname "$target_file")"
	if base64 --help 2>/dev/null | grep -q -- '--decode'; then
		printf '%s' "$value" | base64 --decode >"$target_file"
	else
		printf '%s' "$value" | base64 -D >"$target_file"
	fi
}

if [[ "$OSTYPE" != darwin* ]]; then
	die "App Store Connect upload requires macOS."
fi

if [[ -z "$API_KEY_PATH" && -n "${APP_STORE_CONNECT_API_KEY_P8_BASE64:-}" ]]; then
	API_KEY_PATH="${RUNNER_TEMP:-/tmp}/AuthKey_${API_KEY_ID}.p8"
	decode_base64_to_file "$APP_STORE_CONNECT_API_KEY_P8_BASE64" "$API_KEY_PATH"
fi

[[ -n "$API_KEY_PATH" && -s "$API_KEY_PATH" ]] || die "APP_STORE_CONNECT_API_KEY_PATH or APP_STORE_CONNECT_API_KEY_P8_BASE64 is required."
[[ -n "$API_KEY_ID" ]] || die "APP_STORE_CONNECT_KEY_ID is required."
[[ -n "$API_ISSUER_ID" ]] || die "APP_STORE_CONNECT_ISSUER_ID is required."

if [[ -n "${APPLE_DISTRIBUTION_CERTIFICATE_P12_BASE64:-}" ]]; then
	CERTIFICATE_PATH="${RUNNER_TEMP:-/tmp}/tuindice-distribution.p12"
	decode_base64_to_file "$APPLE_DISTRIBUTION_CERTIFICATE_P12_BASE64" "$CERTIFICATE_PATH"
	[[ -n "${APPLE_DISTRIBUTION_CERTIFICATE_PASSWORD:-}" ]] || die "APPLE_DISTRIBUTION_CERTIFICATE_PASSWORD is required when APPLE_DISTRIBUTION_CERTIFICATE_P12_BASE64 is set."

	log "Creating temporary signing keychain."
	security create-keychain -p "$KEYCHAIN_PASSWORD" "$KEYCHAIN_PATH"
	security set-keychain-settings -lut 21600 "$KEYCHAIN_PATH"
	security unlock-keychain -p "$KEYCHAIN_PASSWORD" "$KEYCHAIN_PATH"
	security import "$CERTIFICATE_PATH" \
		-P "$APPLE_DISTRIBUTION_CERTIFICATE_PASSWORD" \
		-A \
		-t cert \
		-f pkcs12 \
		-k "$KEYCHAIN_PATH"
	security list-keychain -d user -s "$KEYCHAIN_PATH"
	security set-key-partition-list -S apple-tool:,apple:,codesign: -s -k "$KEYCHAIN_PASSWORD" "$KEYCHAIN_PATH"
fi

if [[ -n "${APPLE_PROVISIONING_PROFILE_BASE64:-}" ]]; then
	PROFILE_DIR="${HOME}/Library/MobileDevice/Provisioning Profiles"
	mkdir -p "$PROFILE_DIR"
	decode_base64_to_file "$APPLE_PROVISIONING_PROFILE_BASE64" "${PROFILE_DIR}/tuindice.mobileprovision"
fi

bash "$REPO_ROOT/.github/scripts/sync-app-version.sh"

if [[ -f "$ROOT_DIR/Podfile" ]] && command -v pod >/dev/null 2>&1; then
	if [[ ! -d "$WORKSPACE_PATH" || "${FORCE_POD_INSTALL:-0}" == "1" ]]; then
		(cd "$ROOT_DIR" && pod install --silent)
	fi
fi

declare -a xcodebuild_base_args
if [[ -d "$WORKSPACE_PATH" ]]; then
	xcodebuild_base_args=(-workspace "$WORKSPACE_PATH")
else
	xcodebuild_base_args=(-project "$PROJECT_PATH")
fi

declare -a auth_args=(
	-allowProvisioningUpdates
	-authenticationKeyPath "$API_KEY_PATH"
	-authenticationKeyID "$API_KEY_ID"
	-authenticationKeyIssuerID "$API_ISSUER_ID"
)

mkdir -p "$(dirname "$ARCHIVE_PATH")" "$EXPORT_PATH"

log "Archiving ${SCHEME_NAME} for App Store Connect."
xcodebuild \
	"${xcodebuild_base_args[@]}" \
	-scheme "$SCHEME_NAME" \
	-configuration "$CONFIGURATION_NAME" \
	-sdk iphoneos \
	-destination "generic/platform=iOS" \
	-archivePath "$ARCHIVE_PATH" \
	DEVELOPMENT_TEAM="$APPLE_TEAM_ID_VALUE" \
	CODE_SIGN_STYLE=Automatic \
	"${auth_args[@]}" \
	archive

EXPORT_OPTIONS_PLIST="${RUNNER_TEMP:-/tmp}/tuindice-export-options.plist"
cat >"$EXPORT_OPTIONS_PLIST" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>destination</key>
	<string>upload</string>
	<key>manageAppVersionAndBuildNumber</key>
	<false/>
	<key>method</key>
	<string>app-store-connect</string>
	<key>signingStyle</key>
	<string>automatic</string>
	<key>stripSwiftSymbols</key>
	<true/>
	<key>teamID</key>
	<string>${APPLE_TEAM_ID_VALUE}</string>
	<key>uploadSymbols</key>
	<true/>
</dict>
</plist>
EOF

log "Uploading archive to App Store Connect."
xcodebuild \
	-exportArchive \
	-archivePath "$ARCHIVE_PATH" \
	-exportPath "$EXPORT_PATH" \
	-exportOptionsPlist "$EXPORT_OPTIONS_PLIST" \
	"${auth_args[@]}"

log "App Store Connect upload requested. The build will appear after Apple finishes processing it."
