#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

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

if [[ -n "${ANDROID_GOOGLE_SERVICES_JSON_BASE64:-}" ]]; then
	info "Materializing Android Firebase configuration."
	decode_base64_to_file "$ANDROID_GOOGLE_SERVICES_JSON_BASE64" "app/google-services.json"
fi

if [[ -n "${IOS_GOOGLE_SERVICE_INFO_PLIST_BASE64:-}" ]]; then
	if [[ -d "iosApp/Resources" ]]; then
		info "Materializing iOS Firebase configuration."
		decode_base64_to_file "$IOS_GOOGLE_SERVICE_INFO_PLIST_BASE64" "iosApp/Resources/GoogleService-Info.plist"
	else
		warn "IOS_GOOGLE_SERVICE_INFO_PLIST_BASE64 is present, but iosApp/Resources does not exist on this branch. Skipping iOS Firebase config."
	fi
fi

if [[ "${REQUIRE_FIREBASE_CONFIGS:-0}" == "1" ]]; then
	[[ -s app/google-services.json ]] || die "Missing app/google-services.json. Provide ANDROID_GOOGLE_SERVICES_JSON_BASE64."
fi

if [[ "${REQUIRE_IOS_FIREBASE_CONFIG:-0}" == "1" ]]; then
	[[ -s iosApp/Resources/GoogleService-Info.plist ]] || die "Missing iosApp/Resources/GoogleService-Info.plist. Provide IOS_GOOGLE_SERVICE_INFO_PLIST_BASE64."
fi
