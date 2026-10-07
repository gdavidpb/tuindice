#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

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

require_tool() {
	command -v "$1" >/dev/null 2>&1 || die "Required tool '$1' is not available."
}

is_zero_sha() {
	local sha="${1:-}"
	[[ -z "$sha" || "$sha" =~ ^0+$ ]]
}

append_unique_line() {
	local file="$1"
	local value="$2"

	if [[ -n "$value" ]]; then
		printf '%s\n' "$value" >>"$file"
	fi
}

file_to_csv() {
	local file="$1"

	if [[ -s "$file" ]]; then
		paste -sd, "$file"
	fi
}

file_to_space_list() {
	local file="$1"

	if [[ -s "$file" ]]; then
		paste -sd' ' "$file"
	fi
}

file_to_json_array() {
	local file="$1"

	if [[ -s "$file" ]]; then
		jq -R -s -c 'split("\n") | map(select(length > 0))' <"$file"
	else
		printf '[]\n'
	fi
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

get_app_version_property_at_git_ref() {
	local property_name="$1"
	local git_ref="$2"
	local contents

	contents="$(git show "${git_ref}:$(app_version_file)" 2>/dev/null || true)"
	printf '%s' "$contents" | extract_property_from_stdin "$property_name"
}

validate_semver() {
	local version="$1"
	[[ "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]
}

validate_positive_integer() {
	local value="$1"
	[[ "$value" =~ ^[1-9][0-9]*$ ]]
}

app_tag_name() {
	local version="$1"
	printf 'app-%s\n' "$version"
}

existing_tag_target() {
	local tag_name="$1"

	if git rev-parse -q --verify "refs/tags/${tag_name}" >/dev/null 2>&1; then
		git rev-list -n 1 "$tag_name"
	fi
}

changed_files_between_refs() {
	local before_sha="$1"
	local after_sha="$2"

	# -z: git quotes a non-ASCII name according to core.quotePath, and a quoted name matches no path pattern of the detector.
	if is_zero_sha "$before_sha"; then
		info "Using single-commit diff because the previous SHA is empty."
		git diff-tree --no-renames --no-commit-id --name-only -r -z "$after_sha" | tr '\0' '\n'
	else
		info "Detecting changes between ${before_sha} and ${after_sha}."
		# --no-renames: a moved file is a deletion at its origin and an addition at its destination. Folding it
		# into the destination hides the origin, so moving runtime sources into a test source set asked for nothing.
		git diff --no-renames --name-only -z "$before_sha" "$after_sha" | tr '\0' '\n'
	fi
}

# The module dependency graph lives in scripts/module-graph.txt (validated by
# scripts/validate-module-graph.sh). Module lists and impact closures are
# derived from it so CI scoping cannot drift from the agreed boundaries.
module_graph_file() {
	local common_sh_dir
	common_sh_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
	printf '%s/../../scripts/module-graph.txt\n' "$common_sh_dir"
}

module_graph_entries() {
	grep -vE '^[[:space:]]*(#|$)' "$(module_graph_file)"
}

module_graph_modules() {
	module_graph_entries | awk -F= 'NF { print $1 }' | sort
}

module_graph_dependencies() {
	local module="$1"

	module_graph_entries \
		| awk -F= -v m="$module" '$1 == m { print $2 }' \
		| tr ' ' '\n' \
		| sed 's/^://' \
		| grep -v '^-*$' || true
}

module_graph_direct_dependents() {
	local module="$1"
	local entry
	local entry_module

	while IFS= read -r entry; do
		entry_module="${entry%%=*}"
		if printf '%s\n' "${entry#*=}" | tr ' ' '\n' | grep -Fxq ":${module}"; then
			printf '%s\n' "$entry_module"
		fi
	done < <(module_graph_entries)
}

# Emits the module plus every transitive dependent, derived from the graph.
# The iOS host is appended whenever maincore is impacted because it links the
# maincore umbrella framework.
module_reverse_closure() {
	local module="$1"
	local visited=$'\n'"${module}"$'\n'
	local queue="$module"
	local current
	local dependent
	local rest

	printf '%s\n' "$module"

	while [[ -n "$queue" ]]; do
		current="${queue%%$'\n'*}"
		rest="${queue#"$current"}"
		queue="${rest#$'\n'}"

		while IFS= read -r dependent; do
			[[ -n "$dependent" ]] || continue
			if [[ "$visited" != *$'\n'"${dependent}"$'\n'* ]]; then
				visited="${visited}${dependent}"$'\n'
				printf '%s\n' "$dependent"
				if [[ -n "$queue" ]]; then
					queue="${queue}"$'\n'"${dependent}"
				else
					queue="$dependent"
				fi
			fi
		done < <(module_graph_direct_dependents "$current")
	done

	if [[ "$visited" == *$'\n'maincore$'\n'* ]]; then
		printf 'iosApp\n'
	fi
}

# Modules with Kotlin Multiplatform tasks: everything but the Android app and the
# Android-only instrumentation runner of the E2E scenarios.
kmp_modules() {
	module_graph_modules | grep -Fxv app | grep -Fxv scenariorunner
}

# Modules that ship in the app. The test-only modules (testkit and the E2E
# scenario kit, catalog and runner) never reach a release build.
runtime_modules() {
	{
		module_graph_modules |
			grep -Fxv testkit | grep -Fxv scenariokit | grep -Fxv scenarios | grep -Fxv scenariorunner
		printf 'iosApp\n'
	} | sort
}

module_is_kmp() {
	local module="$1"
	kmp_modules | grep -Fx "$module" >/dev/null 2>&1
}

module_is_runtime() {
	local module="$1"
	runtime_modules | grep -Fx "$module" >/dev/null 2>&1
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
	local common_sh_dir
	local layout_file
	local value

	common_sh_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
	layout_file="${common_sh_dir}/../../e2e/scripts/shared/layout.env"
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

append_module_closure() {
	local module="$1"
	local target_file="$2"
	local impacted

	case "$module" in
		iosApp)
			append_unique_line "$target_file" iosApp
			return 0
			;;
	esac

	module_graph_modules | grep -Fxq "$module" || return 0

	while IFS= read -r impacted; do
		append_unique_line "$target_file" "$impacted"
	done < <(module_reverse_closure "$module")
}

sort_file_if_present() {
	local file="$1"

	if [[ -s "$file" ]]; then
		sort -u "$file" -o "$file"
	fi
}

write_version_xcconfig_contents() {
	local version_name="$1"
	local ios_build_number="$2"

	printf '// Generated from %s. Do not edit directly.\n' "$(app_version_file)"
	printf 'MARKETING_VERSION = %s\n' "$version_name"
	printf 'CURRENT_PROJECT_VERSION = %s\n' "$ios_build_number"
}
