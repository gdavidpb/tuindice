#!/usr/bin/env bash
# Fingerprint v5 of the local E2E evidence of one platform.
#   e2e-fingerprint.sh <android|ios> <suite-id> [git-ref]      prints the 64-hex fingerprint
#   e2e-fingerprint.sh --print-pathspecs <android|ios> [git-ref]   prints required:<path> / optional:<path> / excluded:<prefix>
#
# The fingerprint is a pure function of the ref's git tree and of this script: it reads objects with
# `git ls-tree`/`git show`, never the working tree (layout.env included: it is read from the ref), so a clean CI
# checkout and the local machine agree. What the working tree cannot supply (toolchain, device) is pinned by the
# locks under e2e/toolchain, which are themselves inside the fingerprint.
#
# Not covered, on purpose: test source sets, version bumps, the registration of the e2e*/verify* Gradle tasks
# (gradle/e2e-tasks.gradle.kts), `.github/**` except the two scripts the iOS build runs, `.codex/**`, `docs/**`,
# `e2e/tools/**`, `e2e/platform/**`, `iosApp/scripts/ci-*` and `*.md`.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="${E2E_FINGERPRINT_REPO_ROOT:-$(cd "${SCRIPT_DIR}/../../.." && pwd)}"
LAYOUT_PATH="e2e/scripts/shared/layout.env"

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

# layout.env comes from GIT_REF like everything else: the names it holds (modules, source sets) decide which paths
# are hashed, so reading the working-tree copy would make the fingerprint depend on uncommitted edits.
layout="$(git -C "${REPO_ROOT}" show "${GIT_REF}:${LAYOUT_PATH}")" || {
	printf 'Cannot read %s at %s\n' "${LAYOUT_PATH}" "${GIT_REF}" >&2
	exit 1
}
while IFS='=' read -r key value; do
	[[ "${key}" =~ ^E2E_[A-Z0-9_]+$ ]] || continue
	printf -v "${key}" '%s' "${value}"
done <<<"${layout}"
IFS=, read -r -a common_source_sets <<<"${E2E_COMMON_SOURCE_SETS}"
IFS=, read -r -a android_source_sets <<<"${E2E_ANDROID_SOURCE_SETS}"
IFS=, read -r -a ios_source_sets <<<"${E2E_IOS_SOURCE_SETS}"

declare -a required=() optional=() excluded=()

# Shared by both platforms: what every run executes regardless of the device. The root build file is here because it
# sets the native compiler arguments and the classpath; the registration of the e2e*/verify* tasks lives in a script
# of its own (gradle/e2e-tasks.gradle.kts) so that editing a verification task does not invalidate evidence.
required+=(
	"settings.gradle.kts"
	"build.gradle.kts"
	"gradle.properties"
	"gradle/libs.versions.toml"
	"gradle/gradle-daemon-jvm.properties"
	"gradle/wrapper"
	"gradlew"
	"gradlew.bat"
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
	optional+=("app/src/release")
	runtime_source_sets=("${common_source_sets[@]}" "${android_source_sets[@]}")
	platform_source_sets=("${android_source_sets[@]}")
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
		".github/scripts/materialize-firebase-configs.sh"
		".github/scripts/sync-app-version.sh"
		"${E2E_IOS_UITEST_DIR}"
		"iosApp/TuIndiceHost.xcodeproj/project.pbxproj"
		"iosApp/TuIndiceHost.xcodeproj/xcshareddata/xcschemes"
		"e2e/scripts/ios"
		"e2e/toolchain/ios.lock"
	)
	# The ci-* scripts run in CI only; the E2E build never calls them.
	excluded+=("iosApp/scripts/ci-")
	runtime_source_sets=("${common_source_sets[@]}" "${ios_source_sets[@]}")
	platform_source_sets=("${ios_source_sets[@]}")
fi

# The scenario modules never ship, but they compile for the platform: their platform source sets count like a
# runtime module's (the common ones are listed above).
for source_set in "${platform_source_sets[@]}"; do
	optional+=("${E2E_SCENARIOKIT_MODULE}/src/${source_set}" "${E2E_SCENARIOS_MODULE}/src/${source_set}")
done

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
	for path in "${excluded[@]+"${excluded[@]}"}"; do printf 'excluded:%s\n' "${path}"; done
	exit 0
fi

hash_command=(shasum -a 256)
if ! command -v shasum >/dev/null 2>&1; then
	hash_command=(sha256sum)
fi

# A required path with nothing tracked under it means a rename left the fingerprint reading nothing for it: refuse
# to hash (the coverage verifier says the same about the working tree; here it holds for any ref).
tracked="$(git -C "${REPO_ROOT}" ls-tree -r --name-only "${GIT_REF}" -- "${required[@]}")"
missing="$(awk -v required="${required[*]}" '
	BEGIN { count = split(required, paths, " ") }
	{ for (i = 1; i <= count; i++) if ($0 == paths[i] || index($0, paths[i] "/") == 1) seen[i] = 1 }
	END { for (i = 1; i <= count; i++) if (!seen[i]) print paths[i] }
' <<<"${tracked}")"
if [[ -n "${missing}" ]]; then
	printf 'The fingerprint requires paths that nothing tracks at %s:\n%s\n' "${GIT_REF}" "${missing}" >&2
	exit 1
fi

{
	printf 'tuindice-e2e-fingerprint-v5\n'
	printf 'platform=%s\n' "${PLATFORM}"
	printf 'suite=%s\n' "${SUITE_ID}"
	# Excluded entries are path prefixes (iosApp/scripts/ci- drops the ci-* scripts of that directory).
	git -C "${REPO_ROOT}" ls-tree -r "${GIT_REF}" -- "${required[@]}" "${optional[@]}" | LC_ALL=C sort |
		awk -F'\t' -v excluded="${excluded[*]+"${excluded[*]}"}" '
			BEGIN { count = split(excluded, prefixes, " ") }
			{ for (i = 1; i <= count; i++) if (index($2, prefixes[i]) == 1) next; print }
		'
} | "${hash_command[@]}" | awk '{ print $1 }'
