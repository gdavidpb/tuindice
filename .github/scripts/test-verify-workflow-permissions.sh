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

# ZD-2: the forms a line scan for `statuses: write` used to miss. Each one is a way to grant more than the rules allow.
write_workflow job-write-all "$GOOD_TOP" "$SHARED_READS" '    permissions: write-all\n' "$NO_PERSIST"
expect job-write-all fail "job build permissions have the inline value write-all"

write_workflow top-write-all 'permissions: write-all\n' "$SHARED_READS" "$BUILD_READS" "$NO_PERSIST"
expect top-write-all fail "top-level permissions have the inline value write-all"

write_workflow job-inline-map "$GOOD_TOP" "$SHARED_READS" '    permissions: { statuses: write }\n' "$NO_PERSIST"
expect job-inline-map fail "job build permissions have the inline value { statuses: write }"

write_workflow top-inline-map 'permissions: { statuses: write }\n' "$SHARED_READS" "$BUILD_READS" "$NO_PERSIST"
expect top-inline-map fail "top-level permissions have the inline value { statuses: write }"

write_workflow job-quoted-write "$GOOD_TOP" "$SHARED_READS" '    permissions:\n      contents: read\n      statuses: "write"\n' "$NO_PERSIST"
expect job-quoted-write fail "job build has statuses: write"

write_workflow job-single-quoted-write "$GOOD_TOP" "$SHARED_READS" "    permissions:\\n      contents: read\\n      statuses: 'write'\\n" "$NO_PERSIST"
expect job-single-quoted-write fail "job build has statuses: write"

write_workflow top-quoted-write 'permissions:\n  contents: read\n  statuses: "write"\n' "$SHARED_READS" "$BUILD_READS" "$NO_PERSIST"
expect top-quoted-write fail "top-level permissions grant statuses: write"

write_workflow job-wide-indent "$GOOD_TOP" "$SHARED_READS" '    permissions:\n        contents: read\n        statuses: write\n' "$NO_PERSIST"
expect job-wide-indent fail "job build has statuses: write"

write_workflow job-id-with-comment "$GOOD_TOP" "$SHARED_READS" '    permissions:\n      contents: read\n      statuses: write\n' "$NO_PERSIST"
sed -i.bak 's/^  build:$/  build: # builds the thing/' "${WORK}/job-id-with-comment.yml"
expect job-id-with-comment fail "job build has statuses: write"

# ...and the inline values that are fine: read-all and the empty map, with a trailing comment.
write_workflow job-read-all "$GOOD_TOP" "$SHARED_READS" '    permissions: read-all\n' "$NO_PERSIST"
expect job-read-all pass
write_workflow job-empty-map-comment "$GOOD_TOP" "$SHARED_READS" '    permissions: {} # nothing\n' "$NO_PERSIST"
expect job-empty-map-comment pass
write_workflow job-quoted-read "$GOOD_TOP" "$SHARED_READS" '    permissions:\n      contents: read\n      statuses: "read"\n' "$NO_PERSIST"
expect job-quoted-read pass

# ZD-2: with --if-pull-request only the workflows triggered by a pull request are verified, whichever file they are in.
expect_if_pull_request() {
	local name="$1"
	local expected="$2"
	local status

	set +e
	bash "$VERIFY" "${WORK}/${name}.yml" --if-pull-request >/dev/null 2>&1
	status=$?
	set -e
	if [[ "$expected" == "pass" && "$status" != "0" ]] || [[ "$expected" == "fail" && "$status" == "0" ]]; then
		printf 'Fixture %s (--if-pull-request) was expected to %s but exited %s.\n' "$name" "$expected" "$status" >&2
		exit 1
	fi
}
write_workflow new-pr-workflow "$GOOD_TOP" "$SHARED_WRITES" "$BUILD_READS" "$NO_PERSIST"
expect_if_pull_request new-pr-workflow fail
write_workflow new-pr-target-workflow "$GOOD_TOP" "$SHARED_WRITES" "$BUILD_READS" "$NO_PERSIST"
sed -i.bak 's/^  pull_request:$/  pull_request_target:/' "${WORK}/new-pr-target-workflow.yml"
expect_if_pull_request new-pr-target-workflow fail
write_workflow new-inline-trigger "$GOOD_TOP" "$SHARED_WRITES" "$BUILD_READS" "$NO_PERSIST"
sed -i.bak 's/^on:$/on: [push, pull_request]/; /^  pull_request:$/d' "${WORK}/new-inline-trigger.yml"
expect_if_pull_request new-inline-trigger fail
write_workflow push-only-workflow "$GOOD_TOP" "$SHARED_WRITES" "$BUILD_READS" "$NO_PERSIST"
sed -i.bak 's/^  pull_request:$/  push:/' "${WORK}/push-only-workflow.yml"
expect_if_pull_request push-only-workflow pass
# YD-1: pull_request_review also runs the code of the pull request (refs/pull/N/merge).
write_workflow review-trigger-only "$GOOD_TOP" "$SHARED_WRITES" "$BUILD_READS" "$NO_PERSIST"
sed -i.bak 's/^  pull_request:$/  pull_request_review:/' "${WORK}/review-trigger-only.yml"
expect_if_pull_request review-trigger-only fail

# YD-1: forms a line scan does not see must fail closed, never read as "nothing to verify" or "least-privilege".
write_workflow quoted-on "$GOOD_TOP" "$SHARED_WRITES" "$BUILD_READS" "$NO_PERSIST"
sed -i.bak 's/^on:$/"on":/' "${WORK}/quoted-on.yml"
expect_if_pull_request quoted-on fail
write_workflow trigger-with-empty-map "$GOOD_TOP" "$SHARED_WRITES" "$BUILD_READS" "$NO_PERSIST"
sed -i.bak 's/^  pull_request:$/  pull_request: {}/' "${WORK}/trigger-with-empty-map.yml"
expect_if_pull_request trigger-with-empty-map fail
write_workflow trigger-with-inline-filter "$GOOD_TOP" "$SHARED_WRITES" "$BUILD_READS" "$NO_PERSIST"
sed -i.bak 's/^  pull_request:$/  pull_request: { branches: [production] }/' "${WORK}/trigger-with-inline-filter.yml"
expect_if_pull_request trigger-with-inline-filter fail

write_workflow quoted-statuses-key "$GOOD_TOP" "$SHARED_READS" '    permissions:\n      contents: read\n      "statuses": write\n' "$NO_PERSIST"
expect quoted-statuses-key fail "job build has statuses: write"

write_workflow quoted-job-id "$GOOD_TOP" "$SHARED_READS" '    permissions:\n      contents: read\n      statuses: write\n' "$NO_PERSIST"
sed -i.bak 's/^  build:$/  "build":/' "${WORK}/quoted-job-id.yml"
expect quoted-job-id fail "job build has statuses: write"

write_workflow unreadable-job-id "$GOOD_TOP" "$SHARED_READS" "$BUILD_READS" "$NO_PERSIST"
sed -i.bak 's/^  build:$/  build :/' "${WORK}/unreadable-job-id.yml"
expect unreadable-job-id fail "cannot read the job id"

write_workflow wide-layout "$GOOD_TOP" "$SHARED_READS" '    permissions:\n      contents: read\n      statuses: write\n' "$NO_PERSIST"
sed -i.bak 's/^\( *\)/\1\1/' "${WORK}/wide-layout.yml"
expect wide-layout fail "cannot read any job"

printf 'Workflow permission fixtures passed.\n'
