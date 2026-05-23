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
SUITE_ID="$(basename "${E2E_MAESTRO_SUITE}" .yaml)"
CERTIFICATION_DIR="${E2E_CERTIFICATION_DIR:-${REPO_ROOT}/build/e2e/certifications/${COMMIT_SHA}/${PLATFORM}/${SUITE_ID}}"
STATUS_CONTEXT="local-e2e/${PLATFORM}/${SUITE_ID}"
MANIFEST_FILE="${CERTIFICATION_DIR}/manifest.json"
LOG_FILE="${CERTIFICATION_DIR}/maestro.log"
REPORT_FILE="${CERTIFICATION_DIR}/junit.xml"
TEST_OUTPUT_DIR="${CERTIFICATION_DIR}/maestro-output"
DEBUG_OUTPUT_DIR="${CERTIFICATION_DIR}/maestro-debug"

mkdir -p "${CERTIFICATION_DIR}" "${TEST_OUTPUT_DIR}" "${DEBUG_OUTPUT_DIR}"

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
	--arg deviceInfo "${device_info}" \
	--arg versionName "${version_name}" \
	--arg androidVersionCode "${android_version_code}" \
	--arg iosBuildNumber "${ios_build_number}" \
	'{
		commitSha: $commitSha,
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
		deviceInfo: $deviceInfo,
		appVersion: {
			versionName: $versionName,
			androidVersionCode: ($androidVersionCode | tonumber),
			iosBuildNumber: ($iosBuildNumber | tonumber)
		}
	}' >"${MANIFEST_FILE}"

publish_state="success"
publish_description="Local E2E ${SUITE_ID} passed for ${COMMIT_SHA:0:7}."
if [[ "$status" != "0" ]]; then
	publish_state="failure"
	publish_description="Local E2E ${SUITE_ID} failed for ${COMMIT_SHA:0:7}."
fi

if [[ "${E2E_PUBLISH_GITHUB_STATUS:-0}" == "1" ]]; then
	require_command gh
	log "Publishing GitHub commit status ${STATUS_CONTEXT}=${publish_state}."
	gh api \
		-X POST \
		"repos/{owner}/{repo}/statuses/${COMMIT_SHA}" \
		-f state="${publish_state}" \
		-f context="${STATUS_CONTEXT}" \
		-f description="${publish_description}"
fi

log "E2E evidence manifest: ${MANIFEST_FILE}"
exit "$status"
