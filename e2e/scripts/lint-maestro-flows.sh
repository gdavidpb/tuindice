#!/usr/bin/env bash
set -euo pipefail

# Structural lint for Maestro flow yamls. Maestro only reports an unknown
# property when the flow actually runs, so a bad edit can sit unnoticed until
# it burns a full evidence run (an invalid `timeout` on assertVisible cost one
# during certification). This gate validates, without running anything:
#   - every top-level command is a known Maestro command,
#   - every depth-1 property is valid for its command,
#   - assertVisible/assertNotVisible carry no timeout (use extendedWaitUntil),
#   - extendedWaitUntil declares timeout plus visible/notVisible,
#   - scalar runFlow refs resolve to an existing file.
# Nested commands (inside retry/runFlow blocks) are outside v1 scope.
#
# Usage: e2e/scripts/lint-maestro-flows.sh [flow.yaml ...]   (default: all flows)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
FLOWS_ROOT="${REPO_ROOT}/e2e/maestro/flows"

KNOWN_COMMANDS=" assertNotVisible assertVisible doubleTapOn extendedWaitUntil inputText launchApp retry runFlow scrollUntilVisible swipe takeScreenshot tapOn waitForAnimationToEnd "

KNOWN_PROPS="
assertNotVisible.id assertNotVisible.text assertNotVisible.enabled assertNotVisible.index
assertVisible.id assertVisible.text assertVisible.enabled assertVisible.index
doubleTapOn.id doubleTapOn.text doubleTapOn.point
extendedWaitUntil.visible extendedWaitUntil.notVisible extendedWaitUntil.timeout
launchApp.appId launchApp.arguments launchApp.clearState launchApp.stopApp launchApp.permissions
retry.commands retry.maxRetries
runFlow.when runFlow.commands runFlow.file runFlow.env
scrollUntilVisible.element scrollUntilVisible.direction scrollUntilVisible.timeout scrollUntilVisible.visibilityPercentage scrollUntilVisible.centerElement scrollUntilVisible.speed
swipe.direction swipe.duration swipe.start swipe.end swipe.from
tapOn.id tapOn.text tapOn.point tapOn.index tapOn.enabled tapOn.childOf tapOn.retryTapIfNoChange tapOn.waitUntilVisible tapOn.longPress tapOn.repeat tapOn.delay
waitForAnimationToEnd.timeout
"

lint_file() {
	local file="$1"
	local flat_props

	# BSD awk rejects newlines inside -v values; flatten the map first.
	flat_props=" $(printf '%s' "${KNOWN_PROPS}" | tr '\n' ' ') "

	awk -v known_commands="${KNOWN_COMMANDS}" -v known_props="${flat_props}" -v file="${file}" '
		BEGIN {
			errors = 0
		}
		function report(line, message) {
			printf "%s:%d: %s\n", file, line, message
			errors++
		}
		/^---[[:space:]]*$/ && inCommands == 0 { inCommands = 1; next }
		inCommands == 0 { next }
		/^[[:space:]]*#/ { next }
		/^-([[:space:]]|$)/ {
			if (cmd == "extendedWaitUntil" && !(hasTimeout && hasCondition)) {
				report(cmdLine, "extendedWaitUntil requires timeout plus visible/notVisible")
			}
			cmd = $0
			sub(/^-[[:space:]]*/, "", cmd)
			sub(/[:[:space:]].*$/, "", cmd)
			cmdLine = NR
			hasTimeout = 0
			hasCondition = 0
			if (cmd == "") {
				report(NR, "top-level entry without a command name")
			} else if (index(known_commands, " " cmd " ") == 0) {
				report(NR, "unknown Maestro command: " cmd)
				cmd = ""
			}
			next
		}
		/^    [A-Za-z][A-Za-z0-9]*:/ && cmd != "" {
			prop = $0
			sub(/^    /, "", prop)
			sub(/:.*$/, "", prop)
			if ((cmd == "assertVisible" || cmd == "assertNotVisible") && prop == "timeout") {
				report(NR, cmd " does not support timeout; use extendedWaitUntil for bounded waits")
				next
			}
			if (index(known_props, " " cmd "." prop " ") == 0) {
				report(NR, "unknown property for " cmd ": " prop)
				next
			}
			if (cmd == "extendedWaitUntil") {
				if (prop == "timeout") hasTimeout = 1
				if (prop == "visible" || prop == "notVisible") hasCondition = 1
			}
			next
		}
		END {
			if (cmd == "extendedWaitUntil" && !(hasTimeout && hasCondition)) {
				report(cmdLine, "extendedWaitUntil requires timeout plus visible/notVisible")
			}
			exit errors > 0 ? 1 : 0
		}
	' "${file}"
}

lint_runflow_refs() {
	local file="$1"
	local dir ref status=0

	dir="$(dirname "${file}")"
	while IFS= read -r ref; do
		[[ -n "${ref}" ]] || continue
		if [[ ! -f "${dir}/${ref}" ]]; then
			printf '%s: runFlow target does not exist: %s\n' "${file}" "${ref}"
			status=1
		fi
	done < <(awk '
		/^---[[:space:]]*$/ { inCommands = 1; next }
		inCommands && /^-[[:space:]]+runFlow:[[:space:]]*[^[:space:]]/ {
			sub(/^-[[:space:]]+runFlow:[[:space:]]*/, "")
			gsub(/^["'\''[:space:]]+|["'\''[:space:]]+$/, "")
			print
		}
	' "${file}")
	return "${status}"
}

files=("$@")
if [[ "${#files[@]}" -eq 0 ]]; then
	while IFS= read -r file; do
		case "$(basename "${file}")" in
			zz-probe-*) continue ;;
		esac
		files+=("${file}")
	done < <(find "${FLOWS_ROOT}" -name '*.yaml' -type f | sort)
fi

status=0
for file in "${files[@]}"; do
	lint_file "${file}" || status=1
	lint_runflow_refs "${file}" || status=1
done

if [[ "${status}" != "0" ]]; then
	printf 'Maestro flow lint failed.\n' >&2
	exit 1
fi
printf 'Maestro flows lint passed (%d files).\n' "${#files[@]}"
