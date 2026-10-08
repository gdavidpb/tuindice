#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

require_tool bash

mktemp_portability_violations="$(
	grep -R -n -E 'mktemp [^#]*XXXXXX[[:alnum:]_.-]+' .github/scripts e2e/scripts e2e/tools .codex/skills iosApp/scripts 2>/dev/null || true
)"
if [[ -n "$mktemp_portability_violations" ]]; then
	printf '%s\n' "$mktemp_portability_violations" >&2
	die "mktemp templates must end in XXXXXX for macOS portability."
fi

# The iOS UI test job restores the Gradle entries that warm-ios-caches.yml publishes: both must use one cache identity.
warm_cache_job="$(awk '/GRADLE_BUILD_ACTION_CACHE_KEY_JOB:/ { print $2; exit }' .github/workflows/warm-ios-caches.yml)"
uitest_cache_job="$(awk '/^  ios-uitest-preflight:/ { in_job = 1 } in_job && /GRADLE_BUILD_ACTION_CACHE_KEY_JOB:/ { print $2; exit }' .github/workflows/preflight-production-pr.yml)"
if [[ -z "$warm_cache_job" || "$warm_cache_job" != "$uitest_cache_job" ]]; then
	die "ios-uitest-preflight must set GRADLE_BUILD_ACTION_CACHE_KEY_JOB to the identity warm-ios-caches.yml publishes (warm: '${warm_cache_job}', job: '${uitest_cache_job}')."
fi

# verifyE2eContract runs on Linux, where the harness tests that need macOS tools skip: the macOS job that runs them whole
# must exist and the single required job must wait for it.
preflight_workflow=".github/workflows/preflight-production-pr.yml"
if ! awk '/^  e2e-harness-preflight:/ { in_job = 1; next } in_job && /^  [a-z]/ { in_job = 0 } in_job && /runs-on: macos/ { macos = 1 } in_job && /run-harness-tests\.sh/ { runs = 1 } END { exit !(macos && runs) }' "$preflight_workflow"; then
	die "e2e-harness-preflight must run e2e/tools/tests/run-harness-tests.sh on a macOS runner."
fi
if ! awk '/^  preflight-production-pr:/ { in_job = 1; next } in_job && /^  [a-z]/ { in_job = 0 } in_job && /^      - e2e-harness-preflight$/ { found = 1 } END { exit !found }' "$preflight_workflow"; then
	die "preflight-production-pr must wait for e2e-harness-preflight."
fi

# The jobs of every workflow triggered by a pull request run code of it: no job writes commit statuses (the owner publishes the evidence), and no token is left in .git/config except in shared-preflight, which may have to fetch a commit.
for workflow_file in .github/workflows/*.yml .github/workflows/*.yaml; do
	[[ -f "$workflow_file" ]] || continue
	bash "${SCRIPT_DIR}/verify-workflow-permissions.sh" "$workflow_file" --if-pull-request --credentials shared-preflight
done

# The two invocations of the preflight (the workflow of a pull request and the stage, through deploy-production.sh) pass the
# same certification flag, contexts and scope: the check that an empty context file is a wiring fault needs the scope, and
# one invocation without it would print "required and validated" for nothing. Only the stage asks for the merged scope.
for variable in E2E_ANDROID_CONTEXTS_FILE E2E_IOS_CONTEXTS_FILE E2E_SCOPE_FILE REQUIRES_E2E_CERTIFICATION; do
	if ! grep -q "^[[:space:]]*${variable}: " "$preflight_workflow"; then
		die "The pull request preflight step does not pass ${variable} to preflight-production.sh."
	fi
	if ! grep -q "^[[:space:]]*${variable}=" "${SCRIPT_DIR}/deploy-production.sh"; then
		die "deploy-production.sh does not pass ${variable} to preflight-production.sh (the stage and the pull request must pass the same contexts and scope)."
	fi
done
if ! grep -q '^[[:space:]]*E2E_EVIDENCE_SCOPE=merged' "${SCRIPT_DIR}/deploy-production.sh"; then
	die "deploy-production.sh must pass E2E_EVIDENCE_SCOPE=merged to preflight-production.sh: the stage accepts the evidence of the merged commits."
fi
if grep -q 'E2E_EVIDENCE_SCOPE' "$preflight_workflow"; then
	die "The pull request workflow must not set E2E_EVIDENCE_SCOPE: in a pull request the evidence has to be on the head."
fi

# Documents and skills run the vocabulary gate alone: the detector's output must reach a step of the shared job.
if ! awk '/vocabulary_gate_required/ { outputs += 1 } /verify-e2e-vocabulary\.sh/ { runs = 1 } END { exit !(outputs >= 2 && runs) }' "$preflight_workflow"; then
	die "shared-preflight must expose vocabulary_gate_required and run e2e/tools/verify/verify-e2e-vocabulary.sh when it is true."
fi

while IFS= read -r script_file; do
	[[ -n "$script_file" ]] || continue
	info "Checking shell syntax: ${script_file}"
	bash -n "$script_file"
done < <(
	find .github/scripts e2e/scripts e2e/tools .codex/skills iosApp/scripts -name '*.sh' -type f 2>/dev/null | sort
)

while IFS= read -r python_file; do
	[[ -n "$python_file" ]] || continue
	info "Checking Python syntax: ${python_file}"
	# compile() in memory: py_compile would write __pycache__ into the tree.
	python3 -c 'import sys; compile(open(sys.argv[1]).read(), sys.argv[1], "exec")' "$python_file"
done < <(
	find e2e .codex/skills -name '*.py' -type f 2>/dev/null | sort
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
bash "${SCRIPT_DIR}/test-verify-workflow-permissions.sh"
bash "${SCRIPT_DIR}/test-detect-changed-app.sh"
bash "${SCRIPT_DIR}/test-fingerprint-detector-parity.sh"
bash "${SCRIPT_DIR}/test-google-play-draft-check.sh"
bash "${SCRIPT_DIR}/test-appstore-connect-check.sh"
bash "${SCRIPT_DIR}/test-production-release-artifact.sh"

bash "${SCRIPT_DIR}/verify-workflow-refs.sh"

info "CI configuration syntax checks passed."
