#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

require_tool jq

VERSION_NAME="$(get_app_version_name)"
ANDROID_VERSION_CODE="$(get_android_version_code)"
INHERITED_NOTES_WARNING="The new draft ${VERSION_NAME} (${ANDROID_VERSION_CODE}) inherits the release notes of the replaced draft; review them in Play Console before publishing."

assert_file_contains_line() {
	local file="$1"
	local expected="$2"
	local label="$3"

	if ! grep -Fxq "$expected" "$file"; then
		printf 'Expected %s to contain "%s", got:\n' "$label" "$expected" >&2
		cat "$file" >&2 || true
		exit 1
	fi
}

assert_log_contains() {
	local file="$1"
	local expected="$2"

	if ! grep -Fq "$expected" "$file"; then
		printf 'Expected the log to contain "%s", got:\n' "$expected" >&2
		cat "$file" >&2 || true
		exit 1
	fi
}

assert_log_lacks() {
	local file="$1"
	local unexpected="$2"

	if grep -Fq "$unexpected" "$file"; then
		printf 'Expected the log not to contain "%s", got:\n' "$unexpected" >&2
		cat "$file" >&2 || true
		exit 1
	fi
}

assert_no_play_response_files() {
	local dir="$1"
	local label="$2"
	local leftover

	leftover="$(find "$dir" -name 'tuindice-play-response.*' -print)"
	if [[ -n "$leftover" ]]; then
		printf 'Expected %s to remove its Google Play response files, found:\n%s\n' "$label" "$leftover" >&2
		exit 1
	fi
}

write_google_play_curl_stub() {
	local bin_dir="$1"

	cat >"${bin_dir}/curl" <<'SH'
#!/usr/bin/env bash
set -euo pipefail

mode="${TUINDICE_GOOGLE_PLAY_TEST_MODE:?}"
version_code="${TUINDICE_GOOGLE_PLAY_TEST_VERSION_CODE:?}"
put_payload="${TUINDICE_GOOGLE_PLAY_TEST_PUT_PAYLOAD:-}"
method="GET"
output_file=""
data_binary=""
write_status=0
url=""

while [[ "$#" -gt 0 ]]; do
	case "$1" in
		-X)
			method="$2"
			shift 2
			;;
		-H|-o|-w|--retry|--retry-delay)
			if [[ "$1" == "-o" ]]; then
				output_file="$2"
			fi
			if [[ "$1" == "-w" ]]; then
				write_status=1
			fi
			shift 2
			;;
		--data-binary)
			data_binary="${2#@}"
			shift 2
			;;
		--fail|--silent|--show-error)
			shift
			;;
		*)
			url="$1"
			shift
			;;
	esac
done

# Answers like curl run with -o and -w: the body goes to the output file, the HTTP code to stdout.
reply() {
	local body="$1"
	local status="$2"

	if [[ -n "$output_file" ]]; then
		printf '%s\n' "$body" >"$output_file"
	else
		printf '%s\n' "$body"
	fi
	if [[ "$write_status" == "1" ]]; then
		printf '%s' "$status"
	fi
}

case "$method:$url" in
	POST:*/edits)
		reply '{"id":"edit-1"}' 200
		;;
	GET:*/tracks/*)
		[[ -n "$output_file" ]] || {
			printf 'Expected curl -o output file.\n' >&2
			exit 1
		}
		case "$mode" in
			exists)
				jq -n --argjson version_code "$version_code" '{
					releases: [
						{
							status: "draft",
							versionCodes: [$version_code]
						}
					]
				}' >"$output_file"
				;;
			publish-preserves-releases|publish-replaces-draft|publish-replaces-draft-no-notes|check-older-draft|newer-draft|unknown-draft|multiple-drafts|multiple-codes)
				jq -n --arg mode "$mode" --argjson version_code "$version_code" '{
					releases: ([
						{
							name: "6.0.0 (40)",
							status: "completed",
							versionCodes: [40]
						},
						({
							name: "6.1.0 (41)",
							status: (if $mode == "publish-preserves-releases" then "inProgress" else "draft" end),
							versionCodes: (if $mode == "newer-draft" then [($version_code + 1)] elif $mode == "unknown-draft" then [] elif $mode == "multiple-codes" then ["41", "42"] else ["41"] end),
							inAppUpdatePriority: 1
						}
						+ (if $mode == "publish-replaces-draft-no-notes" then {} else {releaseNotes: [{language: "es-419", text: "Notas anteriores"}]} end)
						+ (if $mode == "publish-preserves-releases" then {userFraction: 0.1} else {} end)
						+ (if $mode == "publish-replaces-draft" then {userFraction: 0.1, countryTargeting: {countries: ["CL"], includeRestOfWorld: false}} else {} end))
					] + (if $mode == "multiple-drafts" then [{name: "Second draft", status: "draft", versionCodes: ["42"]}] else [] end))
				}' >"$output_file"
				;;
			publish-first-release)
				printf '{"error":{"code":404}}\n' >"$output_file"
				printf '404'
				exit 0
				;;
			*)
				printf '{"releases":[]}\n' >"$output_file"
				;;
		esac
		printf '200'
		;;
	POST:*/bundles\?uploadType=media)
		[[ -s "$data_binary" ]] || {
			printf 'Expected an uploaded Android App Bundle.\n' >&2
			exit 1
		}
		if [[ "$mode" == "upload-http-error" ]]; then
			reply '{"error":{"message":"Bundle rejected by Play"}}' 400
		else
			reply "$(printf '{"versionCode":%s}' "$version_code")" 200
		fi
		;;
	PUT:*/tracks/*)
		[[ -n "$put_payload" ]] || {
			printf 'Expected TUINDICE_GOOGLE_PLAY_TEST_PUT_PAYLOAD to be set.\n' >&2
			exit 1
		}
		cp "$data_binary" "$put_payload"
		case "$mode" in
			publish-put-transport-error)
				# curl prints 000 when no HTTP status arrived, and exits with its transport error code.
				printf '000'
				exit 52
				;;
			publish-put-error)
				reply '{"error":{"message":"Play track rejects request"}}' 400
				;;
			*)
				reply '{}' 200
				;;
		esac
		;;
	POST:*/edits/*:commit)
		if [[ "$mode" == "commit-http-error" ]]; then
			reply '{"error":{"message":"Commit rejected by Play"}}' 400
		else
			reply '{"id":"edit-1"}' 200
		fi
		;;
	DELETE:*/edits/*)
		;;
	*)
		printf 'Unexpected Google Play API call: %s %s\n' "$method" "$url" >&2
		exit 1
		;;
esac
SH
	chmod +x "${bin_dir}/curl"
}

new_google_play_fixture_dir() {
	local label="$1"
	local temp_dir

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-google-play-${label}-test.XXXXXX")"
	mkdir -p "${temp_dir}/bin"
	printf 'staged-bundle\n' >"${temp_dir}/app-release.aab"
	write_google_play_curl_stub "${temp_dir}/bin"
	printf '%s\n' "$temp_dir"
}

run_google_play_check_fixture() {
	local name="$1"
	local expected_exists="$2"
	local expected_status="$3"
	local temp_dir
	local bin_dir
	local github_output
	local exists_file
	local output_log

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-google-play-check-test.XXXXXX")"
	bin_dir="${temp_dir}/bin"
	github_output="${temp_dir}/github-output.txt"
	exists_file="${temp_dir}/android-release-exists.env"
	output_log="${temp_dir}/output.log"
	mkdir -p "$bin_dir"
	write_google_play_curl_stub "$bin_dir"

	if ! (
		cd "${REPO_ROOT}"
		PATH="${bin_dir}:${PATH}" \
		RUNNER_TEMP="${temp_dir}" \
		ANDROID_PUBLISHER_ACCESS_TOKEN="test-token" \
		GOOGLE_PLAY_CHECK_ONLY=1 \
		GOOGLE_PLAY_RELEASE_EXISTS_FILE="$exists_file" \
		GITHUB_OUTPUT="$github_output" \
		TUINDICE_GOOGLE_PLAY_TEST_MODE="$name" \
		TUINDICE_GOOGLE_PLAY_TEST_VERSION_CODE="$ANDROID_VERSION_CODE" \
			bash ./.github/scripts/publish-google-play-draft.sh
	) >"$output_log" 2>&1; then
		cat "$output_log" >&2
		exit 1
	fi

	assert_file_contains_line "$exists_file" "exists=${expected_exists}" "Google Play state"
	assert_file_contains_line "$exists_file" "status=${expected_status}" "Google Play state"
	assert_file_contains_line "$github_output" "android_draft_exists=${expected_exists}" "GitHub output"
	assert_file_contains_line "$github_output" "android_draft_status=${expected_status}" "GitHub output"

	if [[ "$name" == "check-older-draft" ]]; then
		assert_log_contains "$output_log" "An older Google Play draft on track production will be replaced by ${VERSION_NAME} (${ANDROID_VERSION_CODE})."
		assert_log_lacks "$output_log" "Replacing an older Google Play draft"
		assert_log_lacks "$output_log" "$INHERITED_NOTES_WARNING"
	fi
	assert_no_play_response_files "$temp_dir" "check fixture ${name}"
}

run_google_play_publish_fixture() {
	local name="$1"
	local expected_version_codes="$2"
	local temp_dir
	local bin_dir
	local github_output
	local track_payload
	local aab_path
	local output_log
	local actual_version_codes
	local published_status

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-google-play-publish-test.XXXXXX")"
	bin_dir="${temp_dir}/bin"
	github_output="${temp_dir}/github-output.txt"
	track_payload="${temp_dir}/track-payload.json"
	aab_path="${temp_dir}/app-release.aab"
	output_log="${temp_dir}/output.log"
	mkdir -p "$bin_dir"
	printf 'staged-bundle\n' >"$aab_path"
	write_google_play_curl_stub "$bin_dir"

	if ! (
		cd "${REPO_ROOT}"
		PATH="${bin_dir}:${PATH}" \
		RUNNER_TEMP="${temp_dir}" \
		ANDROID_PUBLISHER_ACCESS_TOKEN="test-token" \
		ANDROID_AAB_PATH="$aab_path" \
		GITHUB_OUTPUT="$github_output" \
		TUINDICE_GOOGLE_PLAY_TEST_MODE="$name" \
		TUINDICE_GOOGLE_PLAY_TEST_VERSION_CODE="$ANDROID_VERSION_CODE" \
		TUINDICE_GOOGLE_PLAY_TEST_PUT_PAYLOAD="$track_payload" \
			bash ./.github/scripts/publish-google-play-draft.sh
	) >"$output_log" 2>&1; then
		cat "$output_log" >&2
		exit 1
	fi

	[[ -s "$track_payload" ]] || {
		printf 'Expected fixture %s to send a Google Play track payload.\n' "$name" >&2
		exit 1
	}

	actual_version_codes="$(jq -r '[.releases[].versionCodes[] | tostring] | join(",")' "$track_payload")"
	if [[ "$actual_version_codes" != "$expected_version_codes" ]]; then
		printf 'Expected fixture %s to send version codes "%s", got "%s":\n' "$name" "$expected_version_codes" "$actual_version_codes" >&2
		cat "$track_payload" >&2
		exit 1
	fi

	published_status="$(
		jq -r \
			--arg version_code "$ANDROID_VERSION_CODE" \
			'[
				.releases[]
				| select(any(.versionCodes[]?; tostring == $version_code))
				| .status
			][0] // empty' \
			"$track_payload"
	)"
	if [[ "$published_status" != "draft" ]]; then
		printf 'Expected fixture %s to publish version code %s as a draft, got "%s".\n' "$name" "$ANDROID_VERSION_CODE" "$published_status" >&2
		exit 1
	fi
	if [[ "$name" == "publish-preserves-releases" ]] && ! jq -e \
		'any(.releases[]; .status == "completed" and .versionCodes == [40]) and
		 any(.releases[]; .status == "inProgress" and .userFraction == 0.1 and .versionCodes == ["41"])' \
		"$track_payload" >/dev/null; then
		printf 'Expected existing production releases to remain unchanged, got:\n' >&2
		cat "$track_payload" >&2
		exit 1
	fi
	if [[ "$name" == "publish-replaces-draft" ]] && ! jq -e \
		--arg version_code "$ANDROID_VERSION_CODE" \
		--arg release_name "${VERSION_NAME} (${ANDROID_VERSION_CODE})" \
		'([.releases[] | select(.status == "draft")] | length == 1) and
		 any(.releases[]; .status == "draft" and .versionCodes == [$version_code] and
		   .name == $release_name and .releaseNotes == [{language: "es-419", text: "Notas anteriores"}] and .inAppUpdatePriority == 1 and
		   (has("userFraction") | not) and (has("countryTargeting") | not))' \
		"$track_payload" >/dev/null; then
		printf 'Expected one updated draft retaining its notes and priority, without userFraction or countryTargeting, got:\n' >&2
		cat "$track_payload" >&2
		exit 1
	fi
	if [[ "$name" == "publish-replaces-draft-no-notes" ]] && ! jq -e \
		--arg version_code "$ANDROID_VERSION_CODE" \
		'any(.releases[]; .status == "draft" and .versionCodes == [$version_code] and (has("releaseNotes") | not))' \
		"$track_payload" >/dev/null; then
		printf 'Expected one updated draft without release notes, got:\n' >&2
		cat "$track_payload" >&2
		exit 1
	fi

	if [[ "$name" == "publish-replaces-draft" ]]; then
		assert_log_contains "$output_log" "Replacing an older Google Play draft on track production with ${VERSION_NAME} (${ANDROID_VERSION_CODE}) when the edit is committed."
		assert_log_contains "$output_log" "$INHERITED_NOTES_WARNING"
	else
		assert_log_lacks "$output_log" "$INHERITED_NOTES_WARNING"
	fi
	assert_no_play_response_files "$temp_dir" "publish fixture ${name}"
}

run_google_play_failing_publish() {
	local mode="$1"
	local temp_dir="$2"

	if (
		cd "$REPO_ROOT"
		PATH="${temp_dir}/bin:${PATH}" \
		RUNNER_TEMP="$temp_dir" \
		ANDROID_PUBLISHER_ACCESS_TOKEN="test-token" \
		ANDROID_AAB_PATH="${temp_dir}/app-release.aab" \
		TUINDICE_GOOGLE_PLAY_TEST_MODE="$mode" \
		TUINDICE_GOOGLE_PLAY_TEST_VERSION_CODE="$ANDROID_VERSION_CODE" \
		TUINDICE_GOOGLE_PLAY_TEST_PUT_PAYLOAD="${temp_dir}/payload.json" \
			bash ./.github/scripts/publish-google-play-draft.sh
	) >"${temp_dir}/output.log" 2>&1; then
		printf 'Expected Google Play publish fixture %s to fail.\n' "$mode" >&2
		cat "${temp_dir}/output.log" >&2
		exit 1
	fi
}

run_google_play_put_error_fixture() {
	local temp_dir
	temp_dir="$(new_google_play_fixture_dir put-error)"

	run_google_play_failing_publish publish-put-error "$temp_dir"
	assert_log_contains "${temp_dir}/output.log" 'Play track rejects request'
	assert_log_contains "${temp_dir}/output.log" 'Google Play API PUT failed with HTTP 400'
	assert_log_contains "${temp_dir}/output.log" 'Deleting uncommitted Google Play edit'
	assert_log_lacks "${temp_dir}/output.log" 'Committing Google Play edit'
	assert_no_play_response_files "$temp_dir" "publish-put-error"
}

run_google_play_put_transport_error_fixture() {
	local temp_dir
	temp_dir="$(new_google_play_fixture_dir put-transport-error)"

	run_google_play_failing_publish publish-put-transport-error "$temp_dir"
	assert_log_contains "${temp_dir}/output.log" 'Deleting uncommitted Google Play edit'
	assert_log_lacks "${temp_dir}/output.log" 'Committing Google Play edit'
	assert_log_lacks "${temp_dir}/output.log" 'Google Play draft release created'
	assert_no_play_response_files "$temp_dir" "publish-put-transport-error"
}

run_google_play_commit_error_fixture() {
	local temp_dir
	temp_dir="$(new_google_play_fixture_dir commit-error)"

	run_google_play_failing_publish commit-http-error "$temp_dir"
	assert_log_contains "${temp_dir}/output.log" 'Commit rejected by Play'
	assert_log_contains "${temp_dir}/output.log" 'Google Play API POST failed with HTTP 400'
	assert_log_contains "${temp_dir}/output.log" 'Deleting uncommitted Google Play edit'
	assert_log_lacks "${temp_dir}/output.log" 'Google Play draft release created'
	assert_no_play_response_files "$temp_dir" "commit-http-error"
}

run_google_play_upload_error_fixture() {
	local temp_dir
	temp_dir="$(new_google_play_fixture_dir upload-error)"

	run_google_play_failing_publish upload-http-error "$temp_dir"
	assert_log_contains "${temp_dir}/output.log" 'Bundle rejected by Play'
	assert_log_contains "${temp_dir}/output.log" 'Google Play API POST failed with HTTP 400'
	assert_log_contains "${temp_dir}/output.log" 'Deleting uncommitted Google Play edit'
	assert_log_lacks "${temp_dir}/output.log" 'Creating Google Play draft release'
	if [[ -e "${temp_dir}/payload.json" ]]; then
		printf 'Expected a rejected bundle upload not to send a track payload.\n' >&2
		exit 1
	fi
	assert_no_play_response_files "$temp_dir" "upload-http-error"
}

run_google_play_conflicting_draft_fixture() {
	local mode="$1"
	local check_only="$2"
	local expected_message="$3"
	local temp_dir
	local github_output
	local exists_file

	temp_dir="$(new_google_play_fixture_dir conflict)"
	github_output="${temp_dir}/github-output.txt"
	exists_file="${temp_dir}/android-release-exists.env"

	if (
		cd "$REPO_ROOT"
		PATH="${temp_dir}/bin:${PATH}" \
		RUNNER_TEMP="$temp_dir" \
		ANDROID_PUBLISHER_ACCESS_TOKEN="test-token" \
		ANDROID_AAB_PATH="${temp_dir}/app-release.aab" \
		GOOGLE_PLAY_CHECK_ONLY="$check_only" \
		GOOGLE_PLAY_RELEASE_EXISTS_FILE="$exists_file" \
		GITHUB_OUTPUT="$github_output" \
		TUINDICE_GOOGLE_PLAY_TEST_MODE="$mode" \
		TUINDICE_GOOGLE_PLAY_TEST_VERSION_CODE="$ANDROID_VERSION_CODE" \
		TUINDICE_GOOGLE_PLAY_TEST_PUT_PAYLOAD="${temp_dir}/payload.json" \
			bash ./.github/scripts/publish-google-play-draft.sh
	) >"${temp_dir}/output.log" 2>&1; then
		printf 'Expected %s (CHECK_ONLY=%s) to block publishing.\n' "$mode" "$check_only" >&2
		cat "${temp_dir}/output.log" >&2
		exit 1
	fi

	assert_log_contains "${temp_dir}/output.log" "$expected_message"
	assert_log_lacks "${temp_dir}/output.log" 'Uploading '
	if [[ -e "${temp_dir}/payload.json" ]]; then
		printf 'Expected a conflicting draft (%s) not to send a track payload.\n' "$mode" >&2
		exit 1
	fi
	if [[ -e "$exists_file" ]] || grep -Fq 'exists=' "$github_output" 2>/dev/null; then
		printf 'Expected a conflicting draft (%s, CHECK_ONLY=%s) not to write an exists= state.\n' "$mode" "$check_only" >&2
		exit 1
	fi
	assert_no_play_response_files "$temp_dir" "conflict fixture ${mode}"
}

run_google_play_check_fixture missing false ""
run_google_play_check_fixture exists true draft
run_google_play_check_fixture check-older-draft false ""
run_google_play_publish_fixture publish-preserves-releases "40,41,${ANDROID_VERSION_CODE}"
run_google_play_publish_fixture publish-first-release "${ANDROID_VERSION_CODE}"
run_google_play_publish_fixture publish-replaces-draft "40,${ANDROID_VERSION_CODE}"
run_google_play_publish_fixture publish-replaces-draft-no-notes "40,${ANDROID_VERSION_CODE}"
run_google_play_put_error_fixture
run_google_play_put_transport_error_fixture
run_google_play_commit_error_fixture
run_google_play_upload_error_fixture

for check_only in 0 1; do
	run_google_play_conflicting_draft_fixture newer-draft "$check_only" 'has a newer or ambiguous draft'
	run_google_play_conflicting_draft_fixture unknown-draft "$check_only" 'has a newer or ambiguous draft'
	run_google_play_conflicting_draft_fixture multiple-codes "$check_only" 'has a newer or ambiguous draft'
	run_google_play_conflicting_draft_fixture multiple-drafts "$check_only" 'has multiple draft releases'
done

printf 'Google Play draft fixtures passed for %s (%s).\n' "$VERSION_NAME" "$ANDROID_VERSION_CODE"
