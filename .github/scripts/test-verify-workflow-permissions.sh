#!/usr/bin/env bash
# Fixtures of verify-workflow-permissions.sh: the shape it accepts and each way a workflow can break its rules.
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VERIFY="${SCRIPT_DIR}/verify-workflow-permissions.sh"
WORK="$(mktemp -d "${TMPDIR:-/tmp}/tuindice-workflow-permissions.XXXXXX")"
trap 'rm -rf "$WORK"' EXIT

write_workflow() {
	local name="$1"
	local top_permissions="$2"
	local shared_permissions="$3"
	local build_permissions="$4"
	local build_checkout_with="$5"

	{
		printf 'name: fixture\non:\n  pull_request:\n\n'
		printf '%b' "$top_permissions"
		printf '\njobs:\n  shared:\n    runs-on: ubuntu-latest\n'
		printf '%b' "$shared_permissions"
		printf '    steps:\n      - name: Check out repository\n        uses: actions/checkout@abc # v4\n        with:\n          fetch-depth: 0\n'
		printf '      - name: Reuse\n        run: echo reuse\n\n'
		printf '  build:\n    runs-on: ubuntu-latest\n    needs:\n      - shared\n'
		printf '%b' "$build_permissions"
		printf '    steps:\n      - name: Check out repository\n        uses: actions/checkout@abc # v4\n'
		printf '%b' "$build_checkout_with"
		printf '      - name: Build\n        run: echo build\n'
	} >"${WORK}/${name}.yml"
}

GOOD_TOP='permissions:\n  contents: read\n'
SHARED_READS='    permissions:\n      contents: read\n      statuses: read\n'
SHARED_WRITES='    permissions:\n      contents: read\n      statuses: write\n'
BUILD_READS='    permissions:\n      contents: read\n'
NO_PERSIST='        with:\n          ref: x\n          persist-credentials: false\n'

expect() {
	local name="$1"
	local expected="$2"
	local message="${3:-}"
	local output status

	set +e
	output="$(bash "$VERIFY" "${WORK}/${name}.yml" --credentials shared 2>&1)"
	status=$?
	set -e
	if [[ "$expected" == "pass" && "$status" != "0" ]]; then
		printf 'Fixture %s was expected to pass but failed:\n%s\n' "$name" "$output" >&2
		exit 1
	fi
	if [[ "$expected" == "fail" ]]; then
		if [[ "$status" == "0" ]]; then
			printf 'Fixture %s was expected to fail but passed.\n' "$name" >&2
			exit 1
		fi
		if [[ "$output" != *"$message"* ]]; then
			printf 'Fixture %s failed without saying "%s":\n%s\n' "$name" "$message" "$output" >&2
			exit 1
		fi
	fi
}

write_workflow good "$GOOD_TOP" "$SHARED_READS" "$BUILD_READS" "$NO_PERSIST"
expect good pass

write_workflow top-statuses 'permissions:\n  contents: read\n  statuses: write\n' "$SHARED_READS" "$BUILD_READS" "$NO_PERSIST"
expect top-statuses fail "top-level permissions grant statuses: write"

write_workflow no-top "" "$SHARED_READS" "$BUILD_READS" "$NO_PERSIST"
expect no-top fail "no top-level permissions"

write_workflow job-without-permissions "$GOOD_TOP" "$SHARED_READS" "" "$NO_PERSIST"
expect job-without-permissions fail "job build declares no permissions"

write_workflow job-writes-statuses "$GOOD_TOP" "$SHARED_READS" '    permissions:\n      contents: read\n      statuses: write\n' "$NO_PERSIST"
expect job-writes-statuses fail "job build has statuses: write; no job of this workflow may write commit statuses"

# Option C: no job of the workflow of a pull request writes commit statuses, not even the one that reads them.
write_workflow shared-writes-statuses "$GOOD_TOP" "$SHARED_WRITES" "$BUILD_READS" "$NO_PERSIST"
expect shared-writes-statuses fail "job shared has statuses: write; no job of this workflow may write commit statuses"

write_workflow checkout-keeps-token "$GOOD_TOP" "$SHARED_READS" "$BUILD_READS" '        with:\n          fetch-depth: 0\n'
expect checkout-keeps-token fail "job build: an actions/checkout step does not set persist-credentials: false"

write_workflow checkout-without-with "$GOOD_TOP" "$SHARED_READS" "$BUILD_READS" ""
expect checkout-without-with fail "job build: an actions/checkout step does not set persist-credentials: false"

# The empty permission set is a declaration too.
write_workflow empty-permissions "$GOOD_TOP" "$SHARED_READS" '    permissions: {}\n' "$NO_PERSIST"
expect empty-permissions pass

printf 'Workflow permission fixtures passed.\n'
