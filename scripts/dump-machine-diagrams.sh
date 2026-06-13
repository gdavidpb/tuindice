#!/usr/bin/env bash
# Renders every screen machine's transition table as a Mermaid state diagram.
#
# The diagrams are test-only tooling, not production code: each
# `<Screen>StateMachineContractTest` prints its machine via testkit's
# `exportToMermaid` (a pure function over `machine.table`), and this script runs
# those export tests and lifts the diagrams out of the Gradle test reports into
# build/diagrams/<machine>.mmd (one file per machine, named after its wrapper).
#
# Paste a .mmd into https://mermaid.live, a ```mermaid block on GitHub, or an IDE
# Mermaid preview to see it rendered. build/ is gitignored — these are ephemeral
# artifacts, regenerated on demand.
#
# Usage: scripts/dump-machine-diagrams.sh [module ...]
#   No args: every machine-bearing module. Pass module names (e.g. "summary
#   record") to scope the run.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

# Modules whose *StateMachineContractTest exports diagrams. The export test runs
# on testAndroidHostTest (pure table read, no resources), so the host gate serves
# every diagram even for screens whose walks are iOS-only.
DEFAULT_MODULES=(auth summary pensum record subjects evaluations wizard maincore about enrollmentproof)
MODULES=("$@")
[[ ${#MODULES[@]} -eq 0 ]] && MODULES=("${DEFAULT_MODULES[@]}")

OUT_DIR="${ROOT_DIR}/build/diagrams"
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

GRADLE_TASKS=()
for module in "${MODULES[@]}"; do
	GRADLE_TASKS+=(":${module}:testAndroidHostTest" "--tests" "*ToMermaid")
done

echo "Running export tests for: ${MODULES[*]}"
# --rerun-tasks: the export tests are usually UP-TO-DATE, but we always want fresh
# system-out to extract from.
./gradlew "${GRADLE_TASKS[@]}" --rerun-tasks -q

echo "Extracting diagrams into build/diagrams/ ..."
python3 - "$OUT_DIR" "${MODULES[@]}" <<'PYEOF'
import glob
import re
import sys
import xml.etree.ElementTree as ET

out_dir = sys.argv[1]
modules = sys.argv[2:]
written = 0

for module in modules:
    pattern = f"{module}/build/test-results/testAndroidHostTest/TEST-*StateMachineContractTest.xml"
    for report in glob.glob(pattern):
        system_out = ET.parse(report).getroot().findtext("system-out") or ""
        # One report's stdout may hold several diagrams (e.g. Record + CST).
        for body in system_out.split("stateDiagram-v2")[1:]:
            diagram = "stateDiagram-v2" + body
            # Each diagram ends at the wrapper's closing brace on its own line.
            close = re.search(r"^\}", diagram, re.MULTILINE)
            if close:
                diagram = diagram[: close.end()]
            name_match = re.search(r"^state (\w+) \{", diagram, re.MULTILINE)
            name = name_match.group(1) if name_match else f"machine_{written}"
            with open(f"{out_dir}/{name}.mmd", "w") as handle:
                handle.write(diagram.strip() + "\n")
            print(f"  build/diagrams/{name}.mmd")
            written += 1

if written == 0:
    raise SystemExit("No diagrams found — did the export tests run?")
print(f"{written} diagram(s) written to build/diagrams/")
PYEOF
