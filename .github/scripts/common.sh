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

	[[ -n "$value" ]] || return 0
	if [[ ! -s "$file" ]] || ! grep -Fx "$value" "$file" >/dev/null 2>&1; then
		printf '%s\n' "$value" >>"$file"
	fi
}

sort_file_if_present() {
	local file="$1"

	if [[ -s "$file" ]]; then
		sort -u "$file" -o "$file"
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

	[[ -n "$git_ref" ]] || return 0
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
