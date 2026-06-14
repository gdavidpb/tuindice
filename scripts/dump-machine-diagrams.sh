#!/usr/bin/env bash
# Renders every screen machine's transition table as a Mermaid state diagram.
#
# The diagrams are test-only tooling, not production code: each
# `<Screen>StateMachineContractTest` prints its machine via testkit's
# `exportToMermaid` (a pure function over `machine.table`), and this script runs
# those export tests and lifts the diagrams out of the Gradle test reports into
# build/diagrams/<machine>.mmd (one file per machine, named from each diagram's
# `%% machine: <name>` comment).
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

def is_diagram_line(line):
    # The flat diagram is comments, state declarations, transitions and blanks.
    # The first line that fits none of those marks the end of the diagram in stdout.
    stripped = line.strip()
    return (
        stripped == ""
        or stripped.startswith("%%")
        or stripped.startswith("state ")
        or stripped.startswith("[*]")
        or "-->" in stripped
    )

for module in modules:
    pattern = f"{module}/build/test-results/testAndroidHostTest/TEST-*StateMachineContractTest.xml"
    for report in glob.glob(pattern):
        system_out = ET.parse(report).getroot().findtext("system-out") or ""
        # One report's stdout may hold several diagrams (e.g. Record + CST).
        for body in system_out.split("stateDiagram-v2")[1:]:
            # The diagram is flat (no closing brace): keep diagram-shaped lines until
            # the first line of unrelated stdout.
            lines = ["stateDiagram-v2"]
            started = False
            for line in body.splitlines():
                if not is_diagram_line(line):
                    break
                # Drop the blank that splitting on "stateDiagram-v2" leaves at the
                # front; keep blanks once the diagram body has started.
                if not started and line.strip() == "":
                    continue
                started = True
                lines.append(line)
            diagram = "\n".join(lines).strip()
            # Each diagram self-identifies via a `%% machine: <name>` comment.
            name_match = re.search(r"^%% machine: (\w+)", diagram, re.MULTILINE)
            name = name_match.group(1) if name_match else f"machine_{written}"
            with open(f"{out_dir}/{name}.mmd", "w") as handle:
                handle.write(diagram + "\n")
            print(f"  build/diagrams/{name}.mmd")
            written += 1

if written == 0:
    raise SystemExit("No diagrams found — did the export tests run?")
print(f"{written} diagram(s) written to build/diagrams/")
PYEOF
