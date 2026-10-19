#!/usr/bin/env bash
# Verifies that every domain engine and every use case added on this branch has its test (verifyEngineAndUseCaseTests).
#
# - Engines: each type declared at top level in */src/commonMain/**/domain/engine/*.kt (not private) needs a test file
#   named <Type>Test.kt somewhere in the repository's test source sets.
# - Use cases: each *UseCase.kt under a domain/usecase package that does not exist in the branch base needs a test file
#   that names it (a file <UseCase>Test.kt, or a grouped test that constructs it). Existing use cases are not checked
#   here: their coverage is the module's own concern.
# The semantic generality fixtures under config/semgrep/generality are not product code and are excluded.
#
# Usage: scripts/verify-engine-and-usecase-tests.sh [base-ref]
#   base-ref: the branch to take the merge-base from. Default: origin/production, or production without an origin.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

BASE_REF="${1:-}"
if [[ -z "$BASE_REF" ]]; then
	if git rev-parse -q --verify refs/remotes/origin/production >/dev/null; then
		BASE_REF="origin/production"
	else
		BASE_REF="production"
	fi
fi

if ! BASE_COMMIT="$(git merge-base HEAD "$BASE_REF" 2>/dev/null)"; then
	echo "FAIL: cannot compute the merge-base of HEAD and ${BASE_REF}; pass -PbaselineRef=<ref>."
	exit 1
fi

status=0

# Test files of the repository, by base name, excluding the generality fixtures.
TEST_FILES="$(git ls-files -co --exclude-standard -- '*Test.kt' | grep -v '^config/semgrep/' | LC_ALL=C sort)"

has_test_named() {
	grep -Eq "(^|/)$1\$" <<<"$TEST_FILES"
}

# Top-level, non-private type names declared in a Kotlin file.
top_level_types() {
	awk '
		/^(public |internal )?((data|sealed|enum|value|abstract|open|annotation|fun) )*(class|object|interface) [A-Za-z0-9_]/ {
			for (i = 1; i < NF; i++) {
				if ($i == "class" || $i == "object" || $i == "interface") {
					name = $(i + 1)
					sub(/[^A-Za-z0-9_].*/, "", name)
					print name
					break
				}
			}
		}
	' "$1" | LC_ALL=C sort -u || true
}

ENGINE_FILES="$(git ls-files -co --exclude-standard -- '*/src/commonMain/*/domain/engine/*.kt' | grep -v '^config/semgrep/' | LC_ALL=C sort || true)"

while IFS= read -r engine_file; do
	[[ -n "$engine_file" ]] || continue
	while IFS= read -r type_name; do
		[[ -n "$type_name" ]] || continue
		if ! has_test_named "${type_name}Test.kt"; then
			echo "FAIL [${engine_file}]: engine type ${type_name} has no test named ${type_name}Test.kt"
			status=1
		fi
	done < <(top_level_types "$engine_file")
done <<<"$ENGINE_FILES"

USECASE_FILES="$(git ls-files -co --exclude-standard -- '*/domain/usecase/*UseCase.kt' '*/domain/usecase/**/*UseCase.kt' | grep -v '^config/semgrep/' | LC_ALL=C sort -u || true)"

while IFS= read -r usecase_file; do
	[[ -n "$usecase_file" ]] || continue
	if git cat-file -e "${BASE_COMMIT}:${usecase_file}" 2>/dev/null; then
		continue
	fi
	use_case_name="$(basename "$usecase_file" .kt)"
	# A use case is tested when a test of the repository names it: a file <UseCase>Test.kt, or a grouped test that
	# constructs or calls it. The name is matched as a whole word in test sources only.
	if ! has_test_named "${use_case_name}Test.kt" && ! git grep -lw --untracked -e "$use_case_name" -- '*/src/*Test/*' '*/src/test/*' >/dev/null 2>&1; then
		echo "FAIL [${usecase_file}]: new use case ${use_case_name} (not in ${BASE_REF}) has no test that names it"
		status=1
	fi
done <<<"$USECASE_FILES"

if [[ $status -eq 0 ]]; then
	engine_count="$(printf '%s\n' "$ENGINE_FILES" | grep -c . || true)"
	echo "OK: every domain engine (${engine_count} files) and every use case new since ${BASE_REF} has its test."
fi

exit $status
