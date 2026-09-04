#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
CATALOG="${SCRIPT_DIR}/flow-catalog.yaml"
MVI_ACTION_CATALOG="${SCRIPT_DIR}/mvi-action-catalog.yaml"
CRITICAL_SELECTORS="${SCRIPT_DIR}/critical-selectors.txt"
FLOWS_ROOT="${REPO_ROOT}/e2e/maestro/flows"
FIXTURES_ENV="${SCRIPT_DIR}/fixture-contract.env"
QUARANTINE_FILE="${SCRIPT_DIR}/quarantine.txt"
FIXTURES_KT="${REPO_ROOT}/testkit/src/commonMain/kotlin/com/gdavidpb/tuindice/testkit/e2e/E2eFixtureContract.kt"

if [[ ! -f "${CATALOG}" ]]; then
	printf 'Missing E2E flow catalog: %s\n' "${CATALOG}" >&2
	exit 1
fi

if [[ ! -d "${FLOWS_ROOT}" ]]; then
	printf 'Missing Maestro flows root: %s\n' "${FLOWS_ROOT}" >&2
	exit 1
fi

if [[ ! -f "${MVI_ACTION_CATALOG}" ]]; then
	printf 'Missing MVI action catalog: %s\n' "${MVI_ACTION_CATALOG}" >&2
	exit 1
fi

if [[ ! -f "${FIXTURES_ENV}" ]]; then
	printf 'Missing E2E fixture contract values: %s\n' "${FIXTURES_ENV}" >&2
	exit 1
fi

# shellcheck disable=SC1090
source "${FIXTURES_ENV}"

catalog_paths="$(awk '/^[[:space:]]*path:/ { print $2 }' "${CATALOG}" | sort -u)"
missing_catalog_entries=0

# A killed or delayed producer behind `< <(...)` is indistinguishable from an
# empty result to the reading loop, so a producer that dies under CI memory
# pressure silently yields "0 flows found" instead of a loud failure. Writing
# to a real file first and checking the producer's exit status turns that
# into a hard error instead of a false "missing" verdict downstream.
require_producer_output() {
	local out_file
	out_file="$(mktemp)"
	if ! "$@" >"${out_file}"; then
		printf 'E2E contract validation producer failed: %s\n' "$*" >&2
		rm -f "${out_file}"
		exit 1
	fi
	printf '%s\n' "${out_file}"
}

flow_files_list="$(require_producer_output find "${FLOWS_ROOT}" -name '*.yaml' -type f)"
sort -o "${flow_files_list}" "${flow_files_list}"
while IFS= read -r flow_file; do
	relative_path="${flow_file#"${REPO_ROOT}/"}"
	if ! printf '%s\n' "${catalog_paths}" | grep --fixed-strings --line-regexp --quiet "${relative_path}"; then
		printf 'Flow missing from catalog: %s\n' "${relative_path}" >&2
		missing_catalog_entries=1
	fi
done < "${flow_files_list}"
rm -f "${flow_files_list}"

missing_flow_files=0
while IFS= read -r catalog_path; do
	[[ -z "${catalog_path}" ]] && continue
	if [[ ! -f "${REPO_ROOT}/${catalog_path}" ]]; then
		printf 'Catalog references missing flow: %s\n' "${catalog_path}" >&2
		missing_flow_files=1
	fi
done < <(printf '%s\n' "${catalog_paths}")

mvi_contract_actions="$(mktemp)"
mvi_catalog_entries="$(mktemp)"
mvi_catalog_actions="$(mktemp)"
flow_catalog_actions="$(mktemp)"
flow_catalog_records="$(mktemp)"
flow_catalog_path_actions="$(mktemp)"
flow_catalog_primary_selectors="$(mktemp)"
flow_catalog_active_executable_paths="$(mktemp)"
local_certification_reachable_paths="$(mktemp)"
trap 'rm -f "${mvi_contract_actions}" "${mvi_catalog_entries}" "${mvi_catalog_actions}" "${flow_catalog_actions}" "${flow_catalog_records}" "${flow_catalog_path_actions}" "${flow_catalog_primary_selectors}" "${flow_catalog_active_executable_paths}" "${local_certification_reachable_paths}"' EXIT

while IFS= read -r contract_file; do
	relative_path="${contract_file#"${REPO_ROOT}/"}"
	module_name="${relative_path%%/src/*}"
	contract_name="$(basename "${contract_file}" .kt)"

	awk -v action_prefix="${module_name}.${contract_name}." '
		function countChar(value, char, pos, total) {
			total = 0
			for (pos = 1; pos <= length(value); pos++) {
				if (substr(value, pos, 1) == char) {
					total++
				}
			}
			return total
		}
		/^[[:space:]]*sealed[[:space:]]+(class|interface)[[:space:]]+Action([[:space:]]|:|\{|$)/ {
			inAction = 1
			depth = countChar($0, "{") - countChar($0, "}")
			next
		}
		inAction == 1 {
			line = $0
			sub(/\/\/.*/, "", line)
			candidate = line
			sub(/^[[:space:]]*/, "", candidate)
			if (candidate ~ /^(data[[:space:]]+)?(object|class)[[:space:]]+[A-Za-z_][A-Za-z0-9_]*/) {
				sub(/^data[[:space:]]+/, "", candidate)
				sub(/^(object|class)[[:space:]]+/, "", candidate)
				sub(/[^A-Za-z0-9_].*/, "", candidate)
				print action_prefix candidate
			}
			depth += countChar(line, "{") - countChar(line, "}")
			if (depth <= 0) {
				inAction = 0
			}
		}
	' "${contract_file}"
done < <(find "${REPO_ROOT}" -path '*/src/commonMain/kotlin/*/presentation/contract/*.kt' -type f | sort) \
	| sort -u > "${mvi_contract_actions}"

awk '
	/^[[:space:]]*-[[:space:]]*id:/ {
		if (id != "") {
			print id "|" classification "|" flow "|" platformEdge
		}
		id = $3
		classification = ""
		flow = ""
		platformEdge = ""
		next
	}
	/^[[:space:]]*classification:/ {
		classification = $2
		next
	}
	/^[[:space:]]*flow:/ {
		flow = $2
		next
	}
	/^[[:space:]]*platform_edge:/ {
		platformEdge = $0
		sub(/^[[:space:]]*platform_edge:[[:space:]]*/, "", platformEdge)
		next
	}
	END {
		if (id != "") {
			print id "|" classification "|" flow "|" platformEdge
		}
	}
' "${MVI_ACTION_CATALOG}" > "${mvi_catalog_entries}"

cut -d '|' -f 1 "${mvi_catalog_entries}" | sort -u > "${mvi_catalog_actions}"

awk '
	/^[[:space:]]*actions_covered:/ {
		inActions = 1
		next
	}
	inActions && /^[[:space:]]*-[[:space:]]*[A-Za-z0-9_.]+/ {
		action = $2
		if (action ~ /^[a-z]+[.][A-Za-z][A-Za-z0-9_]*[.][A-Za-z][A-Za-z0-9_]*$/) {
			print action
		}
		next
	}
	inActions && /^[[:space:]]*[A-Za-z_][A-Za-z0-9_]*:/ {
		inActions = 0
	}
' "${CATALOG}" | sort -u > "${flow_catalog_actions}"

awk -v records="${flow_catalog_records}" -v pathActions="${flow_catalog_path_actions}" '
	function flush(i) {
		if (id != "" && path != "") {
			print path "|" id "|" module "|" type > records
			for (i = 1; i <= actionCount; i++) {
				print path "|" actions[i] > pathActions
			}
		}
		id = ""
		path = ""
		module = ""
		type = ""
		actionCount = 0
		delete actions
		inActions = 0
	}
	/^[[:space:]]*-[[:space:]]*id:/ {
		flush()
		id = $3
		next
	}
	/^[[:space:]]*path:/ {
		path = $2
		next
	}
	/^[[:space:]]*module:/ {
		module = $2
		next
	}
	/^[[:space:]]*type:/ {
		type = $2
		next
	}
	/^[[:space:]]*actions_covered:/ {
		inActions = 1
		next
	}
	inActions && /^[[:space:]]*-[[:space:]]*[A-Za-z0-9_.]+/ {
		actionCount++
		actions[actionCount] = $2
		next
	}
	inActions && /^[[:space:]]*[A-Za-z_][A-Za-z0-9_]*:/ {
		inActions = 0
	}
	END {
		flush()
	}
' "${CATALOG}"

# primary_selectors are per-flow coverage claims, so they must appear in that
# flow or in a nested helper it runs. Dynamic selectors are validated at the YAML
# boundary, not by searching for generated tag strings in Kotlin.
awk '
	function flush_inline_selectors(value, itemCount, items, i, selector) {
		gsub(/[\[\]]/, "", value)
		itemCount = split(value, items, ",")
		for (i = 1; i <= itemCount; i++) {
			selector = items[i]
			gsub(/^[[:space:]]+|[[:space:]]+$/, "", selector)
			if (path != "" && selector != "") {
				print path "|" selector
			}
		}
	}
	/^[[:space:]]*-[[:space:]]*id:/ {
		path = ""
		inPrimary = 0
		next
	}
	/^[[:space:]]*path:/ {
		path = $2
		next
	}
	/^[[:space:]]*primary_selectors:[[:space:]]*\[/ {
		value = $0
		sub(/^[[:space:]]*primary_selectors:[[:space:]]*/, "", value)
		flush_inline_selectors(value)
		inPrimary = 0
		next
	}
	/^[[:space:]]*primary_selectors:/ {
		inPrimary = 1
		next
	}
	inPrimary && /^[[:space:]]*-[[:space:]]*/ {
		selector = $2
		if (path != "" && selector != "") {
			print path "|" selector
		}
		next
	}
	inPrimary && /^[[:space:]]*[A-Za-z_][A-Za-z0-9_]*:/ {
		inPrimary = 0
	}
' "${CATALOG}" | sort -u > "${flow_catalog_primary_selectors}"

awk '
	function flush() {
		if (id != "" && path != "" && status == "active" && flowType != "setup" && flowType != "suite" && module != "shared") {
			print path
		}
		id = ""
		path = ""
		module = ""
		flowType = ""
		status = ""
	}
	/^[[:space:]]*-[[:space:]]*id:/ {
		flush()
		id = $3
		next
	}
	/^[[:space:]]*path:/ {
		path = $2
		next
	}
	/^[[:space:]]*module:/ {
		module = $2
		next
	}
	/^[[:space:]]*type:/ {
		flowType = $2
		next
	}
	/^[[:space:]]*status:/ {
		status = $2
		next
	}
	END {
		flush()
	}
' "${CATALOG}" | sort -u > "${flow_catalog_active_executable_paths}"

normalize_flow_path() {
	local parent_path="$1"
	local child_ref="$2"
	local parent_dir child_dir child_base normalized_dir

	parent_dir="$(dirname "${REPO_ROOT}/${parent_path}")"
	child_dir="$(dirname "${child_ref}")"
	child_base="$(basename "${child_ref}")"
	normalized_dir="$(cd "${parent_dir}/${child_dir}" && pwd -P)"

	printf '%s/%s\n' "${normalized_dir#"${REPO_ROOT}/"}" "${child_base}"
}

# The seen-set is shared across one search instead of carried down each branch. With 436 runFlow
# edges and shared children reached from dozens of flows, a per-branch set re-explores every shared
# subtree once per path into it, and the work grows exponentially with the graph — a full preflight
# was observed spinning for hours on it. Within a single search the memo is sound: a flow that did
# not contain the selector cannot start containing it when reached by another route. It is reset per
# search, because that answer *is* specific to the selector being looked for.
FLOW_SEARCH_VISITED=""

flow_contains_selector() {
	FLOW_SEARCH_VISITED=""
	flow_contains_selector_within_search "$1" "$2"
}

flow_contains_selector_within_search() {
	local flow_path="$1"
	local selector="$2"
	local absolute_path="${REPO_ROOT}/${flow_path}"
	local child_ref child_path

	[[ -f "${absolute_path}" ]] || return 1
	case "|${FLOW_SEARCH_VISITED}|" in
		*"|${flow_path}|"*)
			return 1
			;;
	esac

	if grep --fixed-strings --quiet "${selector}" "${absolute_path}"; then
		return 0
	fi

	FLOW_SEARCH_VISITED="${FLOW_SEARCH_VISITED}|${flow_path}"
	while IFS= read -r child_ref; do
		[[ -z "${child_ref}" ]] && continue
		child_path="$(normalize_flow_path "${flow_path}" "${child_ref}")"
		if flow_contains_selector_within_search "${child_path}" "${selector}"; then
			return 0
		fi
	done < <(
		awk '
			/runFlow:[[:space:]]*[^[:space:]].*[.]ya?ml/ {
				line = $0
				sub(/^.*runFlow:[[:space:]]*/, "", line)
				gsub(/[" ]/, "", line)
				print line
			}
		' "${absolute_path}"
	)

	return 1
}

# Same discipline, same reason. Reset per root: the caller asks what one root reaches, so a flow
# already emitted for a previous root must still be emitted for this one.
FLOW_REACHABLE_VISITED=""

collect_reachable_flow_paths() {
	FLOW_REACHABLE_VISITED=""
	collect_reachable_flow_paths_within_walk "$1"
}

collect_reachable_flow_paths_within_walk() {
	local flow_path="$1"
	local absolute_path="${REPO_ROOT}/${flow_path}"
	local child_ref child_path

	[[ -f "${absolute_path}" ]] || return 0
	case "|${FLOW_REACHABLE_VISITED}|" in
		*"|${flow_path}|"*)
			return 0
			;;
	esac

	printf '%s\n' "${flow_path}"
	FLOW_REACHABLE_VISITED="${FLOW_REACHABLE_VISITED}|${flow_path}"
	while IFS= read -r child_ref; do
		[[ -z "${child_ref}" ]] && continue
		child_path="$(normalize_flow_path "${flow_path}" "${child_ref}")"
		collect_reachable_flow_paths_within_walk "${child_path}"
	done < <(
		awk '
			/runFlow:[[:space:]]*[^[:space:]].*[.]ya?ml/ {
				line = $0
				sub(/^.*runFlow:[[:space:]]*/, "", line)
				gsub(/[" ]/, "", line)
				print line
			}
		' "${absolute_path}"
	)
}

missing_primary_selectors=0
while IFS='|' read -r flow_path primary_selector; do
	[[ -n "${flow_path}" && -n "${primary_selector}" ]] || continue
	if ! flow_contains_selector "${flow_path}" "${primary_selector}"; then
		printf 'Flow primary selector is not exercised by flow or nested runFlow: %s -> %s\n' "${flow_path}" "${primary_selector}" >&2
		missing_primary_selectors=1
	fi
done < "${flow_catalog_primary_selectors}"

local_certification_suite="e2e/maestro/flows/suites/local-certification-suite.yaml"
collect_reachable_flow_paths "${local_certification_suite}" | sort -u > "${local_certification_reachable_paths}"

missing_local_certification_flows=0
while IFS= read -r active_flow_path; do
	[[ -n "${active_flow_path}" ]] || continue
	if ! grep --fixed-strings --line-regexp --quiet "${active_flow_path}" "${local_certification_reachable_paths}"; then
		printf 'Active flow is not reachable from local-certification-suite: %s\n' "${active_flow_path}" >&2
		missing_local_certification_flows=1
	fi
done < "${flow_catalog_active_executable_paths}"

suite_action_inheritance_issues=0
while IFS='|' read -r suite_path suite_id suite_module suite_type; do
	[[ "${suite_type}" == "suite" ]] || continue
	if ! grep --fixed-strings --quiet "${suite_path}|" "${flow_catalog_path_actions}"; then
		continue
	fi

	suite_actions="$(awk -F '|' -v path="${suite_path}" '$1 == path { print $2 }' "${flow_catalog_path_actions}")"

	suite_child_refs="$(require_producer_output awk '
		/runFlow:[[:space:]]*[^[:space:]].*[.]ya?ml/ {
			line = $0
			sub(/^.*runFlow:[[:space:]]*/, "", line)
			gsub(/[" ]/, "", line)
			print line
		}
	' "${REPO_ROOT}/${suite_path}")"

	while IFS= read -r child_ref; do
		[[ -z "${child_ref}" ]] && continue

		child_path="$(normalize_flow_path "${suite_path}" "${child_ref}")"
		child_record="$(awk -F '|' -v path="${child_path}" '$1 == path { print; exit }' "${flow_catalog_records}")"
		if [[ -z "${child_record}" ]]; then
			continue
		fi

		IFS='|' read -r _child_path child_id child_module child_type <<< "${child_record}"
		if [[ "${child_type}" == "setup" ]]; then
			continue
		fi
		if [[ "${suite_module}" != "all" && "${child_module}" != "${suite_module}" ]]; then
			continue
		fi

		child_actions_list="$(require_producer_output awk -F '|' -v path="${child_path}" '$1 == path { print $2 }' "${flow_catalog_path_actions}")"
		while IFS= read -r child_action; do
			[[ -z "${child_action}" ]] && continue
			if ! printf '%s\n' "${suite_actions}" | grep --fixed-strings --line-regexp --quiet "${child_action}"; then
				printf 'Suite %s omits child action %s from %s\n' "${suite_id}" "${child_action}" "${child_id}" >&2
				suite_action_inheritance_issues=1
			fi
		done < "${child_actions_list}"
		rm -f "${child_actions_list}"
	done < "${suite_child_refs}"
	rm -f "${suite_child_refs}"
done < "${flow_catalog_records}"

invalid_flow_actions=0
while IFS= read -r action_id; do
	[[ -z "${action_id}" ]] && continue

	if ! grep --fixed-strings --line-regexp --quiet "${action_id}" "${mvi_contract_actions}"; then
		printf 'Flow catalog actions_covered references unknown Action: %s\n' "${action_id}" >&2
		invalid_flow_actions=1
		continue
	fi

	if ! grep --fixed-strings --line-regexp --quiet "${action_id}" "${mvi_catalog_actions}"; then
		printf 'Flow catalog actions_covered is missing from MVI action catalog: %s\n' "${action_id}" >&2
		invalid_flow_actions=1
	fi
done < "${flow_catalog_actions}"

missing_mvi_actions=0
while IFS= read -r action_id; do
	[[ -z "${action_id}" ]] && continue
	printf 'MVI Action missing from catalog: %s\n' "${action_id}" >&2
	missing_mvi_actions=1
done < <(comm -23 "${mvi_contract_actions}" "${mvi_catalog_actions}")

unknown_mvi_actions=0
while IFS= read -r action_id; do
	[[ -z "${action_id}" ]] && continue
	printf 'MVI catalog references unknown Action: %s\n' "${action_id}" >&2
	unknown_mvi_actions=1
done < <(comm -13 "${mvi_contract_actions}" "${mvi_catalog_actions}")

invalid_mvi_entries=0
while IFS='|' read -r action_id classification flow_path platform_edge; do
	if [[ -z "${action_id}" ]]; then
		continue
	fi

	case "${classification}" in
		user|internal|platform-edge)
			;;
		*)
			printf 'MVI Action has invalid classification: %s (%s)\n' "${action_id}" "${classification}" >&2
			invalid_mvi_entries=1
			;;
	esac

	if [[ "${classification}" == "user" && -z "${flow_path}" && -z "${platform_edge}" ]]; then
		printf 'User MVI Action has no flow or platform edge: %s\n' "${action_id}" >&2
		invalid_mvi_entries=1
	fi

	if [[ "${classification}" == "platform-edge" && -z "${platform_edge}" ]]; then
		printf 'Platform-edge MVI Action lacks platform_edge rationale: %s\n' "${action_id}" >&2
		invalid_mvi_entries=1
	fi

	if [[ -n "${flow_path}" && ! -f "${REPO_ROOT}/${flow_path}" ]]; then
		printf 'MVI Action references missing flow: %s -> %s\n' "${action_id}" "${flow_path}" >&2
		invalid_mvi_entries=1
	fi
done < "${mvi_catalog_entries}"

missing_selectors=0
while IFS= read -r selector; do
	[[ -z "${selector}" ]] && continue
	[[ "${selector}" == \#* ]] && continue
	if ! grep --recursive --fixed-strings --quiet "${selector}" "${FLOWS_ROOT}"; then
		printf 'Critical selector is not used by any Maestro flow: %s\n' "${selector}" >&2
		missing_selectors=1
	fi
done < "${CRITICAL_SELECTORS}"

invalid_auth_usb_id_inputs=0
while IFS= read -r auth_flow; do
	awk -v file="${auth_flow#"${REPO_ROOT}/"}" '
		/id:[[:space:]]*auth_usb_id_text_field/ {
			expectUsbIdInput = 1
			next
		}
		expectUsbIdInput && /^[[:space:]]*-[[:space:]]*inputText:/ {
			value = $0
			sub(/^[^"]*"/, "", value)
			sub(/".*$/, "", value)
			if (value !~ /^([0-9]{7}|[0-9]{2}-[0-9]{5}|[A-Za-z][A-Za-z0-9._-]*(@usb[.]ve)?)$/) {
				printf "Auth flow writes invalid sign-in identifier %s in %s\n", value, file > "/dev/stderr"
				exit 42
			}
			expectUsbIdInput = 0
		}
	' "${auth_flow}" || {
		status=$?
		if [[ "${status}" == "42" ]]; then
			invalid_auth_usb_id_inputs=1
		else
			exit "${status}"
		fi
	}
done < <(find "${FLOWS_ROOT}/auth" -name '*.yaml' -type f | sort)

record_search_fixture_mismatches=0
bash "${SCRIPT_DIR}/validate-record-search-fixtures.sh" || record_search_fixture_mismatches=1

fixture_contract_mismatches=0

kotlin_fixture_value() {
	local const_name="$1"
	awk -v name="${const_name}" '
		$0 ~ "const val " name " = " {
			line = $0
			sub(/^[^"]*"/, "", line)
			sub(/".*$/, "", line)
			print line
		}
	' "${FIXTURES_KT}"
}

check_fixture_pair() {
	local env_value="$1"
	local const_name="$2"
	local kotlin_value

	kotlin_value="$(kotlin_fixture_value "${const_name}")"
	if [[ "${kotlin_value}" != "${env_value}" ]]; then
		printf 'Fixture contract drift: %s is "%s" in fixture-contract.env but "%s" in E2eFixtureContract.kt\n' "${const_name}" "${env_value}" "${kotlin_value}" >&2
		fixture_contract_mismatches=1
	fi
}

if [[ ! -f "${FIXTURES_KT}" ]]; then
	printf 'Missing Kotlin fixture contract mirror: %s\n' "${FIXTURES_KT}" >&2
	fixture_contract_mismatches=1
else
	check_fixture_pair "${E2E_CANONICAL_USBID_RAW}" "CANONICAL_USBID_RAW"
	check_fixture_pair "${E2E_CANONICAL_USBID_FORMATTED}" "CANONICAL_USBID_FORMATTED"
	check_fixture_pair "${E2E_USB_EMAIL_LOCAL}" "USB_EMAIL_LOCAL"
	check_fixture_pair "${E2E_USB_EMAIL_FULL}" "USB_EMAIL_FULL"
	check_fixture_pair "${E2E_CANONICAL_PASSWORD}" "CANONICAL_PASSWORD"
	check_fixture_pair "${E2E_INVALID_USBID_RAW}" "INVALID_USBID_RAW"
	check_fixture_pair "${E2E_SUMMARY_REFRESH_RETRY_PASSWORD}" "SUMMARY_REFRESH_RETRY_PASSWORD"
	check_fixture_pair "${E2E_RECORD_REFRESH_RETRY_PASSWORD}" "RECORD_REFRESH_RETRY_PASSWORD"
	check_fixture_pair "${E2E_RECORD_TERM_REJECTED_PASSWORD}" "RECORD_TERM_REJECTED_PASSWORD"
	check_fixture_pair "${E2E_PENSUM_EQUIVALENCE_PASSWORD}" "PENSUM_EQUIVALENCE_PASSWORD"
	check_fixture_pair "${E2E_RECORD_SEARCH_PRIORITY_PLANNED}" "PRIORITY_PLANNED"
	check_fixture_pair "${E2E_RECORD_SEARCH_PRIORITY_BLOCKED}" "PRIORITY_BLOCKED"
	check_fixture_pair "${E2E_RECORD_SEARCH_HISTORICAL_RETIRED}" "HISTORICAL_RETIRED"
	check_fixture_pair "${E2E_RECORD_SEARCH_HISTORICAL_FAILED}" "HISTORICAL_FAILED"
	check_fixture_pair "${E2E_RECORD_SEARCH_HISTORICAL_APPROVED}" "HISTORICAL_APPROVED"
fi

# Flows may split a value across several inputText steps (keystroke guards), so the
# check runs over the concatenation of everything the flow types. There is no mocks/
# counterpart check on purpose: the WireMock matchers are value-agnostic by design and
# behavior is owned by scenarios/transformers, not by literal credential fixtures.
login_success_flow="${FLOWS_ROOT}/auth/login-success.yaml"
if [[ ! -f "${login_success_flow}" ]]; then
	printf 'Missing canonical login flow: %s\n' "${login_success_flow}" >&2
	fixture_contract_mismatches=1
else
	flow_typed_input="$(awk '/inputText:/ { line = $0; sub(/^[^"]*"/, "", line); sub(/".*$/, "", line); printf "%s", line }' "${login_success_flow}")"
	case "${flow_typed_input}" in
		*"${E2E_CANONICAL_USBID_RAW}"*)
			;;
		*)
			printf 'Canonical USBID %s is not typed by the canonical login flow\n' "${E2E_CANONICAL_USBID_RAW}" >&2
			fixture_contract_mismatches=1
			;;
	esac
	case "${flow_typed_input}" in
		*"${E2E_CANONICAL_PASSWORD}"*)
			;;
		*)
			printf 'Canonical password is not typed by the canonical login flow\n' >&2
			fixture_contract_mismatches=1
			;;
	esac
fi

email_login_flow="${FLOWS_ROOT}/auth/login-usb-email-success.yaml"
if [[ ! -f "${email_login_flow}" ]]; then
	printf 'Missing USB email login flow: %s\n' "${email_login_flow}" >&2
	fixture_contract_mismatches=1
else
	email_flow_typed_input="$(awk '/inputText:/ { line = $0; sub(/^[^"]*"/, "", line); sub(/".*$/, "", line); printf "%s", line }' "${email_login_flow}")"
	case "${email_flow_typed_input}" in
		*"${E2E_USB_EMAIL_FULL}"*)
			;;
		*)
			printf 'Canonical USB email %s is not typed by the USB email login flow\n' "${E2E_USB_EMAIL_FULL}" >&2
			fixture_contract_mismatches=1
			;;
	esac
	case "${email_flow_typed_input}" in
		*"${E2E_CANONICAL_PASSWORD}"*)
			;;
		*)
			printf 'Canonical password is not typed by the USB email login flow\n' >&2
			fixture_contract_mismatches=1
			;;
	esac
fi

refresh_retry_fixture_mismatches=0

flow_typed_input() {
	local flow_path="$1"
	awk '/inputText:/ { line = $0; sub(/^[^"]*"/, "", line); sub(/".*$/, "", line); printf "%s", line }' "${flow_path}"
}

check_flow_types_value() {
	local flow_path="$1"
	local expected_value="$2"
	local description="$3"
	local typed_input

	if [[ ! -f "${flow_path}" ]]; then
		printf 'Missing %s flow: %s\n' "${description}" "${flow_path}" >&2
		refresh_retry_fixture_mismatches=1
		return
	fi

	typed_input="$(flow_typed_input "${flow_path}")"
	case "${typed_input}" in
		*"${expected_value}"*)
			;;
		*)
			printf '%s does not type expected value %s\n' "${description}" "${expected_value}" >&2
			refresh_retry_fixture_mismatches=1
			;;
	esac
}

check_flow_contains() {
	local flow_path="$1"
	local expected_text="$2"
	local description="$3"

	if [[ ! -f "${flow_path}" ]]; then
		printf 'Missing %s flow: %s\n' "${description}" "${flow_path}" >&2
		refresh_retry_fixture_mismatches=1
		return
	fi

	if ! grep --fixed-strings --quiet "${expected_text}" "${flow_path}"; then
		printf '%s flow does not contain expected text: %s\n' "${description}" "${expected_text}" >&2
		refresh_retry_fixture_mismatches=1
	fi
}

check_mapping_contains() {
	local mapping_path="$1"
	local expected_text="$2"
	local description="$3"

	if [[ ! -f "${mapping_path}" ]]; then
		printf 'Missing %s mapping: %s\n' "${description}" "${mapping_path}" >&2
		refresh_retry_fixture_mismatches=1
		return
	fi

	if ! grep --fixed-strings --quiet "${expected_text}" "${mapping_path}"; then
		printf '%s mapping does not contain expected matcher: %s\n' "${description}" "${expected_text}" >&2
		refresh_retry_fixture_mismatches=1
	fi
}

check_mapping_not_contains() {
	local mapping_path="$1"
	local rejected_text="$2"
	local description="$3"

	if [[ ! -f "${mapping_path}" ]]; then
		printf 'Missing %s mapping: %s\n' "${description}" "${mapping_path}" >&2
		refresh_retry_fixture_mismatches=1
		return
	fi

	if grep --fixed-strings --quiet "${rejected_text}" "${mapping_path}"; then
		printf '%s mapping contains stale matcher: %s\n' "${description}" "${rejected_text}" >&2
		refresh_retry_fixture_mismatches=1
	fi
}

check_path_absent() {
	local path="$1"
	local description="$2"

	if [[ -e "${path}" ]]; then
		printf '%s should not exist: %s\n' "${description}" "${path}" >&2
		refresh_retry_fixture_mismatches=1
	fi
}

check_protected_mappings_require_bearer() {
	local protected_path_pattern='^/(users/v1($|/)|messaging/v1$|record/v5($|/)|evaluations/v3($|/)|enrollment-proof/v1$|subjects/v1($|/)|pensums/v4$)'
	local protected_mapping_issues=0

	while IFS= read -r mapping_path; do
		if jq -e --arg pattern "${protected_path_pattern}" '
			def authMatcher: .request.headers.Authorization?;
			(.request.urlPath? // "" | test($pattern)) and
				(
					authMatcher == null or
					(((authMatcher.equalTo? // authMatcher.matches? // "") | startswith("Bearer ")) | not)
				)
		' "${mapping_path}" >/dev/null; then
			printf 'Protected mapping must require Bearer Authorization: %s\n' "${mapping_path#"${REPO_ROOT}/"}" >&2
			protected_mapping_issues=1
		fi
	done < <(find "${REPO_ROOT}/mocks/mappings" -name '*.json' -type f | sort)

	if [[ "${protected_mapping_issues}" -ne 0 ]]; then
		fixture_contract_mismatches=1
	fi
}

check_protected_mappings_require_bearer

# Mappings whose delay a flow depends on (cancel windows, reveal timers) must
# pin the value the fast profile keeps; without the marker the fast profile
# collapses the delay to 250ms and the dependent flow breaks only at runtime.
check_semantic_delays_declare_fast_profile() {
	local semantic_delay_issues=0

	while IFS= read -r mapping_path; do
		if jq -e '
			(.metadata.fastDelayMilliseconds? // null) as $fast |
			(((.response.fixedDelayMilliseconds? // 0) >= 5000) and $fast == null) or
				($fast != null and (($fast | type) != "number"))
		' "${mapping_path}" >/dev/null; then
			printf 'Mapping with a semantic delay (>=5000ms) must declare numeric metadata.fastDelayMilliseconds: %s\n' "${mapping_path#"${REPO_ROOT}/"}" >&2
			semantic_delay_issues=1
		fi
	done < <(find "${REPO_ROOT}/mocks/mappings" -name '*.json' -type f | sort)

	if [[ "${semantic_delay_issues}" -ne 0 ]]; then
		fixture_contract_mismatches=1
	fi
}

check_semantic_delays_declare_fast_profile

check_flow_types_value \
	"${FLOWS_ROOT}/summary/summary-refresh-retry.yaml" \
	"${E2E_SUMMARY_REFRESH_RETRY_PASSWORD}" \
	"Summary refresh retry"
check_flow_types_value \
	"${FLOWS_ROOT}/record/record-refresh-retry.yaml" \
	"${E2E_RECORD_REFRESH_RETRY_PASSWORD}" \
	"Record refresh retry"
check_flow_types_value \
	"${FLOWS_ROOT}/record/record-synthetic-term-rejected.yaml" \
	"${E2E_RECORD_TERM_REJECTED_PASSWORD}" \
	"Record synthetic term rejected"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/record/patch-synthetic-term-rejected.json" \
	"\"equalTo\": \"Bearer record.term.rejected.mock.access\"" \
	"Record synthetic term rejected patch"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/login/auth-record-term-rejected-exchange-success.json" \
	"\"access_token\": \"record.term.rejected.mock.access\"" \
	"Record synthetic term rejected exchange"
check_flow_contains \
	"${FLOWS_ROOT}/record/record-refresh-retry.yaml" \
	"coachmark_bubble|maincore_tuindice_bottom_bar_record_item|base_error_view_container" \
	"Record refresh retry post-login"

check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/sync/post-sync-summary-refresh-retry-unavailable.json" \
	"\$[?(@.password == '${E2E_SUMMARY_REFRESH_RETRY_PASSWORD}')]" \
	"Summary refresh retry sync"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/sync/post-sync-summary-refresh-retry-unavailable.json" \
	"\"bodyFileName\": \"sync/post-sync-record-unavailable.json\"" \
	"Summary refresh retry sync"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/sync/post-sync-summary-refresh-retry-unavailable.json" \
	"\"newScenarioState\": \"InitialSyncUnavailable\"" \
	"Summary refresh retry sync"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/sync/post-sync-record-refresh-retry-unavailable.json" \
	"\$[?(@.password == '${E2E_RECORD_REFRESH_RETRY_PASSWORD}')]" \
	"Record refresh retry sync"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/sync/post-sync-record-refresh-retry-unavailable.json" \
	"\"bodyFileName\": \"sync/post-sync-record-unavailable.json\"" \
	"Record refresh retry sync"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/sync/post-sync-record-refresh-retry-unavailable.json" \
	"\"newScenarioState\": \"InitialSyncUnavailable\"" \
	"Record refresh retry sync"
check_path_absent \
	"${REPO_ROOT}/mocks/mappings/sync/post-sync-record-refresh-retry-success.json" \
	"Stale record refresh retry sync success mapping"
check_path_absent \
	"${REPO_ROOT}/mocks/mappings/sync/post-sync-summary-refresh-retry-success.json" \
	"Stale summary refresh retry sync success mapping"

for summary_retry_mapping in \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-fails-once.json" \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-fails-android-first.json" \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-fails-android-second.json" \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-success.json" \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-success-ios.json"
do
	check_mapping_contains \
		"${summary_retry_mapping}" \
		"\"Authorization\"" \
		"Summary refresh retry user"
done
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-fails-once.json" \
	"\"requiredScenarioState\": \"InitialSyncUnavailable\"" \
	"Summary refresh retry first failure"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-fails-once.json" \
	"\"newScenarioState\": \"FailedOnce\"" \
	"Summary refresh retry iOS first failure"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-fails-once.json" \
	"\"contains\": \"iOS\"" \
	"Summary refresh retry iOS first failure"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-fails-android-first.json" \
	"\"requiredScenarioState\": \"InitialSyncUnavailable\"" \
	"Summary refresh retry Android first failure"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-fails-android-first.json" \
	"\"newScenarioState\": \"AndroidFailedOnce\"" \
	"Summary refresh retry Android first failure"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-fails-android-first.json" \
	"\"contains\": \"Android\"" \
	"Summary refresh retry Android first failure"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-fails-android-second.json" \
	"\"requiredScenarioState\": \"AndroidFailedOnce\"" \
	"Summary refresh retry Android second failure"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-fails-android-second.json" \
	"\"newScenarioState\": \"FailedOnce\"" \
	"Summary refresh retry Android second failure"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-fails-android-second.json" \
	"\"contains\": \"Android\"" \
	"Summary refresh retry Android second failure"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-success.json" \
	"\"requiredScenarioState\": \"FailedOnce\"" \
	"Summary refresh retry Android success"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-success.json" \
	"\"contains\": \"Android\"" \
	"Summary refresh retry Android success"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-success-ios.json" \
	"\"requiredScenarioState\": \"FailedOnce\"" \
	"Summary refresh retry iOS success"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-success-ios.json" \
	"\"contains\": \"iOS\"" \
	"Summary refresh retry iOS success"
check_path_absent \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-fails-twice.json" \
	"Stale summary refresh retry second failure mapping"
check_path_absent \
	"${REPO_ROOT}/mocks/mappings/summary/get-user-refresh-retry-fails-ios-second.json" \
	"Stale summary refresh retry iOS second failure mapping"

for record_retry_mapping in \
	"${REPO_ROOT}/mocks/mappings/record/get-record-refresh-retry-unavailable-once.json" \
	"${REPO_ROOT}/mocks/mappings/record/get-record-refresh-retry-success.json"
do
	check_mapping_contains \
		"${record_retry_mapping}" \
		"\"Authorization\"" \
		"Record refresh retry record"
done
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/record/get-record-refresh-retry-unavailable-once.json" \
	"\"requiredScenarioState\": \"InitialSyncUnavailable\"" \
	"Record refresh retry first failure"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/record/get-record-refresh-retry-unavailable-once.json" \
	"\"newScenarioState\": \"FirstFailure\"" \
	"Record refresh retry first failure"
check_mapping_contains \
	"${REPO_ROOT}/mocks/mappings/record/get-record-refresh-retry-success.json" \
	"\"requiredScenarioState\": \"FirstFailure\"" \
	"Record refresh retry success"

quarantine_issues=0
if [[ -f "${QUARANTINE_FILE}" ]]; then
	while IFS= read -r entry; do
		[[ -z "${entry}" ]] && continue
		case "${entry}" in
			e2e/maestro/flows/_shared/*)
				printf 'Quarantine cannot list shared setup flows: %s\n' "${entry}" >&2
				quarantine_issues=1
				continue
				;;
			e2e/maestro/flows/*)
				;;
			*)
				printf 'Quarantine entries must be repo-relative flow paths: %s\n' "${entry}" >&2
				quarantine_issues=1
				continue
				;;
		esac
		if [[ ! -f "${REPO_ROOT}/${entry}" ]]; then
			printf 'Quarantine references missing flow: %s\n' "${entry}" >&2
			quarantine_issues=1
		fi
	done < <(sed -e 's/#.*$//' -e 's/[[:space:]]*$//' -e '/^[[:space:]]*$/d' "${QUARANTINE_FILE}")
fi

maestro_lint_issues=0
if ! bash "${REPO_ROOT}/e2e/scripts/lint-maestro-flows.sh"; then
	maestro_lint_issues=1
fi

mock_reset_issues=0
if ! bash "${REPO_ROOT}/e2e/scripts/verify-mock-reset-parity.sh"; then
	mock_reset_issues=1
fi

fingerprint_coverage_issues=0
if ! bash "${REPO_ROOT}/e2e/scripts/verify-e2e-fingerprint-coverage.sh"; then
	fingerprint_coverage_issues=1
fi

suite_vocabulary_issues=0
if ! bash "${REPO_ROOT}/e2e/scripts/verify-suite-vocabulary.sh"; then
	suite_vocabulary_issues=1
fi

if [[ "${missing_catalog_entries}" == "1" || "${missing_flow_files}" == "1" || "${invalid_flow_actions}" == "1" || "${missing_primary_selectors}" == "1" || "${missing_local_certification_flows}" == "1" || "${suite_action_inheritance_issues}" == "1" || "${missing_mvi_actions}" == "1" || "${unknown_mvi_actions}" == "1" || "${invalid_mvi_entries}" == "1" || "${missing_selectors}" == "1" || "${invalid_auth_usb_id_inputs}" == "1" || "${record_search_fixture_mismatches}" == "1" || "${fixture_contract_mismatches}" == "1" || "${refresh_retry_fixture_mismatches}" == "1" || "${quarantine_issues}" == "1" || "${maestro_lint_issues}" == "1" || "${mock_reset_issues}" == "1" || "${fingerprint_coverage_issues}" == "1" || "${suite_vocabulary_issues}" == "1" ]]; then
	exit 1
fi

printf 'E2E contract is valid.\n'
