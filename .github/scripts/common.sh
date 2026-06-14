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

	if is_zero_sha "$before_sha"; then
		info "Using single-commit diff because the previous SHA is empty."
		git diff-tree --no-commit-id --name-only -r "$after_sha"
	else
		info "Detecting changes between ${before_sha} and ${after_sha}."
		git diff --name-only "$before_sha" "$after_sha"
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

kmp_modules() {
	module_graph_modules | grep -Fxv app
}

runtime_modules() {
	{
		module_graph_modules | grep -Fxv testkit
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

module_e2e_suite() {
	local module="$1"

	case "$module" in
		about|auth|enrollmentproof|evaluations|maincore|pensum|record|subjects|summary|wizard)
			printf '%s-suite\n' "$module"
			;;
	esac
}

# Feature modules in the reverse closure of a module, i.e. every module with
# its own E2E suite whose behavior the change can impact. maincore is excluded:
# its suite is only required by its own semantic triggers (maincore changes,
# persistence bootstrap paths, shared E2E flow changes).
module_impacted_feature_suites() {
	local module="$1"
	local impacted
	local suite

	while IFS= read -r impacted; do
		[[ -n "$impacted" && "$impacted" != "maincore" ]] || continue
		suite="$(module_e2e_suite "$impacted" || true)"
		if [[ -n "$suite" ]]; then
			printf '%s\n' "$impacted"
		fi
	done < <(module_reverse_closure "$module") | sort -u
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
