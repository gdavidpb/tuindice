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
CHECK_ONLY="${GOOGLE_PLAY_CHECK_ONLY:-0}"
RELEASE_EXISTS_FILE="${GOOGLE_PLAY_RELEASE_EXISTS_FILE:-}"
VERSION_NAME="$(get_app_version_name)"
ANDROID_VERSION_CODE="$(get_android_version_code)"
API_ROOT="https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${ANDROID_PACKAGE_NAME}"
UPLOAD_ROOT="https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/${ANDROID_PACKAGE_NAME}"

[[ -n "$ACCESS_TOKEN" ]] || die "ANDROID_PUBLISHER_ACCESS_TOKEN or GOOGLE_OAUTH_ACCESS_TOKEN is required."

write_github_output() {
	local key="$1"
	local value="$2"

	if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
		printf '%s=%s\n' "$key" "$value" >>"$GITHUB_OUTPUT"
	fi
}

write_release_exists_state() {
	local exists="$1"
	local status="${2:-}"

	write_github_output "android_draft_exists" "$exists"
	write_github_output "android_draft_status" "$status"

	if [[ -n "$RELEASE_EXISTS_FILE" ]]; then
		mkdir -p "$(dirname "$RELEASE_EXISTS_FILE")"
		{
			printf 'exists=%s\n' "$exists"
			printf 'status=%s\n' "$status"
		} >"$RELEASE_EXISTS_FILE"
	fi
}

# One request to the Google Play API. curl runs without --fail so the body Play returns is kept: on a non-2xx status it
# goes to stderr with the HTTP code, and on a transport failure it is returned with curl's exit code. A 2xx body goes to
# stdout. The token is never printed. Usage: play_api_request METHOD URL [CONTENT_TYPE DATA_FILE]
play_api_request() {
	local method="$1"
	local url="$2"
	local content_type="${3:-}"
	local data_file="${4:-}"
	local body_args=()
	local response_file
	local status_code
	local curl_status

	if [[ -n "$data_file" ]]; then
		body_args=(-H "Content-Type: ${content_type}" --data-binary "@${data_file}")
	fi

	response_file="$(mktemp "${RUNNER_TEMP:-/tmp}/tuindice-play-response.XXXXXX")"
	if status_code="$(
		curl --silent --show-error \
			-X "$method" \
			-H "Authorization: Bearer ${ACCESS_TOKEN}" \
			-H "Accept: application/json" \
			${body_args[@]+"${body_args[@]}"} \
			-o "$response_file" \
			-w '%{http_code}' \
			"$url"
	)"; then
		if [[ ! "$status_code" =~ ^2[0-9][0-9]$ ]]; then
			cat "$response_file" >&2 || true
			rm -f "$response_file"
			die "Google Play API ${method} failed with HTTP ${status_code}: ${url}"
		fi
		cat "$response_file"
		rm -f "$response_file"
	else
		curl_status="$?"
		cat "$response_file" >&2 || true
		rm -f "$response_file"
		return "$curl_status"
	fi
}

api_post() {
	play_api_request POST "$1"
}

api_get_json() {
	local url="$1"
	local output_file="$2"
	local status_code

	status_code="$(
		curl --silent --show-error --retry 3 \
			-X GET \
			-H "Authorization: Bearer ${ACCESS_TOKEN}" \
			-H "Accept: application/json" \
			-o "$output_file" \
			-w '%{http_code}' \
			"$url"
	)"

	if [[ "$status_code" == "404" ]]; then
		return 1
	fi

	if [[ ! "$status_code" =~ ^2 ]]; then
		cat "$output_file" >&2 || true
		die "Google Play API GET failed with HTTP ${status_code}: ${url}"
	fi
}

api_put_json() {
	local url="$1"
	local json_file="$2"

	play_api_request PUT "$url" "application/json" "$json_file"
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

TRACK_STATE="$(mktemp "${RUNNER_TEMP:-/tmp}/tuindice-play-track-state.XXXXXX")"
TRACK_EXISTS=0
DRAFT_TO_REPLACE='{}'
if api_get_json "${API_ROOT}/edits/${EDIT_ID}/tracks/${GOOGLE_PLAY_TRACK}" "$TRACK_STATE"; then
	TRACK_EXISTS=1
	EXISTING_RELEASE_STATUS="$(
		jq -r \
			--arg version_code "$ANDROID_VERSION_CODE" \
			'[
				.releases[]?
				| select(any(.versionCodes[]?; tostring == $version_code))
				| (.status // "unknown")
			][0] // empty' \
			"$TRACK_STATE"
	)"
	if [[ -n "$EXISTING_RELEASE_STATUS" ]]; then
		write_release_exists_state "true" "$EXISTING_RELEASE_STATUS"
		info "Google Play release for ${VERSION_NAME} (${ANDROID_VERSION_CODE}) already exists on track ${GOOGLE_PLAY_TRACK} with status ${EXISTING_RELEASE_STATUS}; skipping bundle upload."
		exit 0
	fi

	DRAFT_COUNT="$(jq '[.releases[]? | select(.status == "draft")] | length' "$TRACK_STATE")"
	if (( DRAFT_COUNT > 1 )); then
		die "Google Play track ${GOOGLE_PLAY_TRACK} has multiple draft releases; resolve them before publishing ${VERSION_NAME} (${ANDROID_VERSION_CODE})."
	fi
	if (( DRAFT_COUNT == 1 )); then
		DRAFT_TO_REPLACE="$(jq -c '[.releases[]? | select(.status == "draft")][0]' "$TRACK_STATE")"
		if ! jq -e --arg version_code "$ANDROID_VERSION_CODE" '
			(.versionCodes // []) as $codes
			| if ($codes | type) != "array" or ($codes | length) != 1 then false
			  elif ($codes[0] | tostring | test("^[0-9]+$") | not) then false
			  else ($codes[0] | tonumber) < ($version_code | tonumber) end
		' <<<"$DRAFT_TO_REPLACE" >/dev/null; then
			die "Google Play track ${GOOGLE_PLAY_TRACK} has a newer or ambiguous draft; refusing to replace it with ${VERSION_NAME} (${ANDROID_VERSION_CODE})."
		fi
		if [[ "$CHECK_ONLY" == "1" ]]; then
			info "An older Google Play draft on track ${GOOGLE_PLAY_TRACK} will be replaced by ${VERSION_NAME} (${ANDROID_VERSION_CODE})."
		else
			info "Replacing an older Google Play draft on track ${GOOGLE_PLAY_TRACK} with ${VERSION_NAME} (${ANDROID_VERSION_CODE}) when the edit is committed."
		fi
	fi
else
	info "Google Play track ${GOOGLE_PLAY_TRACK} does not exist in edit ${EDIT_ID}; continuing with first draft upload."
fi

write_release_exists_state "false" ""

if [[ "$CHECK_ONLY" == "1" ]]; then
	info "Google Play release for ${VERSION_NAME} (${ANDROID_VERSION_CODE}) does not exist on track ${GOOGLE_PLAY_TRACK}; bundle build/upload is required."
	exit 0
fi

[[ -s "$ANDROID_AAB_PATH" ]] || die "Android App Bundle not found: ${ANDROID_AAB_PATH}"

info "Uploading ${ANDROID_AAB_PATH} to Google Play edit ${EDIT_ID}."
UPLOADED_VERSION_CODE="$(
	play_api_request POST "${UPLOAD_ROOT}/edits/${EDIT_ID}/bundles?uploadType=media" \
		"application/octet-stream" "$ANDROID_AAB_PATH" \
		| jq -r '.versionCode // empty'
)"

[[ -n "$UPLOADED_VERSION_CODE" ]] || die "Google Play bundle upload did not return a versionCode."
[[ "$UPLOADED_VERSION_CODE" == "$ANDROID_VERSION_CODE" ]] || die "Uploaded versionCode ${UPLOADED_VERSION_CODE} does not match $(app_version_file) androidVersionCode ${ANDROID_VERSION_CODE}."

# Every release but the draft validated above goes back unchanged, so this does not depend on whether the API treats an
# update as a full or a partial replacement; the new draft takes the place of the old one.
PRESERVED_RELEASES='[]'
if [[ "$TRACK_EXISTS" == "1" ]]; then
	PRESERVED_RELEASES="$(
		jq -c \
			--arg version_code "$ANDROID_VERSION_CODE" \
			'[
				.releases[]?
				| select(.status != "draft")
				| select(any(.versionCodes[]?; tostring == $version_code) | not)
			]' \
			"$TRACK_STATE"
	)"
fi

TRACK_PAYLOAD="$(mktemp "${RUNNER_TEMP:-/tmp}/tuindice-play-track.XXXXXX")"
jq -n \
	--arg track "$GOOGLE_PLAY_TRACK" \
	--arg release_name "${VERSION_NAME} (${ANDROID_VERSION_CODE})" \
	--arg version_code "$ANDROID_VERSION_CODE" \
	--argjson preserved_releases "$PRESERVED_RELEASES" \
	--argjson previous_draft "$DRAFT_TO_REPLACE" \
	'{
		track: $track,
		releases: (
			$preserved_releases + [
				(($previous_draft | {releaseNotes, inAppUpdatePriority} | with_entries(select(.value != null))) + {
					name: $release_name,
					versionCodes: [$version_code],
					status: "draft"
				})
			]
		)
	}' >"$TRACK_PAYLOAD"

if jq -e '(.releaseNotes // []) | length > 0' <<<"$DRAFT_TO_REPLACE" >/dev/null; then
	warn "The new draft ${VERSION_NAME} (${ANDROID_VERSION_CODE}) inherits the release notes of the replaced draft; review them in Play Console before publishing."
fi

info "Creating Google Play draft release on track ${GOOGLE_PLAY_TRACK}."
api_put_json "${API_ROOT}/edits/${EDIT_ID}/tracks/${GOOGLE_PLAY_TRACK}" "$TRACK_PAYLOAD" >/dev/null

info "Committing Google Play edit ${EDIT_ID}."
api_post "${API_ROOT}/edits/${EDIT_ID}:commit" >/dev/null
EDIT_COMMITTED=1
info "Google Play draft release created for ${VERSION_NAME} (${ANDROID_VERSION_CODE})."
