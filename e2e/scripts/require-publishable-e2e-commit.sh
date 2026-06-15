#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

COMMIT_SHA="${1:-${E2E_COMMIT_SHA:-${E2E_HEAD_SHA:-$(git -C "${REPO_ROOT}" rev-parse HEAD)}}}"
COMMIT_SHA="$(git -C "${REPO_ROOT}" rev-parse "${COMMIT_SHA}^{commit}")"

publish_mode_is_disabled() {
	case "${E2E_PUBLISH_GITHUB_STATUS:-auto}" in
		0|false|False|FALSE|no|No|NO|off|Off|OFF|never|Never|NEVER)
			return 0
			;;
	esac

	return 1
}

validate_publish_mode() {
	case "${E2E_PUBLISH_GITHUB_STATUS:-auto}" in
		0|false|False|FALSE|no|No|NO|off|Off|OFF|never|Never|NEVER|\
		1|true|True|TRUE|yes|Yes|YES|on|On|ON|always|Always|ALWAYS|\
		auto|Auto|AUTO|"")
			return 0
			;;
	esac

	printf 'Unsupported E2E_PUBLISH_GITHUB_STATUS value: %s\n' "${E2E_PUBLISH_GITHUB_STATUS}" >&2
	exit 1
}

validate_publish_mode

if publish_mode_is_disabled; then
	log "Skipping GitHub commit status publishing preflight because E2E_PUBLISH_GITHUB_STATUS=${E2E_PUBLISH_GITHUB_STATUS}."
	exit 0
fi

require_command git

if [[ -n "$(git -C "${REPO_ROOT}" status --porcelain --untracked-files=all)" ]]; then
	printf 'Working tree has uncommitted changes; commit or stash them before running publishable E2E evidence.\n' >&2
	exit 1
fi

if [[ "${E2E_ALLOW_NON_HEAD_COMMIT_STATUS:-0}" != "1" && "$(git -C "${REPO_ROOT}" rev-parse HEAD)" != "${COMMIT_SHA}" ]]; then
	printf 'Refusing to publish E2E statuses for non-HEAD commit %s.\n' "${COMMIT_SHA}" >&2
	exit 1
fi

if [[ "${E2E_ALLOW_NON_HEAD_COMMIT_STATUS:-0}" != "1" ]]; then
	if ! git -C "${REPO_ROOT}" rev-parse --abbrev-ref --symbolic-full-name '@{u}' >/dev/null 2>&1; then
		printf 'Current branch has no upstream; push the branch before running publishable E2E evidence.\n' >&2
		exit 1
	fi

	if [[ "$(git -C "${REPO_ROOT}" rev-parse HEAD)" != "$(git -C "${REPO_ROOT}" rev-parse '@{u}')" ]]; then
		printf 'HEAD does not match upstream; push all commits before running publishable E2E evidence.\n' >&2
		exit 1
	fi
fi

if ! command -v gh >/dev/null 2>&1; then
	printf 'Missing required command: gh\n' >&2
	exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
	printf 'GitHub CLI is not authenticated; run gh auth login before publishing E2E statuses.\n' >&2
	exit 1
fi

if ! gh api "repos/{owner}/{repo}/commits/${COMMIT_SHA}" >/dev/null 2>&1; then
	printf 'Commit %s is not available through GitHub API for this repository.\n' "${COMMIT_SHA}" >&2
	exit 1
fi

log "GitHub commit status publishing preflight passed for ${COMMIT_SHA}."
