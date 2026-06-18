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

while IFS= read -r flow_file; do
	relative_path="${flow_file#"${REPO_ROOT}/"}"
	if ! printf '%s\n' "${catalog_paths}" | grep --fixed-strings --line-regexp --quiet "${relative_path}"; then
		printf 'Flow missing from catalog: %s\n' "${relative_path}" >&2
		missing_catalog_entries=1
	fi
done < <(find "${FLOWS_ROOT}" -name '*.yaml' -type f | sort)

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
trap 'rm -f "${mvi_contract_actions}" "${mvi_catalog_entries}" "${mvi_catalog_actions}"' EXIT

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
	check_fixture_pair "${E2E_RECORD_SEARCH_PRIORITY_PLANNED}" "PRIORITY_PLANNED"
	check_fixture_pair "${E2E_RECORD_SEARCH_PRIORITY_UNAVAILABLE}" "PRIORITY_UNAVAILABLE"
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

if [[ "${missing_catalog_entries}" == "1" || "${missing_flow_files}" == "1" || "${missing_mvi_actions}" == "1" || "${unknown_mvi_actions}" == "1" || "${invalid_mvi_entries}" == "1" || "${missing_selectors}" == "1" || "${invalid_auth_usb_id_inputs}" == "1" || "${record_search_fixture_mismatches}" == "1" || "${fixture_contract_mismatches}" == "1" || "${quarantine_issues}" == "1" ]]; then
	exit 1
fi

printf 'E2E contract is valid.\n'
