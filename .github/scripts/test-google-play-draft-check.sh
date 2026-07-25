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

case "$method:$url" in
	POST:*/edits)
		printf '{"id":"edit-1"}\n'
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
			publish-preserves-releases)
				jq -n '{
					releases: [
						{
							name: "6.0.0 (40)",
							status: "completed",
							versionCodes: [40]
						},
						{
							name: "6.1.0 (41)",
							status: "draft",
							versionCodes: [41]
						}
					]
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
		printf '{"versionCode":%s}\n' "$version_code"
		;;
	PUT:*/tracks/*)
		[[ -n "$put_payload" ]] || {
			printf 'Expected TUINDICE_GOOGLE_PLAY_TEST_PUT_PAYLOAD to be set.\n' >&2
			exit 1
		}
		cp "$data_binary" "$put_payload"
		printf '{}\n'
		;;
	POST:*/edits/*:commit)
		printf '{"id":"edit-1"}\n'
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

run_google_play_check_fixture() {
	local name="$1"
	local expected_exists="$2"
	local expected_status="$3"
	local temp_dir
	local bin_dir
	local github_output
	local exists_file

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-google-play-check-test.XXXXXX")"
	bin_dir="${temp_dir}/bin"
	github_output="${temp_dir}/github-output.txt"
	exists_file="${temp_dir}/android-release-exists.env"
	mkdir -p "$bin_dir"
	write_google_play_curl_stub "$bin_dir"

	(
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
	)

	assert_file_contains_line "$exists_file" "exists=${expected_exists}" "Google Play state"
	assert_file_contains_line "$exists_file" "status=${expected_status}" "Google Play state"
	assert_file_contains_line "$github_output" "android_draft_exists=${expected_exists}" "GitHub output"
	assert_file_contains_line "$github_output" "android_draft_status=${expected_status}" "GitHub output"
}

run_google_play_publish_fixture() {
	local name="$1"
	local expected_version_codes="$2"
	local temp_dir
	local bin_dir
	local github_output
	local track_payload
	local aab_path
	local actual_version_codes
	local published_status

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-google-play-publish-test.XXXXXX")"
	bin_dir="${temp_dir}/bin"
	github_output="${temp_dir}/github-output.txt"
	track_payload="${temp_dir}/track-payload.json"
	aab_path="${temp_dir}/app-release.aab"
	mkdir -p "$bin_dir"
	printf 'staged-bundle\n' >"$aab_path"
	write_google_play_curl_stub "$bin_dir"

	(
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
	)

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
}

run_google_play_check_fixture missing false ""
run_google_play_check_fixture exists true draft
run_google_play_publish_fixture publish-preserves-releases "40,41,${ANDROID_VERSION_CODE}"
run_google_play_publish_fixture publish-first-release "${ANDROID_VERSION_CODE}"

printf 'Google Play draft fixtures passed for %s (%s).\n' "$VERSION_NAME" "$ANDROID_VERSION_CODE"
