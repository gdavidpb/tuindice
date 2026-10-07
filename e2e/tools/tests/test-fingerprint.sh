#!/usr/bin/env bash
# Tests of e2e/scripts/shared/e2e-fingerprint.sh (v5) and of the coverage verifier, against a throwaway git
# repository with the minimal layout. No network, no devices; the temporary repository is removed on exit.
#   bash e2e/tools/tests/test-fingerprint.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REAL_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
FINGERPRINT="${REAL_ROOT}/e2e/scripts/shared/e2e-fingerprint.sh"
VERIFIER="${REAL_ROOT}/e2e/tools/verify/verify-e2e-fingerprint-coverage.sh"

WORK="$(mktemp -d "${TMPDIR:-/tmp}/tuindice-fingerprint-test.XXXXXX")"
trap 'rm -rf "${WORK}"' EXIT
REPO="${WORK}/repo"
failures=0
checks=0

fail() {
	printf 'FAIL: %s\n' "$1" >&2
	failures=$((failures + 1))
}

ok() {
	checks=$((checks + 1))
}

fp() {
	E2E_FINGERPRINT_REPO_ROOT="${REPO}" bash "${FINGERPRINT}" "$1" "${3:-local-certification-suite}" "${2:-HEAD}"
}

# commit_files <base> path=content ...: a commit made with plumbing only, so the working tree is never touched.
commit_files() {
	local base="$1"
	local index="${WORK}/index.$RANDOM$RANDOM"
	local spec path content blob tree
	shift
	GIT_INDEX_FILE="${index}" git -C "${REPO}" read-tree "${base}"
	for spec in "$@"; do
		path="${spec%%=*}"
		content="${spec#*=}"
		blob="$(printf '%s\n' "${content}" | git -C "${REPO}" hash-object -w --stdin)"
		GIT_INDEX_FILE="${index}" git -C "${REPO}" update-index --add --cacheinfo "100644,${blob},${path}"
	done
	tree="$(GIT_INDEX_FILE="${index}" git -C "${REPO}" write-tree)"
	rm -f "${index}"
	git -C "${REPO}" commit-tree "${tree}" -p "${base}" -m "test"
}

# commit_without <base> path ...: like commit_files, removing the paths instead of writing them.
commit_without() {
	local base="$1"
	local index="${WORK}/index.$RANDOM$RANDOM"
	local path tree
	shift
	GIT_INDEX_FILE="${index}" git -C "${REPO}" read-tree "${base}"
	for path in "$@"; do
		GIT_INDEX_FILE="${index}" git -C "${REPO}" update-index --force-remove "${path}"
	done
	tree="$(GIT_INDEX_FILE="${index}" git -C "${REPO}" write-tree)"
	rm -f "${index}"
	git -C "${REPO}" commit-tree "${tree}" -p "${base}" -m "test"
}

# expect_moves <label> <android yes|no> <ios yes|no> path[=content] ...
expect_moves() {
	local label="$1"
	local want_android="$2"
	local want_ios="$3"
	local specs=()
	local spec commit got_android got_ios
	shift 3
	for spec in "$@"; do
		[[ "${spec}" == *=* ]] && specs+=("${spec}") || specs+=("${spec}=changed by ${label}")
	done
	commit="$(commit_files "${BASE}" "${specs[@]}")"
	got_android=no
	got_ios=no
	[[ "$(fp android "${commit}")" != "${BASE_ANDROID}" ]] && got_android=yes
	[[ "$(fp ios "${commit}")" != "${BASE_IOS}" ]] && got_ios=yes
	if [[ "${got_android}" == "${want_android}" && "${got_ios}" == "${want_ios}" ]]; then
		ok
	else
		fail "${label}: android moved=${got_android} (wanted ${want_android}), ios moved=${got_ios} (wanted ${want_ios})"
	fi
}

# --- the minimal repository ---------------------------------------------------------------------------------
mkdir -p "${REPO}"
git -C "${REPO}" init -q
git -C "${REPO}" config user.name "Fingerprint Test"
git -C "${REPO}" config user.email "fingerprint-test@example.invalid"
git -C "${REPO}" config commit.gpgsign false

while IFS= read -r path; do
	mkdir -p "${REPO}/$(dirname "${path}")"
	printf 'base %s\n' "${path}" >"${REPO}/${path}"
done <<'LAYOUT'
settings.gradle.kts
build.gradle.kts
gradle.properties
gradlew
gradlew.bat
gradle/libs.versions.toml
gradle/gradle-daemon-jvm.properties
gradle/wrapper/gradle-wrapper.properties
gradle/wrapper/gradle-wrapper.jar
gradle/app-version.properties
gradle/e2e-tasks.gradle.kts
mocks/mappings/a.json
e2e/catalog/scenarios.json
e2e/scripts/shared/e2e.py
e2e/scripts/android/adapter.sh
e2e/scripts/ios/adapter.sh
e2e/tools/tests/t.py
e2e/platform/android/README.md
e2e/README.md
scenariokit/build.gradle.kts
scenariokit/src/commonMain/K.kt
scenariokit/src/androidMain/K.kt
scenariokit/src/iosMain/K.kt
scenariokit/src/commonTest/T.kt
scenarios/build.gradle.kts
scenarios/src/commonMain/S.kt
scenarios/src/androidMain/S.kt
scenarios/src/iosMain/S.kt
scenarios/src/androidHostTest/T.kt
scenariorunner/build.gradle.kts
scenariorunner/src/main/R.kt
app/build.gradle.kts
app/src/main/A.kt
app/src/debug/D.kt
app/src/release/R.kt
app/src/test/T.kt
iosApp/Config/Debug.xcconfig
iosApp/Config/Release.xcconfig
iosApp/Config/UITests.xcconfig
iosApp/Config/Version.xcconfig
iosApp/Podfile
iosApp/Podfile.lock
iosApp/Resources/r.txt
iosApp/Sources/S.swift
iosApp/scripts/s.sh
iosApp/scripts/ci-build.sh
iosApp/UITests/T.swift
iosApp/TuIndiceHost.xcodeproj/project.pbxproj
iosApp/TuIndiceHost.xcodeproj/xcshareddata/xcschemes/x.xcscheme
auth/build.gradle.kts
auth/src/commonMain/A.kt
auth/src/androidMain/A.kt
auth/src/iosMain/A.kt
auth/src/commonTest/T.kt
auth/src/androidHostTest/T.kt
security/build.gradle.kts
security/src/commonMain/A.kt
testkit/build.gradle.kts
testkit/src/commonMain/T.kt
testkit/e2e/validate-x.sh
testkit/e2e/notes.md
.github/scripts/x.sh
.github/scripts/materialize-firebase-configs.sh
.github/scripts/sync-app-version.sh
.codex/skills/x.md
docs/x.md
README.md
LAYOUT
printf 'apply(from = "gradle/e2e-tasks.gradle.kts")\n' >"${REPO}/build.gradle.kts"
cp "${REAL_ROOT}/e2e/scripts/shared/layout.env" "${REPO}/e2e/scripts/shared/layout.env"
mkdir -p "${REPO}/e2e/toolchain"
cp "${REAL_ROOT}/e2e/toolchain/android.lock" "${REAL_ROOT}/e2e/toolchain/ios.lock" "${REPO}/e2e/toolchain/"
mkdir -p "${REPO}/scripts"
cat >"${REPO}/scripts/module-graph.txt" <<'GRAPH'
# fixture graph
app=:auth :base
auth=:base :security
base=-
scenariokit=-
scenariorunner=:scenariokit
scenarios=:auth :scenariokit
security=:base
testkit=:base
GRAPH
mkdir -p "${REPO}/base/src/commonMain" "${REPO}/base/src/commonTest"
printf 'b\n' >"${REPO}/base/build.gradle.kts"
printf 'b\n' >"${REPO}/base/src/commonMain/B.kt"
git -C "${REPO}" add -A
git -C "${REPO}" commit -q -m "base layout"
BASE="$(git -C "${REPO}" rev-parse HEAD)"
BASE_GRAPH_COPY="${WORK}/module-graph.base"
cp "${REPO}/scripts/module-graph.txt" "${BASE_GRAPH_COPY}"
BASE_ANDROID="$(fp android)"
BASE_IOS="$(fp ios)"

# --- shape and stability ------------------------------------------------------------------------------------
for value in "${BASE_ANDROID}" "${BASE_IOS}"; do
	if [[ "${value}" =~ ^[0-9a-f]{64}$ ]]; then ok; else fail "the fingerprint is not 64 hex characters: ${value}"; fi
done
if [[ "$(fp android)" == "${BASE_ANDROID}" && "$(fp ios)" == "${BASE_IOS}" ]]; then ok; else fail "the fingerprint is not stable across two calls"; fi
if [[ "${BASE_ANDROID}" != "${BASE_IOS}" ]]; then ok; else fail "both platforms share a fingerprint"; fi
if [[ "$(fp android HEAD other-suite)" != "${BASE_ANDROID}" ]]; then ok; else fail "the suite id does not enter the fingerprint"; fi
if [[ "$(fp android "${BASE}")" == "${BASE_ANDROID}" ]]; then ok; else fail "HEAD and its explicit SHA disagree"; fi

# --- only the git tree counts: not the working tree, not uncommitted or untracked files, not the directory ----
printf 'uncommitted edit\n' >"${REPO}/app/src/main/A.kt"
printf 'uncommitted edit\n' >"${REPO}/mocks/mappings/a.json"
printf 'untracked\n' >"${REPO}/iosApp/Sources/Untracked.swift"
# layout.env names the source sets: it is read from the ref too, not from the working tree.
printf 'E2E_IOS_SOURCE_SETS=bogusMain\n' >"${REPO}/e2e/scripts/shared/layout.env"
git -C "${REPO}" rm -q --cached e2e/catalog/scenarios.json
# A module dropped from the graph on disk must not drop out of the fingerprint: the graph is read from the ref.
grep -v '^security=' "${BASE_GRAPH_COPY}" >"${REPO}/scripts/module-graph.txt"
if [[ "$(fp android)" == "${BASE_ANDROID}" && "$(fp ios)" == "${BASE_IOS}" ]]; then
	ok
else
	fail "the fingerprint changed with uncommitted, untracked or unstaged files"
fi
if [[ "$(cd / && E2E_FINGERPRINT_REPO_ROOT="${REPO}" bash "${FINGERPRINT}" android local-certification-suite HEAD)" == "${BASE_ANDROID}" ]]; then
	ok
else
	fail "the fingerprint depends on the current directory"
fi
# restore the working tree by rewriting what the commit holds
printf 'base app/src/main/A.kt\n' >"${REPO}/app/src/main/A.kt"
printf 'base mocks/mappings/a.json\n' >"${REPO}/mocks/mappings/a.json"
rm -f "${REPO}/iosApp/Sources/Untracked.swift"
git -C "${REPO}" add e2e/catalog/scenarios.json
git -C "${REPO}" show "${BASE}:scripts/module-graph.txt" >"${REPO}/scripts/module-graph.txt"
git -C "${REPO}" show "${BASE}:e2e/scripts/shared/layout.env" >"${REPO}/e2e/scripts/shared/layout.env"

# --- shared group: both platforms move ----------------------------------------------------------------------
for path in settings.gradle.kts build.gradle.kts gradle.properties gradlew gradlew.bat gradle/libs.versions.toml \
	gradle/gradle-daemon-jvm.properties gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.jar \
	mocks/mappings/a.json mocks/mappings/new.json e2e/catalog/scenarios.json e2e/scripts/shared/e2e.py e2e/scripts/shared/new.py \
	scenariokit/build.gradle.kts scenariokit/src/commonMain/K.kt scenarios/build.gradle.kts scenarios/src/commonMain/S.kt; do
	expect_moves "shared ${path}" yes yes "${path}"
done

# --- a required path that nothing tracks makes the hash refuse, for the platform that requires it ---------------
without_podfile="$(commit_without "${BASE}" iosApp/Podfile.lock)"
if ! fp ios "${without_podfile}" >/dev/null 2>&1 && fp android "${without_podfile}" >/dev/null 2>&1; then
	ok
else
	fail "a required pathspec that tracks nothing must stop the iOS hash (and leave Android's alone)"
fi
without_release="$(commit_without "${BASE}" app/src/release/R.kt)"
if fp android "${without_release}" >/dev/null 2>&1; then ok; else fail "an optional pathspec that tracks nothing must not stop the hash"; fi

# --- the source-set lists come from layout.env of the ref ----------------------------------------------------
layout_base="$(git -C "${REPO}" show "${BASE}:e2e/scripts/shared/layout.env")"
layout_commit="$(commit_files "${BASE}" "e2e/scripts/shared/layout.env=${layout_base}
E2E_IOS_SOURCE_SETS=iosMain,customMain")"
custom_commit="$(commit_files "${layout_commit}" "auth/src/customMain/X.kt=custom")"
if [[ "$(fp ios "${custom_commit}")" != "$(fp ios "${layout_commit}")" && "$(fp android "${custom_commit}")" == "$(fp android "${layout_commit}")" ]]; then
	ok
else
	fail "a source set listed in layout.env of the ref does not enter the fingerprint of its platform only"
fi

# --- Android group: only Android moves -----------------------------------------------------------------------
for path in app/build.gradle.kts app/src/main/A.kt app/src/debug/D.kt app/src/release/R.kt e2e/scripts/android/adapter.sh \
	e2e/scripts/android/new.sh e2e/toolchain/android.lock scenariorunner/build.gradle.kts scenariorunner/src/main/R.kt \
	scenariokit/src/androidMain/K.kt scenarios/src/androidMain/S.kt scenariokit/src/androidMain/New.kt; do
	expect_moves "android ${path}" yes no "${path}"
done

# --- iOS group: only iOS moves -------------------------------------------------------------------------------
for path in iosApp/Config/Debug.xcconfig iosApp/Config/Release.xcconfig iosApp/Config/UITests.xcconfig iosApp/Podfile \
	iosApp/Podfile.lock iosApp/Resources/r.txt iosApp/Sources/S.swift iosApp/scripts/s.sh iosApp/UITests/T.swift \
	iosApp/UITests/Generated/G.swift iosApp/TuIndiceHost.xcodeproj/project.pbxproj \
	iosApp/TuIndiceHost.xcodeproj/xcshareddata/xcschemes/x.xcscheme e2e/scripts/ios/adapter.sh e2e/toolchain/ios.lock \
	scenariokit/src/iosMain/K.kt scenariokit/src/appleMain/K.kt scenariokit/src/iosSimulatorArm64Main/K.kt \
	scenarios/src/iosMain/S.kt .github/scripts/materialize-firebase-configs.sh .github/scripts/sync-app-version.sh \
	iosApp/scripts/new-tool.sh; do
	expect_moves "ios ${path}" no yes "${path}"
done

# --- KMP runtime modules, derived from the module graph of the ref ------------------------------------------
expect_moves "runtime auth commonMain" yes yes auth/src/commonMain/A.kt
expect_moves "runtime auth build file" yes yes auth/build.gradle.kts
expect_moves "runtime auth androidMain" yes no auth/src/androidMain/A.kt
expect_moves "runtime auth iosMain" no yes auth/src/iosMain/A.kt
expect_moves "runtime auth appleMain" no yes auth/src/appleMain/A.kt
expect_moves "runtime security (only in the graph)" yes yes security/src/commonMain/A.kt
expect_moves "runtime base commonMain" yes yes base/src/commonMain/B.kt
# A module that is not in the graph is not read; adding it to the graph (in the ref) makes it count.
expect_moves "module outside the graph" no no newmod/src/commonMain/N.kt
graph_commit="$(commit_files "${BASE}" "scripts/module-graph.txt=$(git -C "${REPO}" show "${BASE}:scripts/module-graph.txt")
newmod=:base" "newmod/src/commonMain/N.kt=new")"
if [[ "$(fp android "${graph_commit}")" != "${BASE_ANDROID}" && "$(fp ios "${graph_commit}")" != "${BASE_IOS}" ]]; then
	ok
else
	fail "a module added to the graph of the ref does not enter the fingerprint"
fi

# --- excluded: neither platform moves ------------------------------------------------------------------------
for path in e2e/tools/tests/t.py e2e/tools/verify/new.sh e2e/platform/android/README.md e2e/README.md README.md docs/x.md \
	.github/scripts/x.sh .codex/skills/x.md gradle/e2e-tasks.gradle.kts gradle/app-version.properties iosApp/Config/Version.xcconfig \
	iosApp/scripts/ci-build.sh iosApp/scripts/ci-new.sh \
	auth/src/commonTest/T.kt auth/src/androidHostTest/T.kt scenarios/src/androidHostTest/T.kt scenariokit/src/commonTest/T.kt \
	app/src/test/T.kt testkit/build.gradle.kts testkit/src/commonMain/T.kt testkit/e2e/validate-x.sh testkit/e2e/notes.md; do
	expect_moves "excluded ${path}" no no "${path}"
done

# --- --print-pathspecs ----------------------------------------------------------------------------------------
android_specs="$(E2E_FINGERPRINT_REPO_ROOT="${REPO}" bash "${FINGERPRINT}" --print-pathspecs android)"
ios_specs="$(E2E_FINGERPRINT_REPO_ROOT="${REPO}" bash "${FINGERPRINT}" --print-pathspecs ios)"
expect_line() {
	local label="$1" specs="$2" line="$3"
	if printf '%s\n' "${specs}" | grep -Fxq "${line}"; then ok; else fail "${label}: missing ${line}"; fi
}
reject_line() {
	local label="$1" specs="$2" line="$3"
	if printf '%s\n' "${specs}" | grep -Fxq "${line}"; then fail "${label}: unexpected ${line}"; else ok; fi
}
expect_line "android pathspecs" "${android_specs}" "required:app/src/main"
expect_line "android pathspecs" "${android_specs}" "required:scenariorunner"
expect_line "android pathspecs" "${android_specs}" "required:e2e/toolchain/android.lock"
expect_line "android pathspecs" "${android_specs}" "optional:auth/src/androidMain"
reject_line "android pathspecs" "${android_specs}" "required:iosApp/UITests"
reject_line "android pathspecs" "${android_specs}" "optional:auth/src/iosMain"
reject_line "android pathspecs" "${android_specs}" "optional:app/build.gradle.kts"
reject_line "android pathspecs" "${android_specs}" "optional:testkit/build.gradle.kts"
expect_line "ios pathspecs" "${ios_specs}" "required:iosApp/UITests"
expect_line "ios pathspecs" "${ios_specs}" "required:iosApp/Config/UITests.xcconfig"
expect_line "ios pathspecs" "${ios_specs}" "required:e2e/toolchain/ios.lock"
expect_line "ios pathspecs" "${ios_specs}" "optional:auth/src/iosSimulatorArm64Main"
expect_line "ios pathspecs" "${ios_specs}" "required:.github/scripts/sync-app-version.sh"
expect_line "ios pathspecs" "${ios_specs}" "required:.github/scripts/materialize-firebase-configs.sh"
expect_line "ios pathspecs" "${ios_specs}" "excluded:iosApp/scripts/ci-"
expect_line "ios pathspecs" "${ios_specs}" "optional:scenarios/src/iosMain"
expect_line "ios pathspecs" "${ios_specs}" "optional:scenariokit/src/appleMain"
reject_line "android pathspecs" "${android_specs}" "excluded:iosApp/scripts/ci-"
reject_line "android pathspecs" "${android_specs}" "required:.github/scripts/sync-app-version.sh"
reject_line "ios pathspecs" "${ios_specs}" "required:app/src/main"
reject_line "ios pathspecs" "${ios_specs}" "optional:auth/src/androidMain"
reject_line "ios pathspecs" "${ios_specs}" "optional:scenarios/build.gradle.kts"
for specs in "${android_specs}" "${ios_specs}"; do
	for line in required:mocks required:e2e/catalog required:e2e/scripts/shared required:scenariokit/src/commonMain required:scenarios/src/commonMain; do
		expect_line "shared pathspecs" "${specs}" "${line}"
	done
	for line in required:build.gradle.kts required:gradlew required:gradle/wrapper required:gradle/gradle-daemon-jvm.properties; do
		expect_line "shared pathspecs" "${specs}" "${line}"
	done
	reject_line "pathspecs" "${specs}" "required:gradle/e2e-tasks.gradle.kts"
	reject_line "pathspecs" "${specs}" "required:e2e/tools"
	reject_line "pathspecs" "${specs}" "optional:scenarios/src/commonTest"
done

# --- the coverage verifier ------------------------------------------------------------------------------------
verify() {
	E2E_VERIFY_REPO_ROOT="${REPO}" bash "${VERIFIER}" 2>&1
}
expect_verifier() {
	local label="$1" want="$2" output status
	set +e
	output="$(verify)"
	status=$?
	set -e
	if [[ "${want}" == "pass" && "${status}" == "0" ]] || [[ "${want}" == "fail" && "${status}" != "0" ]]; then
		ok
	else
		fail "verifier ${label}: wanted ${want}, exit ${status}: ${output}"
	fi
}
expect_verifier "on the minimal layout" pass

printf 'stray\n' >"${REPO}/e2e/scripts/stray.sh"
git -C "${REPO}" add e2e/scripts/stray.sh
expect_verifier "with a loose script under e2e/scripts" fail
git -C "${REPO}" rm -q --cached -f e2e/scripts/stray.sh
rm -f "${REPO}/e2e/scripts/stray.sh"

mkdir -p "${REPO}/e2e/unlisted"
printf 'orphan\n' >"${REPO}/e2e/unlisted/x.txt"
git -C "${REPO}" add e2e/unlisted/x.txt
expect_verifier "with an uncovered file under e2e/" fail
git -C "${REPO}" rm -q --cached -f e2e/unlisted/x.txt
rm -rf "${REPO}/e2e/unlisted"

printf 'data\n' >"${REPO}/testkit/e2e/runtime-data.yaml"
git -C "${REPO}" add testkit/e2e/runtime-data.yaml
expect_verifier "with an unread data file under testkit/e2e" fail
git -C "${REPO}" rm -q --cached -f testkit/e2e/runtime-data.yaml
rm -f "${REPO}/testkit/e2e/runtime-data.yaml"

git -C "${REPO}" rm -q --cached -f scenariorunner/src/main/R.kt scenariorunner/build.gradle.kts
expect_verifier "with a required pathspec that tracks nothing" fail
git -C "${REPO}" add scenariorunner

cp "${REPO}/e2e/toolchain/ios.lock" "${WORK}/ios.lock.keep"
printf 'IOS_UNREAD_KEY=1\n' >>"${REPO}/e2e/toolchain/ios.lock"
expect_verifier "with a lock key toolchain.py does not read" fail
cp "${WORK}/ios.lock.keep" "${REPO}/e2e/toolchain/ios.lock"

cp "${REPO}/e2e/toolchain/android.lock" "${WORK}/android.lock.keep"
grep -v '^ANDROID_LOCALE=' "${WORK}/android.lock.keep" >"${REPO}/e2e/toolchain/android.lock"
expect_verifier "with a lock key missing" fail
cp "${WORK}/android.lock.keep" "${REPO}/e2e/toolchain/android.lock"

cp "${REPO}/scenariorunner/build.gradle.kts" "${WORK}/runner-build.keep"
printf 'dependencies {\n\timplementation("androidx.test.uiautomator:uiautomator:2.4.0")\n}\n' >"${REPO}/scenariorunner/build.gradle.kts"
expect_verifier "with an androidx.test version pinned in the runner" fail
cp "${WORK}/runner-build.keep" "${REPO}/scenariorunner/build.gradle.kts"

cp "${REPO}/build.gradle.kts" "${WORK}/root-build.keep"
printf 'tasks.register<Exec>("e2eStatus") {\n}\n' >>"${REPO}/build.gradle.kts"
expect_verifier "with an E2E task registered in the root build file" fail
printf 'registerE2eRun("e2eIos", "d", "ios", "diagnose", t)\n' >"${REPO}/build.gradle.kts"
expect_verifier "with the E2E task helper called from the root build file and no apply" fail
printf 'tasks.register("verifySharedTests") {\n}\napply(from = "gradle/e2e-tasks.gradle.kts")\n' >"${REPO}/build.gradle.kts"
expect_verifier "with a non-E2E task in the root build file" pass
printf 'tasks.register("verifySharedTests") {\n}\n' >"${REPO}/build.gradle.kts"
expect_verifier "with the root build file not applying the E2E tasks script" fail
cp "${WORK}/root-build.keep" "${REPO}/build.gradle.kts"

expect_verifier "after restoring the layout" pass

if (( failures > 0 )); then
	printf 'test-fingerprint: %d of %d checks failed\n' "${failures}" "$((checks + failures))" >&2
	exit 1
fi
printf 'test-fingerprint: %d checks passed\n' "${checks}"
