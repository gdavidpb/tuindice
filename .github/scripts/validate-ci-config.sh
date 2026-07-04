#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

require_tool bash

mktemp_portability_violations="$(
	grep -R -n -E 'mktemp [^#]*XXXXXX[[:alnum:]_.-]+' .github/scripts e2e/scripts iosApp/scripts 2>/dev/null || true
)"
if [[ -n "$mktemp_portability_violations" ]]; then
	printf '%s\n' "$mktemp_portability_violations" >&2
	die "mktemp templates must end in XXXXXX for macOS portability."
fi

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

info "Validating module dependency graph."
bash "${SCRIPT_DIR}/../../scripts/validate-module-graph.sh"

ios_framework_cache_hash="$(bash "${SCRIPT_DIR}/ios-framework-cache-key.sh")"
if [[ ! "$ios_framework_cache_hash" =~ ^[0-9a-f]{64}$ ]]; then
	die "iOS framework cache key script returned an invalid hash: ${ios_framework_cache_hash}"
fi
info "Validated iOS framework cache key hash."

bash "${SCRIPT_DIR}/test-preflight-production.sh"
bash "${SCRIPT_DIR}/test-detect-changed-app.sh"
bash "${SCRIPT_DIR}/test-google-play-draft-check.sh"
bash "${SCRIPT_DIR}/test-appstore-connect-check.sh"
bash "${SCRIPT_DIR}/test-production-release-artifact.sh"

bash "${SCRIPT_DIR}/verify-workflow-refs.sh"

info "CI configuration syntax checks passed."
