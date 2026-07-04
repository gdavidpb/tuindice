#!/usr/bin/env bash
# Runs the Semgrep architecture ruleset (config/semgrep/rules) over the repo.
#
# The rules encode the reference architecture documented in README.md
# ("Capas por feature", "Límites KMP", "Convenciones de Koin", "Flujo estándar")
# and in .codex/skills/implement-tuindice-module. Each rule file ships with a
# same-named .kt fixture validated via `semgrep --test`, and the synthetic
# module in config/semgrep/generality proves every rule fires on layouts and
# module names that do not exist in the repo (no fine-tuning to current code).
# Scan exclusions live in .semgrepignore at the repo root.
#
# Usage:
#   scripts/semgrep-architecture.sh              # validate + fixtures + generality + scan
#   scripts/semgrep-architecture.sh test         # validate rules + run fixtures only
#   scripts/semgrep-architecture.sh generality   # synthetic-module generality check only
#   scripts/semgrep-architecture.sh scan         # scan only
set -euo pipefail

cd "$(dirname "$0")/.."

REPO_ROOT="$(pwd)"
RULES_DIR="config/semgrep/rules"
GENERALITY_DIR="config/semgrep/generality"
MODE="${1:-all}"

command -v semgrep >/dev/null 2>&1 || {
	echo "[semgrep] semgrep CLI not found (brew install semgrep / pipx install semgrep)" >&2
	exit 1
}

run_rule_tests() {
	echo "[semgrep] validating rule syntax"
	semgrep --validate --config "$RULES_DIR" --quiet
	echo "[semgrep] running rule fixtures"
	semgrep --test "$RULES_DIR"
}

run_generality_check() {
	echo "[semgrep] running generality check against synthetic module"

	local expected_ids
	expected_ids="$(grep -h "^  - id:" "$RULES_DIR"/*.yaml | sed 's/.*id: *//' | sort -u | tr '\n' ',')"

	# El módulo sintético se copia fuera del repo para escanearlo: dentro del
	# árbol git, el .semgrepignore de la raíz (config/semgrep/) lo excluiría.
	local staging_dir
	staging_dir="$(mktemp -d "${TMPDIR:-/tmp}/tuindice-semgrep-generality.XXXXXX")"
	trap 'rm -rf "$staging_dir"' RETURN
	cp -R "$GENERALITY_DIR"/. "$staging_dir/"

	(cd "$staging_dir" && semgrep --config "$REPO_ROOT/$RULES_DIR" --json --quiet . 2>/dev/null) \
		| EXPECTED_RULE_IDS="$expected_ids" python3 -c '
import json, os, sys

data = json.loads(sys.stdin.read() or "{\"results\": []}")
expected = set(filter(None, os.environ["EXPECTED_RULE_IDS"].split(",")))

fired = set()
control_findings = []
CONTROL_SUFFIXES = ("Draft.kt", "FakeDispatchers.kt", "IosPlatformModule.kt")
for result in data["results"]:
    rule_id = result["check_id"].split(".")[-1]
    fired.add(rule_id)
    if result["path"].endswith(CONTROL_SUFFIXES):
        path = result["path"]
        line = result["start"]["line"]
        control_findings.append(rule_id + " @ " + path + ":" + str(line))

missing = sorted(expected - fired)
if missing or control_findings:
    print("[semgrep] generality check FAILED")
    if missing:
        print("  reglas que no dispararon sobre el módulo sintético: " + ", ".join(missing))
    for finding in control_findings:
        print("  control negativo con finding (debería estar exento): " + finding)
    sys.exit(1)

print(f"[semgrep] generality check OK ({len(expected)} reglas disparan; controles negativos limpios)")
'
}

run_scan() {
	echo "[semgrep] scanning repository"
	semgrep --config "$RULES_DIR" --error --quiet .
	echo "[semgrep] scan clean"
}

case "$MODE" in
	all)
		run_rule_tests
		run_generality_check
		run_scan
		;;
	test)
		run_rule_tests
		;;
	generality)
		run_generality_check
		;;
	scan)
		run_scan
		;;
	*)
		echo "Usage: $0 [all|test|generality|scan]" >&2
		exit 1
		;;
esac
