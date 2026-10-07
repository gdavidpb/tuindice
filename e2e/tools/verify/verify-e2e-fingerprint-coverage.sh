#!/usr/bin/env bash
# The v5 fingerprint must read everything that can change what a scenario does, and nothing else may hide under e2e/.
# Fails when:
#   1. a tracked file sits directly under e2e/scripts (the harness lives in shared/, android/ and ios/);
#   2. a tracked file under e2e/ is neither inside a fingerprinted pathspec nor under e2e/tools, e2e/platform or e2e/README.md;
#   3. a `required:` pathspec has no tracked file (a rename left the fingerprint reading nothing);
#   4. a tracked file under testkit/e2e is neither validate-*.sh, *.md, nor covered;
#   5. a toolchain lock key is not read by harness/toolchain.py, or the reverse;
#   6. the scenario runner pins an androidx.test version instead of using the version catalog.
# Pre-v5 files that the cut deletes are tolerated while they exist (legacy-until-f26.txt).
#
# Usage: e2e/tools/verify/verify-e2e-fingerprint-coverage.sh
# Test seams: E2E_VERIFY_REPO_ROOT (the repository to check), E2E_VERIFY_FINGERPRINT_SCRIPT.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOOLS_REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
REPO_ROOT="${E2E_VERIFY_REPO_ROOT:-${TOOLS_REPO_ROOT}}"
FINGERPRINT_SCRIPT="${E2E_VERIFY_FINGERPRINT_SCRIPT:-${TOOLS_REPO_ROOT}/e2e/scripts/shared/e2e-fingerprint.sh}"
LEGACY_LIST="${SCRIPT_DIR}/legacy-until-f26.txt"
# shellcheck source=e2e/scripts/shared/layout.env
source "${TOOLS_REPO_ROOT}/e2e/scripts/shared/layout.env"

issues=0
report() {
	printf '%s\n' "$1" >&2
	issues=1
}

is_legacy() {
	local file="$1"
	local entry
	[[ -f "${LEGACY_LIST}" ]] || return 1
	while IFS= read -r entry; do
		[[ -n "${entry}" && "${entry}" != "#"* ]] || continue
		if [[ "${entry}" == */ ]]; then
			[[ "${file}" == "${entry}"* ]] && return 0
		elif [[ "${file}" == "${entry}" ]]; then
			return 0
		fi
	done <"${LEGACY_LIST}"
	return 1
}

pathspecs() {
	E2E_FINGERPRINT_REPO_ROOT="${REPO_ROOT}" bash "${FINGERPRINT_SCRIPT}" --print-pathspecs "$1" HEAD
}

covered_by() {
	local file="$1"
	local spec
	shift
	for spec in "$@"; do
		if [[ "${file}" == "${spec}" || "${file}" == "${spec}/"* ]]; then
			return 0
		fi
	done
	return 1
}

all_specs=()
required_specs=()
for platform in android ios; do
	while IFS= read -r line; do
		all_specs+=("${line#*:}")
		if [[ "${line}" == required:* ]]; then
			required_specs+=("${line#*:}")
		fi
	done < <(pathspecs "${platform}")
done

# 3. A required pathspec nobody tracks.
for spec in "${required_specs[@]}"; do
	if [[ -z "$(git -C "${REPO_ROOT}" ls-files -- "${spec}")" ]]; then
		report "The fingerprint requires ${spec} but no tracked file lives there; it would hash nothing for that path."
	fi
done

while IFS= read -r file; do
	[[ -n "${file}" ]] || continue
	case "${file}" in
		e2e/*)
			# 1. Loose scripts directly under e2e/scripts.
			if [[ "${file}" == e2e/scripts/* && "${file#e2e/scripts/}" != */* ]] && ! is_legacy "${file}"; then
				report "${file} sits directly under e2e/scripts; the harness lives in e2e/scripts/shared, android or ios."
				continue
			fi
			# 2. Not covered and not exempt.
			case "${file}" in
				e2e/tools/*|e2e/platform/*|e2e/README.md) continue ;;
			esac
			if ! covered_by "${file}" "${all_specs[@]}" && ! is_legacy "${file}"; then
				report "${file} is not inside any fingerprinted pathspec and is not under e2e/tools, e2e/platform or e2e/README.md; a change to it would leave published evidence valid."
			fi
			;;
		testkit/e2e/*)
			# 4. Data under testkit/e2e that the fingerprint does not read.
			case "${file}" in
				testkit/e2e/validate-*.sh|*.md) continue ;;
			esac
			if ! covered_by "${file}" "${all_specs[@]}" && ! is_legacy "${file}"; then
				report "${file} is neither a validator, a document nor read by the fingerprint."
			fi
			;;
	esac
done < <(git -C "${REPO_ROOT}" ls-files e2e testkit/e2e)

# 5. Lock keys against the keys toolchain.py reads.
lock_check="$(
	PYTHONDONTWRITEBYTECODE=1 python3 - "${REPO_ROOT}" "${TOOLS_REPO_ROOT}/e2e/scripts/shared" "${E2E_TOOLCHAIN_DIR}" <<'PY'
import os
import re
import sys

repo, shared, lock_dir = sys.argv[1:4]
sys.path.insert(0, shared)
from harness.toolchain import LOCK_KEYS

problems = []
for platform, expected in LOCK_KEYS.items():
    path = os.path.join(repo, lock_dir, "%s.lock" % platform)
    if not os.path.exists(path):
        problems.append("%s does not exist" % path)
        continue
    keys = set()
    for line in open(path):
        match = re.match(r"([A-Z0-9_]+)=", line)
        if match:
            keys.add(match.group(1))
    for key in sorted(keys - set(expected)):
        problems.append("%s.lock has %s, which toolchain.py does not read" % (platform, key))
    for key in sorted(set(expected) - keys):
        problems.append("toolchain.py reads %s for %s, which %s.lock does not define" % (key, platform, platform))
print("\n".join(problems))
PY
)"
if [[ -n "${lock_check}" ]]; then
	report "${lock_check}"
fi

# 6. androidx.test pinned in the runner build file instead of the version catalog.
runner_build="${REPO_ROOT}/${E2E_ANDROID_TEST_MODULE}/build.gradle.kts"
if [[ -f "${runner_build}" ]] && grep -nE '"androidx\.test[^"]*:[^"]*:[0-9]' "${runner_build}" >&2; then
	report "${E2E_ANDROID_TEST_MODULE}/build.gradle.kts hard-codes an androidx.test version; take it from gradle/libs.versions.toml, which the fingerprint reads."
fi

if [[ "${issues}" != "0" ]]; then
	exit 1
fi

printf 'E2E fingerprint coverage is valid.\n'
