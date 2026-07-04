#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

PLATFORM="${1:-}"
SUITE_ID="${2:-}"
GIT_REF="${3:-HEAD}"

if [[ "$PLATFORM" != "android" && "$PLATFORM" != "ios" ]]; then
	printf 'Usage: %s <android|ios> <suite-id> [git-ref]\n' "$0" >&2
	exit 1
fi

if [[ -z "$SUITE_ID" ]]; then
	printf 'Usage: %s <android|ios> <suite-id> [git-ref]\n' "$0" >&2
	exit 1
fi

require_command git

# Pathspecs are platform-scoped so a host-only fix on one platform does not
# invalidate the other platform's published evidence. Shared runtime content
# (KMP modules, mocks, Maestro flows, testkit) stays in both fingerprints.
declare -a fingerprint_pathspecs=(
	"settings.gradle.kts"
	"gradle.properties"
	"gradle/libs.versions.toml"
	"gradle/wrapper/gradle-wrapper.properties"
	"mocks"
	"e2e/maestro"
	"testkit/e2e"
)

if [[ "$PLATFORM" == "android" ]]; then
	fingerprint_pathspecs+=(
		"app/build.gradle.kts"
		"app/src/main"
		"app/src/debug"
		"app/src/release"
	)
else
	fingerprint_pathspecs+=(
		"iosApp/Config/Debug.xcconfig"
		"iosApp/Config/Release.xcconfig"
		"iosApp/Podfile"
		"iosApp/Podfile.lock"
		"iosApp/Resources"
		"iosApp/Sources"
		"iosApp/TuIndiceHost.xcodeproj/project.pbxproj"
		"iosApp/TuIndiceHost.xcodeproj/xcshareddata/xcschemes"
	)
fi

platform_source_sets() {
	if [[ "$PLATFORM" == "android" ]]; then
		printf '%s\n' commonMain androidMain
	else
		printf '%s\n' commonMain iosMain appleMain nativeMain iosArm64Main iosSimulatorArm64Main iosX64Main
	fi
}

append_kmp_runtime_pathspecs() {
	local module="$1"
	local source_set

	fingerprint_pathspecs+=("${module}/build.gradle.kts")
	while IFS= read -r source_set; do
		fingerprint_pathspecs+=("${module}/src/${source_set}")
	done < <(platform_source_sets)
}

while IFS= read -r module; do
	append_kmp_runtime_pathspecs "$module"
done <<'MODULES'
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
MODULES

hash_command=(shasum -a 256)
if ! command -v shasum >/dev/null 2>&1; then
	hash_command=(sha256sum)
fi

{
	printf 'tuindice-e2e-fingerprint-v3\n'
	printf 'platform=%s\n' "$PLATFORM"
	printf 'suite=%s\n' "$SUITE_ID"
	git -C "$REPO_ROOT" ls-tree -r "$GIT_REF" -- "${fingerprint_pathspecs[@]}" | LC_ALL=C sort
} | "${hash_command[@]}" | awk '{ print $1 }'
