#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
CATALOG="${SCRIPT_DIR}/flow-catalog.yaml"
CRITICAL_SELECTORS="${SCRIPT_DIR}/critical-selectors.txt"
FLOWS_ROOT="${REPO_ROOT}/e2e/maestro/flows"

if [[ ! -f "${CATALOG}" ]]; then
	printf 'Missing E2E flow catalog: %s\n' "${CATALOG}" >&2
	exit 1
fi

if [[ ! -d "${FLOWS_ROOT}" ]]; then
	printf 'Missing Maestro flows root: %s\n' "${FLOWS_ROOT}" >&2
	exit 1
fi

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

missing_selectors=0
while IFS= read -r selector; do
	[[ -z "${selector}" ]] && continue
	[[ "${selector}" == \#* ]] && continue
	if ! grep --recursive --fixed-strings --quiet "${selector}" "${FLOWS_ROOT}"; then
		printf 'Critical selector is not used by any Maestro flow: %s\n' "${selector}" >&2
		missing_selectors=1
	fi
done < "${CRITICAL_SELECTORS}"

if [[ "${missing_catalog_entries}" == "1" || "${missing_flow_files}" == "1" || "${missing_selectors}" == "1" ]]; then
	exit 1
fi

printf 'E2E contract is valid.\n'
