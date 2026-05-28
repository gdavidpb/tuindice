#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

require_tool curl
require_tool jq

ANDROID_PACKAGE_NAME="${ANDROID_PACKAGE_NAME:-com.gdavidpb.tuindice}"
GOOGLE_PLAY_TRACK="${GOOGLE_PLAY_TRACK:-production}"
ANDROID_AAB_PATH="${ANDROID_AAB_PATH:-app/build/outputs/bundle/release/app-release.aab}"
ACCESS_TOKEN="${ANDROID_PUBLISHER_ACCESS_TOKEN:-${GOOGLE_OAUTH_ACCESS_TOKEN:-}}"
VERSION_NAME="$(get_app_version_name)"
ANDROID_VERSION_CODE="$(get_android_version_code)"
API_ROOT="https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${ANDROID_PACKAGE_NAME}"
UPLOAD_ROOT="https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/${ANDROID_PACKAGE_NAME}"

[[ -n "$ACCESS_TOKEN" ]] || die "ANDROID_PUBLISHER_ACCESS_TOKEN or GOOGLE_OAUTH_ACCESS_TOKEN is required."
[[ -s "$ANDROID_AAB_PATH" ]] || die "Android App Bundle not found: ${ANDROID_AAB_PATH}"

api_post() {
	local url="$1"
	curl --fail --silent --show-error \
		-X POST \
		-H "Authorization: Bearer ${ACCESS_TOKEN}" \
		-H "Accept: application/json" \
		"$url"
}

api_put_json() {
	local url="$1"
	local json_file="$2"
	curl --fail --silent --show-error \
		-X PUT \
		-H "Authorization: Bearer ${ACCESS_TOKEN}" \
		-H "Accept: application/json" \
		-H "Content-Type: application/json" \
		--data-binary "@${json_file}" \
		"$url"
}

api_delete() {
	local url="$1"
	curl --fail --silent --show-error \
		-X DELETE \
		-H "Authorization: Bearer ${ACCESS_TOKEN}" \
		-H "Accept: application/json" \
		"$url" >/dev/null || true
}

info "Creating Google Play edit for ${ANDROID_PACKAGE_NAME}."
EDIT_ID="$(api_post "${API_ROOT}/edits" | jq -r '.id // empty')"
[[ -n "$EDIT_ID" ]] || die "Google Play edit creation did not return an edit id."

cleanup_edit() {
	if [[ -n "${EDIT_ID:-}" && "${EDIT_COMMITTED:-0}" != "1" ]]; then
		warn "Deleting uncommitted Google Play edit ${EDIT_ID}."
		api_delete "${API_ROOT}/edits/${EDIT_ID}"
	fi
}
trap cleanup_edit EXIT

info "Uploading ${ANDROID_AAB_PATH} to Google Play edit ${EDIT_ID}."
UPLOADED_VERSION_CODE="$(
	curl --fail --silent --show-error \
		-X POST \
		-H "Authorization: Bearer ${ACCESS_TOKEN}" \
		-H "Accept: application/json" \
		-H "Content-Type: application/octet-stream" \
		--data-binary "@${ANDROID_AAB_PATH}" \
		"${UPLOAD_ROOT}/edits/${EDIT_ID}/bundles?uploadType=media" \
		| jq -r '.versionCode // empty'
)"

[[ -n "$UPLOADED_VERSION_CODE" ]] || die "Google Play bundle upload did not return a versionCode."
[[ "$UPLOADED_VERSION_CODE" == "$ANDROID_VERSION_CODE" ]] || die "Uploaded versionCode ${UPLOADED_VERSION_CODE} does not match $(app_version_file) androidVersionCode ${ANDROID_VERSION_CODE}."

TRACK_PAYLOAD="$(mktemp "${RUNNER_TEMP:-/tmp}/tuindice-play-track.XXXXXX.json")"
jq -n \
	--arg track "$GOOGLE_PLAY_TRACK" \
	--arg release_name "${VERSION_NAME} (${ANDROID_VERSION_CODE})" \
	--arg version_code "$ANDROID_VERSION_CODE" \
	'{
		track: $track,
		releases: [
			{
				name: $release_name,
				versionCodes: [($version_code | tonumber)],
				status: "draft"
			}
		]
	}' >"$TRACK_PAYLOAD"

info "Creating Google Play draft release on track ${GOOGLE_PLAY_TRACK}."
api_put_json "${API_ROOT}/edits/${EDIT_ID}/tracks/${GOOGLE_PLAY_TRACK}" "$TRACK_PAYLOAD" >/dev/null

info "Committing Google Play edit ${EDIT_ID}."
api_post "${API_ROOT}/edits/${EDIT_ID}:commit" >/dev/null
EDIT_COMMITTED=1
info "Google Play draft release created for ${VERSION_NAME} (${ANDROID_VERSION_CODE})."
