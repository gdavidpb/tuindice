#!/usr/bin/env bash
set -euo pipefail

# Verifies that every repo file referenced by the GitHub workflows is tracked
# in git. The root /scripts/* gitignore pattern allowlists files one by one,
# so a workflow can reference a script that exists locally but was never
# committed: local parity passes against the working tree while the CI
# checkout has no file and the job dies with exit 127. That exact drift
# shipped once with semgrep-architecture.sh; this gate makes it structural.
#
# Scope: workflow tokens that look like repo source files (known prefixes,
# known extensions, no runtime interpolation and no build outputs).

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
WORKFLOWS_DIR="${REPO_ROOT}/.github/workflows"

if [[ ! -d "${WORKFLOWS_DIR}" ]]; then
	printf 'No workflows directory at %s\n' "${WORKFLOWS_DIR}" >&2
	exit 1
fi

status=0
checked=0

while IFS= read -r token; do
	[[ -n "${token}" ]] || continue

	case "${token}" in
		*'$'*|*'*'*) continue ;;
		*/build/*|build/*) continue ;;
		scripts/*|.github/*|e2e/*|mocks/*|testkit/*|config/*|gradle/*|iosApp/Config/*) ;;
		*) continue ;;
	esac

	checked=$((checked + 1))
	if ! git -C "${REPO_ROOT}" ls-files --error-unmatch "${token}" >/dev/null 2>&1; then
		if [[ -e "${REPO_ROOT}/${token}" ]]; then
			printf 'Workflow-referenced file exists locally but is NOT tracked by git (gitignored?): %s\n' "${token}" >&2
		else
			printf 'Workflow references a file that does not exist in the repo: %s\n' "${token}" >&2
		fi
		status=1
	fi
done < <(
	grep -rhoE '(\./)?[A-Za-z0-9._/-]+\.(sh|py|kts|json|ya?ml|txt|jar|gradle|properties|xcconfig)' "${WORKFLOWS_DIR}" |
		sed -e 's|^\./||' |
		sort -u
)

if [[ "${status}" != "0" ]]; then
	printf 'Workflow file references check failed.\n' >&2
	exit 1
fi
printf 'Workflow file references are tracked (%d checked).\n' "${checked}"
