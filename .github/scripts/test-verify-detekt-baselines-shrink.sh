#!/usr/bin/env bash
# Fixtures of verify-detekt-baselines-shrink.sh: a new detekt entry is accepted only when a chain of declared re-keys leads
# from an entry that disappears from the same baseline to it.
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GATE="${SCRIPT_DIR}/../../scripts/verify-detekt-baselines-shrink.sh"
WORK="$(mktemp -d "${TMPDIR:-/tmp}/tuindice-detekt-shrink-test.XXXXXX")"
trap 'rm -rf "$WORK"' EXIT

# A baseline with one entry per argument.
baseline_with() {
	local entry

	printf '<?xml version="1.0" encoding="UTF-8"?>\n<SmellBaseline>\n  <CurrentIssues>\n'
	for entry in "$@"; do
		printf '    <ID>%s</ID>\n' "$entry"
	done
	printf '  </CurrentIssues>\n</SmellBaseline>\n'
}

# A repository whose base commit (branch production) holds the baseline with the entry $2. The working tree then holds the
# entry $3 and the re-keys $4. The gate is copied into the fixture, so its ROOT_DIR is the fixture.
make_fixture() {
	local name="$1"
	local base_entry="$2"
	local head_entry="$3"
	local rekeys="$4"
	local repo="${WORK}/${name}"

	mkdir -p "${repo}/scripts" "${repo}/config/detekt" "${repo}/module"
	cp "$GATE" "${repo}/scripts/verify-detekt-baselines-shrink.sh"
	git -C "$repo" init -q
	git -C "$repo" symbolic-ref HEAD refs/heads/production
	baseline_with "$base_entry" >"${repo}/module/detekt-baseline.xml"
	git -C "$repo" add -A
	git -C "$repo" -c user.name=fixture -c user.email=fixture@example.invalid -c commit.gpgsign=false commit -q -m base

	baseline_with "$head_entry" >"${repo}/module/detekt-baseline.xml"
	printf '%s' "$rekeys" >"${repo}/config/detekt/baseline-rekeys.txt"
	printf '%s\n' "$repo"
}

run_gate() {
	local repo="$1"

	bash "${repo}/scripts/verify-detekt-baselines-shrink.sh" production 2>&1
}

expect_accepted() {
	local label="$1"
	local repo="$2"
	local output

	if ! output="$(run_gate "$repo")"; then
		printf 'Expected the gate to accept %s, got:\n%s\n' "$label" "$output" >&2
		exit 1
	fi
	if [[ "$output" != OK:* ]]; then
		printf 'Expected the gate to print OK for %s, got:\n%s\n' "$label" "$output" >&2
		exit 1
	fi
}

expect_rejected() {
	local label="$1"
	local repo="$2"
	local reason="$3"
	local output

	if output="$(run_gate "$repo")"; then
		printf 'Expected the gate to reject %s, got:\n%s\n' "$label" "$output" >&2
		exit 1
	fi
	if [[ "$output" != *"$reason"* ]]; then
		printf 'Expected the gate to report "%s" for %s, got:\n%s\n' "$reason" "$label" "$output" >&2
		exit 1
	fi
}

# A is removed and C is added: A => B and B => C declare the chain, so C is the same finding and is accepted.
chain="$(make_fixture chain A C $'A => B\nB => C\n')"
expect_accepted "a re-key chain A => B => C" "$chain"

# A is removed and C is added with no declared pair: no removed entry leads to C, so it is a new accepted finding.
orphan="$(make_fixture orphan A C '')"
expect_rejected "a new entry C with no origin" "$orphan" "new entry not in production: C"

printf 'Detekt baseline shrink fixtures passed.\n'
