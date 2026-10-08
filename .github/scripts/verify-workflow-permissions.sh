#!/usr/bin/env bash
# The jobs of a workflow that runs code of the pull request get the least token they need.
#   verify-workflow-permissions.sh <workflow.yml> [--if-pull-request] [--credentials JOB]...
# Rules (the workflow files have a regular two-space layout; this is a line scan, not a YAML parser):
#   1. the workflow declares top-level `permissions` and none of them is `statuses: write`;
#   2. every job declares its own `permissions`;
#   3. no job has `statuses: write`: the workflow of a pull request reads the evidence statuses and never publishes them
#      (the owner publishes from the machine that ran the evidence);
#   4. a `permissions:` (top-level or of a job) is either a block of keys or `{}` / `read-all`: any other value on the same
#      line (`write-all`, an inline map such as `{ statuses: write }`) is refused, because this scan cannot read it;
#   5. every actions/checkout step of a job not named with --credentials sets `persist-credentials: false`, so the token
#      is not left in .git/config for the code that job runs.
# A block is every line indented deeper than its `permissions:` line, whatever the indentation; the value of a key may be
# quoted; a job id may carry a comment. validate-ci-config.sh runs this with --if-pull-request over every workflow: the
# ones with a pull_request or pull_request_target trigger (the ones that run code of a pull request) are verified, the
# others are left alone.
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

[[ $# -ge 1 ]] || die "Usage: verify-workflow-permissions.sh <workflow.yml> [--if-pull-request] [--credentials JOB]..."
workflow="$1"
shift
[[ -f "$workflow" ]] || die "No such workflow: ${workflow}"

credentials=","
only_pull_request=false
while [[ $# -gt 0 ]]; do
	case "$1" in
		--if-pull-request) only_pull_request=true; shift ;;
		--credentials) credentials="${credentials}${2:?--credentials needs a job}," ; shift 2 ;;
		*) die "Unknown argument: $1" ;;
	esac
done

# A workflow triggered by pull_request or pull_request_target runs code of the pull request; the others do not concern this check.
if [[ "$only_pull_request" == "true" ]] && ! awk '
	/^on:/ { in_on = 1; if ($0 ~ /pull_request/) { found = 1 }; next }
	/^[^ #]/ { in_on = 0 }
	in_on && /^[ ]+(- *)?["\047]?pull_request(_target)?["\047]?:?[ ]*(#.*)?$/ { found = 1 }
	END { exit !found }
' "$workflow"; then
	info "Not triggered by a pull request, nothing to verify: ${workflow}"
	exit 0
fi

awk -v credentials="$credentials" -v file="$workflow" '
	function fail(message) { printf "%s: %s\n", file, message > "/dev/stderr"; failed = 1 }
	function indent_of(line) { match(line, /^ */); return RLENGTH }
	function value_of(line,   v) {
		v = line
		sub(/^[^:]*:/, "", v)
		sub(/[ \t]+#.*$/, "", v)
		gsub(/^[ \t]+|[ \t]+$/, "", v)
		return v
	}
	function close_checkout() {
		if (in_checkout && !(credentials ~ ("," job ",")) && !persist_false) {
			fail("job " job ": an actions/checkout step does not set persist-credentials: false")
		}
		in_checkout = 0; persist_false = 0
	}
	function close_job() {
		close_checkout()
		if (job != "" && !job_has_permissions) { fail("job " job " declares no permissions") }
		job = ""
	}
	# Starts the permissions of a scope ("top-level" or a job). A block is followed line by line; an inline value is only
	# accepted when it is empty-map or read-all.
	function open_permissions(line, scope,   v) {
		v = value_of(line)
		if (v == "") {
			perm_active = 1; perm_indent = indent_of(line); perm_scope = scope
		} else if (v != "{}" && v != "read-all") {
			fail((scope == "top-level" ? "top-level" : "job " scope) " permissions have the inline value " v "; write them as a block of keys (only {} and read-all are accepted inline)")
		}
	}
	/^[ \t]*(#.*)?$/ { next }
	{
		line_indent = indent_of($0)
		if (perm_active && line_indent <= perm_indent) { perm_active = 0 }
		if (perm_active) {
			if ($0 ~ /statuses:[ \t]*["\047]?write/) {
				if (perm_scope == "top-level") { fail("top-level permissions grant statuses: write") }
				else { fail("job " perm_scope " has statuses: write; no job of this workflow may write commit statuses") }
			}
			next
		}
	}
	/^[^ #]/ {
		section = $1; sub(/:.*/, "", section)
		if (section == "permissions") { top_has_permissions = 1; open_permissions($0, "top-level") }
		next
	}
	section == "jobs" && /^  [A-Za-z0-9_-]+:[ \t]*(#.*)?$/ {
		close_job()
		job = $1; sub(/:.*/, "", job)
		job_has_permissions = 0
		next
	}
	section == "jobs" && job != "" {
		if ($0 ~ /^    permissions:/) { job_has_permissions = 1; open_permissions($0, job); next }
		if ($0 ~ /^      - /) { close_checkout() }
		if ($0 ~ /uses: *actions\/checkout@/) { in_checkout = 1 }
		if (in_checkout && $0 ~ /persist-credentials: *false/) { persist_false = 1 }
	}
	END {
		close_job()
		if (!top_has_permissions) { fail("the workflow declares no top-level permissions") }
		exit failed ? 1 : 0
	}
' "$workflow"

info "Workflow permissions are least-privilege: ${workflow}"
