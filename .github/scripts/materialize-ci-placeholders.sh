#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

# PR preflight builds release artifacts without production secrets: Firebase
# configs and the signing keystore only need to be structurally valid for the
# build to succeed. Real configs and the release keystore are materialized
# exclusively by the deploy workflow under the production environment.

ANDROID_PLACEHOLDER_PACKAGE="${ANDROID_PLACEHOLDER_PACKAGE:-com.gdavidpb.tuindice}"
IOS_PLACEHOLDER_BUNDLE_ID="${IOS_PLACEHOLDER_BUNDLE_ID:-com.gdavidpb.tuindice}"
PLACEHOLDER_KEYSTORE_PATH="${PLACEHOLDER_KEYSTORE_PATH:-${RUNNER_TEMP:-/tmp}/tuindice-ci-placeholder.jks}"
PLACEHOLDER_KEY_ALIAS="tuindice-ci-placeholder"
PLACEHOLDER_KEY_PASSWORD="tuindice-ci-placeholder"

write_android_placeholder_config() {
	local target_file="app/google-services.json"

	if [[ -s "$target_file" ]]; then
		info "Keeping existing ${target_file}."
		return 0
	fi

	info "Writing placeholder ${target_file}."
	cat >"$target_file" <<EOF
{
  "project_info": {
    "project_number": "000000000000",
    "project_id": "tuindice-ci-placeholder",
    "storage_bucket": "tuindice-ci-placeholder.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:000000000000:android:0000000000000000",
        "android_client_info": {
          "package_name": "${ANDROID_PLACEHOLDER_PACKAGE}"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "AIzaCiPlaceholder0000000000000000000000"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
EOF
}

write_ios_placeholder_config() {
	local target_file="iosApp/Resources/GoogleService-Info.plist"

	if [[ -s "$target_file" ]]; then
		info "Keeping existing ${target_file}."
		return 0
	fi

	info "Writing placeholder ${target_file}."
	mkdir -p "$(dirname "$target_file")"
	cat >"$target_file" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>API_KEY</key>
	<string>AIzaCiPlaceholder0000000000000000000000</string>
	<key>GCM_SENDER_ID</key>
	<string>000000000000</string>
	<key>PLIST_VERSION</key>
	<string>1</string>
	<key>BUNDLE_ID</key>
	<string>${IOS_PLACEHOLDER_BUNDLE_ID}</string>
	<key>PROJECT_ID</key>
	<string>tuindice-ci-placeholder</string>
	<key>STORAGE_BUCKET</key>
	<string>tuindice-ci-placeholder.appspot.com</string>
	<key>IS_ADS_ENABLED</key>
	<false/>
	<key>IS_ANALYTICS_ENABLED</key>
	<false/>
	<key>IS_APPINVITE_ENABLED</key>
	<false/>
	<key>IS_GCM_ENABLED</key>
	<true/>
	<key>IS_SIGNIN_ENABLED</key>
	<false/>
	<key>GOOGLE_APP_ID</key>
	<string>1:000000000000:ios:0000000000000000</string>
</dict>
</plist>
EOF
}

materialize_placeholder_keystore() {
	require_tool keytool

	if [[ -s "$PLACEHOLDER_KEYSTORE_PATH" ]]; then
		info "Keeping existing placeholder keystore at ${PLACEHOLDER_KEYSTORE_PATH}."
	else
		info "Generating placeholder signing keystore at ${PLACEHOLDER_KEYSTORE_PATH}."
		mkdir -p "$(dirname "$PLACEHOLDER_KEYSTORE_PATH")"
		keytool -genkeypair \
			-keystore "$PLACEHOLDER_KEYSTORE_PATH" \
			-storepass "$PLACEHOLDER_KEY_PASSWORD" \
			-keypass "$PLACEHOLDER_KEY_PASSWORD" \
			-alias "$PLACEHOLDER_KEY_ALIAS" \
			-dname "CN=TuIndice CI Placeholder" \
			-keyalg RSA \
			-keysize 2048 \
			-validity 1 >/dev/null 2>&1
		chmod 600 "$PLACEHOLDER_KEYSTORE_PATH"
	fi

	if [[ -n "${GITHUB_ENV:-}" ]]; then
		{
			printf 'TU_INDICE_KEY_STORE_PATH=%s\n' "$PLACEHOLDER_KEYSTORE_PATH"
			printf 'TU_INDICE_KEY_ALIAS=%s\n' "$PLACEHOLDER_KEY_ALIAS"
			printf 'TU_INDICE_KEY_PASSWORD=%s\n' "$PLACEHOLDER_KEY_PASSWORD"
			printf 'TU_INDICE_KEY_STORE_PASSWORD=%s\n' "$PLACEHOLDER_KEY_PASSWORD"
		} >>"$GITHUB_ENV"
	else
		info "Export these variables to sign with the placeholder keystore:"
		info "  TU_INDICE_KEY_STORE_PATH=${PLACEHOLDER_KEYSTORE_PATH}"
		info "  TU_INDICE_KEY_ALIAS=${PLACEHOLDER_KEY_ALIAS}"
		info "  TU_INDICE_KEY_PASSWORD=${PLACEHOLDER_KEY_PASSWORD}"
		info "  TU_INDICE_KEY_STORE_PASSWORD=${PLACEHOLDER_KEY_PASSWORD}"
	fi
}

if [[ "${CI_PLACEHOLDER_ANDROID:-1}" == "1" ]]; then
	write_android_placeholder_config
	materialize_placeholder_keystore
fi

if [[ "${CI_PLACEHOLDER_IOS:-1}" == "1" ]]; then
	write_ios_placeholder_config
fi

info "CI placeholder configuration is ready."
