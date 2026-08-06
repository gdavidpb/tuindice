#!/usr/bin/env bash
set -euo pipefail

# Runs several flows/suites through diagnose-suite.sh in the given order, on both
# platforms, stopping at the first failure.
#
# The order is the whole point: put the flows whose assertions depend on what the
# diff changed first, so the cheapest run finds the most. A full evidence rotation
# costs 60-100+ minutes and has to be repeated after every fix; each flow here is
# about 4, needs no clean or pushed tree, and publishes nothing.
#
# Usage: e2e/scripts/diagnose-flows.sh [--survey] <flow-or-suite.yaml>...
#   --survey  reports every failing case per suite in one pass instead of
#             stopping at the first (passed through to diagnose-suite.sh).

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

survey_args=()
targets=()
for arg in "$@"; do
	case "${arg}" in
		--survey) survey_args=(--survey) ;;
		-*)
			printf 'Unknown option: %s\n' "${arg}" >&2
			exit 1
			;;
		*) targets+=("${arg}") ;;
	esac
done

if [[ "${#targets[@]}" == "0" ]]; then
	printf 'Usage: %s [--survey] <flow-or-suite.yaml>...\n' "$0" >&2
	exit 1
fi

missing=0
for target in "${targets[@]}"; do
	if [[ ! -f "${target}" ]]; then
		printf 'No such flow or suite: %s\n' "${target}" >&2
		missing=1
	fi
done
if [[ "${missing}" != "0" ]]; then
	exit 1
fi

total="${#targets[@]}"
index=0
for target in "${targets[@]}"; do
	index=$((index + 1))
	printf '\n[diagnose-flows] %s/%s: %s\n' "${index}" "${total}" "${target}"

	status=0
	# ${survey_args[@]} alone throws unbound-variable under set -u when --survey was
	# never passed (empty array); this expansion is the POSIX-safe empty-or-elements form.
	bash "${SCRIPT_DIR}/diagnose-suite.sh" ${survey_args[@]+"${survey_args[@]}"} "${target}" || status="$?"

	if [[ "${status}" != "0" ]]; then
		printf '\n[diagnose-flows] Stopped at %s/%s: %s failed (exit %s).\n' \
			"${index}" "${total}" "${target}" "${status}"
		printf '[diagnose-flows] Fix it and rerun from this target; the ones before it already passed.\n'
		exit "${status}"
	fi
done

printf '\n[diagnose-flows] All %s target(s) passed on both platforms.\n' "${total}"
