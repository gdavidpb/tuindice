#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

PLATFORM="${1:-}"
if [[ "${PLATFORM}" != "android" && "${PLATFORM}" != "ios" ]]; then
	printf 'Usage: %s <android|ios>\n' "$0" >&2
	exit 1
fi

require_command jq
require_command maestro
require_command git

if [[ "${PLATFORM}" == "android" ]]; then
	require_command adb
else
	if ! is_macos; then
		printf 'iOS Maestro profiling requires macOS.\n' >&2
		exit 1
	fi
	require_command xcrun
fi

COMMIT_SHA="$(git -C "${REPO_ROOT}" rev-parse HEAD)"
PROFILE_OUTPUT_DIR="${E2E_PROFILE_OUTPUT_DIR:-${REPO_ROOT}/build/e2e/profiles}/${COMMIT_SHA}/${PLATFORM}"
PROFILE_LOG_DIR="${PROFILE_OUTPUT_DIR}/logs"
mkdir -p "${PROFILE_OUTPUT_DIR}" "${PROFILE_LOG_DIR}"

"${SCRIPT_DIR}/start-wiremock.sh"

case "${PLATFORM}" in
	android)
		capture_streamed_last_line \
			APP_PATH \
			"${E2E_TMP_DIR}/profile-android-build-output.log" \
			"${SCRIPT_DIR}/build-android-debug.sh"
		adb install -r "${APP_PATH}" >/dev/null
		"${SCRIPT_DIR}/reset-android-app.sh"
		;;
	ios)
		capture_streamed_last_line \
			APP_PATH \
			"${E2E_TMP_DIR}/profile-ios-build-output.log" \
			"${SCRIPT_DIR}/build-ios-debug.sh"
		"${SCRIPT_DIR}/reset-ios-app.sh"
		xcrun simctl install "${E2E_IOS_DEVICE_ID}" "${APP_PATH}"
		;;
esac

profile_suite_source="${E2E_MAESTRO_SUITE}"
prepared_suite="$("${SCRIPT_DIR}/prepare-maestro-suite.sh" "${profile_suite_source}")"
prepared_root="$(dirname "$(dirname "${prepared_suite}")")"
prepared_suite_dir="$(dirname "${prepared_suite}")"

if [[ ! -f "${prepared_suite}" ]]; then
	printf 'Prepared Maestro suite not found: %s\n' "${prepared_suite}" >&2
	exit 1
fi

resolve_run_flow_target() {
	local target="$1"
	local base_dir="$2"

	case "${target}" in
		/*)
			printf '%s\n' "${target}"
			;;
		*)
			printf '%s/%s\n' "$(cd "${base_dir}" && pwd -P)" "${target}"
			;;
	esac
}

current_millis() {
	perl -MTime::HiRes=time -e 'printf "%.0f\n", time() * 1000'
}

target_needs_seed() {
	local target="$1"
	local target_file

	target_file="$(resolve_run_flow_target "${target}" "${prepared_suite_dir}")"
	if [[ ! -f "${target_file}" ]]; then
		return 0
	fi

	if awk '
		/^---$/ { inCommands = 1; next }
		inCommands && commandCount < 6 {
			if ($0 == "- runFlow: ../_shared/launch-clean.yaml" ||
				$0 == "- runFlow: ../_shared/launch-seeded-authenticated.yaml") {
				found = 1
			}
			if ($0 ~ /^- /) {
				commandCount++
			}
		}
		END { exit found ? 0 : 1 }
	' "${target_file}"; then
		return 1
	fi

	return 0
}

run_profile_item() {
	local index="$1"
	local target="$2"
	local safe_name
	local suite_file
	local log_file
	local report_file
	local started_ms
	local finished_ms
	local duration_ms
	local status

	safe_name="$(printf '%s' "${target}" | tr '/.' '__' | tr -cd 'A-Za-z0-9_-')"
	suite_file="${prepared_suite_dir}/__profile_${index}_${safe_name}.yaml"
	log_file="${PROFILE_LOG_DIR}/${index}-${safe_name}.log"
	report_file="${PROFILE_LOG_DIR}/${index}-${safe_name}.xml"

	{
		printf 'appId: %s\n' "${E2E_APP_ID}"
		printf 'name: TuIndice Maestro profile %s\n' "${target}"
		printf -- '---\n'
		if target_needs_seed "${target}"; then
			printf -- '- runFlow: ../_shared/launch-seeded-authenticated.yaml\n'
		fi
		printf -- '- runFlow: %s\n' "${target}"
	} >"${suite_file}"

	reset_wiremock

	started_ms="$(current_millis)"
	set +e
	if [[ "${PLATFORM}" == "ios" ]]; then
		local maestro_ios_device_id="${E2E_IOS_DEVICE_ID}"
		if [[ "${maestro_ios_device_id}" == "booted" ]]; then
			maestro_ios_device_id="$(xcrun simctl list devices booted | awk -F'[()]' '/Booted/ { print $2; exit }')"
		fi
		maestro --device "${maestro_ios_device_id}" test \
			--format junit \
			--output "${report_file}" \
			"${suite_file}" >"${log_file}" 2>&1
	else
		maestro test \
			--format junit \
			--output "${report_file}" \
			"${suite_file}" >"${log_file}" 2>&1
	fi
	status=$?
	set -e
	finished_ms="$(current_millis)"
	duration_ms="$((finished_ms - started_ms))"

	jq -n -c \
		--arg platform "${PLATFORM}" \
		--arg target "${target}" \
		--arg suiteFile "${suite_file}" \
		--arg logFile "${log_file}" \
		--arg reportFile "${report_file}" \
		--argjson index "${index}" \
		--argjson durationMs "${duration_ms}" \
		--argjson status "${status}" \
		'{
			index: $index,
			platform: $platform,
			target: $target,
			durationMs: $durationMs,
			statusCode: $status,
			suiteFile: $suiteFile,
			logFile: $logFile,
			junitReport: $reportFile
		}' >>"${PROFILE_OUTPUT_DIR}/profile.jsonl"

	printf '%s,%s,%s,%s,%s\n' \
		"${index}" \
		"${PLATFORM}" \
		"${duration_ms}" \
		"${status}" \
		"${target}" >>"${PROFILE_OUTPUT_DIR}/profile.csv"

	return "${status}"
}

: >"${PROFILE_OUTPUT_DIR}/profile.jsonl"
printf 'index,platform,duration_ms,status_code,target\n' >"${PROFILE_OUTPUT_DIR}/profile.csv"

profile_targets=()
while IFS= read -r profile_target; do
	[[ -n "${profile_target}" ]] || continue
	profile_targets+=("${profile_target}")
done < <(
	awk '
		/^---$/ { inCommands = 1; next }
		inCommands && /^[[:space:]]*-[[:space:]]*runFlow:[[:space:]]*/ {
			value = $0
			sub(/^[[:space:]]*-[[:space:]]*runFlow:[[:space:]]*/, "", value)
			if (value != "") {
				print value
			}
		}
	' "${prepared_suite}"
)

if [[ "${#profile_targets[@]}" == "0" ]]; then
	printf 'No top-level runFlow entries found in %s\n' "${prepared_suite}" >&2
	exit 1
fi

overall_status=0
for index in "${!profile_targets[@]}"; do
	target="${profile_targets[$index]}"
	log "Profiling ${PLATFORM} Maestro target ${target}."
	run_profile_item "$((index + 1))" "${target}" || overall_status="$?"
done

jq -s \
	--arg platform "${PLATFORM}" \
	--arg commitSha "${COMMIT_SHA}" \
	--arg sourceSuite "${profile_suite_source}" \
	'{
		platform: $platform,
		commitSha: $commitSha,
		sourceSuite: $sourceSuite,
		targets: .,
		totalDurationMs: (map(.durationMs) | add // 0),
		failedTargets: map(select(.statusCode != 0))
	}' "${PROFILE_OUTPUT_DIR}/profile.jsonl" >"${PROFILE_OUTPUT_DIR}/summary.json"

log "Maestro profile written to ${PROFILE_OUTPUT_DIR}."
exit "${overall_status}"
