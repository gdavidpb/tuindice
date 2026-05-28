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
IOS_BUNDLE_IDENTIFIER="${IOS_BUNDLE_IDENTIFIER:-com.gdavidpb.tuindice}"
CODE_SIGN_STYLE_VALUE="${IOS_CODE_SIGN_STYLE:-Manual}"
CODE_SIGN_IDENTITY_VALUE="${IOS_CODE_SIGN_IDENTITY:-Apple Distribution}"
PROFILE_SPECIFIER="${APPLE_PROVISIONING_PROFILE_SPECIFIER:-}"
PROFILE_UUID="${APPLE_PROVISIONING_PROFILE_UUID:-}"
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

plist_value() {
	local plist_file="$1"
	local key="$2"

	/usr/libexec/PlistBuddy -c "Print :${key}" "$plist_file" 2>/dev/null || true
}

xml_escape() {
	local value="$1"

	value="${value//&/&amp;}"
	value="${value//</&lt;}"
	value="${value//>/&gt;}"
	value="${value//\"/&quot;}"
	value="${value//\'/&apos;}"
	printf '%s' "$value"
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

if [[ "$CODE_SIGN_STYLE_VALUE" == "Manual" ]]; then
	[[ -n "${APPLE_DISTRIBUTION_CERTIFICATE_P12_BASE64:-}" ]] || die "APPLE_DISTRIBUTION_CERTIFICATE_P12_BASE64 is required for manual iOS signing."
	[[ -n "${APPLE_DISTRIBUTION_CERTIFICATE_PASSWORD:-}" ]] || die "APPLE_DISTRIBUTION_CERTIFICATE_PASSWORD is required for manual iOS signing."
	[[ -n "${APPLE_PROVISIONING_PROFILE_BASE64:-}" || -n "$PROFILE_SPECIFIER" || -n "$PROFILE_UUID" ]] || die "APPLE_PROVISIONING_PROFILE_BASE64, APPLE_PROVISIONING_PROFILE_SPECIFIER, or APPLE_PROVISIONING_PROFILE_UUID is required for manual iOS signing."
fi

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
		-f pkcs12 \
		-k "$KEYCHAIN_PATH"
	security list-keychain -d user -s "$KEYCHAIN_PATH"
	security default-keychain -d user -s "$KEYCHAIN_PATH"
	security set-key-partition-list -S apple-tool:,apple:,codesign: -s -k "$KEYCHAIN_PASSWORD" "$KEYCHAIN_PATH"

	if [[ "$CODE_SIGN_STYLE_VALUE" == "Manual" ]]; then
		security find-identity -v -p codesigning "$KEYCHAIN_PATH"
		security find-identity -v -p codesigning "$KEYCHAIN_PATH" | grep -q "$CODE_SIGN_IDENTITY_VALUE" ||
			die "No ${CODE_SIGN_IDENTITY_VALUE} signing identity with private key was imported."
	fi
fi

if [[ -n "${APPLE_PROVISIONING_PROFILE_BASE64:-}" ]]; then
	PROFILE_DIR="${HOME}/Library/MobileDevice/Provisioning Profiles"
	PROFILE_SOURCE_PATH="${RUNNER_TEMP:-/tmp}/tuindice.mobileprovision"
	PROFILE_PLIST_PATH="${RUNNER_TEMP:-/tmp}/tuindice.mobileprovision.plist"
	mkdir -p "$PROFILE_DIR"
	decode_base64_to_file "$APPLE_PROVISIONING_PROFILE_BASE64" "$PROFILE_SOURCE_PATH"
	security cms -D -i "$PROFILE_SOURCE_PATH" >"$PROFILE_PLIST_PATH"

	if [[ -z "$PROFILE_UUID" ]]; then
		PROFILE_UUID="$(plist_value "$PROFILE_PLIST_PATH" UUID)"
	fi
	if [[ -z "$PROFILE_SPECIFIER" ]]; then
		PROFILE_SPECIFIER="$(plist_value "$PROFILE_PLIST_PATH" Name)"
	fi

	[[ -n "$PROFILE_UUID" ]] || die "Unable to resolve provisioning profile UUID."
	cp "$PROFILE_SOURCE_PATH" "${PROFILE_DIR}/${PROFILE_UUID}.mobileprovision"
fi

if [[ "$CODE_SIGN_STYLE_VALUE" == "Manual" ]]; then
	[[ -n "$PROFILE_SPECIFIER" || -n "$PROFILE_UUID" ]] || die "Unable to resolve manual provisioning profile specifier."
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

declare -a archive_signing_args=(
	DEVELOPMENT_TEAM="$APPLE_TEAM_ID_VALUE"
	CODE_SIGN_STYLE="$CODE_SIGN_STYLE_VALUE"
)

if [[ "$CODE_SIGN_STYLE_VALUE" == "Manual" ]]; then
	archive_signing_args+=(
		CODE_SIGN_IDENTITY="$CODE_SIGN_IDENTITY_VALUE"
	)
	if [[ -n "$PROFILE_SPECIFIER" ]]; then
		archive_signing_args+=(
			PROVISIONING_PROFILE_SPECIFIER="$PROFILE_SPECIFIER"
		)
	else
		archive_signing_args+=(
			PROVISIONING_PROFILE="$PROFILE_UUID"
		)
	fi
else
	archive_signing_args+=(
		"${auth_args[@]}"
	)
fi

mkdir -p "$(dirname "$ARCHIVE_PATH")" "$EXPORT_PATH"

log "Archiving ${SCHEME_NAME} for App Store Connect."
xcodebuild \
	"${xcodebuild_base_args[@]}" \
	-scheme "$SCHEME_NAME" \
	-configuration "$CONFIGURATION_NAME" \
	-sdk iphoneos \
	-destination "generic/platform=iOS" \
	-archivePath "$ARCHIVE_PATH" \
	"${archive_signing_args[@]}" \
	archive

EXPORT_OPTIONS_PLIST="${RUNNER_TEMP:-/tmp}/tuindice-export-options.plist"
if [[ "$CODE_SIGN_STYLE_VALUE" == "Manual" ]]; then
	PROFILE_EXPORT_VALUE="$(xml_escape "${PROFILE_SPECIFIER:-$PROFILE_UUID}")"
	BUNDLE_IDENTIFIER_VALUE="$(xml_escape "$IOS_BUNDLE_IDENTIFIER")"
	SIGNING_STYLE_EXPORT="manual"
else
	PROFILE_EXPORT_VALUE=""
	BUNDLE_IDENTIFIER_VALUE=""
	SIGNING_STYLE_EXPORT="automatic"
fi

{
cat <<EOF
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
	<string>${SIGNING_STYLE_EXPORT}</string>
EOF

if [[ "$CODE_SIGN_STYLE_VALUE" == "Manual" ]]; then
cat <<EOF
	<key>provisioningProfiles</key>
	<dict>
		<key>${BUNDLE_IDENTIFIER_VALUE}</key>
		<string>${PROFILE_EXPORT_VALUE}</string>
	</dict>
EOF
fi

cat <<EOF
	<key>stripSwiftSymbols</key>
	<true/>
	<key>teamID</key>
	<string>${APPLE_TEAM_ID_VALUE}</string>
	<key>uploadSymbols</key>
	<true/>
</dict>
</plist>
EOF
} >"$EXPORT_OPTIONS_PLIST"

log "Uploading archive to App Store Connect."
xcodebuild \
	-exportArchive \
	-archivePath "$ARCHIVE_PATH" \
	-exportPath "$EXPORT_PATH" \
	-exportOptionsPlist "$EXPORT_OPTIONS_PLIST" \
	"${auth_args[@]}"

log "App Store Connect upload requested. The build will appear after Apple finishes processing it."
