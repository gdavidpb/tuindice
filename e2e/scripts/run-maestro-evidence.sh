#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

PLATFORM="${1:-}"
if [[ "$PLATFORM" != "android" && "$PLATFORM" != "ios" ]]; then
	printf 'Usage: %s <android|ios>\n' "$0" >&2
	exit 1
fi

require_command git
require_command jq

COMMIT_SHA="${E2E_COMMIT_SHA:-$(git -C "${REPO_ROOT}" rev-parse HEAD)}"
COMMIT_SHA="$(git -C "${REPO_ROOT}" rev-parse "${COMMIT_SHA}^{commit}")"
SUITE_ID="$(basename "${E2E_MAESTRO_SUITE}" .yaml)"
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

validate_publish_mode

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
	--arg suitePath "${E2E_MAESTRO_SUITE}" \
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
exit "$status"
