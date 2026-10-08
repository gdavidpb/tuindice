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

# The default trust list is the one under test; a variable of the caller must not change it.
unset E2E_TRUSTED_STATUS_CREATORS

# The curl stand-in: answers the GETs of the preflight from fixtures and logs every request that is not a GET.
write_curl_stub() {
	cat >"$1/curl" <<'SH'
#!/usr/bin/env bash
set -euo pipefail

mode="${TUINDICE_PREFLIGHT_TEST_CURL_MODE:?}"
method="GET"
payload=""
url=""

while [[ "$#" -gt 0 ]]; do
	case "$1" in
		-X|--request)
			method="$2"
			shift 2
			;;
		-X?*)
			method="${1#-X}"
			shift
			;;
		--data|--data-raw|--data-binary|--data-urlencode|-d|--json|--form|-F)
			# A body makes the request a POST unless a method was given.
			payload="$2"
			[[ "${method}" != "GET" ]] || method="POST"
			shift 2
			;;
		-H|--header|--output|--retry|--retry-delay)
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

# The CI never writes: any request that is not a GET lands here, and every fixture asserts this log stays empty.
if [[ "${method}" != "GET" ]]; then
	printf '%s %s %s\n' "${method}" "${url}" "${payload}" >>"${TUINDICE_PREFLIGHT_TEST_POST_LOG:?}"
	printf '{"state":"success","context":"unexpected-write"}\n'
	exit 0
fi

if [[ "${url}" == *"/commits/"*"/pulls" ]]; then
	# The pull request a squash merge left outside the history: its head is reachable only through this lookup.
	if [[ -n "${TUINDICE_PREFLIGHT_TEST_PR_HEAD:-}" ]]; then
		printf '[{"head":{"sha":"%s"}}]\n' "${TUINDICE_PREFLIGHT_TEST_PR_HEAD}"
	else
		printf '[]\n'
	fi
	exit 0
fi

if [[ "${url}" == *"/commits/"*"/statuses" ]]; then
	printf '%s\n' "${url}" >>"${TUINDICE_PREFLIGHT_TEST_GET_LOG:-/dev/null}"
	# The list endpoint (plural /statuses) returns a bare array of full status
	# objects, creator included -- unlike the combined-status endpoint
	# (singular /status), which never includes creator. preflight-production.sh
	# queries the former precisely so a real creator is visible to check.
	if [[ "${mode}" == "latest-failure" && "${url}" == *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		# Newest first, as the API lists them: a later failure hides the earlier success.
		printf '[{"context":"local-e2e/android/local-certification-suite","state":"failure","creator":{"login":"gdavidpb"},"description":"Local E2E android failed."},{"context":"local-e2e/android/local-certification-suite","state":"success","creator":{"login":"gdavidpb"},"description":"Local E2E android 12/12 passed for 1234567 fp fixture-fing."}]\n'
	elif [[ "${mode}" == "reuse-only-old-sha" ]]; then
		if [[ "${url}" == *"${TUINDICE_PREFLIGHT_TEST_OLD_SHA}"* ]]; then
			printf '[{"context":"local-e2e/android/local-certification-suite","state":"success","creator":{"login":"gdavidpb"},"description":"Local E2E android 12/12 passed for 1234567 fp fixture-fing."}]\n'
		else
			printf '[]\n'
		fi
	elif [[ "${mode}" == "reuse-success" && "${url}" != *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/local-certification-suite","state":"success","creator":{"login":"gdavidpb"},"description":"Local E2E android 12/12 passed for 1234567 fp fixture-fing."}]\n'
	elif [[ "${mode}" == "direct-success" && "${url}" == *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/local-certification-suite","state":"success","creator":{"login":"gdavidpb"},"description":"Local E2E android 12/12 passed for 1234567 fp fixture-fing."}]\n'
	elif [[ "${mode}" == "bot-newer-than-owner" && "${url}" == *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		# Newest first: the Actions bot posted after the owner, and the owner's status is still the evidence.
		printf '[{"context":"local-e2e/android/local-certification-suite","state":"success","creator":{"login":"github-actions[bot]"},"description":"Local E2E android 12/12 passed for 1234567 fp fixture-fing."},{"context":"local-e2e/android/local-certification-suite","state":"success","creator":{"login":"gdavidpb"},"description":"Local E2E android 12/12 passed for 1234567 fp fixture-fing."}]\n'
	elif [[ "${mode}" == "bot-status" && "${url}" == *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/local-certification-suite","state":"success","creator":{"login":"github-actions[bot]"},"description":"Local E2E android 12/12 passed for 1234567 fp fixture-fing."}]\n'
	elif [[ "${mode}" == "forged-status" && "${url}" == *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/local-certification-suite","state":"success","creator":{"login":"gdavidpb"},"description":"Local E2E android 12/12 passed for 1234567 fp 000000000000."}]\n'
	elif [[ "${mode}" == "untrusted-creator" && "${url}" == *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/local-certification-suite","state":"success","creator":{"login":"intruder"},"description":"Local E2E android 12/12 passed for 1234567 fp fixture-fing."}]\n'
	elif [[ "${mode}" == "untrusted-candidate" && "${url}" != *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/local-certification-suite","state":"success","creator":{"login":"intruder"},"description":"Local E2E android 12/12 passed for 1234567 fp fixture-fing."}]\n'
	elif [[ "${mode}" == "forged-candidate" && "${url}" != *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/local-certification-suite","state":"success","creator":{"login":"gdavidpb"},"description":"no fingerprint at all"}]\n'
	elif [[ "${mode}" == "legacy-context" ]]; then
		printf '[{"context":"local-e2e/android/record-suite","state":"success","creator":{"login":"gdavidpb"},"description":"Local E2E android 12/12 passed for 1234567 fp fixture-fing."}]\n'
	elif [[ "${mode}" == "anonymous-status" && "${url}" == *"${TUINDICE_PREFLIGHT_TEST_TARGET_SHA}"* ]]; then
		printf '[{"context":"local-e2e/android/local-certification-suite","state":"success","description":"Local E2E android 12/12 passed for 1234567 fp fixture-fing."}]\n'
	else
		printf '[]\n'
	fi
	exit 0
fi

printf '{}\n'
SH
	chmod +x "$1/curl"
}

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
	local post_log
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
	post_log="${temp_dir}/writes.log"
	get_log="${temp_dir}/statuses-gets.log"
	: >"${post_log}"
	: >"${get_log}"
	mkdir -p "${bin_dir}"

	if [[ -n "${TUINDICE_PREFLIGHT_TEST_NO_CONTEXTS:-}" ]]; then
		: >"${android_contexts_file}"
	else
		printf '%s\n' "${TUINDICE_PREFLIGHT_TEST_CONTEXT:-local-e2e/android/local-certification-suite}" >"${android_contexts_file}"
	fi
	: >"${ios_contexts_file}"
	# The scope the detector wrote (one "platform,suite,reason" line per platform); empty unless a fixture sets it.
	scope_file="${temp_dir}/e2e-scope.csv"
	printf '%s' "${TUINDICE_PREFLIGHT_TEST_SCOPE:-}" >"${scope_file}"
	: >"${missing_version_file}"
	cat >"${fingerprint_script}" <<'SH'
#!/usr/bin/env bash
set -euo pipefail
# The fingerprint is a function of the ref: with TUINDICE_PREFLIGHT_TEST_OTHER_FP set, every ref but the target's
# has another one, as a commit whose tree differs would.
if [[ -n "${TUINDICE_PREFLIGHT_TEST_OTHER_FP:-}" && "$3" != "${TUINDICE_PREFLIGHT_TEST_TARGET_SHA:-}" ]]; then
	printf 'other-fingerprint-%s-%s\n' "$1" "$2"
else
	printf 'fixture-fingerprint-%s-%s\n' "$1" "$2"
fi
SH
	chmod +x "${fingerprint_script}"

	write_curl_stub "${bin_dir}"

	set +e
	(
		cd "${REPO_ROOT}"
		PATH="${bin_dir}:${PATH}" \
		TUINDICE_PREFLIGHT_TEST_CURL_MODE="${curl_mode}" \
		TUINDICE_PREFLIGHT_TEST_TARGET_SHA="${TARGET_SHA}" \
		TUINDICE_PREFLIGHT_TEST_OLD_SHA="${TUINDICE_PREFLIGHT_TEST_OLD_SHA:-}" \
		TUINDICE_PREFLIGHT_TEST_PR_HEAD="${TUINDICE_PREFLIGHT_TEST_PR_HEAD:-}" \
		TUINDICE_PREFLIGHT_TEST_POST_LOG="${post_log}" \
		TUINDICE_PREFLIGHT_TEST_GET_LOG="${get_log}" \
		E2E_EVIDENCE_SCOPE="${TUINDICE_PREFLIGHT_TEST_EVIDENCE_SCOPE-merged}" \
		GITHUB_EVENT_NAME="${TUINDICE_PREFLIGHT_TEST_EVENT:-}" \
		TUINDICE_PREFLIGHT_TEST_OTHER_FP="${TUINDICE_PREFLIGHT_TEST_OTHER_FP:-}" \
		GITHUB_REPOSITORY="gdavidpb/tuindice" \
		GITHUB_TOKEN="fixture-token" \
		GITHUB_API_URL="https://api.github.test" \
		TARGET_GIT_SHA="${TARGET_SHA}" \
		E2E_REUSE_BASE_SHA="${TUINDICE_PREFLIGHT_TEST_REUSE_BASE:-}" \
		E2E_REUSE_MAX_COMMITS="5" \
		E2E_REUSE_STATUS_BY_FINGERPRINT="${E2E_REUSE_STATUS_BY_FINGERPRINT:-1}" \
		E2E_FINGERPRINT_SCRIPT="${fingerprint_script}" \
		MISSING_E2E_STATUSES_FILE="${status_file}" \
		MISSING_VERSION_BUMP_FILE="${missing_version_file}" \
		E2E_ANDROID_CONTEXTS_FILE="${android_contexts_file}" \
		E2E_IOS_CONTEXTS_FILE="${ios_contexts_file}" \
		E2E_SCOPE_FILE="${scope_file}" \
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
			if ! grep -q 'Trusted E2E status creators: gdavidpb$' "${summary_file}"; then
				printf 'Preflight fixture %s did not print the effective trust list in the summary.\n' "${name}" >&2
				cat "${summary_file}" >&2
				exit 1
			fi
			;;
		strict-without-scope|strict-merge-group|merged-scope-never-in-a-pull-request)
			# ZD-3: the evidence has to be on the head unless the caller asked for the merged scope, and a pull request
			# never gets it.
			if ! grep -q "is on .* but not on the head" "${output_file}" || grep -q 'Reused successful E2E status' "${output_file}"; then
				printf 'Preflight fixture %s accepted evidence that is not on the head.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			;;
		trusted-list-with-spaces)
			# ZD-6: the list is trimmed as the harness trims it, the summary shows it, and the bot in it is called out.
			if ! grep -q 'Trusted E2E status creators: gdavidpb, github-actions\[bot\]$' "${summary_file}" ||
				! grep -q 'trusts the Actions bot' "${output_file}"; then
				printf 'Preflight fixture %s did not show the effective list or warn about the bot.\n' "${name}" >&2
				cat "${output_file}" "${summary_file}" >&2
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
		no-contexts)
			if ! grep -q 'neither context file lists a context' "${output_file}"; then
				printf 'Preflight fixture %s did not reject a required certification with no contexts.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			;;
		scope-without-contexts)
			if ! grep -q 'scope lists ios but its context file is empty' "${output_file}"; then
				printf 'Preflight fixture %s did not reject a scope of two platforms with one empty context file.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			;;
		reuse-from-base)
			if ! grep -q 'Reused successful E2E status' "${output_file}"; then
				printf 'Preflight fixture %s did not reuse the status found on the base commit.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			;;
		pr-evidence-on-ancestor)
			# Evidence on an ancestor is not evidence on the head: the run says where it is and how to publish it.
			if ! grep -q 'Missing successful E2E status' "${output_file}" ||
				! grep -q "$(git -C "${REPO_ROOT}" rev-parse HEAD~1)" "${output_file}" ||
				! grep -q 'python3 e2e/scripts/shared/e2e.py publish --platform android' "${output_file}"; then
				printf 'Preflight fixture %s did not name the commit that holds the evidence and the publish command.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			if grep -q 'Reused successful E2E status' "${output_file}"; then
				printf 'Preflight fixture %s accepted evidence that is not on the head.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			;;
		bot-status-untrusted)
			if ! grep -q "creator 'github-actions\[bot\]' is not trusted" "${output_file}"; then
				printf 'Preflight fixture %s did not reject the Actions bot as creator.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			;;
		missing-status|missing-status-reuse|untrusted-candidate|forged-candidate|anonymous-status|latest-failure|reuse-other-fingerprint|reuse-before-base)
			if ! grep -q 'Missing successful E2E status' "${output_file}"; then
				printf 'Preflight fixture %s did not report missing evidence.\n' "${name}" >&2
				cat "${output_file}" >&2
				exit 1
			fi
			;;
	esac

	# ZD-13: a candidate that measures another fingerprint is never asked about, so only the target is queried.
	case "${name}" in
		reuse-other-fingerprint)
			if [[ ! -s "${get_log}" ]] || [[ -n "$(grep -v "/commits/${TARGET_SHA}/statuses" "${get_log}" || true)" ]]; then
				printf 'Preflight fixture %s asked GitHub about commits other than the target:\n' "${name}" >&2
				cat "${get_log}" >&2
				exit 1
			fi
			;;
	esac

	# No route of the script writes a commit status: the curl stand-in logs every request that is not a GET.
	if [[ -s "${post_log}" ]]; then
		printf 'Preflight fixture %s wrote to GitHub:\n' "${name}" >&2
		cat "${post_log}" >&2
		exit 1
	fi

	# Adversarial fixtures run in the production configuration
	# (E2E_REUSE_STATUS_BY_FINGERPRINT=1): fingerprint reuse must never launder a
	# rejected status into a published one.
	case "${name}" in
		*-reuse|untrusted-candidate|forged-candidate|reuse-other-fingerprint|reuse-before-base|latest-failure)
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
E2E_REUSE_STATUS_BY_FINGERPRINT=0 run_preflight_fixture missing-status missing-status failure
run_preflight_fixture forged-status-reuse forged-status failure
run_preflight_fixture untrusted-creator-reuse untrusted-creator failure
run_preflight_fixture missing-status-reuse missing-status failure
run_preflight_fixture untrusted-candidate untrusted-candidate failure
run_preflight_fixture forged-candidate forged-candidate failure
run_preflight_fixture anonymous-status anonymous-status failure
# A context other than the platform's single status context is never evidence, even when it is green.
TUINDICE_PREFLIGHT_TEST_CONTEXT=local-e2e/android/record-suite run_preflight_fixture legacy-context legacy-context failure

# D-14: a certification that is required but lists no context cannot pass by checking nothing.
TUINDICE_PREFLIGHT_TEST_NO_CONTEXTS=1 run_preflight_fixture no-contexts direct-success failure
# ...and neither can a scope of two platforms whose iOS context file is empty (the android one has its context).
TUINDICE_PREFLIGHT_TEST_SCOPE=$'android,local-certification-suite,module-runtime\nios,local-certification-suite,module-runtime\n' \
	run_preflight_fixture scope-without-contexts direct-success failure
# The same files are fine when the scope names only the platform that has its context.
TUINDICE_PREFLIGHT_TEST_SCOPE=$'android,local-certification-suite,module-runtime\n' run_preflight_fixture one-platform-scope direct-success success
# D-15: the fake fingerprint now depends on the ref, so a candidate whose tree differs is not reused...
TUINDICE_PREFLIGHT_TEST_OTHER_FP=1 run_preflight_fixture reuse-other-fingerprint reuse-success failure
# ...the base commit is a candidate and an older commit is not (E2E_REUSE_BASE_SHA is no longer always empty)...
BASE_PARENT_SHA="$(git -C "${REPO_ROOT}" rev-parse HEAD~1)"
OLDER_SHA="$(git -C "${REPO_ROOT}" rev-parse HEAD~3)"
TUINDICE_PREFLIGHT_TEST_REUSE_BASE="${BASE_PARENT_SHA}" TUINDICE_PREFLIGHT_TEST_OLD_SHA="${BASE_PARENT_SHA}" \
	run_preflight_fixture reuse-from-base reuse-only-old-sha success
TUINDICE_PREFLIGHT_TEST_REUSE_BASE="${BASE_PARENT_SHA}" TUINDICE_PREFLIGHT_TEST_OLD_SHA="${OLDER_SHA}" \
	run_preflight_fixture reuse-before-base reuse-only-old-sha failure
# ...and the newest status of the context is the one that counts.
run_preflight_fixture latest-failure latest-failure failure

# Option C: the workflow of a pull request never publishes. The evidence has to be on the head, published by the owner.
# The owner's status on the head passes, with nothing written...
TUINDICE_PREFLIGHT_TEST_EVENT=pull_request run_preflight_fixture pr-owner-on-head direct-success success
# ...evidence only on an ancestor does not: the run names the commit and the command that publishes it on the head...
TUINDICE_PREFLIGHT_TEST_EVENT=pull_request run_preflight_fixture pr-evidence-on-ancestor reuse-success failure
# ...the Actions bot is not a creator of confidence by default (any workflow of a branch publishes as the bot)...
TUINDICE_PREFLIGHT_TEST_EVENT=pull_request run_preflight_fixture bot-status-untrusted bot-status failure
# ...unless the owner lists it in E2E_TRUSTED_STATUS_CREATORS.
E2E_TRUSTED_STATUS_CREATORS='gdavidpb,github-actions[bot]' TUINDICE_PREFLIGHT_TEST_EVENT=pull_request \
	run_preflight_fixture bot-status-trusted-by-variable bot-status success
# The stage (a push to production) finds the owner's evidence without republishing it: in the ancestors of a merge
# commit (reuse-from-base above) and, after a squash, on the head of the pull request the API associates with the commit.
TUINDICE_PREFLIGHT_TEST_EVENT=push TUINDICE_PREFLIGHT_TEST_REUSE_BASE="${BASE_PARENT_SHA}" TUINDICE_PREFLIGHT_TEST_OLD_SHA="${OLDER_SHA}" \
	TUINDICE_PREFLIGHT_TEST_PR_HEAD="${OLDER_SHA}" run_preflight_fixture stage-squash reuse-only-old-sha success

# ZD-3: accepting the evidence of the merged commits is asked for by the caller (the stage), it is not what a missing or
# unexpected event gets, and a pull request never gets it...
TUINDICE_PREFLIGHT_TEST_EVIDENCE_SCOPE= run_preflight_fixture strict-without-scope reuse-success failure
TUINDICE_PREFLIGHT_TEST_EVIDENCE_SCOPE= TUINDICE_PREFLIGHT_TEST_EVENT=merge_group run_preflight_fixture strict-merge-group reuse-success failure
TUINDICE_PREFLIGHT_TEST_EVENT=pull_request run_preflight_fixture merged-scope-never-in-a-pull-request reuse-success failure
# ZD-6: a space after the comma in the list is trimmed (the harness trims it too) and the bot in it is called out.
E2E_TRUSTED_STATUS_CREATORS='gdavidpb, github-actions[bot]' TUINDICE_PREFLIGHT_TEST_EVENT=pull_request \
	run_preflight_fixture trusted-list-with-spaces bot-status success
# ZD-9: a later status of someone else does not hide the owner's evidence on the head.
TUINDICE_PREFLIGHT_TEST_EVENT=pull_request run_preflight_fixture bot-newer-than-owner bot-newer-than-owner success

# ZD-7: the curl stand-in logs every way of writing, not only `-X`; a stand-in that missed one would let a write pass.
stub_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-preflight-stub.XXXXXX")"
write_curl_stub "${stub_dir}"
stub_write_url="https://api.github.test/repos/gdavidpb/tuindice/statuses/abc"
expect_stub_write() {
	local label="$1"
	shift
	: >"${stub_dir}/writes.log"
	TUINDICE_PREFLIGHT_TEST_CURL_MODE=none TUINDICE_PREFLIGHT_TEST_POST_LOG="${stub_dir}/writes.log" \
		TUINDICE_PREFLIGHT_TEST_TARGET_SHA=none "${stub_dir}/curl" "$@" >/dev/null
	if [[ ! -s "${stub_dir}/writes.log" ]]; then
		printf 'The curl stand-in did not log a write made with %s.\n' "${label}" >&2
		exit 1
	fi
}
expect_stub_write '-X POST' -X POST "${stub_write_url}"
expect_stub_write '--request POST' --request POST "${stub_write_url}"
expect_stub_write '-XPOST' -XPOST "${stub_write_url}"
expect_stub_write '--data (implicit POST)' --data 'state=success' "${stub_write_url}"
expect_stub_write '-d (implicit POST)' -d 'state=success' "${stub_write_url}"
expect_stub_write '--data-raw' --data-raw 'x' "${stub_write_url}"
expect_stub_write '--json' --json '{}' "${stub_write_url}"
expect_stub_write '--form' --form 'a=b' "${stub_write_url}"
: >"${stub_dir}/writes.log"
TUINDICE_PREFLIGHT_TEST_CURL_MODE=none TUINDICE_PREFLIGHT_TEST_POST_LOG="${stub_dir}/writes.log" \
	TUINDICE_PREFLIGHT_TEST_TARGET_SHA=none "${stub_dir}/curl" --fail --silent -H 'A: b' "${stub_write_url}" >/dev/null
if [[ -s "${stub_dir}/writes.log" ]]; then
	printf 'The curl stand-in logged a plain GET as a write.\n' >&2
	exit 1
fi
# ...and neither script that reads statuses can write one: no request to a statuses/<sha> URL, no gh api, no write method.
for script_file in preflight-production.sh deploy-production.sh; do
	write_routes="$(grep -nE -e '/statuses/|gh +api|(-X|--request)[ =]+(POST|PUT|PATCH|DELETE)|--data|--json' "${SCRIPT_DIR}/${script_file}" || true)"
	if [[ -n "${write_routes}" ]]; then
		printf '%s has a route that writes to GitHub:\n%s\n' "${script_file}" "${write_routes}" >&2
		exit 1
	fi
done

printf 'Preflight production shell fixtures passed.\n'
