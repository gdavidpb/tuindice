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

validate_ios_firebase_config() {
	local plist_file="$1"
	local expected_bundle_id="${IOS_FIREBASE_EXPECTED_BUNDLE_ID:-com.gdavidpb.tuindice}"

	PLIST_FILE="$plist_file" EXPECTED_BUNDLE_ID="$expected_bundle_id" python3 - <<'PY'
import os
import plistlib
import re
import sys

plist_file = os.environ["PLIST_FILE"]
expected_bundle_id = os.environ["EXPECTED_BUNDLE_ID"]

with open(plist_file, "rb") as plist_handle:
    plist = plistlib.load(plist_handle)

api_key = str(plist.get("API_KEY", ""))
app_id = str(plist.get("GOOGLE_APP_ID", ""))
bundle_id = str(plist.get("BUNDLE_ID", ""))
sender_id = str(plist.get("GCM_SENDER_ID", ""))

errors = []
placeholder_markers = ("placeholder", "debugonly", "000000")

if bundle_id != expected_bundle_id:
    errors.append(f"BUNDLE_ID must be {expected_bundle_id}, got {bundle_id or '<empty>'}")
if not api_key.startswith("AIza") or len(api_key) < 30:
    errors.append("API_KEY does not look like a Firebase iOS API key")
if any(marker in api_key.lower() for marker in placeholder_markers):
    errors.append("API_KEY contains a placeholder/debug marker")
if not re.fullmatch(r"1:[1-9][0-9]*:ios:[0-9a-fA-F]+", app_id):
    errors.append("GOOGLE_APP_ID does not look like a Firebase iOS app id")
if not sender_id.isdigit() or set(sender_id) == {"0"}:
    errors.append("GCM_SENDER_ID must be a non-zero numeric sender id")

if errors:
    for error in errors:
        print(f"Invalid iOS Firebase config: {error}", file=sys.stderr)
    sys.exit(1)
PY
}

if [[ -n "${ANDROID_GOOGLE_SERVICES_JSON_BASE64:-}" ]]; then
	info "Materializing Android Firebase configuration."
	decode_base64_to_file "$ANDROID_GOOGLE_SERVICES_JSON_BASE64" "app/google-services.json"
fi

if [[ -n "${IOS_GOOGLE_SERVICE_INFO_PLIST_BASE64:-}" ]]; then
	info "Materializing iOS Firebase configuration."
	decode_base64_to_file "$IOS_GOOGLE_SERVICE_INFO_PLIST_BASE64" "iosApp/Resources/GoogleService-Info.plist"
fi

if [[ "${REQUIRE_FIREBASE_CONFIGS:-0}" == "1" ]]; then
	[[ -s app/google-services.json ]] || die "Missing app/google-services.json. Provide ANDROID_GOOGLE_SERVICES_JSON_BASE64."
	[[ -s iosApp/Resources/GoogleService-Info.plist ]] || die "Missing iosApp/Resources/GoogleService-Info.plist. Provide IOS_GOOGLE_SERVICE_INFO_PLIST_BASE64."
	validate_ios_firebase_config "iosApp/Resources/GoogleService-Info.plist"
fi
