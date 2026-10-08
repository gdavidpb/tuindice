#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

# Logging, version helpers, the suite id and the e2e_* functions live in a file the E2E fingerprint reads (see its header).
COMMON_LIBRARY="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/../../e2e/scripts/shared/ci-common.sh"
# shellcheck source=e2e/scripts/shared/ci-common.sh
source "$COMMON_LIBRARY"

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

get_app_version_property_at_git_ref() {
	local property_name="$1"
	local git_ref="$2"
	local contents

	contents="$(git show "${git_ref}:$(app_version_file)" 2>/dev/null || true)"
	printf '%s' "$contents" | extract_property_from_stdin "$property_name"
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

