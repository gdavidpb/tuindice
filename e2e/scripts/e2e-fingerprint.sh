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

declare -a fingerprint_pathspecs=(
	"settings.gradle.kts"
	"gradle.properties"
	"gradle/libs.versions.toml"
	"gradle/wrapper/gradle-wrapper.properties"
	"app/build.gradle.kts"
	"app/src"
	"iosApp/Config"
	"iosApp/Podfile"
	"iosApp/Podfile.lock"
	"iosApp/Resources"
	"iosApp/Sources"
	"iosApp/TuIndiceHost.xcodeproj/project.pbxproj"
	"iosApp/TuIndiceHost.xcodeproj/xcshareddata/xcschemes"
	"mocks"
	"e2e/maestro"
	"testkit/e2e"
)

while IFS= read -r module; do
	fingerprint_pathspecs+=("${module}/build.gradle.kts")
	fingerprint_pathspecs+=("${module}/src")
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
testkit
wizard
MODULES

hash_command=(shasum -a 256)
if ! command -v shasum >/dev/null 2>&1; then
	hash_command=(sha256sum)
fi

{
	printf 'tuindice-e2e-fingerprint-v1\n'
	printf 'platform=%s\n' "$PLATFORM"
	printf 'suite=%s\n' "$SUITE_ID"
	git -C "$REPO_ROOT" ls-tree -r "$GIT_REF" -- "${fingerprint_pathspecs[@]}" | LC_ALL=C sort
} | "${hash_command[@]}" | awk '{ print $1 }'
