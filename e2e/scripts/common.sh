#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
E2E_TMP_DIR="${E2E_TMP_DIR:-/tmp/tuindice-e2e}"
E2E_REPORT_DIR="${E2E_REPORT_DIR:-${REPO_ROOT}/build/e2e}"
E2E_APP_ID="${E2E_APP_ID:-com.gdavidpb.tuindice.debug}"
E2E_IOS_BUNDLE_ID="${E2E_IOS_BUNDLE_ID:-com.gdavidpb.tuindice.debug}"
E2E_WIREMOCK_PORT="${E2E_WIREMOCK_PORT:-8080}"
E2E_WIREMOCK_URL="${E2E_WIREMOCK_URL:-http://127.0.0.1:${E2E_WIREMOCK_PORT}}"
E2E_ANDROID_API_BASE_URL="${E2E_ANDROID_API_BASE_URL:-http://127.0.0.1:${E2E_WIREMOCK_PORT}/}"
E2E_ANDROID_WEB_BASE_URL="${E2E_ANDROID_WEB_BASE_URL:-http://127.0.0.1:${E2E_WIREMOCK_PORT}}"
E2E_IOS_API_BASE_URL="${E2E_IOS_API_BASE_URL:-http://localhost:${E2E_WIREMOCK_PORT}/}"
E2E_IOS_WEB_BASE_URL="${E2E_IOS_WEB_BASE_URL:-http://localhost:${E2E_WIREMOCK_PORT}}"
E2E_IOS_DERIVED_DATA="${E2E_IOS_DERIVED_DATA:-${E2E_TMP_DIR}/ios-derived-data}"
E2E_IOS_DEVICE_ID="${E2E_IOS_DEVICE_ID:-booted}"
E2E_MAESTRO_SUITE="${E2E_MAESTRO_SUITE:-${REPO_ROOT}/e2e/maestro/flows/suites/local-certification-suite.yaml}"
E2E_DISABLE_KEYBOARD_HELPERS="${E2E_DISABLE_KEYBOARD_HELPERS:-1}"
E2E_WIREMOCK_DELAY_PROFILE="${E2E_WIREMOCK_DELAY_PROFILE:-fast}"
E2E_MAESTRO_OPTIMIZE_SETUP="${E2E_MAESTRO_OPTIMIZE_SETUP:-1}"
E2E_MAESTRO_RESUME_FIRST="${E2E_MAESTRO_RESUME_FIRST:-1}"
E2E_MAESTRO_CHECKPOINT_DIR="${E2E_MAESTRO_CHECKPOINT_DIR:-${REPO_ROOT}/build/e2e/checkpoints}"
E2E_MAESTRO_SUITE_RETRIES="${E2E_MAESTRO_SUITE_RETRIES:-1}"
E2E_MAESTRO_QUARANTINE_FILE="${E2E_MAESTRO_QUARANTINE_FILE:-${REPO_ROOT}/testkit/e2e/quarantine.txt}"
E2E_MAESTRO_SUCCESS_REPORT_GRACE_SECONDS="${E2E_MAESTRO_SUCCESS_REPORT_GRACE_SECONDS:-30}"

log() {
	printf '[tuindice-e2e] %s\n' "$*"
}

require_command() {
	local command_name="$1"

	if ! command -v "${command_name}" >/dev/null 2>&1; then
		printf 'Missing required command: %s\n' "${command_name}" >&2
		exit 1
	fi
}

child_processes() {
	local parent_pid="$1"

	if command -v pgrep >/dev/null 2>&1; then
		pgrep -P "${parent_pid}" 2>/dev/null || true
	else
		ps -eo pid=,ppid= 2>/dev/null | awk -v parent_pid="${parent_pid}" '$2 == parent_pid { print $1 }' || true
	fi
}

terminate_process_tree() {
	local pid="$1"
	local child_pid

	for child_pid in $(child_processes "${pid}"); do
		terminate_process_tree "${child_pid}"
	done
	kill "${pid}" >/dev/null 2>&1 || true
}

force_terminate_process_tree() {
	local pid="$1"
	local child_pid

	for child_pid in $(child_processes "${pid}"); do
		force_terminate_process_tree "${child_pid}"
	done
	kill -9 "${pid}" >/dev/null 2>&1 || true
}

capture_streamed_last_line() {
	local result_variable="$1"
	local output_file="$2"
	shift 2

	mkdir -p "$(dirname "${output_file}")"
	set +e
	"$@" 2>&1 | tee "${output_file}"
	local command_status="${PIPESTATUS[0]}"
	set -e
	if [[ "${command_status}" != "0" ]]; then
		return "${command_status}"
	fi

	printf -v "${result_variable}" '%s' "$(tail -n 1 "${output_file}")"
}

elapsed_label() {
	local elapsed_seconds="$1"
	printf '%dm %02ds' "$((elapsed_seconds / 60))" "$((elapsed_seconds % 60))"
}

maestro_progress_percent() {
	local current="$1"
	local total="$2"

	if [[ ! "${current}" =~ ^[0-9]+$ || ! "${total}" =~ ^[0-9]+$ || "${total}" == "0" ]]; then
		printf '0'
		return 0
	fi

	printf '%s' "$((current * 100 / total))"
}

display_path() {
	local path="$1"

	if [[ -z "${path}" ]]; then
		printf '-'
		return 0
	fi

	case "${path}" in
		"${REPO_ROOT}/"*)
			printf '%s' "${path#"${REPO_ROOT}/"}"
			;;
		"${E2E_TMP_DIR}/"*)
			printf '%s' "${path#"${E2E_TMP_DIR}/"}"
			;;
		*)
			printf '%s' "${path}"
			;;
	esac
}

maestro_direct_flow_entries() {
	local suite_path="$1"

	[[ -f "${suite_path}" ]] || return 0
	awk '
		/^[[:space:]]*-[[:space:]]*runFlow:[[:space:]]*[^[:space:]]/ {
			sub(/^[[:space:]]*-[[:space:]]*runFlow:[[:space:]]*/, "")
			gsub(/^["'\''[:space:]]+|["'\''[:space:]]+$/, "")
			print
		}
	' "${suite_path}"
}

maestro_flow_label() {
	local flow="$1"
	flow="${flow##*/}"
	flow="${flow%.yaml}"
	printf '%s' "${flow}"
}

maestro_direct_flow_plan() {
	local suite_path="$1"
	local suite_name
	local preview_limit="${E2E_MAESTRO_FLOW_PREVIEW_LIMIT:-6}"
	local flow
	local flow_count=0
	local preview_count=0
	local preview=""

	[[ -f "${suite_path}" ]] || return 0
	suite_name="$(basename "${suite_path}")"
	if [[ ! "${preview_limit}" =~ ^[0-9]+$ ]]; then
		preview_limit=6
	fi

	while IFS= read -r flow; do
		[[ -n "${flow}" ]] || continue
		flow_count=$((flow_count + 1))
		if [[ "${preview_count}" -lt "${preview_limit}" ]]; then
			if [[ -n "${preview}" ]]; then
				preview="${preview}, "
			fi
			preview="${preview}$(maestro_flow_label "${flow}")"
			preview_count=$((preview_count + 1))
		fi
	done < <(maestro_direct_flow_entries "${suite_path}")

	if [[ "${flow_count}" == "0" ]]; then
		log "Maestro suite: ${suite_name}; inline commands only."
	else
		log "Maestro suite: ${suite_name}; top-level runFlow entries=${flow_count}."
		if [[ "${preview_count}" -gt "0" ]]; then
			if [[ "${flow_count}" -gt "${preview_count}" ]]; then
				preview="${preview}, +$((flow_count - preview_count)) more"
			fi
			log "Maestro flow preview: ${preview}."
		else
			log "Maestro flow preview disabled; set E2E_MAESTRO_FLOW_PREVIEW_LIMIT to show entries."
		fi
	fi

	if [[ "${E2E_MAESTRO_PRINT_FLOW_PLAN:-0}" == "1" && "${flow_count}" != "0" ]]; then
		local index=0
		while IFS= read -r flow; do
			[[ -n "${flow}" ]] || continue
			index=$((index + 1))
			log "  ${index}/${flow_count} ${flow}"
		done < <(maestro_direct_flow_entries "${suite_path}")
	fi
}

log_maestro_start() {
	local platform="$1"
	local suite_path="$2"
	local log_file="$3"
	local report_file="${E2E_MAESTRO_REPORT_FILE:-}"
	local test_output_dir="$4"
	local debug_output_dir="$5"

	log "${platform} Maestro start: suite=$(basename "${suite_path}")."
	log "${platform} Maestro outputs: log=$(display_path "${log_file}"), report=$(display_path "${report_file}"), artifacts=$(display_path "${test_output_dir}"), debug=$(display_path "${debug_output_dir}")."
}

log_maestro_finish() {
	local platform="$1"
	local status="$2"
	local started_at="$3"
	local elapsed

	elapsed="$(elapsed_label "$(($(date +%s) - started_at))")"
	if [[ "${status}" == "0" ]]; then
		log "${platform} Maestro finished: passed in ${elapsed}."
	else
		log "${platform} Maestro finished: failed with exit ${status} after ${elapsed}."
	fi
}

run_maestro_with_progress() {
	local platform="$1"
	local log_file="$2"
	local suite_path="$3"
	local test_output_dir="$4"
	local debug_output_dir="$5"
	shift 5
	local started_at
	local status=0

	maestro_direct_flow_plan "${suite_path}"
	log_maestro_start "${platform}" "${suite_path}" "${log_file}" "${test_output_dir}" "${debug_output_dir}"
	started_at="$(date +%s)"
	if (
		set -o pipefail
		"$@" 2>&1 | tee "${log_file}"
	); then
		status=0
	else
		status="$?"
	fi
	log_maestro_finish "${platform}" "${status}" "${started_at}"
	return "${status}"
}

quarantine_active_entries() {
	[[ -f "${E2E_MAESTRO_QUARANTINE_FILE}" ]] || return 0
	sed -e 's/#.*$//' -e 's/[[:space:]]*$//' -e '/^[[:space:]]*$/d' "${E2E_MAESTRO_QUARANTINE_FILE}"
}

# Suite retries compose with resume-first checkpoints: a failed run leaves the failed
# flow as the resume target, so each retry re-enters the suite exactly there. This is
# per-flow retry built from two existing mechanisms, not a new execution mode.
run_maestro_suite_with_retries() {
	local retries_left="${E2E_MAESTRO_SUITE_RETRIES}"
	local attempt=1

	if [[ ! "${retries_left}" =~ ^[0-9]+$ ]]; then
		retries_left=1
	fi

	while :; do
		local run_status=0
		run_maestro_suite_resume_first "$@" || run_status="$?"
		if [[ "${run_status}" == "0" ]]; then
			return 0
		fi
		if [[ "${retries_left}" -le 0 ]]; then
			return "${run_status}"
		fi
		retries_left=$((retries_left - 1))
		attempt=$((attempt + 1))
		log "Maestro suite failed (exit ${run_status}); retry attempt ${attempt} resumes from the failed flow. Retries left after this one: ${retries_left}."
		if declare -F reset_wiremock >/dev/null 2>&1; then
			reset_wiremock || true
		fi
	done
}

maestro_slug() {
	local value="$1"
	printf '%s' "${value}" | tr '/[:space:]' '__' | tr -cd 'A-Za-z0-9_.-'
}

maestro_checkpoint_dir() {
	local platform="$1"
	local suite_path="$2"
	local suite_id

	suite_id="$(basename "${suite_path}" .yaml)"
	printf '%s/%s/%s\n' \
		"${E2E_MAESTRO_CHECKPOINT_DIR}" \
		"$(maestro_slug "${platform}")" \
		"$(maestro_slug "${suite_id}")"
}

maestro_checkpoint_file() {
	local platform="$1"
	local suite_path="$2"
	printf '%s/resume-target.txt\n' "$(maestro_checkpoint_dir "${platform}" "${suite_path}")"
}

maestro_legacy_checkpoint_file() {
	local platform="$1"
	local suite_path="$2"
	printf '%s/last-failure.txt\n' "$(maestro_checkpoint_dir "${platform}" "${suite_path}")"
}

maestro_checkpoint_metadata_file() {
	local platform="$1"
	local suite_path="$2"
	printf '%s/metadata.env\n' "$(maestro_checkpoint_dir "${platform}" "${suite_path}")"
}

maestro_checkpoint_targets_file() {
	local platform="$1"
	local suite_path="$2"
	printf '%s/targets.txt\n' "$(maestro_checkpoint_dir "${platform}" "${suite_path}")"
}

write_maestro_checkpoint() {
	local platform="$1"
	local suite_path="$2"
	local target="$3"
	local original_index="$4"
	local total_count="$5"
	local log_file="$6"
	local reason="${7:-failed}"
	local checkpoint_dir
	local checkpoint_file
	local metadata_file
	local legacy_checkpoint_file
	local recorded_at

	checkpoint_dir="$(maestro_checkpoint_dir "${platform}" "${suite_path}")"
	checkpoint_file="$(maestro_checkpoint_file "${platform}" "${suite_path}")"
	legacy_checkpoint_file="$(maestro_legacy_checkpoint_file "${platform}" "${suite_path}")"
	metadata_file="$(maestro_checkpoint_metadata_file "${platform}" "${suite_path}")"
	recorded_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
	mkdir -p "${checkpoint_dir}"
	printf '%s\n' "${target}" >"${checkpoint_file}"
	rm -f "${legacy_checkpoint_file}" 2>/dev/null || true
	{
		printf 'recorded_at=%s\n' "${recorded_at}"
		printf 'reason=%s\n' "${reason}"
		if [[ "${reason}" == "failed" ]]; then
			printf 'failed_at=%s\n' "${recorded_at}"
		fi
		printf 'platform=%s\n' "${platform}"
		printf 'suite=%s\n' "$(basename "${suite_path}")"
		printf 'target=%s\n' "${target}"
		printf 'original_index=%s\n' "${original_index}"
		printf 'total_count=%s\n' "${total_count}"
		printf 'log_file=%s\n' "${log_file}"
	} >"${metadata_file}"
}

maestro_checkpoint_reason() {
	local platform="$1"
	local suite_path="$2"
	local metadata_file
	local reason=""

	metadata_file="$(maestro_checkpoint_metadata_file "${platform}" "${suite_path}")"
	if [[ -f "${metadata_file}" ]]; then
		reason="$(awk -F= '$1 == "reason" { print $2; exit }' "${metadata_file}")"
	fi
	printf '%s\n' "${reason:-failed}"
}

clear_maestro_checkpoint() {
	local platform="$1"
	local suite_path="$2"
	local checkpoint_dir

	checkpoint_dir="$(maestro_checkpoint_dir "${platform}" "${suite_path}")"
	if [[ -d "${checkpoint_dir}" ]]; then
		rm -rf "${checkpoint_dir}"
	fi
}

maestro_resolve_flow_target() {
	local target="$1"
	local base_dir="$2"
	local candidate
	local candidate_dir

	case "${target}" in
		/*)
			candidate="${target}"
			;;
		*)
			candidate="$(cd "${base_dir}" && pwd -P)/${target}"
			;;
	esac

	candidate_dir="$(dirname "${candidate}")"
	if [[ -d "${candidate_dir}" ]]; then
		printf '%s/%s\n' "$(cd "${candidate_dir}" && pwd -P)" "$(basename "${candidate}")"
	else
		printf '%s\n' "${candidate}"
	fi
}

maestro_flows_root_for_suite() {
	local suite_path="$1"
	local flows_root="${REPO_ROOT}/e2e/maestro/flows"
	local prepared_prefix="${E2E_TMP_DIR}/maestro-prepared/"
	local relative
	local prepared_suite

	case "${suite_path}" in
		"${flows_root}/"*)
			printf '%s\n' "${flows_root}"
			return 0
			;;
		"${prepared_prefix}"*)
			relative="${suite_path#"${prepared_prefix}"}"
			prepared_suite="${relative%%/*}"
			printf '%s%s\n' "${prepared_prefix}" "${prepared_suite}"
			return 0
			;;
	esac

	printf '%s\n' "${flows_root}"
}

maestro_is_setup_target() {
	local target_file="$1"

	case "${target_file}" in
		*/_shared/launch-clean.yaml|*/_shared/launch-seeded-authenticated.yaml|*/_shared/launch-seeded-authenticated-wizard-pending.yaml)
			return 0
			;;
	esac
	return 1
}

maestro_flow_has_bootstrap() {
	local target_file="$1"

	[[ -f "${target_file}" ]] || return 1
	awk '
		/^---$/ { inCommands = 1; next }
		inCommands && commandCount < 8 {
			if ($0 == "- runFlow: ../_shared/launch-clean.yaml" ||
				$0 == "- runFlow: ../_shared/launch-seeded-authenticated.yaml" ||
				$0 == "- runFlow: ../_shared/launch-seeded-authenticated-wizard-pending.yaml" ||
				$0 ~ /^[[:space:]]*-[[:space:]]*launchApp:/) {
				found = 1
			}
			if ($0 ~ /^- /) {
				commandCount++
			}
		}
		END { exit found ? 0 : 1 }
	' "${target_file}"
}

maestro_item_prelude_entries() {
	local target_file="$1"
	local flows_root="$2"

	if maestro_flow_has_bootstrap "${target_file}"; then
		return 0
	fi

	case "${target_file}" in
		*/auth/login-success.yaml)
			printf '%s/_shared/launch-clean.yaml\n' "${flows_root}"
			return 0
			;;
		*/wizard/*.yaml)
			printf '%s/_shared/launch-seeded-authenticated-wizard-pending.yaml\n' "${flows_root}"
			return 0
			;;
	esac

	printf '%s/_shared/launch-seeded-authenticated.yaml\n' "${flows_root}"
}

write_maestro_item_suite() {
	local source_suite="$1"
	local target="$2"
	local item_suite="$3"
	local source_suite_dir
	local target_file
	local flows_root
	local prelude

	source_suite_dir="$(dirname "${source_suite}")"
	target_file="$(maestro_resolve_flow_target "${target}" "${source_suite_dir}")"
	flows_root="$(maestro_flows_root_for_suite "${source_suite}")"
	mkdir -p "$(dirname "${item_suite}")"
	{
		awk '
			/^---$/ { exit }
			!/^name:[[:space:]]/ { print }
		' "${source_suite}"
		printf 'name: TuIndice Maestro item %s\n' "$(maestro_flow_label "${target}")"
		printf -- '---\n'
		while IFS= read -r prelude; do
			[[ -n "${prelude}" ]] || continue
			printf -- '- runFlow: %s\n' "${prelude}"
		done < <(maestro_item_prelude_entries "${target_file}" "${flows_root}")
		printf -- '- runFlow: %s\n' "${target_file}"
	} >"${item_suite}"
}

maestro_case_targets() {
	local suite_path="$1"
	local suite_dir
	local target
	local target_file

	suite_dir="$(dirname "${suite_path}")"
	while IFS= read -r target; do
		[[ -n "${target}" ]] || continue
		target_file="$(maestro_resolve_flow_target "${target}" "${suite_dir}")"
		if maestro_is_setup_target "${target_file}"; then
			continue
		fi
		printf '%s\n' "${target}"
	done < <(maestro_direct_flow_entries "${suite_path}")
}

maestro_last_useful_line() {
	local log_file="$1"

	[[ -f "${log_file}" ]] || return 0
	tail -n 80 "${log_file}" 2>/dev/null |
		tr -d '\r' |
		awk '
			length($0) == 0 { next }
			$0 == "Waiting for flows to complete..." { next }
			$0 ~ /^[[:space:]]*at[[:space:]]/ { next }
			$0 ~ /^[[:space:]]*\.\.\.[[:space:]][0-9]+[[:space:]]+more$/ { next }
			{ line = $0 }
			END { if (line != "") print substr(line, 1, 220) }
		'
}

maestro_report_has_no_failures() {
	local report_file="$1"

	[[ -f "${report_file}" ]] || return 1
	if grep -Eq '<(testsuite|testsuites)[^>]*(failures|errors)="[1-9][0-9]*"' "${report_file}"; then
		return 1
	fi

	grep -Eq '<testcase[^>]*status="SUCCESS"|<(testsuite|testsuites)[^>]*failures="0"' "${report_file}"
}

maestro_log_reports_passed_flow() {
	local log_file="$1"

	[[ -f "${log_file}" ]] || return 1
	grep -q '\[Passed\]' "${log_file}" && grep -q 'Flow Passed' "${log_file}"
}

maestro_nonzero_after_passed_flow_is_recoverable() {
	local status="$1"
	local log_file="$2"
	local report_file="$3"

	[[ "${status}" != "0" ]] || return 1
	[[ "${E2E_STRICT_MAESTRO_EXIT:-0}" != "1" ]] || return 1
	maestro_log_reports_passed_flow "${log_file}" || return 1
	maestro_report_has_no_failures "${report_file}"
}

xml_escape() {
	local value="$1"
	value="${value//&/&amp;}"
	value="${value//</&lt;}"
	value="${value//>/&gt;}"
	value="${value//\"/&quot;}"
	value="${value//\'/&apos;}"
	printf '%s' "${value}"
}

write_maestro_aggregate_junit() {
	local report_file="$1"
	local suite_name="$2"
	local results_file="$3"
	local tests=0
	local errors=0
	local total_duration=0
	local status
	local duration
	local target
	local item_log
	local escaped_suite
	local escaped_target
	local escaped_log

	[[ -n "${report_file}" ]] || return 0
	[[ -f "${results_file}" ]] || return 0
	mkdir -p "$(dirname "${report_file}")"
	while IFS=$'\t' read -r status duration target item_log; do
		[[ -n "${status}" ]] || continue
		tests=$((tests + 1))
		total_duration=$((total_duration + duration))
		if [[ "${status}" != "0" ]]; then
			errors=$((errors + 1))
		fi
	done <"${results_file}"

	escaped_suite="$(xml_escape "${suite_name}")"
	{
		printf '<?xml version="1.0" encoding="UTF-8"?>\n'
		printf '<testsuite name="%s" tests="%s" failures="0" errors="%s" skipped="0" time="%s">\n' \
			"${escaped_suite}" \
			"${tests}" \
			"${errors}" \
			"${total_duration}"
		while IFS=$'\t' read -r status duration target item_log; do
			[[ -n "${status}" ]] || continue
			escaped_target="$(xml_escape "$(maestro_flow_label "${target}")")"
			escaped_log="$(xml_escape "$(display_path "${item_log}")")"
			if [[ "${status}" == "0" ]]; then
				printf '  <testcase classname="%s" name="%s" time="%s" status="SUCCESS"/>\n' \
					"${escaped_suite}" \
					"${escaped_target}" \
					"${duration}"
			else
				printf '  <testcase classname="%s" name="%s" time="%s" status="ERROR">\n' \
					"${escaped_suite}" \
					"${escaped_target}" \
					"${duration}"
				printf '    <error message="Maestro exit %s">See %s</error>\n' "${status}" "${escaped_log}"
				printf '  </testcase>\n'
			fi
		done <"${results_file}"
		printf '</testsuite>\n'
	} >"${report_file}"
}

run_maestro_item_compact() {
	local platform="$1"
	local label="$2"
	local execution_index="$3"
	local original_index="$4"
	local total_count="$5"
	local item_log="$6"
	local item_report="$7"
	local test_output_dir="$8"
	local debug_output_dir="$9"
	shift 9
	local status=0
	local command_pid
	local success_report_seen_at=""
	local now
	local grace_seconds="${E2E_MAESTRO_SUCCESS_REPORT_GRACE_SECONDS}"
	local restore_errexit=0

	mkdir -p "$(dirname "${item_log}")"
	: >"${item_log}"
	if [[ "${E2E_MAESTRO_RAW_OUTPUT:-0}" == "1" ]]; then
		"$@" > >(tee -a "${item_log}") 2>&1 &
	else
		"$@" >>"${item_log}" 2>&1 &
	fi
	command_pid="$!"

	while kill -0 "${command_pid}" >/dev/null 2>&1; do
		if [[ "${E2E_STRICT_MAESTRO_EXIT:-0}" != "1" &&
			"${grace_seconds}" =~ ^[0-9]+$ &&
			"${grace_seconds}" -gt 0 &&
			-n "${item_report}" ]] &&
			maestro_report_has_no_failures "${item_report}"; then
			now="$(date +%s)"
			if [[ -z "${success_report_seen_at}" ]]; then
				success_report_seen_at="${now}"
			elif (( now - success_report_seen_at >= grace_seconds )); then
				log "${platform} Maestro item ${label} wrote a clean JUnit report but did not exit after ${grace_seconds}s; terminating stuck Maestro CLI and treating the item as PASS."
				printf 'Clean JUnit report detected; terminated stuck Maestro CLI after %ss.\n' "${grace_seconds}" >>"${item_log}"
				terminate_process_tree "${command_pid}"
				if ! wait_for_process_exit "${command_pid}" 5; then
					force_terminate_process_tree "${command_pid}"
					wait_for_process_exit "${command_pid}" 5 >/dev/null 2>&1 || true
				fi
				wait "${command_pid}" >/dev/null 2>&1 || true
				return 0
			fi
		else
			success_report_seen_at=""
		fi
		sleep 1
	done

	case "$-" in
		*e*) restore_errexit=1 ;;
	esac
	set +e
	wait "${command_pid}"
	status="$?"
	if [[ "${restore_errexit}" == "1" ]]; then
		set -e
	fi
	return "${status}"
}

run_maestro_suite_resume_first() {
	local platform="$1"
	local log_file="$2"
	local suite_path="$3"
	local test_output_dir="$4"
	local debug_output_dir="$5"
	local report_file="$6"
	local maestro_home="$7"
	local maestro_device_id="${8:-}"
	local suite_name
	local checkpoint_file
	local legacy_checkpoint_file
	local checkpoint_read_file=""
	local checkpoint_targets_file
	local checkpoint_target=""
	local checkpoint_reason=""
	local start_index=0
	local total_count
	local target
	local found_checkpoint=0
	local item_root
	local results_file
	local item_suite
	local item_log
	local item_report
	local item_test_output_dir
	local item_debug_output_dir
	local execution_index
	local original_zero_index
	local original_index
	local started_at
	local elapsed
	local status=0
	local latest_line
	local target_label
	local target_file
	local progress_percent
	local command_status
	local suite_dir
	local fallback_command
	local maestro_command
	local targets=()

	while IFS= read -r target; do
		[[ -n "${target}" ]] || continue
		targets+=("${target}")
	done < <(maestro_case_targets "${suite_path}")

	total_count="${#targets[@]}"
	if [[ "${total_count}" == "0" ]]; then
		fallback_command=(env "HOME=${maestro_home}" maestro)
		if [[ -n "${maestro_device_id}" ]]; then
			fallback_command+=(--device "${maestro_device_id}")
		fi
		fallback_command+=(test)
		if [[ -n "${E2E_MAESTRO_FORMAT:-}" ]]; then
			fallback_command+=(--format "${E2E_MAESTRO_FORMAT}")
		fi
		if [[ -n "${report_file}" ]]; then
			fallback_command+=(--output "${report_file}")
		fi
		if [[ -n "${test_output_dir}" ]]; then
			fallback_command+=(--test-output-dir "${test_output_dir}")
		fi
		if [[ -n "${debug_output_dir}" ]]; then
			fallback_command+=(--debug-output "${debug_output_dir}")
		fi
		fallback_command+=("${suite_path}")
		run_maestro_with_progress \
			"${platform}" \
			"${log_file}" \
			"${suite_path}" \
			"${test_output_dir}" \
			"${debug_output_dir}" \
			"${fallback_command[@]}"
		return "$?"
	fi

	suite_name="$(basename "${suite_path}" .yaml)"
	checkpoint_file="$(maestro_checkpoint_file "${platform}" "${suite_path}")"
	legacy_checkpoint_file="$(maestro_legacy_checkpoint_file "${platform}" "${suite_path}")"
	checkpoint_targets_file="$(maestro_checkpoint_targets_file "${platform}" "${suite_path}")"
	if [[ -f "${checkpoint_file}" ]]; then
		checkpoint_read_file="${checkpoint_file}"
	elif [[ -f "${legacy_checkpoint_file}" ]]; then
		checkpoint_read_file="${legacy_checkpoint_file}"
	fi
	if [[ -n "${checkpoint_read_file}" ]]; then
		checkpoint_target="$(head -n 1 "${checkpoint_read_file}" | tr -d '\r')"
		checkpoint_reason="$(maestro_checkpoint_reason "${platform}" "${suite_path}")"
		for original_zero_index in "${!targets[@]}"; do
			if [[ "${targets[$original_zero_index]}" == "${checkpoint_target}" ]]; then
				start_index="${original_zero_index}"
				found_checkpoint=1
				break
			fi
		done
		if [[ "${found_checkpoint}" != "1" ]]; then
			log "${platform} Maestro checkpoint target no longer exists for ${suite_name}; restarting from first case."
		fi
	fi

	mkdir -p "$(dirname "${log_file}")" "$(maestro_checkpoint_dir "${platform}" "${suite_path}")"
	: >"${log_file}"
	printf '%s\n' "${targets[@]}" >"${checkpoint_targets_file}"

	item_root="${E2E_TMP_DIR}/maestro-items/$(maestro_slug "${platform}")/$(maestro_slug "${suite_name}")"
	results_file="${item_root}/results.tsv"
	rm -rf "${item_root}"
	mkdir -p "${item_root}"
	: >"${results_file}"

	if [[ "${found_checkpoint}" == "1" ]]; then
		log "${platform} Maestro plan: suite=${suite_name}; cases=${total_count}; resumeTarget=$(maestro_flow_label "${checkpoint_target}") case=$((start_index + 1))/${total_count}; checkpointReason=${checkpoint_reason}; order=rotated."
	else
		log "${platform} Maestro plan: suite=${suite_name}; cases=${total_count}; start=$(maestro_flow_label "${targets[0]}") case=1/${total_count}; order=normal."
	fi
	log "${platform} Maestro outputs: log=$(display_path "${log_file}"), report=$(display_path "${report_file}"), artifacts=$(display_path "${test_output_dir}"), debug=$(display_path "${debug_output_dir}")."

	suite_dir="$(dirname "${suite_path}")"
	for ((execution_index = 1; execution_index <= total_count; execution_index++)); do
		original_zero_index=$(((start_index + execution_index - 1) % total_count))
		original_index=$((original_zero_index + 1))
		target="${targets[$original_zero_index]}"
		target_label="$(maestro_flow_label "${target}")"
		target_file="$(maestro_resolve_flow_target "${target}" "${suite_dir}")"
		item_suite="${item_root}/suite-${execution_index}-case-${original_index}-$(maestro_slug "${target_label}").yaml"
		item_log="${item_root}/logs/${execution_index}-case-${original_index}-$(maestro_slug "${target_label}").log"
		item_report="${item_root}/reports/${execution_index}-case-${original_index}-$(maestro_slug "${target_label}").xml"
		mkdir -p "$(dirname "${item_report}")"
		item_test_output_dir=""
		item_debug_output_dir=""
		if [[ -n "${test_output_dir}" ]]; then
			item_test_output_dir="${test_output_dir}/case-${original_index}-$(maestro_slug "${target_label}")"
			mkdir -p "${item_test_output_dir}"
		fi
		if [[ -n "${debug_output_dir}" ]]; then
			item_debug_output_dir="${debug_output_dir}/case-${original_index}-$(maestro_slug "${target_label}")"
			mkdir -p "${item_debug_output_dir}"
		fi

		write_maestro_item_suite "${suite_path}" "${target}" "${item_suite}"
		if [[ "${E2E_MAESTRO_RESET_WIREMOCK_PER_ITEM:-1}" == "1" ]]; then
			reset_wiremock
		fi

		started_at="$(date +%s)"
		progress_percent="$(maestro_progress_percent "${execution_index}" "${total_count}")"
		write_maestro_checkpoint "${platform}" "${suite_path}" "${target}" "${original_index}" "${total_count}" "${item_log}" "in_progress"
		log "${platform} Maestro START progress=${progress_percent}% case=${original_index}/${total_count} ${target_label}; target=$(display_path "${target_file}")."
		{
			printf '===== START progress=%s%% case=%s/%s %s =====\n' "${progress_percent}" "${original_index}" "${total_count}" "${target}"
		} >>"${log_file}"

		maestro_command=(env "HOME=${maestro_home}" maestro)
		if [[ -n "${maestro_device_id}" ]]; then
			maestro_command+=(--device "${maestro_device_id}")
		fi
		maestro_command+=(test)
		if [[ -n "${E2E_MAESTRO_FORMAT:-}" ]]; then
			maestro_command+=(--format "${E2E_MAESTRO_FORMAT}")
		fi
		if [[ -n "${report_file}" ]]; then
			if [[ -z "${E2E_MAESTRO_FORMAT:-}" ]]; then
				maestro_command+=(--format junit)
			fi
			maestro_command+=(--output "${item_report}")
		elif [[ -n "${E2E_MAESTRO_REPORT_FILE:-}" ]]; then
			maestro_command+=(--output "${item_report}")
		fi
		if [[ -n "${item_test_output_dir}" ]]; then
			maestro_command+=(--test-output-dir "${item_test_output_dir}")
		fi
		if [[ -n "${item_debug_output_dir}" ]]; then
			maestro_command+=(--debug-output "${item_debug_output_dir}")
		fi
		maestro_command+=("${item_suite}")

		set +e
		run_maestro_item_compact \
			"${platform}" \
			"${target_label}" \
			"${execution_index}" \
			"${original_index}" \
			"${total_count}" \
			"${item_log}" \
			"${item_report}" \
			"${item_test_output_dir}" \
			"${item_debug_output_dir}" \
			"${maestro_command[@]}"
		command_status="$?"
		set -e
		elapsed="$(($(date +%s) - started_at))"
		if maestro_nonzero_after_passed_flow_is_recoverable "${command_status}" "${item_log}" "${item_report}"; then
			log "${platform} Maestro recovered nonzero exit ${command_status} after clean PASS for ${target_label}; treating as PASS."
			command_status=0
		fi
		printf '%s\t%s\t%s\t%s\n' "${command_status}" "${elapsed}" "${target}" "${item_log}" >>"${results_file}"
		{
			cat "${item_log}" 2>/dev/null || true
			printf '===== END progress=%s%% case=%s/%s %s status=%s =====\n' "${progress_percent}" "${original_index}" "${total_count}" "${target}" "${command_status}"
		} >>"${log_file}"

		if [[ "${command_status}" == "0" ]]; then
			log "${platform} Maestro PASS progress=${progress_percent}% case=${original_index}/${total_count} ${target_label}; elapsed=$(elapsed_label "${elapsed}")."
			continue
		fi

		status="${command_status}"
		latest_line="$(maestro_last_useful_line "${item_log}")"
		write_maestro_checkpoint "${platform}" "${suite_path}" "${target}" "${original_index}" "${total_count}" "${item_log}" "failed"
		write_maestro_aggregate_junit "${report_file}" "${suite_name}" "${results_file}"
		log "${platform} Maestro FAIL progress=${progress_percent}% case=${original_index}/${total_count} ${target_label}; exit=${status}; elapsed=$(elapsed_label "${elapsed}")."
		if [[ -n "${latest_line}" ]]; then
			log "${platform} Maestro failure detail: failureSummary='${latest_line}'."
		fi
		log "${platform} Maestro failure artifacts: log=$(display_path "${item_log}"), report=$(display_path "${item_report}"), debug=$(display_path "${item_debug_output_dir}")."
		return "${status}"
	done

	write_maestro_aggregate_junit "${report_file}" "${suite_name}" "${results_file}"
	clear_maestro_checkpoint "${platform}" "${suite_path}"
	log "${platform} Maestro finished: passed ${total_count}/${total_count}; checkpoint cleared."
	return 0
}

is_macos() {
	[[ "$(uname -s)" == "Darwin" ]]
}

wait_for_url() {
	local url="$1"
	local timeout_seconds="${2:-30}"
	local start_seconds
	start_seconds="$(date +%s)"

	while true; do
		if curl --fail --silent --output /dev/null "${url}"; then
			return 0
		fi

		if (( "$(date +%s)" - start_seconds >= timeout_seconds )); then
			printf 'Timed out waiting for %s\n' "${url}" >&2
			return 1
		fi

		sleep 1
	done
}

reset_wiremock() {
	require_command curl
	curl --fail --silent --output /dev/null \
		--request POST "${E2E_WIREMOCK_URL}/__admin/scenarios/reset"
	curl --fail --silent --output /dev/null \
		--request DELETE "${E2E_WIREMOCK_URL}/__admin/requests"
}

wiremock_pid_file() {
	printf '%s/wiremock.pid\n' "${E2E_TMP_DIR}"
}

wiremock_listener_pids() {
	if ! command -v lsof >/dev/null 2>&1; then
		return 0
	fi

	lsof -tiTCP:"${E2E_WIREMOCK_PORT}" -sTCP:LISTEN 2>/dev/null || true
}

wait_for_process_exit() {
	local pid="$1"
	local attempts="${2:-10}"

	for _ in $(seq 1 "${attempts}"); do
		if ! kill -0 "${pid}" >/dev/null 2>&1; then
			return 0
		fi

		sleep 1
	done

	return 1
}

stop_wiremock() {
	local restore_errexit=0
	case "$-" in
		*e*) restore_errexit=1 ;;
	esac
	set +e

	local pid_file
	local had_pid_file=0
	local wiremock_pid=""
	local listener_pid

	pid_file="$(wiremock_pid_file)"
	if [[ -f "${pid_file}" ]]; then
		had_pid_file=1
		wiremock_pid="$(cat "${pid_file}" 2>/dev/null || true)"
	fi

	if [[ -n "${wiremock_pid}" ]] && kill -0 "${wiremock_pid}" >/dev/null 2>&1; then
		log "Stopping owned WireMock ${wiremock_pid} on tcp:${E2E_WIREMOCK_PORT}."
		kill "${wiremock_pid}" >/dev/null 2>&1 || true
		if ! wait_for_process_exit "${wiremock_pid}" 10; then
			log "Force stopping owned WireMock ${wiremock_pid} on tcp:${E2E_WIREMOCK_PORT}."
			kill -9 "${wiremock_pid}" >/dev/null 2>&1 || true
			wait_for_process_exit "${wiremock_pid}" 5 >/dev/null 2>&1 || true
		fi
	fi

	if [[ "${had_pid_file}" == "1" ]]; then
		while IFS= read -r listener_pid; do
			[[ -n "${listener_pid}" ]] || continue
			if [[ "${listener_pid}" == "${wiremock_pid}" ]]; then
				continue
			fi

			log "Stopping WireMock listener ${listener_pid} on tcp:${E2E_WIREMOCK_PORT}."
			kill "${listener_pid}" >/dev/null 2>&1 || true
		done < <(wiremock_listener_pids)

		for _ in 1 2 3 4 5; do
			if [[ -z "$(wiremock_listener_pids)" ]]; then
				break
			fi

			sleep 1
		done

		while IFS= read -r listener_pid; do
			[[ -n "${listener_pid}" ]] || continue
			log "Force stopping WireMock listener ${listener_pid} on tcp:${E2E_WIREMOCK_PORT}."
			kill -9 "${listener_pid}" >/dev/null 2>&1 || true
		done < <(wiremock_listener_pids)

		rm -f "${pid_file}"
	fi

	if [[ "${restore_errexit}" == "1" ]]; then
		set -e
	fi
	return 0
}

register_wiremock_cleanup() {
	trap 'status=$?; trap - EXIT INT TERM; stop_wiremock; exit "${status}"' EXIT
	trap 'exit 130' INT
	trap 'exit 143' TERM
}

disable_android_keyboard_helpers() {
	if [[ "${E2E_DISABLE_KEYBOARD_HELPERS}" != "1" ]]; then
		return 0
	fi

	log "Disabling Android keyboard helpers for deterministic text input."
	adb shell settings put secure spell_checker_enabled 0 >/dev/null 2>&1 || true
	adb shell settings put secure selected_spell_checker "" >/dev/null 2>&1 || true
	adb shell settings put secure autofill_service null >/dev/null 2>&1 || true
	adb shell settings put secure show_ime_with_hard_keyboard 0 >/dev/null 2>&1 || true
	adb shell cmd autofill disable >/dev/null 2>&1 || true
}

disable_ios_keyboard_helpers() {
	if [[ "${E2E_DISABLE_KEYBOARD_HELPERS}" != "1" ]]; then
		return 0
	fi

	log "Disabling iOS simulator keyboard helpers for deterministic text input."
	xcrun simctl spawn "${E2E_IOS_DEVICE_ID}" defaults write -g KeyboardAutocorrection -bool NO >/dev/null 2>&1 || true
	xcrun simctl spawn "${E2E_IOS_DEVICE_ID}" defaults write -g KeyboardPrediction -bool NO >/dev/null 2>&1 || true
	xcrun simctl spawn "${E2E_IOS_DEVICE_ID}" defaults write -g KeyboardShowPrediction -bool NO >/dev/null 2>&1 || true
	xcrun simctl spawn "${E2E_IOS_DEVICE_ID}" defaults write -g NSAutomaticSpellingCorrectionEnabled -bool NO >/dev/null 2>&1 || true
	xcrun simctl spawn "${E2E_IOS_DEVICE_ID}" defaults write -g NSAutomaticTextCompletionEnabled -bool NO >/dev/null 2>&1 || true
	xcrun simctl spawn "${E2E_IOS_DEVICE_ID}" defaults write -g NSUseSpellCheckerForCompletions -bool NO >/dev/null 2>&1 || true
}
