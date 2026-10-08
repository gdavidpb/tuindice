#!/usr/bin/env bash
# verifyE2eHarness: shell syntax, Python syntax, the unit tests of the E2E harness and its line budgets.
# No devices, no network, no real gh/adb/xcrun; temporary state lives under a temp directory.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
cd "${REPO_ROOT}"

export PYTHONDONTWRITEBYTECODE=1

shell_failures=0
shell_checked=0
while IFS= read -r -d '' file; do
	shell_checked=$((shell_checked + 1))
	if ! bash -n "${file}"; then
		printf 'Shell syntax error: %s\n' "${file}" >&2
		shell_failures=$((shell_failures + 1))
	fi
done < <(
	{
		find e2e -type f \( -name '*.sh' -o -path '*/fake_bin/*' \) -print0
		find .codex/skills -path '*/scripts/*' -type f -name '*.sh' -print0
	} 2>/dev/null
)
printf 'shell syntax: %d files checked, %d failures\n' "${shell_checked}" "${shell_failures}"
if (( shell_failures > 0 )); then
	exit 1
fi

# Compile every Python file with the default interpreter and, when the system one is
# older (the harness targets Python 3.9), with that one too; the unit tests run on each of them.
interpreters=(python3)
if [[ -x /usr/bin/python3 && "$(command -v python3)" != "/usr/bin/python3" ]]; then
	interpreters+=(/usr/bin/python3)
fi
python_files=()
while IFS= read -r -d '' file; do
	python_files+=("${file}")
done < <(
	{
		find e2e -type f -name '*.py' -print0
		find .codex/skills -path '*/scripts/*' -type f -name '*.py' -print0
	} 2>/dev/null
)
for interpreter in "${interpreters[@]}"; do
	"${interpreter}" - "${python_files[@]}" <<'PY'
import sys

failures = 0
for path in sys.argv[1:]:
    try:
        with open(path) as handle:
            compile(handle.read(), path, "exec")
    except SyntaxError as error:
        failures += 1
        print("Python syntax error in %s: %s" % (path, error), file=sys.stderr)
print("python syntax (%s): %d files checked, %d failures" % (sys.version.split()[0], len(sys.argv) - 1, failures))
sys.exit(1 if failures else 0)
PY
done

# Tests that need a tool only a Mac has skip elsewhere with their reason (support.requires_macos). Off macOS the
# skips are counted and said out loud; on macOS (the CI job that runs this whole) any skip is a failure.
unittest_log="$(mktemp "${TMPDIR:-/tmp}/e2e-harness-unittest.XXXXXX")"
trap 'rm -f "${unittest_log}"' EXIT
# The suite runs on every interpreter of the list: the one the PATH finds is the one that runs the certification (gradle and the
# adapters call `python3`), and the system Python (3.9) is the oldest the harness targets: an API newer than 3.9 would pass on the
# first and fail for whoever runs the second, and a difference in behavior would pass on the second and fail in the evidence run.
unittest_status=0
for suite_python in "${interpreters[@]}"; do
	printf 'unit tests: %s\n' "$("${suite_python}" --version 2>&1)"
	set +e
	"${suite_python}" -m unittest discover -s e2e/tools/tests -p 'test_*.py' -v 2>&1 | tee -a "${unittest_log}"
	unittest_status="${PIPESTATUS[0]}"
	set -e
	if (( unittest_status != 0 )); then
		exit "${unittest_status}"
	fi
done
skipped="$(awk '/ \.\.\. skipped /{ n++ } END { print n + 0 }' "${unittest_log}")"
if [[ "$(uname -s)" == "Darwin" ]]; then
	if (( skipped > 0 )); then
		printf 'On macOS no harness test may skip, but %d did (see the skipped lines above).\n' "${skipped}" >&2
		exit 1
	fi
else
	printf 'NOT RUN HERE: %d harness tests need macOS and were skipped with their reason (the lines above that say "skipped").\n' "${skipped}"
	printf 'The macOS job (e2e-harness-preflight) runs them.\n'
fi

# The fingerprint covers what it must (verifier) and behaves as specified (its own tests).
bash e2e/tools/verify/verify-e2e-fingerprint-coverage.sh
bash e2e/tools/tests/test-fingerprint.sh
bash .github/scripts/test-fingerprint-detector-parity.sh

# The harness, the skill and its runbook stay within the budgets of e2e/tools/verify/line-budgets.env.
bash e2e/tools/verify/verify-line-budgets.sh

# The versioned files use the vocabulary of the system that exists: no name of the retired runner and no Gradle
# task that no build file registers (exceptions are listed, with their reasons, in the script).
bash e2e/tools/verify/verify-e2e-vocabulary.sh
