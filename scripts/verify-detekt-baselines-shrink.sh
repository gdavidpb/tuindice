#!/usr/bin/env bash
# Verifies that no detekt baseline grows against the branch base (verifyDetektBaselinesShrink).
#
# A baseline entry is the detekt <ID> of a finding accepted before the rule existed. The rule for the baselines is that
# they only shrink: an entry that is not in the base is a new accepted finding, and the total of entries per file never
# goes up. The one allowed change of an entry is a re-key: detekt builds the ID from the signature of the code, so a
# finding whose signature changed gets a new ID although it is the same finding. Those pairs are declared in
# config/detekt/baseline-rekeys.txt as "old => new", one per line, and the reviewer reads them in the diff. A new ID
# is accepted only when its pair is declared and the old ID disappears from the same file.
#
# Usage: scripts/verify-detekt-baselines-shrink.sh [base-ref]
#   base-ref: the branch to take the merge-base from. Default: origin/production, or production without an origin.
#   The Gradle task passes it from -PbaselineRef=.

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

REKEYS_FILE="${ROOT_DIR}/config/detekt/baseline-rekeys.txt"

# Baselines tracked in the working tree (the change under verification) and in the base commit.
BASELINES="$(git ls-files -- '*detekt-baseline.xml' 'detekt-baseline.xml' | LC_ALL=C sort)"

if [[ -z "$BASELINES" ]]; then
	echo "FAIL: no detekt-baseline.xml found in the repository."
	exit 1
fi

TMP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/tuindice-detekt-shrink.XXXXXX")"
trap 'rm -rf "$TMP_DIR"' EXIT

# Writes the base copy of every baseline that existed at BASE_COMMIT; a file missing there has an empty base.
while IFS= read -r baseline; do
	[[ -n "$baseline" ]] || continue
	target="${TMP_DIR}/base/${baseline}"
	mkdir -p "$(dirname "$target")"
	if git cat-file -e "${BASE_COMMIT}:${baseline}" 2>/dev/null; then
		git show "${BASE_COMMIT}:${baseline}" >"$target"
	fi
done <<<"$BASELINES"

REKEYS_ARG=""
[[ -f "$REKEYS_FILE" ]] && REKEYS_ARG="$REKEYS_FILE"

BASELINES_FILE="${TMP_DIR}/baselines.txt"
printf '%s\n' "$BASELINES" >"$BASELINES_FILE"

BASE_REF_LABEL="$BASE_REF" BASE_COMMIT="$BASE_COMMIT" python3 - "$ROOT_DIR" "$TMP_DIR" "$BASELINES_FILE" "$REKEYS_ARG" <<'PY'
import os
import sys
import xml.etree.ElementTree as ET
from collections import Counter

root, tmp, baselines_file, rekeys_path = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4]
base_label = os.environ["BASE_REF_LABEL"]
base_commit = os.environ["BASE_COMMIT"]

def ids_of(path):
    if not os.path.exists(path):
        return Counter()
    tree = ET.parse(path)
    return Counter((element.text or "").strip() for element in tree.iter("ID"))

# old => new pairs, one per line; '#' starts a comment.
rekeys = {}
rekeys_lines = []
if rekeys_path and os.path.exists(rekeys_path):
    for number, raw in enumerate(open(rekeys_path, encoding="utf-8"), start=1):
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        if "=>" not in line:
            print(f"FAIL [config/detekt/baseline-rekeys.txt:{number}]: expected 'old => new', got: {line}")
            sys.exit(1)
        old, new = (part.strip() for part in line.split("=>", 1))
        rekeys.setdefault(new, []).append(old)
        rekeys_lines.append(line)

baselines = [line.strip() for line in open(baselines_file, encoding="utf-8") if line.strip()]
failures = []
checked_entries = 0

for baseline in baselines:
    current = ids_of(os.path.join(root, baseline))
    base = ids_of(os.path.join(tmp, "base", baseline))
    checked_entries += sum(current.values())

    if sum(current.values()) > sum(base.values()):
        failures.append(f"[{baseline}] the baseline grew: {sum(base.values())} entries in {base_label} "
                        f"({base_commit[:10]}), {sum(current.values())} now. A baseline only shrinks.")

    added = current - base
    removed = base - current
    for new_id, count in added.items():
        for _ in range(count):
            pair_olds = [old for old in rekeys.get(new_id, []) if removed[old] > 0]
            if pair_olds:
                removed[pair_olds[0]] -= 1
                continue
            failures.append(f"[{baseline}] new entry not in {base_label}: {new_id}")

current_by_baseline = {b: ids_of(os.path.join(root, b)) for b in baselines}
for entry in rekeys_lines:
    old, new = (part.strip() for part in entry.split("=>", 1))
    if not any(new in ids for ids in current_by_baseline.values()):
        failures.append(f"[config/detekt/baseline-rekeys.txt] the new key of a re-key is not in any baseline: {new}")

if failures:
    print("FAIL: detekt baselines grew against " + base_label + " (" + base_commit[:10] + "):")
    for failure in failures:
        print("  " + failure)
    sys.exit(1)

print(f"OK: detekt baselines only shrink against {base_label} ({len(baselines)} baselines, {checked_entries} entries, "
      f"{len(rekeys_lines)} declared re-keys).")
PY
