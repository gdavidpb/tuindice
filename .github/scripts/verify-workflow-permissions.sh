#!/usr/bin/env bash
# The jobs of a workflow that runs code of the pull request get the least token they need.
#   verify-workflow-permissions.sh <workflow.yml> [--credentials JOB]...
# Rules (the workflow files have a regular two-space layout; this is a line scan, not a YAML parser):
#   1. the workflow declares top-level `permissions` and none of them is `statuses: write`;
#   2. every job declares its own `permissions`;
#   3. no job has `statuses: write`: the workflow of a pull request reads the evidence statuses and never publishes them
#      (the owner publishes from the machine that ran the evidence);
#   4. every actions/checkout step of a job not named with --credentials sets `persist-credentials: false`, so the token
#      is not left in .git/config for the code that job runs.
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

[[ $# -ge 1 ]] || die "Usage: verify-workflow-permissions.sh <workflow.yml> [--credentials JOB]..."
workflow="$1"
shift
[[ -f "$workflow" ]] || die "No such workflow: ${workflow}"

credentials=","
while [[ $# -gt 0 ]]; do
	case "$1" in
		--credentials) credentials="${credentials}${2:?--credentials needs a job}," ; shift 2 ;;
		*) die "Unknown argument: $1" ;;
	esac
done

awk -v credentials="$credentials" -v file="$workflow" '
	function fail(message) { printf "%s: %s\n", file, message > "/dev/stderr"; failed = 1 }
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
	/^[^ #]/ { section = $1; sub(/:.*/, "", section); in_permissions = (section == "permissions"); if (section == "permissions") { top_has_permissions = 1; if ($0 ~ /\{\}/) { in_permissions = 0 } } }
	section == "permissions" && /^  statuses: *write/ { fail("top-level permissions grant statuses: write") }
	section == "jobs" && /^  [A-Za-z0-9_-]+:[ ]*$/ {
		close_job()
		job = $1; sub(/:$/, "", job)
		job_has_permissions = 0; job_permissions = 0
		next
	}
	section == "jobs" && job != "" {
		if ($0 ~ /^    permissions:/) { job_has_permissions = 1; job_permissions = ($0 ~ /\{\}/) ? 0 : 1; next }
		if (job_permissions && $0 ~ /^      [a-z-]+:/) {
			if ($0 ~ /statuses: *write/) { fail("job " job " has statuses: write; no job of this workflow may write commit statuses") }
			next
		}
		if ($0 ~ /^    [a-z-]+:/) { job_permissions = 0 }
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
