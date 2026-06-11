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

registered_modules=$(grep -oE '":[a-z]+"' "$ROOT_DIR/settings.gradle.kts" | tr -d '":' | sort)

for module in $registered_modules; do
	expected=$(expected_for "$module")

	if [[ -z "$expected" ]]; then
		echo "FAIL [$module]: registered in settings.gradle.kts but missing from ${GRAPH_FILE}"
		status=1
		continue
	fi

	[[ "$expected" == "-" ]] && expected=""

	actual=$(actual_for "$ROOT_DIR/$module/build.gradle.kts")

	if [[ "$actual" != "$expected" ]]; then
		echo "FAIL [$module]: module dependencies drifted from the agreed graph"
		echo "  expected: ${expected:-<none>}"
		echo "  actual:   ${actual:-<none>}"
		status=1
	fi
done

for module in $(echo "$EXPECTED_GRAPH" | awk -F= 'NF { print $1 }'); do
	if ! echo "$registered_modules" | grep -qx "$module"; then
		echo "FAIL [$module]: present in ${GRAPH_FILE} but not registered in settings.gradle.kts"
		status=1
	fi
done

if [[ $status -eq 0 ]]; then
	echo "OK: module dependency graph matches the agreed boundaries ($(echo "$registered_modules" | wc -l | tr -d ' ') modules)."
fi

exit $status
