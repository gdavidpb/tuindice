#!/usr/bin/env bash
set -euo pipefail

# Every suite name the pipeline can emit must have a suite file behind it.
#
# The names live in three hand-written places — the module-to-suite mapping, the
# contexts the aggregate run publishes, and the flow-directory rules — and none
# of them was tied to what exists on disk. When the wizard flows were deleted the
# three kept naming wizard-suite, so the aggregate published a green
# local-e2e/<platform>/wizard-suite for a suite that could not run, and a change
# to the wizard module demanded a status no tool in the repo could produce.
#
# Usage: e2e/scripts/verify-suite-vocabulary.sh

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SUITES_DIR="${REPO_ROOT}/e2e/maestro/flows/suites"

issues=0

report() {
	printf '%s\n' "$1" >&2
	issues=1
}

# Suite ids named by module_e2e_suite, which prints "<module>-suite".
declare_module_suites() {
	sed -n '/^module_e2e_suite()/,/^}/p' "${REPO_ROOT}/.github/scripts/common.sh" |
		grep -oE '^[[:space:]]*[a-z|]+\)' |
		tr -d ' \t)' |
		tr '|' '\n' |
		sed 's/$/-suite/'
}

# Suite ids the aggregate run publishes commit statuses for.
declare_covered_suites() {
	sed -n '/^covered_status_contexts()/,/^}/p' "${REPO_ROOT}/e2e/scripts/run-maestro-evidence.sh" |
		grep -oE '[a-z-]+-suite'
}

# Suite ids the change detector can put in scope.
declare_detector_suites() {
	grep -oE 'append_e2e_scope [a-z]+ [a-z-]+-suite' "${REPO_ROOT}/.github/scripts/detect-changed-app.sh" |
		awk '{ print $3 }'
}

while IFS= read -r suite; do
	[[ -n "${suite}" ]] || continue
	if [[ ! -f "${SUITES_DIR}/${suite}.yaml" ]]; then
		report "${suite} is named by the pipeline but ${SUITES_DIR#"${REPO_ROOT}/"}/${suite}.yaml does not exist; evidence for it can never be produced, and the aggregate would publish a status certifying nothing."
	fi
done < <(
	{
		declare_module_suites
		declare_covered_suites
		declare_detector_suites
	} | sort -u
)

if [[ "${issues}" != "0" ]]; then
	exit 1
fi

printf 'E2E suite vocabulary is valid.\n'
