#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

require_tool jq
require_tool ruby

VERSION_NAME="$(get_app_version_name)"
IOS_BUILD_NUMBER="$(get_ios_build_number)"

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

run_appstore_check_fixture() {
	local name="$1"
	local expected_exists="$2"
	local expected_build_id="$3"
	local temp_dir
	local bin_dir
	local api_key_path
	local github_output
	local exists_file

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-appstore-check-test.XXXXXX")"
	bin_dir="${temp_dir}/bin"
	api_key_path="${temp_dir}/AuthKey_TESTKEYID1.p8"
	github_output="${temp_dir}/github-output.txt"
	exists_file="${temp_dir}/ios-build-exists.env"
	mkdir -p "$bin_dir"

	ruby -ropenssl -e 'puts OpenSSL::PKey::EC.generate("prime256v1").to_pem' >"$api_key_path"

	cat >"${bin_dir}/curl" <<'SH'
#!/usr/bin/env bash
set -euo pipefail

mode="${TUINDICE_APPSTORE_TEST_MODE:?}"
version_name="${TUINDICE_APPSTORE_TEST_VERSION_NAME:?}"
method="GET"
output_file=""
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
		--silent|--show-error|--fail)
			shift
			;;
		*)
			url="$1"
			shift
			;;
	esac
done

if [[ "$method" != "GET" ]]; then
	printf 'Unexpected method: %s\n' "$method" >&2
	exit 1
fi

[[ -n "$output_file" ]] || {
	printf 'Expected curl -o output file.\n' >&2
	exit 1
}

case "$url" in
	*/apps\?*)
		printf '{"data":[{"id":"app-1"}]}\n' >"$output_file"
		;;
	*/builds\?*)
		if [[ "$mode" == "missing" ]]; then
			printf '{"data":[],"included":[]}\n' >"$output_file"
		else
			processing_state="VALID"
			case "$mode" in
				processing)
					processing_state="PROCESSING"
					;;
				invalid-processing-state)
					processing_state="INVALID"
					;;
				other-marketing-version)
					version_name="0.0.0"
					;;
			esac
			jq -n \
				--arg version_name "$version_name" \
				--arg processing_state "$processing_state" \
				'{
					data: [
						{
							id: "build-1",
							attributes: { processingState: $processing_state },
							relationships: {
								preReleaseVersion: {
									data: { id: "pre-release-1" }
								}
							}
						}
					],
					included: [
						{
							type: "preReleaseVersions",
							id: "pre-release-1",
							attributes: { version: $version_name }
						}
					]
				}' >"$output_file"
		fi
		;;
	*)
		printf 'Unexpected URL: %s\n' "$url" >&2
		exit 1
		;;
esac

printf '200'
SH
	chmod +x "${bin_dir}/curl"

	(
		cd "${REPO_ROOT}"
		PATH="${bin_dir}:${PATH}" \
		RUNNER_TEMP="${temp_dir}" \
		OSTYPE=darwin \
		APP_STORE_CONNECT_CHECK_ONLY=1 \
		APP_STORE_CONNECT_API_KEY_PATH="$api_key_path" \
		APP_STORE_CONNECT_KEY_ID="TESTKEYID1" \
		APP_STORE_CONNECT_ISSUER_ID="00000000-0000-0000-0000-000000000000" \
		APP_STORE_CONNECT_BUILD_EXISTS_FILE="$exists_file" \
		GITHUB_OUTPUT="$github_output" \
		TUINDICE_APPSTORE_TEST_MODE="$name" \
		TUINDICE_APPSTORE_TEST_VERSION_NAME="$VERSION_NAME" \
			bash ./iosApp/scripts/ci-upload-ios-appstore.sh
	)

	assert_file_contains_line "$exists_file" "exists=${expected_exists}" "App Store Connect state"
	assert_file_contains_line "$exists_file" "build_id=${expected_build_id}" "App Store Connect state"
	assert_file_contains_line "$github_output" "ios_build_exists=${expected_exists}" "GitHub output"
	assert_file_contains_line "$github_output" "ios_existing_build_id=${expected_build_id}" "GitHub output"
}

run_appstore_check_fixture missing false ""
run_appstore_check_fixture exists true build-1
run_appstore_check_fixture processing true build-1
run_appstore_check_fixture invalid-processing-state false ""
run_appstore_check_fixture other-marketing-version false ""

printf 'App Store Connect check-only fixtures passed for %s (%s).\n' "$VERSION_NAME" "$IOS_BUILD_NUMBER"
