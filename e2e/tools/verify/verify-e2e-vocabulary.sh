#!/usr/bin/env bash
# The vocabulary of the E2E system is the one that exists. Fails when a versioned file (tracked, or new and not ignored):
#   1. has a name, or a line, that spells the name of the runner the native drivers replaced, in any letter case;
#   2. names a Gradle task of the e2e*, verify* or sync* families that no build file registers;
#   3. spells one of the identifiers retired with the previous harness (the explicit, short list RETIRED below).
#
# A task exists when a build file (*.gradle.kts, *.gradle) registers it under a quoted name. The scan of rule 2 skips
# the files where those prefixes are ordinary identifiers (*.kt, *.swift, *.json, *.xml) and the build files that
# define the tasks. A name followed by `*` is a family glob, not a task.
#
# Exceptions are listed below, each with its reason. A path or token that no longer has a hit is a stale exception and
# fails too, so the list can only shrink; this file does not count as a hit of its own exceptions (it spells the tokens
# it excepts). The retired word and the retired identifiers are built from pieces so that this file needs no exception
# of its own.
#
# Usage: verify-e2e-vocabulary.sh [--root DIR] | --print-exceptions
# Exit 0 when the vocabulary is clean, 1 when it is not, 2 on bad usage.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

WORD="mae""stro"
# Identifiers of the previous harness that must not come back into a versioned file. Whole list: anything else it
# retired is caught by rules 1 and 2. Each is written in pieces: this file is scanned too.
RETIRED=(
	"SEED_""STATE"
	"e2e""Mae""stro"
	"e2e""Platform"
	"Migration""Progress"
	"validate-e2e-""contract"
	"run_sequential_""evidence"
	"flow-""catalog"
	"E2E_STRICT_""IOS"
)
SELF_PATH="e2e/tools/verify/verify-e2e-vocabulary.sh"
TASK_PATTERN='(e2e|verify|sync)[A-Z][A-Za-z0-9]*[*]?'

# kind|subject|reason. Kinds: word-path (rule 1 skips the file), task-path (rule 2 skips the file), task-token (rule 2
# accepts the token anywhere), retired-path (rule 3 skips the file).
EXCEPTIONS="$(cat <<'EOF'
word-path|e2e/tools/e2e-retention.py|looks for the leftovers of the previous harness on disk (build/e2e/<name>-*.log and certifications/*/*/*/<name>-*); a glob has to spell the name
word-path|e2e/tools/tests/test_retention.py|creates those leftovers as fixtures to prove the retention finds them and only --purge-legacy --yes removes them
word-path|docs/e2e-migracion-fidelidad.md|names the tag that preserves the flows of the retired runner (the tag is called after it), so the record can be checked against them
word-path|e2e/tools/tests/test_single_definitions.py|names the retired tasks to assert that the registry no longer has them
task-path|e2e/tools/tests/test_single_definitions.py|same test: the retired task names are what it asserts are absent
task-token|verifyE2e|prefix of the task family in comments and in the regex of the fingerprint-coverage verifier; not a task
retired-path|e2e/tools/tests/test_single_definitions.py|names the retired tasks to assert that the registry no longer has them
retired-path|maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/debug/DebugLaunchArgumentsTest.kt|asserts that the retired seed key is rejected as an unknown launch argument
task-token|syncComposeResourcesForIos|task of the Compose Multiplatform plugin (:maincore:syncComposeResourcesForIos), used by the iOS build scripts
EOF
)"

if [[ "${1:-}" == "--print-exceptions" ]]; then
	printf '%s\n' "${EXCEPTIONS}" | cut -d'|' -f1,2
	exit 0
elif [[ "${1:-}" == "--root" ]]; then
	[[ -d "${2:-}" ]] || { printf 'Usage: %s [--root DIR] | --print-exceptions\n' "$0" >&2; exit 2; }
	ROOT="$(cd "$2" && pwd)"
elif [[ $# -gt 0 ]]; then
	printf 'Usage: %s [--root DIR] | --print-exceptions\n' "$0" >&2
	exit 2
fi

WORK="$(mktemp -d "${TMPDIR:-/tmp}/e2e-vocabulary.XXXXXX")"
trap 'rm -rf "${WORK}"' EXIT

grep_tree() {
	git -C "${ROOT}" grep -I --untracked "$@"
}

exceptions_of() {
	printf '%s\n' "${EXCEPTIONS}" | awk -F'|' -v kind="$1" '$1 == kind { print $2 }'
}

# `git grep` exits 1 when nothing matches; that is a clean tree here, any other status is a failure to look.
search() {
	local status=0
	grep_tree "$@" || status=$?
	if (( status > 1 )); then
		printf 'Could not scan %s (git grep exited %d)\n' "${ROOT}" "${status}" >&2
		exit 2
	fi
}

# Rule 1: path:line: text of every line with the word, and every file name with it.
search -n -i -e "${WORD}" > "${WORK}/word-lines.txt"
git -C "${ROOT}" ls-files --cached --others --exclude-standard | grep -i -e "${WORD}" > "${WORK}/word-names.txt" || true

# Rule 2: path:line:token of every task-like name outside the skipped file types.
search -n -o -w -E "${TASK_PATTERN}" -- . ':!*.kt' ':!*.kts' ':!*.gradle' ':!*.swift' ':!*.json' ':!*.xml' \
	> "${WORK}/task-hits.txt"

# Rule 3: path:line:text of every line with a retired identifier (fixed strings, so a key that embeds one is found).
: > "${WORK}/retired-hits.txt"
for identifier in "${RETIRED[@]}"; do
	search -n -F -e "${identifier}" | sed "s/^\([^:]*:[0-9]*\):.*/\1:${identifier}/" >> "${WORK}/retired-hits.txt"
done

# This file spells the tokens it excepts: its own lines are not uses of them.
grep -v "^${SELF_PATH}:" "${WORK}/task-hits.txt" > "${WORK}/task-hits-others.txt" || true

# The tasks that exist: every quoted name of those families in a build file.
search -h -o -E '"(e2e|verify|sync)[A-Z][A-Za-z0-9]*"' -- '*.gradle.kts' '*.gradle' | tr -d '"' | sort -u > "${WORK}/registered.txt"

issues=0
report() {
	printf '%s\n' "$1" >&2
	issues=1
}

exempt_paths() {
	exceptions_of "$1" | sort -u
}

in_list() {
	local needle="$1" list="$2"
	[[ -n "${list}" ]] && printf '%s\n' "${list}" | grep -qxF -- "${needle}"
}

word_exempt="$(exempt_paths word-path)"
task_exempt="$(exempt_paths task-path)"
token_exempt="$(exceptions_of task-token | sort -u)"

while IFS= read -r line; do
	[[ -n "${line}" ]] || continue
	path="${line%%:*}"
	rest="${line#*:}"
	in_list "${path}" "${word_exempt}" || report "${path}:${rest%%:*}: the retired runner is named here"
done < "${WORK}/word-lines.txt"

while IFS= read -r path; do
	[[ -n "${path}" ]] || continue
	in_list "${path}" "${word_exempt}" || report "${path}: the file name carries the retired runner's name"
done < "${WORK}/word-names.txt"

while IFS= read -r hit; do
	[[ -n "${hit}" ]] || continue
	path="${hit%%:*}"
	rest="${hit#*:}"
	line="${rest%%:*}"
	token="${rest#*:}"
	[[ "${token}" != *'*' ]] || continue
	grep -qxF -- "${token}" "${WORK}/registered.txt" && continue
	in_list "${token}" "${token_exempt}" && continue
	in_list "${path}" "${task_exempt}" && continue
	report "${path}:${line}: ${token} is not a Gradle task registered in any build file"
done < "${WORK}/task-hits.txt"

retired_exempt="$(exempt_paths retired-path)"
while IFS= read -r hit; do
	[[ -n "${hit}" ]] || continue
	path="${hit%%:*}"
	rest="${hit#*:}"
	in_list "${path}" "${retired_exempt}" || report "${path}:${rest%%:*}: ${rest#*:} is an identifier of the retired harness"
done < "${WORK}/retired-hits.txt"

# Stale exceptions: each one has to be hiding something.
while IFS= read -r path; do
	[[ -n "${path}" ]] || continue
	awk -F: -v path="${path}" '$1 == path { found = 1 } END { exit found ? 0 : 1 }' "${WORK}/word-lines.txt" \
		|| report "stale exception: ${path} no longer names the retired runner (word-path)"
done < <(exceptions_of word-path)
while IFS= read -r path; do
	[[ -n "${path}" ]] || continue
	awk -F: -v path="${path}" '$1 == path && $3 !~ /\*$/ { found = 1 } END { exit found ? 0 : 1 }' "${WORK}/task-hits.txt" \
		|| report "stale exception: ${path} no longer names an unregistered task (task-path)"
done < <(exceptions_of task-path)
while IFS= read -r token; do
	[[ -n "${token}" ]] || continue
	awk -F: -v token="${token}" '$3 == token { found = 1 } END { exit found ? 0 : 1 }' "${WORK}/task-hits-others.txt" \
		|| report "stale exception: ${token} appears in no file (task-token)"
done < <(exceptions_of task-token)
while IFS= read -r path; do
	[[ -n "${path}" ]] || continue
	awk -F: -v path="${path}" '$1 == path { found = 1 } END { exit found ? 0 : 1 }' "${WORK}/retired-hits.txt" \
		|| report "stale exception: ${path} no longer names a retired identifier (retired-path)"
done < <(exceptions_of retired-path)

if (( issues > 0 )); then
	printf 'E2E vocabulary: not clean. The exceptions are the list at the top of %s.\n' "$(basename "$0")" >&2
	exit 1
fi
printf 'E2E vocabulary: clean (%d registered task names, %d declared exceptions)\n' \
	"$(wc -l < "${WORK}/registered.txt" | tr -d ' ')" "$(printf '%s\n' "${EXCEPTIONS}" | wc -l | tr -d ' ')"
