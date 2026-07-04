#!/usr/bin/env bash
set -euo pipefail

# Generates an instrumented copy of a Maestro flow with a takeScreenshot after
# every top-level command, so a failing tap/assert can be diagnosed by direct
# observation instead of hypothesis. iOS reports off-viewport lazy items as
# visible, so hierarchy-based asserts can pass while taps silently no-op; the
# per-step screenshots are the reliable signal.
#
# Usage:
#   e2e/scripts/make-probe.sh <flow.yaml>   generate zz-probe-<name>.yaml next to the flow
#   e2e/scripts/make-probe.sh --clean       remove every generated probe yaml and screenshot

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
FLOWS_ROOT="${REPO_ROOT}/e2e/maestro/flows"

if [[ "${1:-}" == "--clean" ]]; then
	find "${FLOWS_ROOT}" -name 'zz-probe-*.yaml' -type f -delete
	find "${REPO_ROOT}" -maxdepth 1 -name 'zz-probe-*.png' -type f -delete
	printf 'Removed generated probe yamls and screenshots.\n'
	exit 0
fi

flow_path="${1:-}"
if [[ -z "${flow_path}" || ! -f "${flow_path}" ]]; then
	printf 'Usage: %s <flow.yaml> | --clean\n' "$0" >&2
	exit 1
fi
flow_path="$(cd "$(dirname "${flow_path}")" && pwd -P)/$(basename "${flow_path}")"

case "${flow_path}" in
	"${FLOWS_ROOT}/"*) ;;
	*)
		printf 'Flow must live under %s so relative runFlow refs resolve.\n' "${FLOWS_ROOT}" >&2
		exit 1
		;;
esac

flow_name="$(basename "${flow_path}" .yaml)"
case "${flow_name}" in
	zz-probe-*)
		printf 'Refusing to instrument an already generated probe: %s\n' "${flow_path}" >&2
		exit 1
		;;
esac

probe_path="$(dirname "${flow_path}")/zz-probe-${flow_name}.yaml"
shot_prefix="zz-probe-${flow_name}"

awk -v prefix="${shot_prefix}" '
	function emit_shot() {
		if (pending_cmd != "") {
			printf "- takeScreenshot: %s-%02d-%s\n", prefix, pending_step, pending_cmd
			pending_cmd = ""
		}
	}
	/^---[[:space:]]*$/ && inCommands == 0 {
		print
		inCommands = 1
		next
	}
	inCommands == 0 {
		print
		next
	}
	/^-([[:space:]]|$)/ {
		emit_shot()
		step++
		cmd = $0
		sub(/^-[[:space:]]*/, "", cmd)
		sub(/[:[:space:]].*$/, "", cmd)
		gsub(/[^A-Za-z0-9]/, "", cmd)
		pending_step = step
		pending_cmd = (cmd == "" ? "step" : cmd)
	}
	{ print }
	END { emit_shot() }
' "${flow_path}" >"${probe_path}"

step_count="$(grep -c '^- takeScreenshot: ' "${probe_path}")"
printf 'Probe written: %s (%s instrumented steps).\n' "${probe_path}" "${step_count}"
printf 'Run it with:\n'
printf '  E2E_MAESTRO_SUITE=%s ./gradlew --console=plain e2eMaestroAndroid\n' "${probe_path#"${REPO_ROOT}"/}"
printf '  E2E_MAESTRO_SUITE=%s ./gradlew --console=plain e2eMaestroIos\n' "${probe_path#"${REPO_ROOT}"/}"
printf 'Screenshots land in the repo root as %s-NN-<command>.png; clean up with %s --clean\n' "${shot_prefix}" "$0"
