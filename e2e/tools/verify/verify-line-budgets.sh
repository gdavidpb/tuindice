#!/usr/bin/env bash
# Verifies the line budgets of line-budgets.env: the harness scripts, the certification SKILL.md and its runbook.
#   verify-line-budgets.sh [--root DIR]     DIR is the tree that is measured (default: this repository);
#                                           the budgets always come from the file next to this script.
# Exit 0 when every budget holds, 1 when one is exceeded, 2 on bad usage.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

if [[ "${1:-}" == "--root" ]]; then
	[[ -d "${2:-}" ]] || { printf 'Usage: %s [--root DIR]\n' "$0" >&2; exit 2; }
	ROOT="$(cd "$2" && pwd)"
elif [[ $# -gt 0 ]]; then
	printf 'Usage: %s [--root DIR]\n' "$0" >&2
	exit 2
fi

# shellcheck source=e2e/tools/verify/line-budgets.env
source "${SCRIPT_DIR}/line-budgets.env"

SKILL_DIR="${ROOT}/.codex/skills/certify-tuindice-pr"

# Lines of every file under the given directories, summed file by file (bash 3.2 and BSD tools have no `find -printf`).
count_tree() {
	local total=0 lines file
	while IFS= read -r -d '' file; do
		lines="$(wc -l < "${file}")"
		total=$((total + ${lines//[[:space:]]/}))
	done < <(find "$@" -type f ! -name '*.pyc' ! -path '*/__pycache__/*' -print0)
	printf '%s\n' "${total}"
}

status=0
check() {
	local name="$1" used="$2" budget="$3"
	if (( used > budget )); then
		printf 'OVER BUDGET: %s has %d lines, the budget is %d (e2e/tools/verify/line-budgets.env)\n' "${name}" "${used}" "${budget}" >&2
		status=1
	else
		printf 'line budget: %s %d of %d\n' "${name}" "${used}" "${budget}"
	fi
}

for dir in shared android ios; do
	[[ -d "${ROOT}/e2e/scripts/${dir}" ]] || { printf 'Missing directory: e2e/scripts/%s\n' "${dir}" >&2; exit 2; }
done
for file in "${SKILL_DIR}/SKILL.md" "${SKILL_DIR}/references/certification-runbook.md"; do
	[[ -f "${file}" ]] || { printf 'Missing file: %s\n' "${file}" >&2; exit 2; }
done
check "e2e/scripts/{shared,android,ios}" \
	"$(count_tree "${ROOT}/e2e/scripts/shared" "${ROOT}/e2e/scripts/android" "${ROOT}/e2e/scripts/ios")" "${E2E_SCRIPTS_MAX_LINES}"
check "SKILL.md" "$(count_tree "${SKILL_DIR}/SKILL.md")" "${SKILL_MD_MAX_LINES}"
check "references/certification-runbook.md" \
	"$(count_tree "${SKILL_DIR}/references/certification-runbook.md")" "${CERTIFICATION_RUNBOOK_MAX_LINES}"
exit "${status}"
