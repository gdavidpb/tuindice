#!/usr/bin/env bash
set -euo pipefail

# Runs a Maestro suite (or single flow) on BOTH platforms sequentially and
# prints a combined verdict. A flow fix verified on one platform only tends to
# resurface as a fresh failure on the other's screen geometry during the next
# 60-90 minute evidence run; two diagnosis runs here cost ~10 minutes instead.
#
# Usage: e2e/scripts/diagnose-suite.sh <suite-or-flow.yaml> [--survey]
#   --survey  sets E2E_MAESTRO_SURVEY_MODE=1 so each platform reports every
#             failing case in one pass instead of stopping at the first.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

suite_path=""
survey=0
for arg in "$@"; do
	case "${arg}" in
		--survey) survey=1 ;;
		-*)
			printf 'Unknown option: %s\n' "${arg}" >&2
			exit 1
			;;
		*) suite_path="${arg}" ;;
	esac
done

if [[ -z "${suite_path}" || ! -f "${suite_path}" ]]; then
	printf 'Usage: %s <suite-or-flow.yaml> [--survey]\n' "$0" >&2
	exit 1
fi

export E2E_MAESTRO_SUITE="${suite_path}"
if [[ "${survey}" == "1" ]]; then
	export E2E_MAESTRO_SURVEY_MODE=1
fi

verdict_label() {
	local status="$1"
	if [[ "${status}" == "0" ]]; then
		printf 'PASS'
	else
		printf 'FAIL (exit %s)' "${status}"
	fi
}

android_status=0
printf '[diagnose-suite] Android run: %s\n' "${suite_path}"
(cd "${REPO_ROOT}" && ./gradlew --console=plain e2eMaestroAndroid) || android_status="$?"

ios_status=0
ios_skipped=0
if [[ "$(uname -s)" == "Darwin" ]] && command -v xcrun >/dev/null 2>&1; then
	printf '[diagnose-suite] iOS run: %s\n' "${suite_path}"
	(cd "${REPO_ROOT}" && ./gradlew --console=plain e2eMaestroIos) || ios_status="$?"
else
	ios_skipped=1
fi

printf '\n[diagnose-suite] Verdict for %s:\n' "${suite_path}"
printf '  Android: %s\n' "$(verdict_label "${android_status}")"
if [[ "${ios_skipped}" == "1" ]]; then
	printf '  iOS:     SKIPPED (simulator toolchain unavailable)\n'
else
	printf '  iOS:     %s\n' "$(verdict_label "${ios_status}")"
fi

if [[ "${android_status}" != "0" ]]; then
	exit "${android_status}"
fi
exit "${ios_status}"
