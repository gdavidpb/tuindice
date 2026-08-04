#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

require_tool() {
	command -v "$1" >/dev/null 2>&1 || {
		printf 'Required tool %s is not available.\n' "$1" >&2
		exit 1
	}
}

require_tool git
require_tool jq

TARGET_SHA="$(git -C "${REPO_ROOT}" rev-parse HEAD)"

run_preflight_fixture() {
	local name="$1"
	local curl_mode="$2"
	local expected_status="$3"
	local temp_dir
	local bin_dir
	local status_file
	local summary_file
	local android_contexts_file
	local ios_contexts_file
	local missing_version_file
	local fingerprint_script
	local output_file
	local status

	temp_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-preflight-test.XXXXXX")"
	bin_dir="${temp_dir}/bin"
	status_file="${temp_dir}/missing-e2e-statuses.txt"
	summary_file="${temp_dir}/summary.md"
	android_contexts_file="${temp_dir}/android-contexts.txt"
	ios_contexts_file="${temp_dir}/ios-contexts.txt"
	missing_version_file="${temp_dir}/missing-version.txt"
	fingerprint_script="${temp_dir}/fingerprint.sh"
	output_file="${temp_dir}/output.log"
	mkdir -p "${bin_dir}"

	printf 'local-e2e/android/record-suite\n' >"${android_contexts_file}"
	: >"${ios_contexts_file}"
	: >"${missing_version_file}"
	cat >"${fingerprint_script}" <<'SH'
#!/usr/bin/env bash
set -euo pipefail
printf 'fixture-fingerprint-%s-%s\n' "$1" "$2"
SH
	chmod +x "${fingerprint_script}"

	cat >"${bin_dir}/curl" <<'SH'
#!/usr/bin/env bash
set -euo pipefail

mode="${TUINDICE_PREFLIGHT_TEST_CURL_MODE:?}"
method="GET"
payload=""
url=""

while [[ "$#" -gt 0 ]]; do
	case "$1" in
		-X)
			method="$2"
			shift 2
			;;
		--data|--data-urlencode|-d)
			payload="$2"
			shift 2
			;;
		-H|--header|--request|--output|--retry|--retry-delay)
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

if [[ "${method}" == "POST" ]]; then
	if [[ "${mode}" == "publish-fails" ]]; then
		printf '{"state":"success","context":"wrong-context"}\n'
		exit 0
	fi

	context="$(printf '%s\n' "${payload}" | jq -r '.context')"
	printf '{"state":"success","context":"%s"}\n' "${context}"
	exit 0
fi

if [[ "${url}" == *"/commits/"*"/pulls" ]]; then
	printf '[]\n'
	exit 0
fi

if [[ "${url}" == *"/commits/"*"/statuses" ]]; then
	# The list endpoint (plural /statuses) returns a bare array of full status
	# objects, creator included -- unlike the combined-status endpoint
	# (singular /status), which never includes creator. preflight-production.sh
	# queries the former precisely so a real creator is visible to check.
	if [[ ("${mode}" == "reuse-success" || "${mode}" == "publish-fails") && "${url}" != *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/record-suite","state":"success","creator":{"login":"gdavidpb"},"description":"Local E2E record-suite passed for 1234567 fp fixture-fing."}]\n'
	elif [[ "${mode}" == "direct-success" && "${url}" == *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/record-suite","state":"success","creator":{"login":"gdavidpb"},"description":"Local E2E record-suite passed for 1234567 fp fixture-fing."}]\n'
	elif [[ "${mode}" == "forged-status" && "${url}" == *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/record-suite","state":"success","creator":{"login":"gdavidpb"},"description":"Local E2E record-suite passed for 1234567 fp 000000000000."}]\n'
	elif [[ "${mode}" == "untrusted-creator" && "${url}" == *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/record-suite","state":"success","creator":{"login":"intruder"},"description":"Local E2E record-suite passed for 1234567 fp fixture-fing."}]\n'
	elif [[ "${mode}" == "untrusted-candidate" && "${url}" != *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/record-suite","state":"success","creator":{"login":"intruder"},"description":"Local E2E record-suite passed for 1234567 fp fixture-fing."}]\n'
	elif [[ "${mode}" == "forged-candidate" && "${url}" != *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/record-suite","state":"success","creator":{"login":"gdavidpb"},"description":"no fingerprint at all"}]\n'
	elif [[ "${mode}" == "anonymous-status" && "${url}" == *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/record-suite","state":"success","description":"Local E2E record-suite passed for 1234567 fp fixture-fing."}]\n'
	else
		printf '[]\n'
	fi
	exit 0
fi

printf '{}\n'
SH
	chmod +x "${bin_dir}/curl"

	set +e
	(
		cd "${REPO_ROOT}"
		PATH="${bin_dir}:${PATH}" \
		TUINDICE_PREFLIGHT_TEST_CURL_MODE="${curl_mode}" \
		TUINDICE_PREFLIGHT_TEST_TARGET_SHA="${TARGET_SHA}" \
		GITHUB_REPOSITORY="gdavidpb/tuindice" \
		GITHUB_TOKEN="fixture-token" \
		GITHUB_API_URL="https://api.github.test" \
		TARGET_GIT_SHA="${TARGET_SHA}" \
		E2E_REUSE_BASE_SHA="" \
		E2E_REUSE_MAX_COMMITS="5" \
		E2E_REUSE_STATUS_BY_FINGERPRINT="${E2E_REUSE_STATUS_BY_FINGERPRINT:-1}" \
		E2E_FINGERPRINT_SCRIPT="${fingerprint_script}" \
		MISSING_E2E_STATUSES_FILE="${status_file}" \
		MISSING_VERSION_BUMP_FILE="${missing_version_file}" \
		E2E_ANDROID_CONTEXTS_FILE="${android_contexts_file}" \
		E2E_IOS_CONTEXTS_FILE="${ios_contexts_file}" \
		REQUIRES_E2E_CERTIFICATION="true" \
		HAS_RELEVANT_CHANGES="true" \
		APP_VERSION_CHANGED="false" \
		HAS_RELEASE_IMPACT="false" \
		SUMMARY_FILE="${summary_file}" \
		bash ./.github/scripts/preflight-production.sh
	) >"${output_file}" 2>&1
	status="$?"
	set -e

	if [[ "${expected_status}" == "success" && "${status}" != "0" ]]; then
		printf 'Preflight fixture %s was expected to pass but failed.\n' "${name}" >&2
		cat "${output_file}" >&2
		exit 1
	fi

	if [[ "${expected_status}" == "failure" && "${status}" == "0" ]]; then
		printf 'Preflight fixture %s was expected to fail but passed.\n' "${name}" >&2
		cat "${output_file}" >&2
		exit 1
	fi

	case "${name}" in
		reuse-success)
			if ! grep -q 'Skipping app version validation' "${output_file}"; then
				printf 'Preflight fixture %s did not skip app version validation for E2E-only scope.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			if grep -q 'Missing successful E2E status' "${output_file}"; then
				printf 'Preflight fixture %s unexpectedly reported missing evidence.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			;;
		direct-success)
			if ! grep -q 'Found successful E2E status' "${output_file}"; then
				printf 'Preflight fixture %s did not accept the direct E2E status.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			;;
		forged-status|forged-status-reuse)
			if ! grep -q 'does not match the current fingerprint' "${output_file}"; then
				printf 'Preflight fixture %s did not reject the forged fingerprint.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			;;
		untrusted-creator|untrusted-creator-reuse)
			if ! grep -q 'is not trusted' "${output_file}"; then
				printf 'Preflight fixture %s did not reject the untrusted status creator.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			;;
		publish-fails|missing-status|missing-status-reuse|untrusted-candidate|forged-candidate|anonymous-status)
			if ! grep -q 'Missing successful E2E status' "${output_file}"; then
				printf 'Preflight fixture %s did not report missing evidence.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			;;
	esac

	# Adversarial fixtures run in the production configuration
	# (E2E_REUSE_STATUS_BY_FINGERPRINT=1): fingerprint reuse must never launder a
	# rejected status into a published one.
	case "${name}" in
		*-reuse|untrusted-candidate|forged-candidate)
			if grep -q 'Reused successful E2E status' "${output_file}"; then
				printf 'Preflight fixture %s reused a status that must not have been reusable.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			;;
	esac
}

run_preflight_fixture reuse-success reuse-success success
run_preflight_fixture direct-success direct-success success
E2E_REUSE_STATUS_BY_FINGERPRINT=0 run_preflight_fixture forged-status forged-status failure
E2E_REUSE_STATUS_BY_FINGERPRINT=0 run_preflight_fixture untrusted-creator untrusted-creator failure
run_preflight_fixture publish-fails publish-fails failure
E2E_REUSE_STATUS_BY_FINGERPRINT=0 run_preflight_fixture missing-status missing-status failure
run_preflight_fixture forged-status-reuse forged-status failure
run_preflight_fixture untrusted-creator-reuse untrusted-creator failure
run_preflight_fixture missing-status-reuse missing-status failure
run_preflight_fixture untrusted-candidate untrusted-candidate failure
run_preflight_fixture forged-candidate forged-candidate failure
run_preflight_fixture anonymous-status anonymous-status failure

printf 'Preflight production shell fixtures passed.\n'
