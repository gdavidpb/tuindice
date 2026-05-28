#!/usr/bin/env bash
set -euo pipefail

REQUESTED_E2E_MAESTRO_SUITE="${E2E_MAESTRO_SUITE:-}"

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

PLATFORM="${1:-}"
if [[ "$PLATFORM" != "android" && "$PLATFORM" != "ios" ]]; then
	printf 'Usage: %s <android|ios>\n' "$0" >&2
	exit 1
fi

require_command git
require_command jq

COMMIT_SHA="${E2E_COMMIT_SHA:-${E2E_HEAD_SHA:-$(git -C "${REPO_ROOT}" rev-parse HEAD)}}"
COMMIT_SHA="$(git -C "${REPO_ROOT}" rev-parse "${COMMIT_SHA}^{commit}")"

publish_mode_is_disabled() {
	case "${E2E_PUBLISH_GITHUB_STATUS:-auto}" in
		0|false|False|FALSE|no|No|NO|off|Off|OFF|never|Never|NEVER)
			return 0
			;;
	esac

	return 1
}

publish_mode_is_required() {
	case "${E2E_PUBLISH_GITHUB_STATUS:-auto}" in
		1|true|True|TRUE|yes|Yes|YES|on|On|ON|always|Always|ALWAYS)
			return 0
			;;
	esac

	return 1
}

validate_publish_mode() {
	case "${E2E_PUBLISH_GITHUB_STATUS:-auto}" in
		0|false|False|FALSE|no|No|NO|off|Off|OFF|never|Never|NEVER|\
		1|true|True|TRUE|yes|Yes|YES|on|On|ON|always|Always|ALWAYS|\
		auto|Auto|AUTO|"")
			return 0
			;;
	esac

	printf 'Unsupported E2E_PUBLISH_GITHUB_STATUS value: %s\n' "${E2E_PUBLISH_GITHUB_STATUS}" >&2
	exit 1
}

github_status_publishing_available() {
	if publish_mode_is_disabled; then
		log "Skipping GitHub commit status publishing because E2E_PUBLISH_GITHUB_STATUS=${E2E_PUBLISH_GITHUB_STATUS}."
		return 1
	fi

	if ! command -v gh >/dev/null 2>&1; then
		if publish_mode_is_required; then
			printf 'Missing required command: gh\n' >&2
			exit 1
		fi
		log "Skipping GitHub commit status publishing because gh is unavailable."
		return 1
	fi

	if ! gh auth status >/dev/null 2>&1; then
		if publish_mode_is_required; then
			printf 'GitHub CLI is not authenticated; run gh auth login before publishing E2E statuses.\n' >&2
			exit 1
		fi
		log "Skipping GitHub commit status publishing because gh is not authenticated."
		return 1
	fi

	if [[ -n "$(git -C "${REPO_ROOT}" status --porcelain --untracked-files=all)" ]]; then
		if publish_mode_is_required; then
			printf 'Working tree has uncommitted changes; commit or stash them before publishing E2E statuses.\n' >&2
			exit 1
		fi
		log "Skipping GitHub commit status publishing because the working tree has uncommitted changes."
		return 1
	fi

	if [[ "${E2E_ALLOW_NON_HEAD_COMMIT_STATUS:-0}" != "1" && "$(git -C "${REPO_ROOT}" rev-parse HEAD)" != "${COMMIT_SHA}" ]]; then
		if publish_mode_is_required; then
			printf 'Refusing to publish E2E statuses for non-HEAD commit %s.\n' "${COMMIT_SHA}" >&2
			exit 1
		fi
		log "Skipping GitHub commit status publishing because ${COMMIT_SHA} is not the current HEAD."
		return 1
	fi

	if ! gh api "repos/{owner}/{repo}/commits/${COMMIT_SHA}" >/dev/null 2>&1; then
		if publish_mode_is_required; then
			printf 'Commit %s is not available through GitHub API for this repository.\n' "${COMMIT_SHA}" >&2
			exit 1
		fi
		log "Skipping GitHub commit status publishing because ${COMMIT_SHA} is not available on GitHub yet."
		return 1
	fi

	return 0
}

covered_status_contexts() {
	local include_covered="${1:-1}"
	local suite

	if [[ "${include_covered}" != "1" || "${SUITE_ID}" != "local-certification-suite" || "${E2E_PUBLISH_COVERED_SUITE_STATUSES:-1}" != "1" ]]; then
		printf '%s\n' "${STATUS_CONTEXT}"
		return 0
	fi

	for suite in \
		local-certification-suite \
		about-suite \
		auth-suite \
		enrollmentproof-suite \
		evaluations-suite \
		maincore-suite \
		pensum-suite \
		record-suite \
		subjects-suite \
		summary-suite \
		wizard-suite; do
		printf 'local-e2e/%s/%s\n' "${PLATFORM}" "${suite}"
	done
}

publish_github_statuses() {
	local state="$1"
	local description="$2"
	local include_covered="${3:-1}"
	local context

	while IFS= read -r context; do
		[[ -n "${context}" ]] || continue
		log "Publishing GitHub commit status ${context}=${state}."
		gh api \
			-X POST \
			"repos/{owner}/{repo}/statuses/${COMMIT_SHA}" \
			-f state="${state}" \
			-f context="${context}" \
			-f description="${description}" \
			>/dev/null
	done < <(covered_status_contexts "${include_covered}")
}

maestro_report_has_no_failures() {
	local report_file="$1"

	[[ -f "$report_file" ]] || return 1
	if grep -Eq '<(testsuite|testsuites)[^>]*(failures|errors)="[1-9][0-9]*"' "$report_file"; then
		return 1
	fi

	grep -Eq '<testcase[^>]*status="SUCCESS"|<(testsuite|testsuites)[^>]*failures="0"' "$report_file"
}

maestro_log_reports_passed_flow() {
	local log_file="$1"

	[[ -f "$log_file" ]] || return 1
	grep -q '\[Passed\]' "$log_file" && grep -q 'Flow Passed' "$log_file"
}

maestro_nonzero_after_passed_flow_is_recoverable() {
	local status="$1"
	local log_file="$2"
	local report_file="$3"

	[[ "$status" != "0" ]] || return 1
	[[ "${E2E_STRICT_MAESTRO_EXIT:-0}" != "1" ]] || return 1
	maestro_log_reports_passed_flow "$log_file" || return 1
	maestro_report_has_no_failures "$report_file"
}

suite_path_for_id() {
	local suite_id="$1"
	printf '%s/e2e/maestro/flows/suites/%s.yaml\n' "$REPO_ROOT" "$suite_id"
}

resolved_suite_ids() {
	bash "${SCRIPT_DIR}/resolve-e2e-scope.sh" "$PLATFORM" | awk -F, '{ print $2 }' | sort -u
}

run_suite_evidence() {
	local suite_path="$1"
	local SUITE_ID
	local E2E_FINGERPRINT
	local CERTIFICATION_DIR
	local FINGERPRINT_CERTIFICATION_DIR
	local STATUS_CONTEXT
	local MANIFEST_FILE
	local LOG_FILE
	local REPORT_FILE
	local TEST_OUTPUT_DIR
	local DEBUG_OUTPUT_DIR
	local status
	local started_at
	local finished_at
	local log_sha256
	local device_info
	local version_name
	local android_version_code
	local ios_build_number
	local publish_description

	SUITE_ID="$(basename "${suite_path}" .yaml)"
	E2E_FINGERPRINT="$("${SCRIPT_DIR}/e2e-fingerprint.sh" "${PLATFORM}" "${SUITE_ID}" "${COMMIT_SHA}")"
	CERTIFICATION_DIR="${E2E_CERTIFICATION_DIR:-${REPO_ROOT}/build/e2e/certifications/${COMMIT_SHA}/${PLATFORM}/${SUITE_ID}}"
	FINGERPRINT_CERTIFICATION_DIR="${REPO_ROOT}/build/e2e/certifications/by-fingerprint/${E2E_FINGERPRINT}/${PLATFORM}/${SUITE_ID}"
	STATUS_CONTEXT="local-e2e/${PLATFORM}/${SUITE_ID}"
	MANIFEST_FILE="${CERTIFICATION_DIR}/manifest.json"
	LOG_FILE="${CERTIFICATION_DIR}/maestro.log"
	REPORT_FILE="${CERTIFICATION_DIR}/junit.xml"
	TEST_OUTPUT_DIR="${CERTIFICATION_DIR}/maestro-output"
	DEBUG_OUTPUT_DIR="${CERTIFICATION_DIR}/maestro-debug"

	mkdir -p "${CERTIFICATION_DIR}" "${TEST_OUTPUT_DIR}" "${DEBUG_OUTPUT_DIR}"

	export E2E_MAESTRO_SUITE="${suite_path}"
	export E2E_REPORT_DIR="${CERTIFICATION_DIR}"
	export E2E_MAESTRO_LOG_FILE="${LOG_FILE}"
	export E2E_MAESTRO_FORMAT="${E2E_MAESTRO_FORMAT:-junit}"
	export E2E_MAESTRO_REPORT_FILE="${REPORT_FILE}"
	export E2E_MAESTRO_TEST_OUTPUT_DIR="${TEST_OUTPUT_DIR}"
	export E2E_MAESTRO_DEBUG_OUTPUT_DIR="${DEBUG_OUTPUT_DIR}"

	started_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
	set +e
	bash "${SCRIPT_DIR}/run-maestro-${PLATFORM}.sh"
	status=$?
	set -e
	finished_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

	if maestro_nonzero_after_passed_flow_is_recoverable "$status" "$LOG_FILE" "$REPORT_FILE"; then
		log "Maestro exited with ${status} after reporting a passing flow and writing a clean JUnit report; treating evidence as passed."
		status=0
	fi

	if [[ -f "${LOG_FILE}" ]]; then
		if command -v shasum >/dev/null 2>&1; then
			log_sha256="$(shasum -a 256 "${LOG_FILE}" | awk '{ print $1 }')"
		else
			log_sha256="$(sha256sum "${LOG_FILE}" | awk '{ print $1 }')"
		fi
	else
		log_sha256=""
	fi

	device_info="unknown"
	case "$PLATFORM" in
		android)
			if command -v adb >/dev/null 2>&1; then
				device_info="$(adb devices | sed '1d;/^$/d' | paste -sd ';' -)"
			fi
			;;
		ios)
			if command -v xcrun >/dev/null 2>&1; then
				device_info="$(xcrun simctl list devices booted | sed '/^$/d' | paste -sd ';' -)"
			fi
			;;
	esac

	version_name="$(awk -F= '$1 == "versionName" { print $2 }' "${REPO_ROOT}/gradle/app-version.properties")"
	android_version_code="$(awk -F= '$1 == "androidVersionCode" { print $2 }' "${REPO_ROOT}/gradle/app-version.properties")"
	ios_build_number="$(awk -F= '$1 == "iosBuildNumber" { print $2 }' "${REPO_ROOT}/gradle/app-version.properties")"

	jq -n \
		--arg commitSha "${COMMIT_SHA}" \
		--arg e2eFingerprint "${E2E_FINGERPRINT}" \
		--arg platform "${PLATFORM}" \
		--arg suite "${SUITE_ID}" \
		--arg suitePath "${suite_path}" \
		--arg statusContext "${STATUS_CONTEXT}" \
		--arg statusCode "${status}" \
		--arg startedAt "${started_at}" \
		--arg finishedAt "${finished_at}" \
		--arg logFile "${LOG_FILE}" \
		--arg logSha256 "${log_sha256}" \
		--arg junitReport "${REPORT_FILE}" \
		--arg testOutputDir "${TEST_OUTPUT_DIR}" \
		--arg debugOutputDir "${DEBUG_OUTPUT_DIR}" \
		--arg fingerprintCertificationDir "${FINGERPRINT_CERTIFICATION_DIR}" \
		--arg deviceInfo "${device_info}" \
		--arg versionName "${version_name}" \
		--arg androidVersionCode "${android_version_code}" \
		--arg iosBuildNumber "${ios_build_number}" \
		'{
			commitSha: $commitSha,
			e2eFingerprint: $e2eFingerprint,
			platform: $platform,
			suite: $suite,
			suitePath: $suitePath,
			statusContext: $statusContext,
			statusCode: ($statusCode | tonumber),
			startedAt: $startedAt,
			finishedAt: $finishedAt,
			logFile: $logFile,
			logSha256: $logSha256,
			junitReport: $junitReport,
			testOutputDir: $testOutputDir,
			debugOutputDir: $debugOutputDir,
			fingerprintCertificationDir: $fingerprintCertificationDir,
			deviceInfo: $deviceInfo,
			appVersion: {
				versionName: $versionName,
				androidVersionCode: ($androidVersionCode | tonumber),
				iosBuildNumber: ($iosBuildNumber | tonumber)
			}
		}' >"${MANIFEST_FILE}"

	if [[ "${CERTIFICATION_DIR}" != "${FINGERPRINT_CERTIFICATION_DIR}" ]]; then
		mkdir -p "${FINGERPRINT_CERTIFICATION_DIR}"
		cp -R "${CERTIFICATION_DIR}/." "${FINGERPRINT_CERTIFICATION_DIR}/"
	fi

	publish_description="Local E2E ${SUITE_ID} passed for ${COMMIT_SHA:0:7} fp ${E2E_FINGERPRINT:0:12}."

	if [[ "$status" == "0" ]]; then
		if github_status_publishing_available; then
			publish_github_statuses "success" "${publish_description}" 1
		fi
	else
		log "Skipping GitHub commit status publishing because E2E failed."
	fi

	log "E2E evidence manifest: ${MANIFEST_FILE}"
	return "$status"
}

validate_publish_mode

overall_status=0

if [[ -n "$REQUESTED_E2E_MAESTRO_SUITE" ]]; then
	run_suite_evidence "$REQUESTED_E2E_MAESTRO_SUITE" || overall_status="$?"
	exit "$overall_status"
fi

if ! resolved_scope="$(resolved_suite_ids)"; then
	exit 1
fi

suites=()
while IFS= read -r suite; do
	[[ -n "$suite" ]] || continue
	suites+=("$suite")
done <<<"$resolved_scope"
if [[ "${#suites[@]}" == "0" ]]; then
	log "No ${PLATFORM} E2E suites are required for this diff."
	exit 0
fi

if [[ "${#suites[@]}" -gt 1 && -n "${E2E_CERTIFICATION_DIR:-}" ]]; then
	printf 'E2E_CERTIFICATION_DIR cannot be shared by multi-suite smart evidence. Set E2E_MAESTRO_SUITE or unset E2E_CERTIFICATION_DIR.\n' >&2
	exit 1
fi

for suite in "${suites[@]}"; do
	suite_path="$(suite_path_for_id "$suite")"
	if [[ ! -f "$suite_path" ]]; then
		printf 'Resolved E2E suite does not exist: %s\n' "$suite_path" >&2
		exit 1
	fi

	log "Running smart ${PLATFORM} E2E evidence suite ${suite}."
	run_suite_evidence "$suite_path" || overall_status="$?"
done

exit "$overall_status"
