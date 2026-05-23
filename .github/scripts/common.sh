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

kmp_modules() {
	cat <<'EOF'
about
academiccore
auth
base
enrollmentproof
evaluations
maincore
pensum
persistence
record
subjects
summary
testkit
wizard
EOF
}

runtime_modules() {
	cat <<'EOF'
about
academiccore
app
auth
base
enrollmentproof
evaluations
iosApp
maincore
pensum
persistence
record
subjects
summary
wizard
EOF
}

feature_modules() {
	cat <<'EOF'
about
auth
enrollmentproof
evaluations
pensum
record
subjects
summary
wizard
EOF
}

all_shared_runtime_modules() {
	cat <<'EOF'
about
academiccore
auth
base
enrollmentproof
evaluations
maincore
pensum
persistence
record
subjects
summary
wizard
EOF
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

append_module_closure() {
	local module="$1"
	local target_file="$2"
	local dependency

	case "$module" in
		base)
			while IFS= read -r dependency; do append_unique_line "$target_file" "$dependency"; done < <(all_shared_runtime_modules)
			append_unique_line "$target_file" app
			append_unique_line "$target_file" iosApp
			;;
		academiccore)
			for dependency in academiccore record evaluations pensum wizard maincore app iosApp; do
				append_unique_line "$target_file" "$dependency"
			done
			;;
		persistence)
			for dependency in persistence summary record evaluations enrollmentproof subjects pensum maincore app iosApp; do
				append_unique_line "$target_file" "$dependency"
			done
			;;
		testkit)
			append_unique_line "$target_file" testkit
			;;
		maincore)
			for dependency in maincore app iosApp; do
				append_unique_line "$target_file" "$dependency"
			done
			;;
		app)
			append_unique_line "$target_file" app
			;;
		iosApp)
			append_unique_line "$target_file" iosApp
			;;
		about|auth|enrollmentproof|evaluations|pensum|record|subjects|summary|wizard)
			for dependency in "$module" maincore app iosApp; do
				append_unique_line "$target_file" "$dependency"
			done
			;;
		*)
			return 0
			;;
	esac
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

	printf 'MARKETING_VERSION = %s\n' "$version_name"
	printf 'CURRENT_PROJECT_VERSION = %s\n' "$ios_build_number"
}
