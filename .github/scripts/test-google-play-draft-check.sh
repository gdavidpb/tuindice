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

	cat >"${bin_dir}/curl" <<'SH'
#!/usr/bin/env bash
set -euo pipefail

mode="${TUINDICE_GOOGLE_PLAY_TEST_MODE:?}"
version_code="${TUINDICE_GOOGLE_PLAY_TEST_VERSION_CODE:?}"
method="GET"
output_file=""
url=""

while [[ "$#" -gt 0 ]]; do
	case "$1" in
		-X)
			method="$2"
			shift 2
			;;
		-H|-o|-w)
			if [[ "$1" == "-o" ]]; then
				output_file="$2"
			fi
			shift 2
			;;
		--data-binary)
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
		if [[ "$mode" == "exists" ]]; then
			jq -n --argjson version_code "$version_code" '{
				releases: [
					{
						status: "draft",
						versionCodes: [$version_code]
					}
				]
			}' >"$output_file"
		else
			printf '{"releases":[]}\n' >"$output_file"
		fi
		printf '200'
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

run_google_play_check_fixture missing false ""
run_google_play_check_fixture exists true draft

printf 'Google Play check-only fixtures passed for %s (%s).\n' "$VERSION_NAME" "$ANDROID_VERSION_CODE"
