#!/usr/bin/env bash
# Build phase of the TuIndiceUITests target: links the ScenarioKit framework for the simulator and
# copies the versioned scenario catalog into the test bundle. It runs no Gradle tests: the catalog
# is e2e/catalog/scenarios.json, kept fresh by syncE2eArtifacts / verifyE2eArtifactsFresh.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
PLATFORM="${PLATFORM_NAME:-iphonesimulator}"
CATALOG_SOURCE="$ROOT_DIR/e2e/catalog/scenarios.json"
LOCAL_GRADLE_USER_HOME_DIR="$ROOT_DIR/.gradle-local"
DEFAULT_GRADLE_USER_HOME_DIR="${HOME:-}/.gradle"
GRADLE_DIST_NAME="$(
	sed -n 's/^distributionUrl=.*\/\(gradle-[^\/]*\)\.zip$/\1/p' \
		"$ROOT_DIR/gradle/wrapper/gradle-wrapper.properties" | head -n 1
)"

if [[ -n "${GRADLE_USER_HOME:-}" ]]; then
	GRADLE_USER_HOME_DIR="$GRADLE_USER_HOME"
else
	GRADLE_USER_HOME_DIR="$LOCAL_GRADLE_USER_HOME_DIR"
fi

prime_local_gradle_wrapper_dist() {
	[[ "$GRADLE_USER_HOME_DIR" == "$LOCAL_GRADLE_USER_HOME_DIR" ]] || return 0
	[[ -n "$GRADLE_DIST_NAME" ]] || return 0

	local source_dist_dir="$DEFAULT_GRADLE_USER_HOME_DIR/wrapper/dists/$GRADLE_DIST_NAME"
	local target_dist_dir="$GRADLE_USER_HOME_DIR/wrapper/dists/$GRADLE_DIST_NAME"
	local target_ok_marker="$target_dist_dir"/*/"$GRADLE_DIST_NAME.zip.ok"
	local target_unpacked_dir="$target_dist_dir"/*/gradle-*

	[[ -d "$source_dist_dir" ]] || return 0
	if compgen -G "$target_ok_marker" >/dev/null &&
		compgen -G "$target_unpacked_dir" >/dev/null; then
		return 0
	fi

	mkdir -p "$target_dist_dir"
	cp -R "$source_dist_dir"/. "$target_dist_dir"/
}

# Xcode script phases may run without JAVA_HOME; Gradle/Kotlin toolchain needs it.
if [[ -z "${JAVA_HOME:-}" ]]; then
	if JAVA_21_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null)"; then
		export JAVA_HOME="$JAVA_21_HOME"
	elif JAVA_DEFAULT_HOME="$(/usr/libexec/java_home 2>/dev/null)"; then
		export JAVA_HOME="$JAVA_DEFAULT_HOME"
	fi
fi

write_script_output_stamp() {
	local stamp="$1"

	if [[ -n "${SCRIPT_OUTPUT_FILE_0:-}" ]]; then
		mkdir -p "$(dirname "$SCRIPT_OUTPUT_FILE_0")"
		echo "$stamp" > "$SCRIPT_OUTPUT_FILE_0"
	fi
}

if [[ "$PLATFORM" != "iphonesimulator" ]]; then
	echo "error: TuIndiceUITests only builds for the iOS simulator (PLATFORM_NAME=$PLATFORM)." >&2
	exit 1
fi

[[ -s "$CATALOG_SOURCE" ]] || {
	echo "error: missing scenario catalog: $CATALOG_SOURCE (run ./gradlew syncE2eArtifacts)." >&2
	exit 1
}

cd "$ROOT_DIR"

if [[ "${SKIP_FRAMEWORK_BUILD:-0}" == "1" ]]; then
	echo "Skipping ScenarioKit framework build; Gradle task already linked it."
else
	echo "Linking the ScenarioKit framework for the iOS simulator"
	mkdir -p "$GRADLE_USER_HOME_DIR"
	prime_local_gradle_wrapper_dist
	export GRADLE_USER_HOME="$GRADLE_USER_HOME_DIR"

	declare -a gradle_args=(
		"-Dorg.gradle.jvmargs=${TUINDICE_IOS_GRADLE_JVM_ARGS:--Xmx4g -XX:MaxMetaspaceSize=1536m -Dfile.encoding=UTF-8}"
		"--no-configuration-cache"
		":scenariokit:linkDebugFrameworkIosSimulatorArm64"
	)

	if [[ -n "${CI:-}" || "${TUINDICE_IOS_GRADLE_STACKTRACE:-0}" == "1" ]]; then
		gradle_args+=("--stacktrace")
	fi

	if [[ -n "${CI:-}" || "${TUINDICE_IOS_GRADLE_NO_DAEMON:-0}" == "1" ]]; then
		gradle_args+=("--no-daemon")
	fi

	./gradlew "${gradle_args[@]}"
fi

if [[ -n "${TARGET_BUILD_DIR:-}" && -n "${UNLOCALIZED_RESOURCES_FOLDER_PATH:-}" ]]; then
	RESOURCES_DIR="$TARGET_BUILD_DIR/$UNLOCALIZED_RESOURCES_FOLDER_PATH"
	mkdir -p "$RESOURCES_DIR"
	cp "$CATALOG_SOURCE" "$RESOURCES_DIR/scenarios.json"
	echo "Copied the scenario catalog to $RESOURCES_DIR/scenarios.json"
fi

write_script_output_stamp "ScenarioKit framework linked and catalog copied"
