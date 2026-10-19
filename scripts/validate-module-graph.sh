#!/usr/bin/env bash
# Validates the module dependency graph against the agreed architecture boundaries.
#
# Source of truth: scripts/module-graph.txt, mirrored in README.md
# ("Dependencias entre módulos"). CI scoping (.github/scripts/common.sh) derives
# its module closures from the same file. If a module boundary changes, update
# the graph file and the README in the same change.
#
# Rules encoded here:
# - Only main-scope dependencies count (test source sets such as commonTest,
#   androidHostTest, and iosTest are ignored; ":testkit" only appears there).
# - Every module registered in settings.gradle.kts must have an entry in the
#   graph file, so adding a module forces an explicit boundary agreement.
# - The iOS framework cache key (.github/scripts/ios-framework-cache-key.sh) must hash the build file and the sources
#   of every module maincore links, directly or transitively, so a change in any of them invalidates the cache.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GRAPH_FILE="${ROOT_DIR}/scripts/module-graph.txt"

[[ -f "$GRAPH_FILE" ]] || {
	echo "FAIL: missing module graph file: $GRAPH_FILE"
	exit 1
}

EXPECTED_GRAPH="$(grep -vE '^[[:space:]]*(#|$)' "$GRAPH_FILE")"

expected_for() {
	echo "$EXPECTED_GRAPH" | awk -F= -v m="$1" '$1 == m { print $2 }'
}

# Extracts main-scope project(":x") dependencies from a build.gradle.kts.
# Tracks `val <sourceSet> by getting` blocks and skips test source sets.
actual_for() {
	awk '
		/val [A-Za-z0-9]+ by getting/ {
			line = $0
			sub(/.*val /, "", line)
			sub(/ by getting.*/, "", line)
			current = line
		}
		/project\(":[a-z]+"\)/ {
			if (tolower(current) !~ /test/) {
				line = $0
				sub(/.*project\(":/, "", line)
				sub(/"\).*/, "", line)
				print ":" line
			}
		}
	' "$1" | sort -u | tr '\n' ' ' | sed 's/ $//'
}

status=0

# Every quoted ":name" of settings.gradle.kts is a module. The graph, the E2E fingerprint and the change detector only
# read lowercase letters (":[a-z]+"), so a module named otherwise would silently stay out of all three: refuse it.
registered_tokens=$(grep -oE '":[^"]*"' "$ROOT_DIR/settings.gradle.kts" | tr -d '"' || true)
for token in $registered_tokens; do
	if [[ ! "$token" =~ ^:[a-z]+$ ]]; then
		echo "FAIL [$token]: settings.gradle.kts includes a module the graph tooling cannot read (names must match :[a-z]+)"
		status=1
	fi
done
registered_modules=$(printf '%s\n' "$registered_tokens" | grep -E '^:[a-z]+$' | tr -d ':' | sort || true)

for module in $registered_modules; do
	expected=$(expected_for "$module")

	if [[ -z "$expected" ]]; then
		echo "FAIL [$module]: registered in settings.gradle.kts but missing from ${GRAPH_FILE}"
		status=1
		continue
	fi

	[[ "$expected" == "-" ]] && expected=""

	unreadable_dependencies=$(grep -oE 'project\(":[^"]*"\)' "$ROOT_DIR/$module/build.gradle.kts" | grep -vE '^project\(":[a-z]+"\)$' || true)
	if [[ -n "$unreadable_dependencies" ]]; then
		echo "FAIL [$module]: depends on a project the graph tooling cannot read (names must match :[a-z]+): $unreadable_dependencies"
		status=1
	fi

	actual=$(actual_for "$ROOT_DIR/$module/build.gradle.kts")

	if [[ "$actual" != "$expected" ]]; then
		echo "FAIL [$module]: module dependencies drifted from the agreed graph"
		echo "  expected: ${expected:-<none>}"
		echo "  actual:   ${actual:-<none>}"
		status=1
	fi
done

for module in $(echo "$EXPECTED_GRAPH" | awk -F= 'NF { print $1 }'); do
	if ! grep -qx "$module" <<<"$registered_modules"; then
		echo "FAIL [$module]: present in ${GRAPH_FILE} but not registered in settings.gradle.kts"
		status=1
	fi
done

# The modules maincore links: its direct dependencies and, transitively, theirs. The cache key must name each of them.
CACHE_KEY_SCRIPT="${ROOT_DIR}/.github/scripts/ios-framework-cache-key.sh"
if [[ ! -f "$CACHE_KEY_SCRIPT" ]]; then
	echo "FAIL [ios-framework-cache-key]: missing ${CACHE_KEY_SCRIPT}"
	status=1
else
	linked_closure=""
	pending="$(expected_for maincore | tr ' ' '\n' | tr -d ':' | grep -vxF -- '-' || true)"
	while [[ -n "$pending" ]]; do
		next=""
		for module in $pending; do
			case " $linked_closure " in
				*" $module "*) continue ;;
			esac
			linked_closure="$linked_closure $module"
			next="$next $(expected_for "$module" | tr ' ' '\n' | tr -d ':' | grep -vxF -- '-' || true)"
		done
		pending="$(printf '%s\n' $next | grep -v '^$' || true)"
	done

	for module in maincore $linked_closure; do
		for cache_input in "${module}/build.gradle.kts" "${module}/src"; do
			if ! grep -qF "\"${cache_input}\"" "$CACHE_KEY_SCRIPT"; then
				echo "FAIL [ios-framework-cache-key]: ${module} is linked into maincore.framework but ${cache_input} is not hashed by ${CACHE_KEY_SCRIPT#"${ROOT_DIR}"/}"
				status=1
			fi
		done
	done
fi

if [[ $status -eq 0 ]]; then
	echo "OK: module dependency graph matches the agreed boundaries ($(echo "$registered_modules" | wc -l | tr -d ' ') modules) and the iOS framework cache key covers maincore's closure."
fi

exit $status
