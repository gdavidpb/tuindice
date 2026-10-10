#!/usr/bin/env bash
# Verifies that the versioned scenario catalog artifacts match what the scenarios generate.
#
# `:scenarios:testAndroidHostTest` (CatalogExportTest) writes the generated files under
# scenarios/build/e2e/catalog; `./gradlew syncE2eArtifacts` copies them into the repo.
# This check compares the two without mutating the tree.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GENERATED_DIR="${ROOT_DIR}/scenarios/build/e2e/catalog"

# <generated file name>:<versioned path relative to the repo root>
PAIRS=(
	"scenarios.json:e2e/catalog/scenarios.json"
	"ScenarioTests.generated.swift:iosApp/UITests/Generated/ScenarioTests.generated.swift"
)

status=0

for pair in "${PAIRS[@]}"; do
	generated="${GENERATED_DIR}/${pair%%:*}"
	versioned="${ROOT_DIR}/${pair#*:}"

	if [[ ! -f "$generated" ]]; then
		echo "FAIL: missing generated file: $generated (run :scenarios:testAndroidHostTest)"
		status=1
		continue
	fi

	if [[ ! -f "$versioned" ]]; then
		echo "FAIL: missing versioned file: ${pair#*:}"
		echo "Run ./gradlew syncE2eArtifacts and commit the result."
		status=1
		continue
	fi

	if ! cmp -s "$generated" "$versioned"; then
		echo "FAIL: ${pair#*:} differs from the scenarios"
		diff -u "$versioned" "$generated" || status=1
		echo "Run ./gradlew syncE2eArtifacts and commit the result."
		status=1
	fi
done

[[ "$status" -eq 0 ]] && echo "OK: e2e artifacts are fresh"

exit "$status"
