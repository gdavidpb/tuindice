#!/usr/bin/env bash
# The part of the shared shell library (.github/scripts/common.sh loads this file) that the E2E build and the E2E harness
# execute: the logging and the version helpers of the two CI scripts the iOS build runs (sync-app-version.sh,
# materialize-firebase-configs.sh), the suite id and the e2e_* functions the harness asks for (status context, base ref,
# trusted creators, source sets). It lives under e2e/scripts/shared on purpose: that directory is read by both
# fingerprints, so changing what a run executes moves the fingerprint. common.sh itself is not, and keeps only what no
# fingerprinted file calls (test_single_definitions fails when one does). Meant to be sourced, never run.

info() {
	printf '[INFO] %s\n' "$*" >&2
}

warn() {
	printf '[WARN] %s\n' "$*" >&2
}

error() {
	printf '[ERROR] %s\n' "$*" >&2
}

die() {
	error "$*"
	exit 1
}

app_version_file() {
	printf 'gradle/app-version.properties\n'
}

extract_property_from_stdin() {
	local property_name="$1"
	awk -F= -v key="$property_name" '
		$1 == key {
			value = $2
			sub(/^[[:space:]]+/, "", value)
			sub(/[[:space:]]+$/, "", value)
			print value
			exit
		}
	'
}

get_app_version_property() {
	local property_name="$1"
	local value

	value="$(extract_property_from_stdin "$property_name" <"$(app_version_file)")"
	[[ -n "$value" ]] || die "Missing app version property '${property_name}' in $(app_version_file)."
	printf '%s\n' "$value"
}

get_app_version_name() {
	get_app_version_property versionName
}

get_android_version_code() {
	get_app_version_property androidVersionCode
}

get_ios_build_number() {
	get_app_version_property iosBuildNumber
}

validate_semver() {
	local version="$1"
	[[ "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]
}

validate_positive_integer() {
	local value="$1"
	[[ "$value" =~ ^[1-9][0-9]*$ ]]
}

write_version_xcconfig_contents() {
	local version_name="$1"
	local ios_build_number="$2"

	printf '// Generated from %s. Do not edit directly.\n' "$(app_version_file)"
	printf 'MARKETING_VERSION = %s\n' "$version_name"
	printf 'CURRENT_PROJECT_VERSION = %s\n' "$ios_build_number"
}

# Local E2E evidence is one suite per platform: the whole scenario catalog. The suite id has this one definition in
# shell (the detector and the status context use it); harness/config.py SUITE_ID must equal it (test_single_definitions).
E2E_SUITE_ID="local-certification-suite"

# The commit status the local E2E evidence of a platform is published under. This is its only definition:
# the change detector writes it into the context files, preflight verifies it, and the harness
# (e2e/scripts/shared/harness/publish.py) asks this function before it publishes.
e2e_status_context() {
	printf 'local-e2e/%s/%s\n' "$1" "$E2E_SUITE_ID"
}

# The base branch every local E2E tool diffs against: origin/production when it resolves (the local branch lags
# behind it), else production. CI computes its own base from the pull request; this is the one local definition,
# shared by the scope resolver, the audit helper, the verdict and the parity script. Prints the ref name, exits 1
# when neither exists. Argument: the repository root (default: the current directory).
e2e_base_ref() {
	local repo_root="${1:-.}"
	local candidate

	for candidate in origin/production production; do
		if git -C "$repo_root" rev-parse --verify --quiet "${candidate}^{commit}" >/dev/null; then
			printf '%s\n' "$candidate"
			return 0
		fi
	done
	return 1
}

# The logins whose `success` status counts as evidence: E2E_TRUSTED_STATUS_CREATORS (comma-separated) or, by
# default, the repository owner (argument) and the Actions bot. One line each. The preflight and the harness's
# verdict both ask this function; an empty variable (an unset repository variable in a workflow) keeps the default.
e2e_trusted_status_creators() {
	local owner="${1:-}"

	printf '%s\n' "${E2E_TRUSTED_STATUS_CREATORS:-${owner},github-actions[bot]}" | tr ',' '\n'
}

# Source sets of a KMP module that feed the E2E fingerprint of one platform. The lists live in
# e2e/scripts/shared/layout.env (E2E_COMMON_SOURCE_SETS, E2E_ANDROID_SOURCE_SETS, E2E_IOS_SOURCE_SETS), the same
# file the fingerprint script reads from the ref, so the detector and the fingerprint cannot drift.
# e2e_load_source_sets reads them once into arrays (callers that classify many files in command substitutions load
# before the loop; e2e_platform_for_kmp_file loads on demand otherwise).
e2e_load_source_sets() {
	local library_dir
	local layout_file
	local value

	library_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
	layout_file="${library_dir}/layout.env"
	value="$(awk -F= '$1 == "E2E_COMMON_SOURCE_SETS" { print substr($0, length($1) + 2); exit }' "$layout_file")"
	IFS=, read -r -a E2E_COMMON_SOURCE_SET_LIST <<<"$value"
	value="$(awk -F= '$1 == "E2E_ANDROID_SOURCE_SETS" { print substr($0, length($1) + 2); exit }' "$layout_file")"
	IFS=, read -r -a E2E_ANDROID_SOURCE_SET_LIST <<<"$value"
	value="$(awk -F= '$1 == "E2E_IOS_SOURCE_SETS" { print substr($0, length($1) + 2); exit }' "$layout_file")"
	IFS=, read -r -a E2E_IOS_SOURCE_SET_LIST <<<"$value"
}

# The platform whose E2E fingerprint a file of a KMP module belongs to: android, ios, all (shared source set
# or build file) or none (anything the fingerprint does not read).
e2e_platform_for_kmp_file() {
	local module="$1"
	local file="$2"
	local source_set

	if [[ "$file" == "$module/build.gradle.kts" ]]; then
		printf 'all\n'
		return 0
	fi

	if [[ -z "${E2E_COMMON_SOURCE_SET_LIST+x}" ]]; then
		e2e_load_source_sets
	fi

	for source_set in "${E2E_COMMON_SOURCE_SET_LIST[@]}"; do
		if [[ "$file" == "$module/src/${source_set}/"* ]]; then
			printf 'all\n'
			return 0
		fi
	done
	for source_set in "${E2E_ANDROID_SOURCE_SET_LIST[@]}"; do
		if [[ "$file" == "$module/src/${source_set}/"* ]]; then
			printf 'android\n'
			return 0
		fi
	done
	for source_set in "${E2E_IOS_SOURCE_SET_LIST[@]}"; do
		if [[ "$file" == "$module/src/${source_set}/"* ]]; then
			printf 'ios\n'
			return 0
		fi
	done
	printf 'none\n'
}
