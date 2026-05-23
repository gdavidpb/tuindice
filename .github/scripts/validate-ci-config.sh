#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

require_tool bash

while IFS= read -r script_file; do
	[[ -n "$script_file" ]] || continue
	info "Checking shell syntax: ${script_file}"
	bash -n "$script_file"
done < <(
	find .github/scripts e2e/scripts iosApp/scripts -name '*.sh' -type f 2>/dev/null | sort
)

while IFS= read -r workflow_file; do
	[[ -n "$workflow_file" ]] || continue
	info "Found workflow: ${workflow_file}"
done < <(
	find .github/workflows \( -name '*.yml' -o -name '*.yaml' \) -type f 2>/dev/null | sort
)

info "CI configuration syntax checks passed."
