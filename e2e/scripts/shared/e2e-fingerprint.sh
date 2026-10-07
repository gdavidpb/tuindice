#!/usr/bin/env bash
# Fingerprint v5 of the local E2E evidence of one platform.
#   e2e-fingerprint.sh <android|ios> <suite-id> [git-ref]      prints the 64-hex fingerprint
#   e2e-fingerprint.sh --print-pathspecs <android|ios> [git-ref]   prints required:<path> / optional:<path>
#
# The fingerprint is a pure function of the ref's git tree and of this script: it reads objects with
# `git ls-tree`/`git show`, never the working tree, so a clean CI checkout and the local machine agree.
# What the working tree cannot supply (toolchain, device) is pinned by the locks under e2e/toolchain,
# which are themselves inside the fingerprint.
#
# Not covered, on purpose: test source sets, version bumps, the root build.gradle.kts, `.github/**`,
# `.codex/**`, `docs/**`, `e2e/tools/**`, `e2e/platform/**` and `*.md`.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="${E2E_FINGERPRINT_REPO_ROOT:-$(cd "${SCRIPT_DIR}/../../.." && pwd)}"
# shellcheck source=e2e/scripts/shared/layout.env
source "${SCRIPT_DIR}/layout.env"

usage() {
	printf 'Usage: %s <android|ios> <suite-id> [git-ref]\n       %s --print-pathspecs <android|ios> [git-ref]\n' "$0" "$0" >&2
	exit 1
}

mode=hash
if [[ "${1:-}" == "--print-pathspecs" ]]; then
	mode=pathspecs
	shift
	PLATFORM="${1:-}"
	SUITE_ID=""
	GIT_REF="${2:-HEAD}"
else
	PLATFORM="${1:-}"
	SUITE_ID="${2:-}"
	GIT_REF="${3:-HEAD}"
	[[ -n "${SUITE_ID}" ]] || usage
fi
[[ "${PLATFORM}" == "android" || "${PLATFORM}" == "ios" ]] || usage
command -v git >/dev/null 2>&1 || { printf 'Missing required command: git\n' >&2; exit 1; }

declare -a required=() optional=()

# Shared by both platforms: what every run executes regardless of the device.
required+=(
	"settings.gradle.kts"
	"gradle.properties"
	"gradle/libs.versions.toml"
	"gradle/wrapper/gradle-wrapper.properties"
	"mocks"
	"e2e/catalog"
	"e2e/scripts/shared"
	"${E2E_SCENARIOKIT_MODULE}/build.gradle.kts"
	"${E2E_SCENARIOKIT_MODULE}/src/commonMain"
	"${E2E_SCENARIOS_MODULE}/build.gradle.kts"
	"${E2E_SCENARIOS_MODULE}/src/commonMain"
)

if [[ "${PLATFORM}" == "android" ]]; then
	required+=(
		"app/build.gradle.kts"
		"app/src/main"
		"app/src/debug"
		"e2e/scripts/android"
		"e2e/toolchain/android.lock"
		"${E2E_ANDROID_TEST_MODULE}"
	)
	optional+=("app/src/release" "${E2E_SCENARIOKIT_MODULE}/src/androidMain")
	runtime_source_sets=(commonMain androidMain)
else
	required+=(
		"iosApp/Config/Debug.xcconfig"
		"iosApp/Config/Release.xcconfig"
		"iosApp/Config/UITests.xcconfig"
		"iosApp/Podfile"
		"iosApp/Podfile.lock"
		"iosApp/Resources"
		"iosApp/Sources"
		"iosApp/scripts"
		"${E2E_IOS_UITEST_DIR}"
		"iosApp/TuIndiceHost.xcodeproj/project.pbxproj"
		"iosApp/TuIndiceHost.xcodeproj/xcshareddata/xcschemes"
		"e2e/scripts/ios"
		"e2e/toolchain/ios.lock"
	)
	optional+=("${E2E_SCENARIOKIT_MODULE}/src/iosMain")
	runtime_source_sets=(commonMain iosMain appleMain nativeMain iosArm64Main iosSimulatorArm64Main iosX64Main)
fi

# KMP runtime modules come from the module graph as stored in GIT_REF. A second hand-written copy is how
# `security` once stayed outside the fingerprint while the detector treated it as runtime. `app` brings its own
# pathspecs above; testkit and the E2E modules never ship in the app (the scenario modules are listed above).
graph="$(git -C "${REPO_ROOT}" show "${GIT_REF}:scripts/module-graph.txt")" || {
	printf 'Cannot read scripts/module-graph.txt at %s\n' "${GIT_REF}" >&2
	exit 1
}
while IFS= read -r module; do
	optional+=("${module}/build.gradle.kts")
	for source_set in "${runtime_source_sets[@]}"; do
		optional+=("${module}/src/${source_set}")
	done
done < <(
	printf '%s\n' "${graph}" |
		sed -e 's/#.*$//' -e '/^[[:space:]]*$/d' |
		awk -F= 'NF { print $1 }' |
		grep -vxE "app|testkit|${E2E_SCENARIOKIT_MODULE}|${E2E_SCENARIOS_MODULE}|${E2E_ANDROID_TEST_MODULE}" |
		sort -u
)

if [[ "${mode}" == "pathspecs" ]]; then
	for path in "${required[@]}"; do printf 'required:%s\n' "${path}"; done
	for path in "${optional[@]}"; do printf 'optional:%s\n' "${path}"; done
	exit 0
fi

hash_command=(shasum -a 256)
if ! command -v shasum >/dev/null 2>&1; then
	hash_command=(sha256sum)
fi

{
	printf 'tuindice-e2e-fingerprint-v5\n'
	printf 'platform=%s\n' "${PLATFORM}"
	printf 'suite=%s\n' "${SUITE_ID}"
	git -C "${REPO_ROOT}" ls-tree -r "${GIT_REF}" -- "${required[@]}" "${optional[@]}" | LC_ALL=C sort
} | "${hash_command[@]}" | awk '{ print $1 }'
